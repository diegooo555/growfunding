package com.g7.crowdfunding.notification.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * In-app notification addressed to a single user.
 */
public record Notification(UUID id, UUID recipientId, NotificationType type, UUID campaignId, String title,
                           String message, UUID sourceEventId, Instant createdAt, boolean read) {

    public Notification {
        Objects.requireNonNull(id);
        Objects.requireNonNull(recipientId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(title);
        Objects.requireNonNull(message);
        Objects.requireNonNull(createdAt);
    }

    public static Notification create(UUID recipientId, NotificationType type, UUID campaignId, String title,
                                      String message, UUID sourceEventId, Instant now) {
        return new Notification(UUID.randomUUID(), recipientId, type, campaignId, title, message, sourceEventId,
                now, false);
    }
}
