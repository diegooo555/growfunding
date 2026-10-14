package com.g7.crowdfunding.notification.application.eventhandler;

import com.g7.crowdfunding.notification.application.port.out.NotificationPort;
import com.g7.crowdfunding.notification.application.port.out.NotificationRepository;
import com.g7.crowdfunding.notification.domain.model.Notification;
import com.g7.crowdfunding.notification.domain.model.NotificationType;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Stores in-app notifications and pushes them through the delivery channel. Any failure
 * propagates to the outbox relay, which rolls the batch back and retries later.
 */
public class NotificationDispatcher {

    private final NotificationRepository repository;
    private final NotificationPort channel;
    private final ClockPort clock;

    public NotificationDispatcher(NotificationRepository repository, NotificationPort channel, ClockPort clock) {
        this.repository = repository;
        this.channel = channel;
        this.clock = clock;
    }

    public void notifyAll(Collection<UUID> recipients, NotificationType type, UUID campaignId, String title,
                          String message, DomainEvent source) {
        List<Notification> notifications = recipients.stream()
                .distinct()
                .map(recipient -> Notification.create(recipient, type, campaignId, title, message,
                        source.eventId(), clock.now()))
                .toList();
        if (notifications.isEmpty()) {
            return;
        }
        repository.saveAll(notifications);
        notifications.forEach(channel::deliver);
    }
}
