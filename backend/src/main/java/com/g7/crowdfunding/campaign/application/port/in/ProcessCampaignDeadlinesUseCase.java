package com.g7.crowdfunding.campaign.application.port.in;

/**
 * Unattended processing triggered by the scheduler (HU-06).
 */
public interface ProcessCampaignDeadlinesUseCase {

    /**
     * @return number of campaigns closed because their deadline passed
     */
    int closeExpiredCampaigns();

    /**
     * @return number of campaigns for which a ClosingSoon event was emitted
     */
    int flagCampaignsClosingSoon();
}
