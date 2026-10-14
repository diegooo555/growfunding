package com.g7.crowdfunding.campaign.application.port.in;

import com.g7.crowdfunding.campaign.application.query.CampaignDetailView;
import com.g7.crowdfunding.campaign.application.query.CampaignSearchCriteria;
import com.g7.crowdfunding.campaign.application.query.CampaignSummaryView;
import com.g7.crowdfunding.shared.application.query.PageResult;

import java.util.List;
import java.util.UUID;

/**
 * Query-side entry point (CQRS read model). Never touches write repositories.
 */
public interface CampaignQueries {

    PageResult<CampaignSummaryView> search(CampaignSearchCriteria criteria);

    CampaignDetailView getDetail(UUID campaignId);

    List<CampaignSummaryView> listByCreator(UUID creatorId);

    PageResult<CampaignSummaryView> listAllForAdmin(UUID adminId, CampaignSearchCriteria criteria);
}
