package com.g7.crowdfunding.campaign.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Flat state of a campaign used to rehydrate and persist the aggregate without
 * leaking persistence concerns into the domain.
 */
public record CampaignSnapshot(
        UUID id,
        UUID creatorId,
        String title,
        String description,
        Category category,
        BigDecimal goalAmount,
        String currency,
        Instant deadline,
        CampaignStatus status,
        BigDecimal raisedAmount,
        int contributionsCount,
        int contributorsCount,
        ContributorsVisibility contributorsVisibility,
        Instant createdAt,
        Instant publishedAt,
        Instant closedAt,
        Instant goalReachedAt,
        boolean closingSoonNotified,
        List<Milestone> milestones,
        Long version) {
}
