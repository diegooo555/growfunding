package com.g7.crowdfunding.campaign.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.campaign.domain.model.MilestoneType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaign_milestones")
public class MilestoneJpaEntity {

    @Id
    UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id")
    CampaignJpaEntity campaign;
    @Enumerated(EnumType.STRING)
    MilestoneType type;
    Instant reachedAt;
    String description;

    protected MilestoneJpaEntity() {
    }

    MilestoneJpaEntity(UUID id, CampaignJpaEntity campaign, MilestoneType type, Instant reachedAt,
                       String description) {
        this.id = id;
        this.campaign = campaign;
        this.type = type;
        this.reachedAt = reachedAt;
        this.description = description;
    }
}
