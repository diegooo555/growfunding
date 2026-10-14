package com.g7.crowdfunding.contribution.infrastructure.adapter.in.web;

import com.g7.crowdfunding.contribution.application.port.in.ContributionQueries;
import com.g7.crowdfunding.contribution.application.query.CampaignContributionView;
import com.g7.crowdfunding.contribution.application.query.ContributionHistoryView;
import com.g7.crowdfunding.shared.infrastructure.web.UserHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
class ContributionQueryController {

    private final ContributionQueries queries;

    ContributionQueryController(ContributionQueries queries) {
        this.queries = queries;
    }

    /**
     * HU-08: 403 when the campaign keeps its contributors private.
     */
    @GetMapping("/api/campaigns/{campaignId}/contributors")
    List<CampaignContributionView> contributors(@PathVariable UUID campaignId,
                                                @RequestHeader(value = UserHeader.NAME, required = false) UUID userId) {
        return queries.contributorsOf(campaignId, userId);
    }

    /**
     * HU-07: contribution history of the current user.
     */
    @GetMapping("/api/contributions/me")
    List<ContributionHistoryView> mine(@RequestHeader(UserHeader.NAME) UUID userId) {
        return queries.historyOf(userId);
    }
}
