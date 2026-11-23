package com.g7.stats.application;

import com.g7.stats.domain.CampaignStats;
import com.g7.stats.domain.ProcessedEvent;
import com.g7.stats.infrastructure.persistence.CampaignStatsRepository;
import com.g7.stats.infrastructure.persistence.ProcessedEventRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatsProjectorTest {

    private static final UUID CAMPAIGN = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final Map<UUID, CampaignStats> rows = new HashMap<>();
    private final Set<UUID> seen = new HashSet<>();
    private StatsProjector projector;

    @BeforeEach
    void setUp() {
        CampaignStatsRepository stats = mock(CampaignStatsRepository.class);
        when(stats.findById(any())).thenAnswer(i -> Optional.ofNullable(rows.get(i.<UUID>getArgument(0))));
        when(stats.save(any())).thenAnswer(i -> {
            CampaignStats s = i.getArgument(0);
            rows.put(s.getCampaignId(), s);
            return s;
        });
        ProcessedEventRepository processed = mock(ProcessedEventRepository.class);
        when(processed.existsById(any())).thenAnswer(i -> seen.contains(i.<UUID>getArgument(0)));
        when(processed.save(any())).thenAnswer(i -> {
            ProcessedEvent e = i.getArgument(0);
            seen.add(e.getEventId());
            return e;
        });
        projector = new StatsProjector(stats, processed, new ObjectMapper());
    }

    private static String contribution(UUID eventId, String amount) {
        return "{\"eventId\":\"" + eventId + "\",\"occurredAt\":\"2026-11-22T10:00:00Z\",\"campaignId\":\"" + CAMPAIGN
                + "\",\"campaignTitle\":\"Huerta escolar\",\"amount\":" + amount + ",\"currency\":\"COP\"}";
    }

    @Test
    void contributionsAccumulateCountAndAmount() {
        projector.apply("ContributionRegistered", contribution(UUID.randomUUID(), "50000"));
        projector.apply("ContributionRegistered", contribution(UUID.randomUUID(), "25000.50"));

        assertThat(rows.get(CAMPAIGN).getContributions()).isEqualTo(2);
        assertThat(rows.get(CAMPAIGN).getRaised()).isEqualByComparingTo(new BigDecimal("75000.50"));
        assertThat(rows.get(CAMPAIGN).getTitle()).isEqualTo("Huerta escolar");
    }

    @Test
    void aRedeliveredEventIsAppliedOnlyOnce() {
        UUID eventId = UUID.randomUUID();

        boolean first = projector.apply("ContributionRegistered", contribution(eventId, "50000"));
        boolean again = projector.apply("ContributionRegistered", contribution(eventId, "50000"));

        assertThat(first).isTrue();
        assertThat(again).isFalse();
        assertThat(rows.get(CAMPAIGN).getContributions()).isEqualTo(1);
    }

    @Test
    void goalReachedAndClosedAreReflected() {
        projector.apply("GoalReached", "{\"eventId\":\"" + UUID.randomUUID() + "\",\"campaignId\":\"" + CAMPAIGN
                + "\",\"campaignTitle\":\"Huerta escolar\"}");
        projector.apply("CampaignClosed", "{\"eventId\":\"" + UUID.randomUUID() + "\",\"campaignId\":\"" + CAMPAIGN
                + "\",\"campaignTitle\":\"Huerta escolar\"}");

        assertThat(rows.get(CAMPAIGN).isGoalReached()).isTrue();
        assertThat(rows.get(CAMPAIGN).isClosed()).isTrue();
    }
}
