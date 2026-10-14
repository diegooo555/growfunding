package com.g7.crowdfunding.campaign.infrastructure.config;

import com.g7.crowdfunding.campaign.application.command.CampaignDeadlineService;
import com.g7.crowdfunding.campaign.application.command.CampaignLifecycleService;
import com.g7.crowdfunding.campaign.application.command.CreateCampaignService;
import com.g7.crowdfunding.campaign.application.port.out.CampaignReadModelPort;
import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.application.query.CampaignQueryService;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.user.application.UserDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Wires the framework-free application services of the campaign module.
 */
@Configuration
public class CampaignModuleConfig {

    private final Duration closingSoonWindow;

    public CampaignModuleConfig(@Value("${crowdfunding.campaign.closing-soon-window:24h}") Duration closingSoonWindow) {
        this.closingSoonWindow = closingSoonWindow;
    }

    @Bean
    CreateCampaignService createCampaignService(CampaignRepository campaigns, UserDirectory users,
                                                TransactionRunner tx, ClockPort clock) {
        return new CreateCampaignService(campaigns, users, tx, clock);
    }

    @Bean
    CampaignLifecycleService campaignLifecycleService(CampaignRepository campaigns, DomainEventOutbox outbox,
                                                      UserDirectory users, TransactionRunner tx, ClockPort clock) {
        return new CampaignLifecycleService(campaigns, outbox, users, tx, clock);
    }

    @Bean
    CampaignDeadlineService campaignDeadlineService(CampaignRepository campaigns, DomainEventOutbox outbox,
                                                    TransactionRunner tx, ClockPort clock) {
        return new CampaignDeadlineService(campaigns, outbox, tx, clock, closingSoonWindow);
    }

    @Bean
    CampaignQueryService campaignQueryService(CampaignReadModelPort readModel, UserDirectory users,
                                              ClockPort clock) {
        return new CampaignQueryService(readModel, users, clock, closingSoonWindow);
    }
}
