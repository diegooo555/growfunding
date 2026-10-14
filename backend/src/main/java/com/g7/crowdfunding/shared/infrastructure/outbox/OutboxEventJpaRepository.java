package com.g7.crowdfunding.shared.infrastructure.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("""
            select e.id from OutboxEventJpaEntity e
            where e.status = com.g7.crowdfunding.shared.infrastructure.outbox.OutboxStatus.PENDING
              and e.nextAttemptAt <= :now
            order by e.createdAt asc
            """)
    List<UUID> findDispatchableIds(@Param("now") Instant now, Pageable pageable);

    long countByStatus(OutboxStatus status);
}
