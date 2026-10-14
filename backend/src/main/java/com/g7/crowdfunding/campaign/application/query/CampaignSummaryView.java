package com.g7.crowdfunding.campaign.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampaignSummaryView(UUID id, String title, String summary, String category, UUID creatorId,
                                  String creatorName, BigDecimal goalAmount, BigDecimal raisedAmount,
                                  String currency, int progressPercent, int contributionsCount,
                                  int contributorsCount, Instant deadline, String status,
                                  String contributorsVisibility, boolean goalReached, boolean closingSoon,
                                  boolean acceptingContributions, boolean cancellable, Instant createdAt,
                                  Instant publishedAt, Instant closedAt) {
}
