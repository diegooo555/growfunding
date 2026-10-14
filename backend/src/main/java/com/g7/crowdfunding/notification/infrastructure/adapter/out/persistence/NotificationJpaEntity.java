package com.g7.crowdfunding.notification.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.notification.domain.model.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    UUID id;
    UUID recipientId;
    @Enumerated(EnumType.STRING)
    NotificationType type;
    UUID campaignId;
    String title;
    String message;
    UUID sourceEventId;
    Instant createdAt;
    @Column(name = "is_read")
    boolean read;

    protected NotificationJpaEntity() {
    }
}
