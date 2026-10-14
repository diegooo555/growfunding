package com.g7.crowdfunding.notification.application.eventhandler;

import com.g7.crowdfunding.campaign.domain.event.GoalReached;
import com.g7.crowdfunding.notification.application.port.out.CampaignAudiencePort;
import com.g7.crowdfunding.notification.domain.model.NotificationType;
import com.g7.crowdfunding.shared.application.event.DomainEventHandler;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * HU-05: every contributor (and the creator) is told the goal was reached.
 */
public class GoalReachedNotificationHandler implements DomainEventHandler<GoalReached> {

    private final CampaignAudiencePort audience;
    private final NotificationDispatcher dispatcher;

    public GoalReachedNotificationHandler(CampaignAudiencePort audience, NotificationDispatcher dispatcher) {
        this.audience = audience;
        this.dispatcher = dispatcher;
    }

    @Override
    public Class<GoalReached> eventType() {
        return GoalReached.class;
    }

    @Override
    public void handle(GoalReached event) {
        Set<UUID> recipients = new LinkedHashSet<>(audience.contributorIdsOf(event.campaignId()));
        recipients.add(event.creatorId());
        dispatcher.notifyAll(recipients, NotificationType.GOAL_REACHED, event.campaignId(),
                "¡Meta alcanzada!",
                "La campaña \"" + event.campaignTitle() + "\" alcanzó su meta de "
                        + event.goalAmount().toPlainString() + " " + event.currency() + ". ¡Gracias por tu apoyo!",
                event);
    }
}
