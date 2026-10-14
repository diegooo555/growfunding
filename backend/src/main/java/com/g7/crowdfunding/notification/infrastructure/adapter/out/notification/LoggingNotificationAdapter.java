package com.g7.crowdfunding.notification.infrastructure.adapter.out.notification;

import com.g7.crowdfunding.notification.application.port.out.NotificationPort;
import com.g7.crowdfunding.notification.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Stand-in for an external channel (email/push). Can simulate failures to demonstrate that the
 * outbox retries delivery and that campaign browsing is unaffected.
 */
@Component
class LoggingNotificationAdapter implements NotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    private final double simulatedFailureRate;

    LoggingNotificationAdapter(@Value("${crowdfunding.notifications.simulated-failure-rate:0.0}")
                               double simulatedFailureRate) {
        this.simulatedFailureRate = simulatedFailureRate;
    }

    @Override
    public void deliver(Notification notification) {
        if (simulatedFailureRate > 0 && ThreadLocalRandom.current().nextDouble() < simulatedFailureRate) {
            throw new IllegalStateException("Simulated notification channel failure");
        }
        LOG.info("[notification] to={} type={} title=\"{}\" message=\"{}\"", notification.recipientId(),
                notification.type(), notification.title(), notification.message());
    }
}
