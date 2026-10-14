package com.g7.crowdfunding.contribution.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One row of the public contributors list. Anonymous contributions hide the contributor identity.
 */
public record CampaignContributionView(UUID contributionId, UUID contributorId, String contributorName,
                                       boolean anonymous, BigDecimal amount, String currency, String message,
                                       Instant createdAt) {

    public CampaignContributionView masked() {
        if (!anonymous) {
            return this;
        }
        return new CampaignContributionView(contributionId, null, null, true, amount, currency, message, createdAt);
    }
}
