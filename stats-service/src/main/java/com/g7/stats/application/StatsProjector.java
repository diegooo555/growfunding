package com.g7.stats.application;

import com.g7.stats.domain.CampaignStats;
import com.g7.stats.domain.ProcessedEvent;
import com.g7.stats.infrastructure.persistence.CampaignStatsRepository;
import com.g7.stats.infrastructure.persistence.ProcessedEventRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Translates each domain event into a change of the read model. Kafka delivers at least once, so
 * every event is applied only the first time its eventId is seen (idempotent consumer); the mark
 * and the change commit in the same transaction.
 */
@Service
public class StatsProjector {

    private static final Logger LOG = LoggerFactory.getLogger(StatsProjector.class);

    private final CampaignStatsRepository stats;
    private final ProcessedEventRepository processed;
    private final ObjectMapper objectMapper;

    public StatsProjector(CampaignStatsRepository stats, ProcessedEventRepository processed, ObjectMapper objectMapper) {
        this.stats = stats;
        this.processed = processed;
        this.objectMapper = objectMapper;
    }

    /** @return true if the event changed the read model, false if it was a duplicate or irrelevant */
    @Transactional
    public boolean apply(String eventType, String payload) {
        JsonNode event = readTree(payload);
        UUID eventId = UUID.fromString(event.get("eventId").asText());
        if (processed.existsById(eventId)) {
            LOG.info("Stats: event {} already processed, ignored", eventId);
            return false;
        }
        UUID campaignId = UUID.fromString(event.get("campaignId").asText());
        String title = event.path("campaignTitle").asText(null);
        Instant at = event.hasNonNull("occurredAt") ? Instant.parse(event.get("occurredAt").asText()) : null;
        CampaignStats campaign = stats.findById(campaignId).orElseGet(() -> CampaignStats.of(campaignId));
        switch (eventType) {
            case "ContributionRegistered" -> campaign.registerContribution(title,
                    event.get("amount").decimalValue(), event.path("currency").asText(null), at);
            case "GoalReached" -> campaign.markGoalReached(title, at);
            case "CampaignClosed" -> campaign.markClosed(title, at);
            default -> {
                // ClosingSoon and future event types do not change the statistics.
                processed.save(new ProcessedEvent(eventId, Instant.now()));
                return false;
            }
        }
        stats.save(campaign);
        processed.save(new ProcessedEvent(eventId, Instant.now()));
        LOG.info("Stats: {} applied to campaign {} (contributions={}, raised={})", eventType, campaignId,
                campaign.getContributions(), campaign.getRaised());
        return true;
    }

    private JsonNode readTree(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unreadable event payload", e);
        }
    }
}
