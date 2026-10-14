package com.g7.crowdfunding.campaign.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class CampaignJpaEntity {

    @Id
    UUID id;
    UUID creatorId;
    String title;
    String description;
    @Enumerated(EnumType.STRING)
    Category category;
    BigDecimal goalAmount;
    String currency;
    Instant deadline;
    @Enumerated(EnumType.STRING)
    CampaignStatus status;
    BigDecimal raisedAmount;
    int contributionsCount;
    int contributorsCount;
    @Enumerated(EnumType.STRING)
    ContributorsVisibility contributorsVisibility;
    Instant createdAt;
    Instant publishedAt;
    Instant closedAt;
    Instant goalReachedAt;
    boolean closingSoonNotified;
    /**
     * Optimistic locking guard: concurrent contributions to the same campaign cannot both win.
     */
    @Version
    Long version;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("reachedAt ASC")
    List<MilestoneJpaEntity> milestones = new ArrayList<>();

    protected CampaignJpaEntity() {
    }

    CampaignJpaEntity(UUID id) {
        this.id = id;
    }
}
