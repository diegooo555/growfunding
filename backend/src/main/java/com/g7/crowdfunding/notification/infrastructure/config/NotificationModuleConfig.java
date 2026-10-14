package com.g7.crowdfunding.notification.infrastructure.config;

import com.g7.crowdfunding.notification.application.command.MarkNotificationsReadService;
import com.g7.crowdfunding.notification.application.eventhandler.CampaignClosedNotificationHandler;
import com.g7.crowdfunding.notification.application.eventhandler.ClosingSoonNotificationHandler;
import com.g7.crowdfunding.notification.application.eventhandler.ContributionRegisteredNotificationHandler;
import com.g7.crowdfunding.notification.application.eventhandler.GoalReachedNotificationHandler;
import com.g7.crowdfunding.notification.application.eventhandler.NotificationDispatcher;
import com.g7.crowdfunding.notification.application.port.out.CampaignAudiencePort;
import com.g7.crowdfunding.notification.application.port.out.NotificationPort;
import com.g7.crowdfunding.notification.application.port.out.NotificationRepository;
import com.g7.crowdfunding.notification.application.query.NotificationQueryService;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.user.application.UserDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationModuleConfig {

    @Bean
    NotificationDispatcher notificationDispatcher(NotificationRepository repository, NotificationPort channel,
                                                  ClockPort clock) {
        return new NotificationDispatcher(repository, channel, clock);
    }

    @Bean
    GoalReachedNotificationHandler goalReachedNotificationHandler(CampaignAudiencePort audience,
                                                                  NotificationDispatcher dispatcher) {
        return new GoalReachedNotificationHandler(audience, dispatcher);
    }

    @Bean
    ClosingSoonNotificationHandler closingSoonNotificationHandler(CampaignAudiencePort audience,
                                                                  NotificationDispatcher dispatcher) {
        return new ClosingSoonNotificationHandler(audience, dispatcher);
    }

    @Bean
    CampaignClosedNotificationHandler campaignClosedNotificationHandler(CampaignAudiencePort audience,
                                                                        NotificationDispatcher dispatcher) {
        return new CampaignClosedNotificationHandler(audience, dispatcher);
    }

    @Bean
    ContributionRegisteredNotificationHandler contributionRegisteredNotificationHandler(
            NotificationDispatcher dispatcher) {
        return new ContributionRegisteredNotificationHandler(dispatcher);
    }

    @Bean
    NotificationQueryService notificationQueryService(NotificationRepository repository, UserDirectory users) {
        return new NotificationQueryService(repository, users);
    }

    @Bean
    MarkNotificationsReadService markNotificationsReadService(NotificationRepository repository,
                                                              UserDirectory users, TransactionRunner tx) {
        return new MarkNotificationsReadService(repository, users, tx);
    }
}
