package com.g7.crowdfunding.contribution.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.contribution.application.port.out.ContributionReadModelPort;
import com.g7.crowdfunding.contribution.application.query.CampaignContributionView;
import com.g7.crowdfunding.contribution.application.query.ContributionHistoryView;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Query-side projections for contributions (HU-07, HU-08).
 */
@Component
@Transactional(readOnly = true)
class ContributionReadAdapter implements ContributionReadModelPort {

    private final EntityManager em;

    ContributionReadAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<ContributionHistoryView> findByContributor(UUID contributorId) {
        return em.createQuery("""
                        select k.id, k.campaignId, c.title, c.status, k.amount, k.currency, k.anonymous,
                               k.message, k.createdAt
                        from ContributionJpaEntity k join CampaignJpaEntity c on c.id = k.campaignId
                        where k.contributorId = :contributorId
                        order by k.createdAt desc
                        """, Object[].class)
                .setParameter("contributorId", contributorId)
                .getResultStream()
                .map(r -> new ContributionHistoryView((UUID) r[0], (UUID) r[1], (String) r[2], r[3].toString(),
                        (BigDecimal) r[4], (String) r[5], (Boolean) r[6], (String) r[7], (Instant) r[8]))
                .toList();
    }

    @Override
    public List<CampaignContributionView> findByCampaign(UUID campaignId) {
        return em.createQuery("""
                        select k.id, k.contributorId, u.displayName, k.anonymous, k.amount, k.currency,
                               k.message, k.createdAt
                        from ContributionJpaEntity k left join UserJpaEntity u on u.id = k.contributorId
                        where k.campaignId = :campaignId
                        order by k.createdAt desc
                        """, Object[].class)
                .setParameter("campaignId", campaignId)
                .getResultStream()
                .map(r -> new CampaignContributionView((UUID) r[0], (UUID) r[1], (String) r[2], (Boolean) r[3],
                        (BigDecimal) r[4], (String) r[5], (String) r[6], (Instant) r[7]))
                .toList();
    }
}
