package com.g7.crowdfunding.campaign.application.port.in;

import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface CreateCampaignUseCase {

    UUID create(CreateCampaignCommand command);

    record CreateCampaignCommand(UUID creatorId, String title, String description, Category category,
                                 BigDecimal goalAmount, String currency, Instant deadline,
                                 ContributorsVisibility contributorsVisibility) {
    }
}
