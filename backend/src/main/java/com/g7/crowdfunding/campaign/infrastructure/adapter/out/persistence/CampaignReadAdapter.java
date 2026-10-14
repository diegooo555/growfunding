package com.g7.crowdfunding.campaign.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.campaign.application.port.out.CampaignReadModelPort;
import com.g7.crowdfunding.campaign.application.query.CampaignRecord;
import com.g7.crowdfunding.campaign.application.query.CampaignSearchFilter;
import com.g7.crowdfunding.campaign.application.query.MilestoneView;
import com.g7.crowdfunding.shared.application.query.PageResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Query-side adapter (CQRS): projection queries straight into read DTOs; never loads aggregates.
 */
@Component
@Transactional(readOnly = true)
class CampaignReadAdapter implements CampaignReadModelPort {

    private static final String PROJECTION = """
            select new com.g7.crowdfunding.campaign.application.query.CampaignRecord(
                c.id, c.creatorId, u.displayName, c.title, c.description, c.category, c.goalAmount,
                c.raisedAmount, c.currency, c.contributionsCount, c.contributorsCount, c.deadline, c.status,
                c.contributorsVisibility, c.createdAt, c.publishedAt, c.closedAt, c.goalReachedAt)
            from CampaignJpaEntity c left join UserJpaEntity u on u.id = c.creatorId
            """;

    private final EntityManager em;
    private final MilestoneJpaRepository milestones;

    CampaignReadAdapter(EntityManager em, MilestoneJpaRepository milestones) {
        this.em = em;
        this.milestones = milestones;
    }

    @Override
    public PageResult<CampaignRecord> search(CampaignSearchFilter filter) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(filter, params);

        TypedQuery<CampaignRecord> query = em.createQuery(PROJECTION + where + orderBy(filter), CampaignRecord.class);
        TypedQuery<Long> count = em.createQuery("select count(c) from CampaignJpaEntity c" + where, Long.class);
        params.forEach((k, v) -> {
            query.setParameter(k, v);
            count.setParameter(k, v);
        });
        query.setFirstResult(filter.page() * filter.size());
        query.setMaxResults(filter.size());
        return PageResult.of(query.getResultList(), filter.page(), filter.size(), count.getSingleResult());
    }

    @Override
    public Optional<CampaignRecord> findById(UUID campaignId) {
        return em.createQuery(PROJECTION + " where c.id = :id", CampaignRecord.class)
                .setParameter("id", campaignId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<MilestoneView> findMilestones(UUID campaignId) {
        return milestones.findByCampaign_IdOrderByReachedAtAsc(campaignId).stream()
                .map(m -> new MilestoneView(m.type.name(), m.reachedAt, m.description))
                .toList();
    }

    private static String buildWhere(CampaignSearchFilter f, Map<String, Object> params) {
        StringBuilder where = new StringBuilder(" where 1 = 1");
        if (f.status() != null) {
            where.append(" and c.status = :status");
            params.put("status", f.status());
        }
        if (f.text() != null) {
            where.append(" and (lower(c.title) like :text or lower(c.description) like :text)");
            params.put("text", "%" + f.text().toLowerCase(Locale.ROOT) + "%");
        }
        if (f.category() != null) {
            where.append(" and c.category = :category");
            params.put("category", f.category());
        }
        if (f.minProgress() != null && f.minProgress() > 0) {
            where.append(" and c.raisedAmount * 100 >= c.goalAmount * :minProgress");
            params.put("minProgress", BigDecimal.valueOf(f.minProgress()));
        }
        if (f.deadlineFrom() != null) {
            where.append(" and c.deadline > :deadlineFrom");
            params.put("deadlineFrom", f.deadlineFrom());
        }
        if (f.deadlineTo() != null) {
            where.append(" and c.deadline <= :deadlineTo");
            params.put("deadlineTo", f.deadlineTo());
        }
        if (f.creatorId() != null) {
            where.append(" and c.creatorId = :creatorId");
            params.put("creatorId", f.creatorId());
        }
        return where.toString();
    }

    private static String orderBy(CampaignSearchFilter f) {
        return switch (f.sort()) {
            case DEADLINE_ASC -> " order by c.deadline asc, c.id";
            case NEWEST -> " order by c.createdAt desc, c.id";
            case PROGRESS_DESC -> " order by (c.raisedAmount / c.goalAmount) desc, c.id";
            case RAISED_DESC -> " order by c.raisedAmount desc, c.id";
        };
    }
}
