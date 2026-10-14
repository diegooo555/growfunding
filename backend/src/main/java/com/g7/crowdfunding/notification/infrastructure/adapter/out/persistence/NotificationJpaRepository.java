package com.g7.crowdfunding.notification.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    @Modifying
    @Query("update NotificationJpaEntity n set n.read = true where n.recipientId = :recipientId and n.read = false")
    int markAllRead(@Param("recipientId") UUID recipientId);
}
