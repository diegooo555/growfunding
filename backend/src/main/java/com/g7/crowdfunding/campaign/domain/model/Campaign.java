package com.g7.crowdfunding.campaign.domain.model;

import com.g7.crowdfunding.campaign.domain.event.CampaignClosed;
import com.g7.crowdfunding.campaign.domain.event.CampaignClosed.CloseReason;
import com.g7.crowdfunding.campaign.domain.event.ClosingSoon;
import com.g7.crowdfunding.campaign.domain.event.ContributionRegistered;
import com.g7.crowdfunding.campaign.domain.event.GoalReached;
import com.g7.crowdfunding.campaign.domain.exception.CampaignHasContributionsException;
import com.g7.crowdfunding.campaign.domain.exception.CampaignNotAcceptingContributionsException;
import com.g7.crowdfunding.campaign.domain.exception.ContributionAfterDeadlineException;
import com.g7.crowdfunding.campaign.domain.exception.InvalidCampaignException;
import com.g7.crowdfunding.campaign.domain.exception.InvalidCampaignStateException;
import com.g7.crowdfunding.contribution.domain.exception.InvalidContributionAmountException;
import com.g7.crowdfunding.contribution.domain.model.Contribution;
import com.g7.crowdfunding.shared.domain.exception.ForbiddenOperationException;
import com.g7.crowdfunding.shared.domain.model.AggregateRoot;
import com.g7.crowdfunding.shared.domain.model.Money;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Campaign aggregate root. Owns funding progress and lifecycle rules.
 */
public final class Campaign extends AggregateRoot {

    public static final int MAX_TITLE_LENGTH = 150;
    public static final int MAX_DESCRIPTION_LENGTH = 4000;

    private final UUID id;
    private final UUID creatorId;
    private final String title;
    private final String description;
    private final Category category;
    private final Goal goal;
    private final Instant deadline;
    private final ContributorsVisibility contributorsVisibility;
    private final Instant createdAt;
    private final List<Milestone> milestones;
    private final Long version;

    private CampaignStatus status;
    private Money raisedAmount;
    private int contributionsCount;
    private int contributorsCount;
    private Instant publishedAt;
    private Instant closedAt;
    private Instant goalReachedAt;
    private boolean closingSoonNotified;

    private Campaign(CampaignSnapshot s) {
        this.id = Objects.requireNonNull(s.id());
        this.creatorId = Objects.requireNonNull(s.creatorId());
        this.title = s.title();
        this.description = s.description();
        this.category = s.category() == null ? Category.OTHER : s.category();
        this.goal = new Goal(Money.of(s.goalAmount(), s.currency()));
        this.deadline = Objects.requireNonNull(s.deadline());
        this.status = Objects.requireNonNull(s.status());
        this.raisedAmount = Money.of(s.raisedAmount(), s.currency());
        this.contributionsCount = s.contributionsCount();
        this.contributorsCount = s.contributorsCount();
        this.contributorsVisibility = s.contributorsVisibility() == null
                ? ContributorsVisibility.PUBLIC : s.contributorsVisibility();
        this.createdAt = Objects.requireNonNull(s.createdAt());
        this.publishedAt = s.publishedAt();
        this.closedAt = s.closedAt();
        this.goalReachedAt = s.goalReachedAt();
        this.closingSoonNotified = s.closingSoonNotified();
        this.milestones = new ArrayList<>(s.milestones() == null ? List.of() : s.milestones());
        this.version = s.version();
    }

    /**
     * HU-01: a creator drafts a new campaign.
     */
    public static Campaign create(UUID creatorId, String title, String description, Category category,
                                  Money goalAmount, Instant deadline,
                                  ContributorsVisibility visibility, Instant now) {
        if (creatorId == null) {
            throw new InvalidCampaignException("Creator is required");
        }
        String cleanTitle = requireText(title, "Title", MAX_TITLE_LENGTH);
        String cleanDescription = requireText(description, "Description", MAX_DESCRIPTION_LENGTH);
        Goal goal = new Goal(goalAmount);
        if (deadline == null || !deadline.isAfter(now)) {
            throw new InvalidCampaignException("Deadline must be in the future");
        }
        return new Campaign(new CampaignSnapshot(
                UUID.randomUUID(), creatorId, cleanTitle, cleanDescription, category,
                goal.amount().amount(), goal.currency(), deadline, CampaignStatus.DRAFT,
                Money.zero(goal.currency()).amount(), 0, 0, visibility, now,
                null, null, null, false, List.of(), null));
    }

    public static Campaign rehydrate(CampaignSnapshot snapshot) {
        return new Campaign(snapshot);
    }

    /**
     * HU-02: DRAFT -> ACTIVE, the campaign becomes publicly visible.
     */
    public void publish(UUID requesterId, Instant now) {
        requireOwner(requesterId);
        if (status != CampaignStatus.DRAFT) {
            throw new InvalidCampaignStateException("Only draft campaigns can be published (current: " + status + ")");
        }
        if (!deadline.isAfter(now)) {
            throw new InvalidCampaignStateException("Cannot publish a campaign whose deadline has already passed");
        }
        status = CampaignStatus.ACTIVE;
        publishedAt = now;
        milestones.add(Milestone.of(MilestoneType.PUBLISHED, now, "Campaign published"));
    }

    /**
     * HU-04 / HU-05: registers a contribution and updates progress atomically. Emits
     * {@link ContributionRegistered} and, the first time the goal is met, {@link GoalReached}.
     * After the goal is reached the campaign keeps accepting contributions.
     *
     * @param newContributor whether this contributor had not contributed to this campaign before
     */
    public Contribution contribute(UUID contributorId, Money amount, boolean anonymous, String message,
                                   UUID idempotencyKey, boolean newContributor, Instant now) {
        Contribution.requirePositive(amount);
        if (!amount.hasSameCurrencyAs(goal.amount())) {
            throw new InvalidContributionAmountException("Contribution currency must be " + goal.currency());
        }
        if (status != CampaignStatus.ACTIVE) {
            throw new CampaignNotAcceptingContributionsException(
                    "Campaign is not accepting contributions (status: " + status + ")");
        }
        if (!now.isBefore(deadline)) {
            throw new ContributionAfterDeadlineException();
        }

        Contribution contribution = Contribution.register(id, contributorId, amount, anonymous, message,
                idempotencyKey, now);
        raisedAmount = raisedAmount.add(amount);
        contributionsCount++;
        if (newContributor) {
            contributorsCount++;
        }
        registerEvent(new ContributionRegistered(UUID.randomUUID(), now, id, title, creatorId,
                contribution.id(), contributorId, amount.amount(), amount.currency(), anonymous));

        if (goalReachedAt == null && goal.isReachedBy(raisedAmount)) {
            goalReachedAt = now;
            milestones.add(Milestone.of(MilestoneType.GOAL_REACHED, now, "Funding goal reached"));
            registerEvent(new GoalReached(UUID.randomUUID(), now, id, title, creatorId,
                    goal.amount().amount(), raisedAmount.amount(), goal.currency()));
        }
        return contribution;
    }

    /**
     * Creator action: closes an active campaign (with or without the goal reached).
     */
    public void closeManually(UUID requesterId, Instant now) {
        requireOwner(requesterId);
        requireActive("closed");
        close(CampaignStatus.CLOSED_MANUALLY, CloseReason.MANUAL, now);
    }

    /**
     * HU-06: the scheduler closes campaigns whose deadline has passed.
     *
     * @return true if the campaign was closed by this call
     */
    public boolean closeByDeadline(Instant now) {
        if (status != CampaignStatus.ACTIVE || now.isBefore(deadline)) {
            return false;
        }
        close(CampaignStatus.CLOSED_BY_DEADLINE, CloseReason.DEADLINE, now);
        return true;
    }

    /**
     * HU-06: emits {@link ClosingSoon} once when the deadline is within the given window.
     *
     * @return true if the event was emitted by this call
     */
    public boolean markClosingSoon(Instant now, Duration window) {
        if (status != CampaignStatus.ACTIVE || closingSoonNotified || !now.isBefore(deadline)) {
            return false;
        }
        if (Duration.between(now, deadline).compareTo(window) > 0) {
            return false;
        }
        closingSoonNotified = true;
        milestones.add(Milestone.of(MilestoneType.CLOSING_SOON, now, "Campaign closes soon"));
        registerEvent(new ClosingSoon(UUID.randomUUID(), now, id, title, creatorId, deadline));
        return true;
    }

    /**
     * HU-09: cancellation is only allowed while the campaign has no contributions.
     */
    public void cancel(UUID requesterId, Instant now) {
        requireOwner(requesterId);
        if (status != CampaignStatus.DRAFT && status != CampaignStatus.ACTIVE) {
            throw new InvalidCampaignStateException("Only draft or active campaigns can be cancelled (current: " + status + ")");
        }
        if (contributionsCount > 0) {
            throw new CampaignHasContributionsException();
        }
        status = CampaignStatus.CANCELLED;
        closedAt = now;
    }

    /**
     * Admin action: suspends a non-terminal campaign.
     */
    public void suspend(Instant now) {
        if (status.isTerminal()) {
            throw new InvalidCampaignStateException("Campaign is already finished (current: " + status + ")");
        }
        close(CampaignStatus.SUSPENDED, CloseReason.SUSPENDED, now);
    }

    public boolean canBeCancelled() {
        return (status == CampaignStatus.DRAFT || status == CampaignStatus.ACTIVE) && contributionsCount == 0;
    }

    public boolean isGoalReached() {
        return goalReachedAt != null;
    }

    public boolean isOwnedBy(UUID userId) {
        return creatorId.equals(userId);
    }

    private void close(CampaignStatus target, CloseReason reason, Instant now) {
        CampaignStatus previous = status;
        status = target;
        closedAt = now;
        milestones.add(Milestone.of(MilestoneType.CLOSED, now, "Campaign closed: " + reason));
        if (previous == CampaignStatus.ACTIVE || contributionsCount > 0) {
            registerEvent(new CampaignClosed(UUID.randomUUID(), now, id, title, creatorId, reason,
                    raisedAmount.amount(), raisedAmount.currency(), isGoalReached()));
        }
    }

    private void requireActive(String action) {
        if (status != CampaignStatus.ACTIVE) {
            throw new InvalidCampaignStateException("Only active campaigns can be " + action + " (current: " + status + ")");
        }
    }

    private void requireOwner(UUID requesterId) {
        if (!creatorId.equals(requesterId)) {
            throw new ForbiddenOperationException("Only the campaign creator can perform this action");
        }
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidCampaignException(field + " is required");
        }
        String stripped = value.strip();
        if (stripped.length() > maxLength) {
            throw new InvalidCampaignException(field + " cannot exceed " + maxLength + " characters");
        }
        return stripped;
    }

    public CampaignSnapshot snapshot() {
        return new CampaignSnapshot(id, creatorId, title, description, category, goal.amount().amount(),
                goal.currency(), deadline, status, raisedAmount.amount(), contributionsCount, contributorsCount,
                contributorsVisibility, createdAt, publishedAt, closedAt, goalReachedAt, closingSoonNotified,
                List.copyOf(milestones), version);
    }

    public UUID id() {
        return id;
    }

    public UUID creatorId() {
        return creatorId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return category;
    }

    public Goal goal() {
        return goal;
    }

    public Instant deadline() {
        return deadline;
    }

    public CampaignStatus status() {
        return status;
    }

    public Money raisedAmount() {
        return raisedAmount;
    }

    public int contributionsCount() {
        return contributionsCount;
    }

    public int contributorsCount() {
        return contributorsCount;
    }

    public ContributorsVisibility contributorsVisibility() {
        return contributorsVisibility;
    }

    public Instant goalReachedAt() {
        return goalReachedAt;
    }

    public boolean closingSoonNotified() {
        return closingSoonNotified;
    }

    public List<Milestone> milestones() {
        return List.copyOf(milestones);
    }

    public Long version() {
        return version;
    }
}
