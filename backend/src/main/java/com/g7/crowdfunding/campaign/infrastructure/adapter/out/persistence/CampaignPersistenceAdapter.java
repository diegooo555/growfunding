package com.g7.crowdfunding.campaign.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.campaign.domain.model.CampaignSnapshot;
import com.g7.crowdfunding.campaign.domain.model.Milestone;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Write-side adapter. Maps the aggregate to JPA entities and enforces the aggregate version.
 * Must run inside a transaction opened by the application's {@code TransactionRunner}.
 */
@Component
class CampaignPersistenceAdapter implements CampaignRepository {

    private final CampaignJpaRepository repository;

    CampaignPersistenceAdapter(CampaignJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Campaign> findById(UUID id) {
        return repository.findById(id).map(CampaignPersistenceAdapter::toDomain);
    }

    @Override
    public void save(Campaign campaign) {
        CampaignSnapshot s = campaign.snapshot();
        CampaignJpaEntity entity;
        if (s.version() == null) {
            entity = new CampaignJpaEntity(s.id());
            copyImmutable(s, entity);
        } else {
            entity = repository.findById(s.id())
                    .orElseThrow(() -> new ObjectOptimisticLockingFailureException(CampaignJpaEntity.class, s.id()));
            if (!Objects.equals(entity.version, s.version())) {
                throw new ObjectOptimisticLockingFailureException(CampaignJpaEntity.class, s.id());
            }
        }
        copyMutable(s, entity);
        Set<UUID> known = entity.milestones.stream().map(m -> m.id).collect(Collectors.toSet());
        for (Milestone m : s.milestones()) {
            if (!known.contains(m.id())) {
                entity.milestones.add(new MilestoneJpaEntity(m.id(), entity, m.type(), m.reachedAt(), m.description()));
            }
        }
        repository.save(entity);
    }

    @Override
    public List<UUID> findActiveIdsWithDeadlineAtOrBefore(Instant instant) {
        return repository.findActiveIdsWithDeadlineAtOrBefore(instant);
    }

    @Override
    public List<UUID> findActiveIdsPendingClosingSoonNotice(Instant deadlineUpTo) {
        return repository.findActiveIdsPendingClosingSoonNotice(deadlineUpTo);
    }

    private static void copyImmutable(CampaignSnapshot s, CampaignJpaEntity e) {
        e.creatorId = s.creatorId();
        e.title = s.title();
        e.description = s.description();
        e.category = s.category();
        e.goalAmount = s.goalAmount();
        e.currency = s.currency();
        e.deadline = s.deadline();
        e.contributorsVisibility = s.contributorsVisibility();
        e.createdAt = s.createdAt();
    }

    private static void copyMutable(CampaignSnapshot s, CampaignJpaEntity e) {
        e.status = s.status();
        e.raisedAmount = s.raisedAmount();
        e.contributionsCount = s.contributionsCount();
        e.contributorsCount = s.contributorsCount();
        e.publishedAt = s.publishedAt();
        e.closedAt = s.closedAt();
        e.goalReachedAt = s.goalReachedAt();
        e.closingSoonNotified = s.closingSoonNotified();
    }

    private static Campaign toDomain(CampaignJpaEntity e) {
        List<Milestone> milestones = e.milestones.stream()
                .map(m -> new Milestone(m.id, m.type, m.reachedAt, m.description))
                .toList();
        return Campaign.rehydrate(new CampaignSnapshot(e.id, e.creatorId, e.title, e.description, e.category,
                e.goalAmount, e.currency, e.deadline, e.status, e.raisedAmount, e.contributionsCount,
                e.contributorsCount, e.contributorsVisibility, e.createdAt, e.publishedAt, e.closedAt,
                e.goalReachedAt, e.closingSoonNotified, milestones, e.version));
    }
}
