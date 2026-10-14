package com.g7.crowdfunding.contribution.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * HU-04: register a contribution. Idempotent on {@code idempotencyKey}.
 */
public interface ContributeToCampaignUseCase {

    ContributionReceipt contribute(ContributeCommand command);

    record ContributeCommand(UUID campaignId, UUID contributorId, BigDecimal amount, String currency,
                             boolean anonymous, String message, UUID idempotencyKey) {
    }

    /**
     * @param replayed true when the request was a retry of an already registered contribution
     */
    record ContributionReceipt(UUID contributionId, UUID campaignId, BigDecimal amount, String currency,
                               Instant createdAt, boolean replayed, BigDecimal campaignRaisedAmount,
                               int campaignContributionsCount, int campaignContributorsCount,
                               boolean campaignGoalReached) {
    }
}
