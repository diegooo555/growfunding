package com.g7.crowdfunding.campaign.application.query;

import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;

/**
 * HU-03 search parameters.
 *
 * @param status      null means any status (admin only)
 * @param minProgress minimum funding progress in percent (0-100+), nullable
 * @param closingSoon only campaigns whose deadline falls inside the closing-soon window
 */
public record CampaignSearchCriteria(String text, CampaignStatus status, Category category, Integer minProgress,
                                     boolean closingSoon, int page, int size, CampaignSort sort) {

    public static final int MAX_PAGE_SIZE = 100;

    public CampaignSearchCriteria {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (minProgress != null && minProgress < 0) {
            throw new IllegalArgumentException("minProgress must be >= 0");
        }
        text = text == null || text.isBlank() ? null : text.strip();
        sort = sort == null ? CampaignSort.DEADLINE_ASC : sort;
    }
}
