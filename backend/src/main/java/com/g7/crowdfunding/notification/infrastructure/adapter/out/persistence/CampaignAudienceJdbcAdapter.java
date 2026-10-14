package com.g7.crowdfunding.notification.infrastructure.adapter.out.persistence;

import com.g7.crowdfunding.notification.application.port.out.CampaignAudiencePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Reads the audience of a campaign with plain SQL so the notification module does not depend
 * on other modules' persistence classes.
 */
@Component
class CampaignAudienceJdbcAdapter implements CampaignAudiencePort {

    private final JdbcTemplate jdbc;

    CampaignAudienceJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Set<UUID> contributorIdsOf(UUID campaignId) {
        return new LinkedHashSet<>(jdbc.queryForList(
                "select distinct contributor_id from contributions where campaign_id = ?", UUID.class, campaignId));
    }
}
