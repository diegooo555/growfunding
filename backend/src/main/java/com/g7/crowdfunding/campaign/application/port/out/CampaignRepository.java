package com.g7.crowdfunding.campaign.application.port.out;

import com.g7.crowdfunding.campaign.domain.model.Campaign;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Write-side port for the Campaign aggregate. {@link #save} must enforce optimistic locking
 * on the aggregate version.
 */
public interface CampaignRepository {

    Optional<Campaign> findById(UUID id);

    void save(Campaign campaign);

    List<UUID> findActiveIdsWithDeadlineAtOrBefore(Instant instant);

    List<UUID> findActiveIdsPendingClosingSoonNotice(Instant deadlineUpTo);
}
