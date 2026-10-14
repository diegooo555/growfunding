package com.g7.crowdfunding.campaign.infrastructure.adapter.in.web;

import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase;
import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase.CreateCampaignCommand;
import com.g7.crowdfunding.campaign.application.port.in.ManageCampaignLifecycleUseCase;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import com.g7.crowdfunding.shared.infrastructure.web.UserHeader;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * Command side of the campaign API (HU-01, HU-02, manual close, HU-09).
 */
@RestController
@RequestMapping("/api/campaigns")
class CampaignCommandController {

    private final CreateCampaignUseCase createCampaign;
    private final ManageCampaignLifecycleUseCase lifecycle;

    CampaignCommandController(CreateCampaignUseCase createCampaign, ManageCampaignLifecycleUseCase lifecycle) {
        this.createCampaign = createCampaign;
        this.lifecycle = lifecycle;
    }

    @PostMapping
    ResponseEntity<CampaignCreatedResponse> create(@RequestHeader(UserHeader.NAME) UUID userId,
                                                   @Valid @RequestBody CreateCampaignRequest request) {
        UUID id = createCampaign.create(new CreateCampaignCommand(userId, request.title(), request.description(),
                request.category(), request.goalAmount(), request.currency(), request.deadline(),
                request.contributorsVisibility() == null ? ContributorsVisibility.PUBLIC
                        : request.contributorsVisibility()));
        return ResponseEntity.created(URI.create("/api/campaigns/" + id))
                .body(new CampaignCreatedResponse(id, CampaignStatus.DRAFT.name()));
    }

    @PostMapping("/{id}/publish")
    ResponseEntity<Void> publish(@PathVariable UUID id, @RequestHeader(UserHeader.NAME) UUID userId) {
        lifecycle.publish(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/close")
    ResponseEntity<Void> close(@PathVariable UUID id, @RequestHeader(UserHeader.NAME) UUID userId) {
        lifecycle.closeManually(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    ResponseEntity<Void> cancel(@PathVariable UUID id, @RequestHeader(UserHeader.NAME) UUID userId) {
        lifecycle.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }
}
