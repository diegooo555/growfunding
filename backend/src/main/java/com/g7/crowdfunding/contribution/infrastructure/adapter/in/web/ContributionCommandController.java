package com.g7.crowdfunding.contribution.infrastructure.adapter.in.web;

import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase.ContributeCommand;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase.ContributionReceipt;
import com.g7.crowdfunding.shared.infrastructure.web.UserHeader;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class ContributionCommandController {

    private final ContributeToCampaignUseCase contribute;

    ContributionCommandController(ContributeToCampaignUseCase contribute) {
        this.contribute = contribute;
    }

    /**
     * HU-04. Returns 201 for a new contribution and 200 when replaying an idempotency key.
     */
    @PostMapping("/api/campaigns/{campaignId}/contributions")
    ResponseEntity<ContributionReceipt> contribute(@PathVariable UUID campaignId,
                                                   @RequestHeader(UserHeader.NAME) UUID userId,
                                                   @Valid @RequestBody ContributeRequest request) {
        ContributionReceipt receipt = contribute.contribute(new ContributeCommand(campaignId, userId,
                request.amount(), request.currency(), request.anonymous(), request.message(),
                request.idempotencyKey()));
        return ResponseEntity.status(receipt.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(receipt);
    }
}
