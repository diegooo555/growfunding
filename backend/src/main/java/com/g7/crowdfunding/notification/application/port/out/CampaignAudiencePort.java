package com.g7.crowdfunding.notification.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface CampaignAudiencePort {

    Set<UUID> contributorIdsOf(UUID campaignId);
}
