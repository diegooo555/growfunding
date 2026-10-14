package com.g7.crowdfunding.notification.application.eventhandler;

import com.g7.crowdfunding.campaign.domain.event.CampaignClosed;
import com.g7.crowdfunding.notification.application.port.out.CampaignAudiencePort;
import com.g7.crowdfunding.notification.domain.model.NotificationType;
import com.g7.crowdfunding.shared.application.event.DomainEventHandler;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class CampaignClosedNotificationHandler implements DomainEventHandler<CampaignClosed> {

    private final CampaignAudiencePort audience;
    private final NotificationDispatcher dispatcher;

    public CampaignClosedNotificationHandler(CampaignAudiencePort audience, NotificationDispatcher dispatcher) {
        this.audience = audience;
        this.dispatcher = dispatcher;
    }

    @Override
    public Class<CampaignClosed> eventType() {
        return CampaignClosed.class;
    }

    @Override
    public void handle(CampaignClosed event) {
        Set<UUID> recipients = new LinkedHashSet<>(audience.contributorIdsOf(event.campaignId()));
        recipients.add(event.creatorId());
        String reason = switch (event.reason()) {
            case DEADLINE -> "alcanzó su fecha límite";
            case MANUAL -> "fue cerrada por su creador";
            case SUSPENDED -> "fue suspendida por la administración";
        };
        String outcome = event.goalReached() ? " La meta fue alcanzada." : " La meta no fue alcanzada.";
        dispatcher.notifyAll(recipients, NotificationType.CAMPAIGN_CLOSED, event.campaignId(),
                "Campaña cerrada",
                "La campaña \"" + event.campaignTitle() + "\" " + reason + ". Total recaudado: "
                        + event.raisedAmount().toPlainString() + " " + event.currency() + "." + outcome,
                event);
    }
}
