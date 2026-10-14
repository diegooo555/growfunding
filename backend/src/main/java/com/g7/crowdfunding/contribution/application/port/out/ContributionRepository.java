package com.g7.crowdfunding.contribution.application.port.out;

import com.g7.crowdfunding.contribution.domain.model.Contribution;

import java.util.Optional;
import java.util.UUID;

public interface ContributionRepository {

    Optional<Contribution> findByIdempotencyKey(UUID idempotencyKey);

    boolean existsByCampaignIdAndContributorId(UUID campaignId, UUID contributorId);

    void save(Contribution contribution);
}
