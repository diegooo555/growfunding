package com.g7.crowdfunding.contribution.infrastructure.config;

import com.g7.crowdfunding.campaign.application.port.out.CampaignReadModelPort;
import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.contribution.application.command.ContributeToCampaignService;
import com.g7.crowdfunding.contribution.application.port.out.ContributionReadModelPort;
import com.g7.crowdfunding.contribution.application.port.out.ContributionRepository;
import com.g7.crowdfunding.contribution.application.query.ContributionQueryService;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.application.port.out.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContributionModuleConfig {

    @Bean
    ContributeToCampaignService contributeToCampaignService(CampaignRepository campaigns,
                                                            ContributionRepository contributions,
                                                            DomainEventOutbox outbox, UserDirectory users,
                                                            TransactionRunner tx, ClockPort clock,
                                                            @Value("${crowdfunding.contribution.max-attempts:5}")
                                                            int maxAttempts) {
        return new ContributeToCampaignService(campaigns, contributions, outbox, users, tx, clock, maxAttempts);
    }

    @Bean
    ContributionQueryService contributionQueryService(ContributionReadModelPort readModel,
                                                      CampaignReadModelPort campaignReadModel,
                                                      UserDirectory userDirectory, UserRepository users) {
        return new ContributionQueryService(readModel, campaignReadModel, userDirectory, users);
    }
}
