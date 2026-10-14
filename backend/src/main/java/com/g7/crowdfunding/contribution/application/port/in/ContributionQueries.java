package com.g7.crowdfunding.contribution.application.port.in;

import com.g7.crowdfunding.contribution.application.query.CampaignContributionView;
import com.g7.crowdfunding.contribution.application.query.ContributionHistoryView;

import java.util.List;
import java.util.UUID;

public interface ContributionQueries {

    /**
     * HU-07: contribution history of the requesting contributor.
     */
    List<ContributionHistoryView> historyOf(UUID contributorId);

    /**
     * HU-08: contributors of a campaign, only when its visibility allows it.
     */
    List<CampaignContributionView> contributorsOf(UUID campaignId, UUID requesterId);
}
