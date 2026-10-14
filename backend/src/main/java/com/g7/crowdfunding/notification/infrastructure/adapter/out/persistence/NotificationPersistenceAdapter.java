package com.g7.crowdfunding.notification.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.notification.application.port.out.NotificationRepository;
import com.g7.crowdfunding.notification.domain.model.Notification;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository repository;
    private final EntityManager em;

    NotificationPersistenceAdapter(NotificationJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    @Override
    public void saveAll(List<Notification> notifications) {
        for (Notification n : notifications) {
            NotificationJpaEntity e = new NotificationJpaEntity();
            e.id = n.id();
            e.recipientId = n.recipientId();
            e.type = n.type();
            e.campaignId = n.campaignId();
            e.title = n.title();
            e.message = n.message();
            e.sourceEventId = n.sourceEventId();
            e.createdAt = n.createdAt();
            e.read = n.read();
            em.persist(e);
        }
    }

    @Override
    public List<Notification> findByRecipient(UUID recipientId, int limit) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(0, limit)).stream()
                .map(e -> new Notification(e.id, e.recipientId, e.type, e.campaignId, e.title, e.message,
                        e.sourceEventId, e.createdAt, e.read))
                .toList();
    }

    @Override
    public int markAllRead(UUID recipientId) {
        return repository.markAllRead(recipientId);
    }
}
