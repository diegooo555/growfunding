package com.g7.crowdfunding.campaign.infrastructure.adapter.in.web;

import com.g7.crowdfunding.campaign.application.port.in.CampaignQueries;
import com.g7.crowdfunding.campaign.application.port.in.SuspendCampaignUseCase;
import com.g7.crowdfunding.campaign.application.query.CampaignSearchCriteria;
import com.g7.crowdfunding.campaign.application.query.CampaignSort;
import com.g7.crowdfunding.campaign.application.query.CampaignSummaryView;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.shared.application.query.PageResult;
import com.g7.crowdfunding.shared.infrastructure.web.UserHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Platform admin endpoints. Every call requires a user with the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/campaigns")
class AdminCampaignController {

    private final CampaignQueries queries;
    private final SuspendCampaignUseCase suspendCampaign;

    AdminCampaignController(CampaignQueries queries, SuspendCampaignUseCase suspendCampaign) {
        this.queries = queries;
        this.suspendCampaign = suspendCampaign;
    }

    @GetMapping
    PageResult<CampaignSummaryView> listAll(@RequestHeader(UserHeader.NAME) UUID adminId,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) CampaignStatus status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return queries.listAllForAdmin(adminId, new CampaignSearchCriteria(q, status, null, null, false, page, size,
                CampaignSort.NEWEST));
    }

    @PostMapping("/{id}/suspend")
    ResponseEntity<Void> suspend(@PathVariable UUID id, @RequestHeader(UserHeader.NAME) UUID adminId) {
        suspendCampaign.suspend(id, adminId);
        return ResponseEntity.noContent().build();
    }
}
