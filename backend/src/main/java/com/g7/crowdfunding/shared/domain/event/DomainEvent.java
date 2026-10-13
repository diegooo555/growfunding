package com.g7.crowdfunding.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker contract for every domain event.
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();

    UUID aggregateId();

    default String eventType() {
        return getClass().getSimpleName();
    }
}
