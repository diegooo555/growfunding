package com.g7.crowdfunding.campaign.application.query;

import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Flat projection returned by the read-model port.
 */
public record CampaignRecord(UUID id, UUID creatorId, String creatorName, String title, String description,
                             Category category, BigDecimal goalAmount, BigDecimal raisedAmount, String currency,
                             int contributionsCount, int contributorsCount, Instant deadline, CampaignStatus status,
                             ContributorsVisibility contributorsVisibility, Instant createdAt, Instant publishedAt,
                             Instant closedAt, Instant goalReachedAt) {
}
