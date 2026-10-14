package com.g7.crowdfunding.notification.application.eventhandler;

import com.g7.crowdfunding.campaign.domain.event.ClosingSoon;
import com.g7.crowdfunding.notification.application.port.out.CampaignAudiencePort;
import com.g7.crowdfunding.notification.domain.model.NotificationType;
import com.g7.crowdfunding.shared.application.event.DomainEventHandler;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class ClosingSoonNotificationHandler implements DomainEventHandler<ClosingSoon> {

    private final CampaignAudiencePort audience;
    private final NotificationDispatcher dispatcher;

    public ClosingSoonNotificationHandler(CampaignAudiencePort audience, NotificationDispatcher dispatcher) {
        this.audience = audience;
        this.dispatcher = dispatcher;
    }

    @Override
    public Class<ClosingSoon> eventType() {
        return ClosingSoon.class;
    }

    @Override
    public void handle(ClosingSoon event) {
        Set<UUID> recipients = new LinkedHashSet<>(audience.contributorIdsOf(event.campaignId()));
        recipients.add(event.creatorId());
        dispatcher.notifyAll(recipients, NotificationType.CLOSING_SOON, event.campaignId(),
                "La campaña cierra pronto",
                "La campaña \"" + event.campaignTitle() + "\" cierra el " + event.deadline() + ".",
                event);
    }
}
