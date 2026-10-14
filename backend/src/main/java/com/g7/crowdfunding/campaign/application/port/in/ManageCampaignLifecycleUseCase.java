package com.g7.crowdfunding.campaign.application.port.in;

import java.util.UUID;

/**
 * Creator-driven lifecycle transitions (HU-02, manual close, HU-09).
 */
public interface ManageCampaignLifecycleUseCase {

    void publish(UUID campaignId, UUID requesterId);

    void closeManually(UUID campaignId, UUID requesterId);

    void cancel(UUID campaignId, UUID requesterId);
}
