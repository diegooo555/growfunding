package com.g7.crowdfunding.campaign.domain.event;

import com.g7.crowdfunding.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ClosingSoon(
        UUID eventId,
        Instant occurredAt,
        UUID campaignId,
        String campaignTitle,
        UUID creatorId,
        Instant deadline) implements DomainEvent {

    @Override
    public UUID aggregateId() {
        return campaignId;
    }
}
