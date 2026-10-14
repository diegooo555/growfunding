package com.g7.crowdfunding.campaign.application.query;

import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;

import java.time.Instant;
import java.util.UUID;

/**
 * Resolved filter handed to the read adapter (relative concepts already turned into instants).
 */
public record CampaignSearchFilter(String text, CampaignStatus status, Category category, Integer minProgress,
                                   Instant deadlineFrom, Instant deadlineTo, UUID creatorId,
                                   int page, int size, CampaignSort sort) {
}
