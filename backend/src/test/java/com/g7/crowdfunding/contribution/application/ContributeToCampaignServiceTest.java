package com.g7.crowdfunding.contribution.application;

import com.g7.crowdfunding.campaign.application.command.CampaignLifecycleService;
import com.g7.crowdfunding.campaign.application.command.CreateCampaignService;
import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase.CreateCampaignCommand;
import com.g7.crowdfunding.campaign.domain.event.ContributionRegistered;
import com.g7.crowdfunding.campaign.domain.event.GoalReached;
import com.g7.crowdfunding.campaign.domain.exception.ContributionAfterDeadlineException;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import com.g7.crowdfunding.contribution.application.command.ContributeToCampaignService;
import com.g7.crowdfunding.contribution.application.exception.IdempotencyKeyConflictException;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase.ContributeCommand;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase.ContributionReceipt;
import com.g7.crowdfunding.contribution.domain.exception.InvalidContributionAmountException;
import com.g7.crowdfunding.shared.application.exception.ConcurrencyConflictException;
import com.g7.crowdfunding.shared.application.exception.ResourceNotFoundException;
import com.g7.crowdfunding.support.InMemoryAdapters.FakeTransactionRunner;
import com.g7.crowdfunding.support.InMemoryAdapters.InMemoryCampaignRepository;
import com.g7.crowdfunding.support.InMemoryAdapters.InMemoryContributionRepository;
import com.g7.crowdfunding.support.InMemoryAdapters.InMemoryUserRepository;
import com.g7.crowdfunding.support.InMemoryAdapters.MutableClock;
import com.g7.crowdfunding.support.InMemoryAdapters.RecordingOutbox;
import com.g7.crowdfunding.user.application.UserDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.g7.crowdfunding.support.InMemoryAdapters.CONTRIBUTOR;
import static com.g7.crowdfunding.support.InMemoryAdapters.CREATOR;
import static com.g7.crowdfunding.support.InMemoryAdapters.OTHER_CONTRIBUTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContributeToCampaignServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-03-01T10:00:00Z"));
    private final InMemoryCampaignRepository campaigns = new InMemoryCampaignRepository();
    private final InMemoryContributionRepository contributions = new InMemoryContributionRepository();
    private final RecordingOutbox outbox = new RecordingOutbox();
    private final FakeTransactionRunner tx = new FakeTransactionRunner();
    private final UserDirectory users = new UserDirectory(new InMemoryUserRepository());

    private ContributeToCampaignService service;
    private UUID campaignId;

    @BeforeEach
    void setUp() {
        service = new ContributeToCampaignService(campaigns, contributions, outbox, users, tx, clock, 3);
        campaignId = new CreateCampaignService(campaigns, users, tx, clock).create(new CreateCampaignCommand(
                CREATOR, "Campaign", "Description", Category.TECHNOLOGY, new BigDecimal("1000"), "USD",
                clock.now().plus(Duration.ofDays(5)), ContributorsVisibility.PUBLIC));
        new CampaignLifecycleService(campaigns, outbox, users, tx, clock).publish(campaignId, CREATOR);
    }

    private ContributeCommand command(UUID contributor, String amount, UUID key) {
        return new ContributeCommand(campaignId, contributor, new BigDecimal(amount), null, false, "go!", key);
    }

    @Test
    void registersContributionUpdatesProgressAndWritesOutbox() {
        ContributionReceipt receipt = service.contribute(command(CONTRIBUTOR, "400", UUID.randomUUID()));

        assertThat(receipt.replayed()).isFalse();
        assertThat(receipt.campaignRaisedAmount()).isEqualByComparingTo("400");
        assertThat(campaigns.findById(campaignId).orElseThrow().contributionsCount()).isEqualTo(1);
        assertThat(contributions.count()).isEqualTo(1);
        assertThat(outbox.eventsOf(ContributionRegistered.class)).hasSize(1);
    }

    @Test
    void sameIdempotencyKeyReturnsTheExistingContribution() {
        UUID key = UUID.randomUUID();
        ContributionReceipt first = service.contribute(command(CONTRIBUTOR, "100", key));
        ContributionReceipt second = service.contribute(command(CONTRIBUTOR, "100", key));

        assertThat(second.replayed()).isTrue();
        assertThat(second.contributionId()).isEqualTo(first.contributionId());
        assertThat(contributions.count()).isEqualTo(1);
        assertThat(campaigns.findById(campaignId).orElseThrow().raisedAmount().amount()).isEqualByComparingTo("100");
    }

    @Test
    void idempotencyKeyReusedByAnotherContributorIsAConflict() {
        UUID key = UUID.randomUUID();
        service.contribute(command(CONTRIBUTOR, "100", key));
        assertThatThrownBy(() -> service.contribute(command(OTHER_CONTRIBUTOR, "100", key)))
                .isInstanceOf(IdempotencyKeyConflictException.class);
    }

    @Test
    void retriesAfterOptimisticLockConflict() {
        tx.failNextWithConflict(2);
        ContributionReceipt receipt = service.contribute(command(CONTRIBUTOR, "50", UUID.randomUUID()));
        assertThat(receipt.replayed()).isFalse();
        assertThat(contributions.count()).isEqualTo(1);
    }

    @Test
    void givesUpAfterMaxAttempts() {
        tx.failNextWithConflict(3);
        assertThatThrownBy(() -> service.contribute(command(CONTRIBUTOR, "50", UUID.randomUUID())))
                .isInstanceOf(ConcurrencyConflictException.class);
        assertThat(contributions.count()).isZero();
    }

    @Test
    void emitsGoalReachedOnceWhenGoalIsMet() {
        service.contribute(command(CONTRIBUTOR, "600", UUID.randomUUID()));
        service.contribute(command(OTHER_CONTRIBUTOR, "400", UUID.randomUUID()));
        service.contribute(command(CONTRIBUTOR, "10", UUID.randomUUID()));

        assertThat(outbox.eventsOf(GoalReached.class)).hasSize(1);
        var campaign = campaigns.findById(campaignId).orElseThrow();
        assertThat(campaign.contributorsCount()).isEqualTo(2);
        assertThat(campaign.contributionsCount()).isEqualTo(3);
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> service.contribute(command(CONTRIBUTOR, "0", UUID.randomUUID())))
                .isInstanceOf(InvalidContributionAmountException.class);
        assertThat(contributions.count()).isZero();
    }

    @Test
    void rejectsContributionAfterDeadline() {
        clock.advance(Duration.ofDays(6));
        assertThatThrownBy(() -> service.contribute(command(CONTRIBUTOR, "10", UUID.randomUUID())))
                .isInstanceOf(ContributionAfterDeadlineException.class);
    }

    @Test
    void unknownCampaignIsNotFound() {
        assertThatThrownBy(() -> service.contribute(new ContributeCommand(UUID.randomUUID(), CONTRIBUTOR,
                BigDecimal.TEN, null, false, null, UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
