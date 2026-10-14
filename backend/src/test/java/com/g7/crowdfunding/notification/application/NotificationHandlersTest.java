package com.g7.crowdfunding.notification.application;

import com.g7.crowdfunding.campaign.domain.event.GoalReached;
import com.g7.crowdfunding.notification.application.eventhandler.GoalReachedNotificationHandler;
import com.g7.crowdfunding.notification.application.eventhandler.NotificationDispatcher;
import com.g7.crowdfunding.notification.application.port.out.NotificationPort;
import com.g7.crowdfunding.notification.application.port.out.NotificationRepository;
import com.g7.crowdfunding.notification.domain.model.Notification;
import com.g7.crowdfunding.notification.domain.model.NotificationType;
import com.g7.crowdfunding.support.InMemoryAdapters.MutableClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationHandlersTest {

    private final List<Notification> stored = new ArrayList<>();
    private final NotificationRepository repository = new NotificationRepository() {
        @Override
        public void saveAll(List<Notification> notifications) {
            stored.addAll(notifications);
        }

        @Override
        public List<Notification> findByRecipient(UUID recipientId, int limit) {
            return stored.stream().filter(n -> n.recipientId().equals(recipientId)).toList();
        }

        @Override
        public int markAllRead(UUID recipientId) {
            return 0;
        }
    };

    private final UUID creator = UUID.randomUUID();
    private final UUID c1 = UUID.randomUUID();
    private final UUID c2 = UUID.randomUUID();
    private final GoalReached event = new GoalReached(UUID.randomUUID(), Instant.now(), UUID.randomUUID(), "Title",
            creator, new BigDecimal("100"), new BigDecimal("120"), "USD");

    @Test
    void goalReachedNotifiesEveryContributorAndTheCreator() {
        List<Notification> delivered = new ArrayList<>();
        var handler = new GoalReachedNotificationHandler(campaignId -> Set.of(c1, c2),
                new NotificationDispatcher(repository, delivered::add, new MutableClock(Instant.now())));

        handler.handle(event);

        assertThat(stored).extracting(Notification::recipientId).containsExactlyInAnyOrder(c1, c2, creator);
        assertThat(stored).allMatch(n -> n.type() == NotificationType.GOAL_REACHED);
        assertThat(delivered).hasSize(3);
    }

    @Test
    void channelFailurePropagatesSoTheOutboxRetries() {
        NotificationPort failing = n -> {
            throw new IllegalStateException("smtp down");
        };
        var handler = new GoalReachedNotificationHandler(campaignId -> Set.of(c1),
                new NotificationDispatcher(repository, failing, new MutableClock(Instant.now())));

        assertThatThrownBy(() -> handler.handle(event)).isInstanceOf(IllegalStateException.class);
    }
}
