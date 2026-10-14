package com.g7.crowdfunding.campaign.application;

import com.g7.crowdfunding.campaign.application.command.CampaignDeadlineService;
import com.g7.crowdfunding.campaign.application.command.CampaignLifecycleService;
import com.g7.crowdfunding.campaign.application.command.CreateCampaignService;
import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase.CreateCampaignCommand;
import com.g7.crowdfunding.campaign.domain.event.CampaignClosed;
import com.g7.crowdfunding.campaign.domain.event.ClosingSoon;
import com.g7.crowdfunding.campaign.domain.exception.CampaignHasContributionsException;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import com.g7.crowdfunding.contribution.application.command.ContributeToCampaignService;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase.ContributeCommand;
import com.g7.crowdfunding.shared.application.exception.AccessDeniedException;
import com.g7.crowdfunding.support.InMemoryAdapters.FakeTransactionRunner;
import com.g7.crowdfunding.support.InMemoryAdapters.InMemoryCampaignRepository;
import com.g7.crowdfunding.support.InMemoryAdapters.InMemoryContributionRepository;
import com.g7.crowdfunding.support.InMemoryAdapters.InMemoryUserRepository;
import com.g7.crowdfunding.support.InMemoryAdapters.MutableClock;
import com.g7.crowdfunding.support.InMemoryAdapters.RecordingOutbox;
import com.g7.crowdfunding.user.application.UserDirectory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.g7.crowdfunding.support.InMemoryAdapters.ADMIN;
import static com.g7.crowdfunding.support.InMemoryAdapters.CONTRIBUTOR;
import static com.g7.crowdfunding.support.InMemoryAdapters.CREATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignCommandServicesTest {

    private static final Duration WINDOW = Duration.ofHours(24);

    private final MutableClock clock = new MutableClock(Instant.parse("2026-03-01T10:00:00Z"));
    private final InMemoryCampaignRepository campaigns = new InMemoryCampaignRepository();
    private final InMemoryContributionRepository contributions = new InMemoryContributionRepository();
    private final RecordingOutbox outbox = new RecordingOutbox();
    private final FakeTransactionRunner tx = new FakeTransactionRunner();
    private final UserDirectory users = new UserDirectory(new InMemoryUserRepository());

    private final CreateCampaignService create = new CreateCampaignService(campaigns, users, tx, clock);
    private final CampaignLifecycleService lifecycle = new CampaignLifecycleService(campaigns, outbox, users, tx, clock);
    private final CampaignDeadlineService deadlines = new CampaignDeadlineService(campaigns, outbox, tx, clock, WINDOW);
    private final ContributeToCampaignService contribute =
            new ContributeToCampaignService(campaigns, contributions, outbox, users, tx, clock, 3);

    private UUID activeCampaign(Duration deadlineIn) {
        UUID id = create.create(new CreateCampaignCommand(CREATOR, "Campaign", "Description", Category.ART,
                new BigDecimal("500"), "USD", clock.now().plus(deadlineIn), ContributorsVisibility.PUBLIC));
        lifecycle.publish(id, CREATOR);
        return id;
    }

    @Test
    void onlyUsersWithCreatorRoleCanCreateCampaigns() {
        assertThatThrownBy(() -> create.create(new CreateCampaignCommand(CONTRIBUTOR, "T", "D", Category.ART,
                BigDecimal.TEN, "USD", clock.now().plusSeconds(3600), ContributorsVisibility.PUBLIC)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createdCampaignIsDraftAndPublishActivatesIt() {
        UUID id = create.create(new CreateCampaignCommand(CREATOR, "T", "D", Category.ART, BigDecimal.TEN, null,
                clock.now().plusSeconds(3600), ContributorsVisibility.PRIVATE));
        assertThat(campaigns.findById(id).orElseThrow().status()).isEqualTo(CampaignStatus.DRAFT);
        lifecycle.publish(id, CREATOR);
        assertThat(campaigns.findById(id).orElseThrow().status()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void cancelFailsOnceTheCampaignHasContributions() {
        UUID id = activeCampaign(Duration.ofDays(3));
        contribute.contribute(new ContributeCommand(id, CONTRIBUTOR, BigDecimal.ONE, null, false, null,
                UUID.randomUUID()));
        assertThatThrownBy(() -> lifecycle.cancel(id, CREATOR)).isInstanceOf(CampaignHasContributionsException.class);
        lifecycle.closeManually(id, CREATOR);
        assertThat(campaigns.findById(id).orElseThrow().status()).isEqualTo(CampaignStatus.CLOSED_MANUALLY);
    }

    @Test
    void schedulerClosesExpiredCampaignsAndEmitsEvent() {
        UUID id = activeCampaign(Duration.ofHours(2));
        UUID later = activeCampaign(Duration.ofDays(10));
        clock.advance(Duration.ofHours(3));

        assertThat(deadlines.closeExpiredCampaigns()).isEqualTo(1);
        assertThat(campaigns.findById(id).orElseThrow().status()).isEqualTo(CampaignStatus.CLOSED_BY_DEADLINE);
        assertThat(campaigns.findById(later).orElseThrow().status()).isEqualTo(CampaignStatus.ACTIVE);
        assertThat(outbox.eventsOf(CampaignClosed.class)).singleElement()
                .extracting(CampaignClosed::reason).isEqualTo(CampaignClosed.CloseReason.DEADLINE);
        assertThat(deadlines.closeExpiredCampaigns()).isZero();
    }

    @Test
    void closingSoonIsFlaggedOnlyOnce() {
        activeCampaign(Duration.ofHours(12));
        activeCampaign(Duration.ofDays(10));

        assertThat(deadlines.flagCampaignsClosingSoon()).isEqualTo(1);
        assertThat(deadlines.flagCampaignsClosingSoon()).isZero();
        assertThat(outbox.eventsOf(ClosingSoon.class)).hasSize(1);
    }

    @Test
    void onlyAdminsCanSuspend() {
        UUID id = activeCampaign(Duration.ofDays(3));
        assertThatThrownBy(() -> lifecycle.suspend(id, CREATOR)).isInstanceOf(AccessDeniedException.class);
        lifecycle.suspend(id, ADMIN);
        assertThat(campaigns.findById(id).orElseThrow().status()).isEqualTo(CampaignStatus.SUSPENDED);
    }
}
