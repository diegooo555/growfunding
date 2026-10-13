package com.g7.crowdfunding.campaign.domain.event;

import com.g7.crowdfunding.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampaignClosed(
        UUID eventId,
        Instant occurredAt,
        UUID campaignId,
        String campaignTitle,
        UUID creatorId,
        CloseReason reason,
        BigDecimal raisedAmount,
        String currency,
        boolean goalReached) implements DomainEvent {

    public enum CloseReason {
        DEADLINE,
        MANUAL,
        SUSPENDED
    }

    @Override
    public UUID aggregateId() {
        return campaignId;
    }
}
