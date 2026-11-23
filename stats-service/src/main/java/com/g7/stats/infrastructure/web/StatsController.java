package com.g7.stats.infrastructure.web;

import com.g7.stats.domain.CampaignStats;
import com.g7.stats.infrastructure.persistence.CampaignStatsRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Query side only: nothing can be modified through this service. */
@RestController
@RequestMapping("/api/stats/campaigns")
public class StatsController {

    private final CampaignStatsRepository stats;

    public StatsController(CampaignStatsRepository stats) {
        this.stats = stats;
    }

    @GetMapping
    public List<CampaignStats> top() {
        return stats.findTop10ByOrderByRaisedDesc();
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<CampaignStats> byCampaign(@PathVariable UUID campaignId) {
        return stats.findById(campaignId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
