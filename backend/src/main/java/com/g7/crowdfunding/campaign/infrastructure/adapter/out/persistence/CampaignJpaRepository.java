package com.g7.crowdfunding.campaign.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface CampaignJpaRepository extends JpaRepository<CampaignJpaEntity, UUID> {

    @Query("""
            select c.id from CampaignJpaEntity c
            where c.status = com.g7.crowdfunding.campaign.domain.model.CampaignStatus.ACTIVE
              and c.deadline <= :instant
            """)
    List<UUID> findActiveIdsWithDeadlineAtOrBefore(@Param("instant") Instant instant);

    @Query("""
            select c.id from CampaignJpaEntity c
            where c.status = com.g7.crowdfunding.campaign.domain.model.CampaignStatus.ACTIVE
              and c.closingSoonNotified = false
              and c.deadline <= :upTo
            """)
    List<UUID> findActiveIdsPendingClosingSoonNotice(@Param("upTo") Instant upTo);
}
