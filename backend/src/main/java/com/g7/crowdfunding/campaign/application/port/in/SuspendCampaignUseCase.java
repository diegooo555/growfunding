package com.g7.crowdfunding.campaign.application.port.in;

import java.util.UUID;

public interface SuspendCampaignUseCase {

    void suspend(UUID campaignId, UUID adminId);
}
