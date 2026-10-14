package com.g7.crowdfunding.notification.application.port.out;

import com.g7.crowdfunding.notification.domain.model.Notification;

/**
 * Outbound delivery channel (email, push, log...). Isolated from campaign browsing: it is only
 * invoked by the outbox relay, never by queries.
 */
public interface NotificationPort {

    void deliver(Notification notification);
}
