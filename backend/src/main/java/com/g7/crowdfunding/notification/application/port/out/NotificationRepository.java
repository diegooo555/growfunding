package com.g7.crowdfunding.notification.application.port.out;

import com.g7.crowdfunding.notification.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void saveAll(List<Notification> notifications);

    List<Notification> findByRecipient(UUID recipientId, int limit);

    int markAllRead(UUID recipientId);
}
