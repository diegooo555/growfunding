package com.g7.crowdfunding.campaign.domain.event;

import com.g7.crowdfunding.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContributionRegistered(
        UUID eventId,
        Instant occurredAt,
        UUID campaignId,
        String campaignTitle,
        UUID creatorId,
        UUID contributionId,
        UUID contributorId,
        BigDecimal amount,
        String currency,
        boolean anonymous) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return campaignId;
    }
}
