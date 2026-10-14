package com.g7.crowdfunding.contribution.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContributionHistoryView(UUID contributionId, UUID campaignId, String campaignTitle,
                                      String campaignStatus, BigDecimal amount, String currency,
                                      boolean anonymous, String message, Instant createdAt) {
}
