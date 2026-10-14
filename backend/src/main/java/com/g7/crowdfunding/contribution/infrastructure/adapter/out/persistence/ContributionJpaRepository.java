package com.g7.crowdfunding.contribution.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ContributionJpaRepository extends JpaRepository<ContributionJpaEntity, UUID> {

    Optional<ContributionJpaEntity> findByIdempotencyKey(UUID idempotencyKey);

    boolean existsByCampaignIdAndContributorId(UUID campaignId, UUID contributorId);
}
