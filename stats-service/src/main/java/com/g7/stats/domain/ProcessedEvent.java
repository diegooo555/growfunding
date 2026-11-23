package com.g7.stats.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Mark that an event was already applied: the base of the idempotent consumer. */
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    private UUID eventId;
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(UUID eventId, Instant processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    public UUID getEventId() { return eventId; }
}
