package com.g7.crowdfunding.campaign.infrastructure.adapter.in.web;

import com.g7.crowdfunding.campaign.application.port.in.CampaignQueries;
import com.g7.crowdfunding.campaign.application.query.CampaignDetailView;
import com.g7.crowdfunding.campaign.application.query.CampaignSearchCriteria;
import com.g7.crowdfunding.campaign.application.query.CampaignSort;
import com.g7.crowdfunding.campaign.application.query.CampaignSummaryView;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.shared.application.query.PageResult;
import com.g7.crowdfunding.shared.infrastructure.web.UserHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Query side of the campaign API (HU-03). Backed only by the read model.
 */
@RestController
@RequestMapping("/api/campaigns")
class CampaignQueryController {

    private final CampaignQueries queries;

    CampaignQueryController(CampaignQueries queries) {
        this.queries = queries;
    }

    @GetMapping
    PageResult<CampaignSummaryView> search(@RequestParam(required = false) String q,
                                           @RequestParam(defaultValue = "ACTIVE") CampaignStatus status,
                                           @RequestParam(required = false) Category category,
                                           @RequestParam(required = false) Integer minProgress,
                                           @RequestParam(defaultValue = "false") boolean closingSoon,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "12") int size,
                                           @RequestParam(required = false) String sort) {
        return queries.search(new CampaignSearchCriteria(q, status, category, minProgress, closingSoon, page, size,
                CampaignSort.parse(sort)));
    }

    @GetMapping("/mine")
    List<CampaignSummaryView> mine(@RequestHeader(UserHeader.NAME) UUID userId) {
        return queries.listByCreator(userId);
    }

    @GetMapping("/{id}")
    CampaignDetailView detail(@PathVariable UUID id) {
        return queries.getDetail(id);
    }
}
