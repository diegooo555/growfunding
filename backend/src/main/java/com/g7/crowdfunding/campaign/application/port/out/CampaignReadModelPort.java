package com.g7.crowdfunding.campaign.application.port.out;

import com.g7.crowdfunding.campaign.application.query.CampaignRecord;
import com.g7.crowdfunding.campaign.application.query.CampaignSearchFilter;
import com.g7.crowdfunding.campaign.application.query.MilestoneView;
import com.g7.crowdfunding.shared.application.query.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Query-side port backed by projection queries, independent from the write repository.
 */
public interface CampaignReadModelPort {

    PageResult<CampaignRecord> search(CampaignSearchFilter filter);

    Optional<CampaignRecord> findById(UUID campaignId);

    List<MilestoneView> findMilestones(UUID campaignId);
}
