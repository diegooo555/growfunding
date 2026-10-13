package com.g7.crowdfunding.contribution.domain.model;

import com.g7.crowdfunding.contribution.domain.exception.InvalidContributionAmountException;
import com.g7.crowdfunding.contribution.domain.exception.InvalidContributionException;
import com.g7.crowdfunding.shared.domain.model.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A single monetary contribution to a campaign. Created only through
 * {@code Campaign#contribute}, which enforces campaign-level rules.
 */
public final class Contribution {

    public static final int MAX_MESSAGE_LENGTH = 500;

    private final UUID id;
    private final UUID campaignId;
    private final UUID contributorId;
    private final Money amount;
    private final boolean anonymous;
    private final String message;
    private final UUID idempotencyKey;
    private final Instant createdAt;

    private Contribution(UUID id, UUID campaignId, UUID contributorId, Money amount, boolean anonymous,
                         String message, UUID idempotencyKey, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.campaignId = Objects.requireNonNull(campaignId);
        this.contributorId = Objects.requireNonNull(contributorId);
        this.amount = Objects.requireNonNull(amount);
        this.anonymous = anonymous;
        this.message = message;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Contribution register(UUID campaignId, UUID contributorId, Money amount, boolean anonymous,
                                        String message, UUID idempotencyKey, Instant now) {
        requirePositive(amount);
        if (contributorId == null) {
            throw new InvalidContributionException("Contributor is required");
        }
        if (idempotencyKey == null) {
            throw new InvalidContributionException("Idempotency key is required");
        }
        String normalizedMessage = message == null || message.isBlank() ? null : message.strip();
        if (normalizedMessage != null && normalizedMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidContributionException("Message cannot exceed " + MAX_MESSAGE_LENGTH + " characters");
        }
        return new Contribution(UUID.randomUUID(), campaignId, contributorId, amount, anonymous,
                normalizedMessage, idempotencyKey, now);
    }

    public static Contribution rehydrate(UUID id, UUID campaignId, UUID contributorId, Money amount,
                                         boolean anonymous, String message, UUID idempotencyKey, Instant createdAt) {
        return new Contribution(id, campaignId, contributorId, amount, anonymous, message, idempotencyKey, createdAt);
    }

    public static void requirePositive(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidContributionAmountException("Contribution amount must be greater than zero");
        }
    }

    public boolean belongsTo(UUID campaignId, UUID contributorId) {
        return this.campaignId.equals(campaignId) && this.contributorId.equals(contributorId);
    }

    public UUID id() {
        return id;
    }

    public UUID campaignId() {
        return campaignId;
    }

    public UUID contributorId() {
        return contributorId;
    }

    public Money amount() {
        return amount;
    }

    public boolean anonymous() {
        return anonymous;
    }

    public String message() {
        return message;
    }

    public UUID idempotencyKey() {
        return idempotencyKey;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
