package com.g7.crowdfunding.notification.application.eventhandler;

import com.g7.crowdfunding.campaign.domain.event.ContributionRegistered;
import com.g7.crowdfunding.notification.domain.model.NotificationType;
import com.g7.crowdfunding.shared.application.event.DomainEventHandler;

import java.util.List;

/**
 * Tells the creator a new contribution arrived.
 */
public class ContributionRegisteredNotificationHandler implements DomainEventHandler<ContributionRegistered> {

    private final NotificationDispatcher dispatcher;

    public ContributionRegisteredNotificationHandler(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Class<ContributionRegistered> eventType() {
        return ContributionRegistered.class;
    }

    @Override
    public void handle(ContributionRegistered event) {
        if (event.creatorId().equals(event.contributorId())) {
            return;
        }
        dispatcher.notifyAll(List.of(event.creatorId()), NotificationType.NEW_CONTRIBUTION, event.campaignId(),
                "Nuevo aporte",
                "Tu campaña \"" + event.campaignTitle() + "\" recibió un aporte de "
                        + event.amount().toPlainString() + " " + event.currency()
                        + (event.anonymous() ? " (anónimo)." : "."),
                event);
    }
}
