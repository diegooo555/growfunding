package com.g7.crowdfunding.campaign.domain;

import com.g7.crowdfunding.campaign.domain.event.CampaignClosed;
import com.g7.crowdfunding.campaign.domain.event.ClosingSoon;
import com.g7.crowdfunding.campaign.domain.event.ContributionRegistered;
import com.g7.crowdfunding.campaign.domain.event.GoalReached;
import com.g7.crowdfunding.campaign.domain.exception.CampaignHasContributionsException;
import com.g7.crowdfunding.campaign.domain.exception.CampaignNotAcceptingContributionsException;
import com.g7.crowdfunding.campaign.domain.exception.ContributionAfterDeadlineException;
import com.g7.crowdfunding.campaign.domain.exception.InvalidCampaignException;
import com.g7.crowdfunding.campaign.domain.exception.InvalidCampaignStateException;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import com.g7.crowdfunding.campaign.domain.model.MilestoneType;
import com.g7.crowdfunding.contribution.domain.exception.InvalidContributionAmountException;
import com.g7.crowdfunding.contribution.domain.model.Contribution;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import com.g7.crowdfunding.shared.domain.exception.ForbiddenOperationException;
import com.g7.crowdfunding.shared.domain.model.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignTest {

    private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");
    private static final Instant DEADLINE = NOW.plus(Duration.ofDays(10));
    private static final UUID CREATOR = UUID.randomUUID();
    private static final UUID CONTRIBUTOR = UUID.randomUUID();

    private static Campaign draft(String goal) {
        return Campaign.create(CREATOR, "Title", "Description", Category.ART, usd(goal), DEADLINE,
                ContributorsVisibility.PUBLIC, NOW);
    }

    private static Campaign active(String goal) {
        Campaign campaign = draft(goal);
        campaign.publish(CREATOR, NOW);
        return campaign;
    }

    private static Money usd(String amount) {
        return Money.of(amount, "USD");
    }

    private static Contribution give(Campaign campaign, String amount, Instant at) {
        return campaign.contribute(CONTRIBUTOR, usd(amount), false, null, UUID.randomUUID(), true, at);
    }

    @Nested
    class Creation {

        @Test
        void newCampaignStartsAsDraftWithZeroProgress() {
            Campaign campaign = draft("1000");
            assertThat(campaign.status()).isEqualTo(CampaignStatus.DRAFT);
            assertThat(campaign.raisedAmount()).isEqualTo(usd("0"));
            assertThat(campaign.contributionsCount()).isZero();
        }

        @Test
        void goalMustBePositive() {
            assertThatThrownBy(() -> draft("0")).isInstanceOf(InvalidCampaignException.class);
        }

        @Test
        void deadlineMustBeInTheFuture() {
            assertThatThrownBy(() -> Campaign.create(CREATOR, "T", "D", Category.ART, usd("10"), NOW,
                    ContributorsVisibility.PUBLIC, NOW)).isInstanceOf(InvalidCampaignException.class);
        }

        @Test
        void titleIsRequired() {
            assertThatThrownBy(() -> Campaign.create(CREATOR, " ", "D", Category.ART, usd("10"), DEADLINE,
                    ContributorsVisibility.PUBLIC, NOW)).isInstanceOf(InvalidCampaignException.class);
        }
    }

    @Nested
    class Publishing {

        @Test
        void creatorPublishesDraft() {
            Campaign campaign = active("1000");
            assertThat(campaign.status()).isEqualTo(CampaignStatus.ACTIVE);
            assertThat(campaign.milestones()).extracting(m -> m.type()).containsExactly(MilestoneType.PUBLISHED);
        }

        @Test
        void onlyCreatorCanPublish() {
            assertThatThrownBy(() -> draft("1000").publish(UUID.randomUUID(), NOW))
                    .isInstanceOf(ForbiddenOperationException.class);
        }

        @Test
        void cannotPublishTwice() {
            Campaign campaign = active("1000");
            assertThatThrownBy(() -> campaign.publish(CREATOR, NOW)).isInstanceOf(InvalidCampaignStateException.class);
        }
    }

    @Nested
    class Contributing {

        @Test
        void contributionUpdatesProgressAndEmitsEvent() {
            Campaign campaign = active("1000");
            Contribution contribution = give(campaign, "250.50", NOW);

            assertThat(campaign.raisedAmount()).isEqualTo(usd("250.50"));
            assertThat(campaign.contributionsCount()).isEqualTo(1);
            assertThat(campaign.contributorsCount()).isEqualTo(1);
            assertThat(contribution.campaignId()).isEqualTo(campaign.id());
            assertThat(campaign.pullDomainEvents()).singleElement().isInstanceOf(ContributionRegistered.class);
        }

        @Test
        void repeatedContributorIsNotCountedTwice() {
            Campaign campaign = active("1000");
            campaign.contribute(CONTRIBUTOR, usd("10"), false, null, UUID.randomUUID(), true, NOW);
            campaign.contribute(CONTRIBUTOR, usd("10"), false, null, UUID.randomUUID(), false, NOW);
            assertThat(campaign.contributionsCount()).isEqualTo(2);
            assertThat(campaign.contributorsCount()).isEqualTo(1);
        }

        @Test
        void zeroAmountIsRejected() {
            Campaign campaign = active("1000");
            assertThatThrownBy(() -> give(campaign, "0", NOW)).isInstanceOf(InvalidContributionAmountException.class);
        }

        @Test
        void negativeAmountIsRejected() {
            Campaign campaign = active("1000");
            assertThatThrownBy(() -> give(campaign, "-5", NOW)).isInstanceOf(InvalidContributionAmountException.class);
            assertThat(campaign.raisedAmount()).isEqualTo(usd("0"));
        }

        @Test
        void contributionAfterDeadlineIsRejected() {
            Campaign campaign = active("1000");
            assertThatThrownBy(() -> give(campaign, "10", DEADLINE))
                    .isInstanceOf(ContributionAfterDeadlineException.class);
        }

        @Test
        void draftDoesNotAcceptContributions() {
            Campaign campaign = draft("1000");
            assertThatThrownBy(() -> give(campaign, "10", NOW))
                    .isInstanceOf(CampaignNotAcceptingContributionsException.class);
        }

        @Test
        void goalReachedIsEmittedOnlyOnceAndCampaignKeepsAcceptingContributions() {
            Campaign campaign = active("100");
            give(campaign, "60", NOW);
            give(campaign, "40", NOW);
            give(campaign, "500", NOW);

            List<DomainEvent> events = campaign.pullDomainEvents();
            assertThat(events).filteredOn(GoalReached.class::isInstance).hasSize(1);
            assertThat(events).filteredOn(ContributionRegistered.class::isInstance).hasSize(3);
            assertThat(campaign.status()).isEqualTo(CampaignStatus.ACTIVE);
            assertThat(campaign.raisedAmount()).isEqualTo(usd("600"));
            assertThat(campaign.isGoalReached()).isTrue();
            assertThat(campaign.milestones()).filteredOn(m -> m.type() == MilestoneType.GOAL_REACHED).hasSize(1);
        }

        @Test
        void currencyMustMatchGoal() {
            Campaign campaign = active("100");
            assertThatThrownBy(() -> campaign.contribute(CONTRIBUTOR, Money.of("10", "EUR"), false, null,
                    UUID.randomUUID(), true, NOW)).isInstanceOf(InvalidContributionAmountException.class);
        }
    }

    @Nested
    class Closing {

        @Test
        void schedulerClosesAfterDeadlineAndItCannotReopenOrReceiveContributions() {
            Campaign campaign = active("1000");
            give(campaign, "10", NOW);
            campaign.pullDomainEvents();

            assertThat(campaign.closeByDeadline(DEADLINE.minusSeconds(1))).isFalse();
            assertThat(campaign.closeByDeadline(DEADLINE)).isTrue();

            assertThat(campaign.status()).isEqualTo(CampaignStatus.CLOSED_BY_DEADLINE);
            assertThat(campaign.pullDomainEvents()).singleElement().isInstanceOf(CampaignClosed.class);
            assertThatThrownBy(() -> campaign.publish(CREATOR, NOW)).isInstanceOf(InvalidCampaignStateException.class);
            assertThatThrownBy(() -> give(campaign, "10", NOW))
                    .isInstanceOf(CampaignNotAcceptingContributionsException.class);
            assertThat(campaign.closeByDeadline(DEADLINE.plusSeconds(60))).isFalse();
        }

        @Test
        void creatorClosesManuallyAfterGoalReached() {
            Campaign campaign = active("10");
            give(campaign, "10", NOW);
            campaign.closeManually(CREATOR, NOW);
            assertThat(campaign.status()).isEqualTo(CampaignStatus.CLOSED_MANUALLY);
        }

        @Test
        void onlyCreatorClosesManually() {
            Campaign campaign = active("10");
            assertThatThrownBy(() -> campaign.closeManually(UUID.randomUUID(), NOW))
                    .isInstanceOf(ForbiddenOperationException.class);
        }

        @Test
        void closingSoonIsEmittedOnceInsideTheWindow() {
            Campaign campaign = active("1000");
            Duration window = Duration.ofHours(24);
            assertThat(campaign.markClosingSoon(NOW, window)).isFalse();

            Instant dayBefore = DEADLINE.minus(Duration.ofHours(23));
            assertThat(campaign.markClosingSoon(dayBefore, window)).isTrue();
            assertThat(campaign.markClosingSoon(dayBefore.plusSeconds(60), window)).isFalse();

            assertThat(campaign.pullDomainEvents()).singleElement().isInstanceOf(ClosingSoon.class);
            assertThat(campaign.closingSoonNotified()).isTrue();
        }

        @Test
        void suspendEmitsClosedEvent() {
            Campaign campaign = active("1000");
            campaign.suspend(NOW);
            assertThat(campaign.status()).isEqualTo(CampaignStatus.SUSPENDED);
            assertThat(campaign.pullDomainEvents()).singleElement().isInstanceOf(CampaignClosed.class);
        }
    }

    @Nested
    class Cancelling {

        @Test
        void campaignWithoutContributionsCanBeCancelled() {
            Campaign campaign = active("1000");
            campaign.cancel(CREATOR, NOW);
            assertThat(campaign.status()).isEqualTo(CampaignStatus.CANCELLED);
        }

        @Test
        void campaignWithContributionsCannotBeCancelledOnlyClosed() {
            Campaign campaign = active("1000");
            give(campaign, "1", NOW);
            assertThat(campaign.canBeCancelled()).isFalse();
            assertThatThrownBy(() -> campaign.cancel(CREATOR, NOW))
                    .isInstanceOf(CampaignHasContributionsException.class);

            campaign.closeManually(CREATOR, NOW);
            assertThat(campaign.status()).isEqualTo(CampaignStatus.CLOSED_MANUALLY);
        }

        @Test
        void onlyCreatorCancels() {
            Campaign campaign = draft("1000");
            assertThatThrownBy(() -> campaign.cancel(UUID.randomUUID(), NOW))
                    .isInstanceOf(ForbiddenOperationException.class);
        }

        @Test
        void cancelledCampaignCannotBePublished() {
            Campaign campaign = draft("1000");
            campaign.cancel(CREATOR, NOW);
            assertThatThrownBy(() -> campaign.publish(CREATOR, NOW)).isInstanceOf(InvalidCampaignStateException.class);
        }
    }
}
