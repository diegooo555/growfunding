package com.g7.crowdfunding.shared.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventJpaEntity {

    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);
    private static final Duration MAX_BACKOFF = Duration.ofMinutes(10);
    private static final int MAX_ERROR_LENGTH = 1000;

    @Id
    private UUID id;
    private String eventType;
    private UUID aggregateId;
    @Column(length = 8000)
    private String payload;
    private Instant occurredAt;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private int attempts;
    private Instant nextAttemptAt;
    private String lastError;
    private Instant processedAt;
    private Instant createdAt;

    protected OutboxEventJpaEntity() {
    }

    public OutboxEventJpaEntity(UUID id, String eventType, UUID aggregateId, String payload, Instant occurredAt,
                                Instant now) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = now;
        this.createdAt = now;
    }

    public void markProcessed(Instant now) {
        attempts++;
        status = OutboxStatus.PROCESSED;
        processedAt = now;
        lastError = null;
    }

    public void registerFailure(String error, Instant now, int maxAttempts) {
        attempts++;
        lastError = error == null ? "unknown error"
                : error.substring(0, Math.min(error.length(), MAX_ERROR_LENGTH));
        if (attempts >= maxAttempts) {
            status = OutboxStatus.FAILED;
            return;
        }
        Duration backoff = BASE_BACKOFF.multipliedBy(1L << Math.min(attempts - 1, 10));
        nextAttemptAt = now.plus(backoff.compareTo(MAX_BACKOFF) > 0 ? MAX_BACKOFF : backoff);
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }
}
