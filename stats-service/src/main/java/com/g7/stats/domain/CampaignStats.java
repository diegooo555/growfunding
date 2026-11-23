package com.g7.stats.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read model: one row per campaign, built only from domain events. It is not the source of truth
 * and can be rebuilt by replaying the topic with a new consumer group.
 */
@Entity
@Table(name = "campaign_stats")
public class CampaignStats {

    @Id
    private UUID campaignId;
    private String title;
    private long contributions;
    private BigDecimal raised = BigDecimal.ZERO;
    private String currency;
    private boolean goalReached;
    private boolean closed;
    private Instant lastEventAt;

    protected CampaignStats() {
    }

    public static CampaignStats of(UUID campaignId) {
        CampaignStats stats = new CampaignStats();
        stats.campaignId = campaignId;
        return stats;
    }

    public void registerContribution(String title, BigDecimal amount, String currency, Instant at) {
        this.title = title;
        this.contributions++;
        this.raised = raised.add(amount);
        this.currency = currency;
        touch(at);
    }

    public void markGoalReached(String title, Instant at) {
        this.title = title;
        this.goalReached = true;
        touch(at);
    }

    public void markClosed(String title, Instant at) {
        this.title = title;
        this.closed = true;
        touch(at);
    }

    private void touch(Instant at) {
        if (lastEventAt == null || (at != null && at.isAfter(lastEventAt))) {
            lastEventAt = at;
        }
    }

    public UUID getCampaignId() { return campaignId; }
    public String getTitle() { return title; }
    public long getContributions() { return contributions; }
    public BigDecimal getRaised() { return raised; }
    public String getCurrency() { return currency; }
    public boolean isGoalReached() { return goalReached; }
    public boolean isClosed() { return closed; }
    public Instant getLastEventAt() { return lastEventAt; }
}
