package com.g7.crowdfunding.campaign.infrastructure.adapter.in.web;

import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Syntactic validation only; business rules (goal > 0, deadline in the future) live in the domain.
 */
public record CreateCampaignRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 4000) String description,
        Category category,
        @NotNull BigDecimal goalAmount,
        @Size(min = 3, max = 3) String currency,
        @NotNull Instant deadline,
        ContributorsVisibility contributorsVisibility) {
}
