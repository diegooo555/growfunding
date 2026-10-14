package com.g7.crowdfunding.contribution.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.contribution.application.port.out.ContributionRepository;
import com.g7.crowdfunding.contribution.domain.model.Contribution;
import com.g7.crowdfunding.shared.domain.model.Money;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class ContributionPersistenceAdapter implements ContributionRepository {

    private final ContributionJpaRepository repository;
    private final EntityManager em;

    ContributionPersistenceAdapter(ContributionJpaRepository repository, EntityManager em) {
        this.repository = repository;
        this.em = em;
    }

    @Override
    public Optional<Contribution> findByIdempotencyKey(UUID idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(ContributionPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByCampaignIdAndContributorId(UUID campaignId, UUID contributorId) {
        return repository.existsByCampaignIdAndContributorId(campaignId, contributorId);
    }

    @Override
    public void save(Contribution c) {
        ContributionJpaEntity e = new ContributionJpaEntity();
        e.id = c.id();
        e.campaignId = c.campaignId();
        e.contributorId = c.contributorId();
        e.amount = c.amount().amount();
        e.currency = c.amount().currency();
        e.anonymous = c.anonymous();
        e.message = c.message();
        e.idempotencyKey = c.idempotencyKey();
        e.createdAt = c.createdAt();
        // persist (not merge): the id is assigned by the domain, so this is always an insert
        em.persist(e);
    }

    private static Contribution toDomain(ContributionJpaEntity e) {
        return Contribution.rehydrate(e.id, e.campaignId, e.contributorId, Money.of(e.amount, e.currency),
                e.anonymous, e.message, e.idempotencyKey, e.createdAt);
    }
}
