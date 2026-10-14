package com.g7.crowdfunding.campaign.infrastructure.scheduling;

import com.g7.crowdfunding.campaign.application.port.in.ProcessCampaignDeadlinesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * HU-06: periodically closes expired campaigns and emits ClosingSoon events.
 */
@Component
@ConditionalOnProperty(name = "crowdfunding.scheduling.enabled", havingValue = "true", matchIfMissing = true)
class CampaignDeadlineScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(CampaignDeadlineScheduler.class);

    private final ProcessCampaignDeadlinesUseCase deadlines;

    CampaignDeadlineScheduler(ProcessCampaignDeadlinesUseCase deadlines) {
        this.deadlines = deadlines;
    }

    @Scheduled(fixedDelayString = "${crowdfunding.scheduler.deadline-check-ms:60000}",
            initialDelayString = "${crowdfunding.scheduler.deadline-initial-delay-ms:5000}")
    void run() {
        int closed = deadlines.closeExpiredCampaigns();
        int flagged = deadlines.flagCampaignsClosingSoon();
        if (closed > 0 || flagged > 0) {
            LOG.info("Deadline check: {} campaign(s) closed, {} flagged as closing soon", closed, flagged);
        }
    }
}
