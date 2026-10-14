package com.g7.crowdfunding.contribution.application.port.out;

import com.g7.crowdfunding.contribution.application.query.CampaignContributionView;
import com.g7.crowdfunding.contribution.application.query.ContributionHistoryView;

import java.util.List;
import java.util.UUID;

public interface ContributionReadModelPort {

    List<ContributionHistoryView> findByContributor(UUID contributorId);

    List<CampaignContributionView> findByCampaign(UUID campaignId);
}
