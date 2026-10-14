package com.g7.crowdfunding.notification.application.query;

import java.time.Instant;
import java.util.UUID;

public record NotificationView(UUID id, String type, UUID campaignId, String title, String message,
                               Instant createdAt, boolean read) {
}
