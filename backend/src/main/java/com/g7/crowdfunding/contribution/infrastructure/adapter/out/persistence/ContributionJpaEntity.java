package com.g7.crowdfunding.contribution.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contributions")
public class ContributionJpaEntity {

    @Id
    UUID id;
    UUID campaignId;
    UUID contributorId;
    BigDecimal amount;
    String currency;
    boolean anonymous;
    String message;
    /**
     * Unique constraint {@code uk_contributions_idempotency_key} guarantees no duplicates.
     */
    UUID idempotencyKey;
    Instant createdAt;

    protected ContributionJpaEntity() {
    }
}
