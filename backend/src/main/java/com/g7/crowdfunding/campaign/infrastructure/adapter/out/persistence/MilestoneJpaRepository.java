package com.g7.crowdfunding.campaign.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface MilestoneJpaRepository extends JpaRepository<MilestoneJpaEntity, UUID> {

    List<MilestoneJpaEntity> findByCampaign_IdOrderByReachedAtAsc(UUID campaignId);
}
