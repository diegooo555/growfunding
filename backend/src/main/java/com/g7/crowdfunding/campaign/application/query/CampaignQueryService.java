package com.g7.crowdfunding.campaign.application.query;

import com.g7.crowdfunding.campaign.application.port.in.CampaignQueries;
import com.g7.crowdfunding.campaign.application.port.out.CampaignReadModelPort;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.shared.application.exception.AccessDeniedException;
import com.g7.crowdfunding.shared.application.exception.ResourceNotFoundException;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.query.PageResult;
import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.domain.model.Role;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CampaignQueryService implements CampaignQueries {

    private static final int SUMMARY_LENGTH = 180;
    private static final int CREATOR_LIST_LIMIT = 100;

    private final CampaignReadModelPort readModel;
    private final UserDirectory userDirectory;
    private final ClockPort clock;
    private final Duration closingSoonWindow;

    public CampaignQueryService(CampaignReadModelPort readModel, UserDirectory userDirectory, ClockPort clock,
                                Duration closingSoonWindow) {
        this.readModel = readModel;
        this.userDirectory = userDirectory;
        this.clock = clock;
        this.closingSoonWindow = closingSoonWindow;
    }

    @Override
    public PageResult<CampaignSummaryView> search(CampaignSearchCriteria criteria) {
        CampaignStatus status = criteria.status() == null ? CampaignStatus.ACTIVE : criteria.status();
        if (status == CampaignStatus.DRAFT) {
            throw new AccessDeniedException("Draft campaigns are not publicly visible");
        }
        return runSearch(criteria, status, null);
    }

    @Override
    public CampaignDetailView getDetail(UUID campaignId) {
        CampaignRecord record = readModel.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignId));
        return new CampaignDetailView(toSummary(record, clock.now()), record.description(),
                readModel.findMilestones(campaignId));
    }

    @Override
    public List<CampaignSummaryView> listByCreator(UUID creatorId) {
        userDirectory.require(creatorId);
        Instant now = clock.now();
        CampaignSearchFilter filter = new CampaignSearchFilter(null, null, null, null, null, null, creatorId,
                0, CREATOR_LIST_LIMIT, CampaignSort.NEWEST);
        return readModel.search(filter).content().stream().map(r -> toSummary(r, now)).toList();
    }

    @Override
    public PageResult<CampaignSummaryView> listAllForAdmin(UUID adminId, CampaignSearchCriteria criteria) {
        userDirectory.requireRole(adminId, Role.ADMIN);
        return runSearch(criteria, criteria.status(), null);
    }

    private PageResult<CampaignSummaryView> runSearch(CampaignSearchCriteria criteria, CampaignStatus status,
                                                      UUID creatorId) {
        Instant now = clock.now();
        Instant deadlineFrom = criteria.closingSoon() ? now : null;
        Instant deadlineTo = criteria.closingSoon() ? now.plus(closingSoonWindow) : null;
        CampaignSearchFilter filter = new CampaignSearchFilter(criteria.text(), status, criteria.category(),
                criteria.minProgress(), deadlineFrom, deadlineTo, creatorId, criteria.page(), criteria.size(),
                criteria.sort());
        return readModel.search(filter).map(r -> toSummary(r, now));
    }

    private CampaignSummaryView toSummary(CampaignRecord r, Instant now) {
        boolean active = r.status() == CampaignStatus.ACTIVE;
        boolean beforeDeadline = now.isBefore(r.deadline());
        boolean closingSoon = active && beforeDeadline
                && Duration.between(now, r.deadline()).compareTo(closingSoonWindow) <= 0;
        boolean cancellable = (r.status() == CampaignStatus.DRAFT || active) && r.contributionsCount() == 0;
        return new CampaignSummaryView(r.id(), r.title(), summarize(r.description()), r.category().name(),
                r.creatorId(), r.creatorName(), r.goalAmount(), r.raisedAmount(), r.currency(),
                progressPercent(r.raisedAmount(), r.goalAmount()), r.contributionsCount(), r.contributorsCount(),
                r.deadline(), r.status().name(), r.contributorsVisibility().name(), r.goalReachedAt() != null,
                closingSoon, active && beforeDeadline, cancellable, r.createdAt(), r.publishedAt(), r.closedAt());
    }

    static int progressPercent(BigDecimal raised, BigDecimal goal) {
        if (goal == null || goal.signum() <= 0) {
            return 0;
        }
        return raised.multiply(BigDecimal.valueOf(100)).divide(goal, 0, RoundingMode.FLOOR).intValue();
    }

    private static String summarize(String description) {
        if (description == null || description.length() <= SUMMARY_LENGTH) {
            return description;
        }
        return description.substring(0, SUMMARY_LENGTH).stripTrailing() + "…";
    }
}
