package com.g7.crowdfunding.campaign.application.command;

import com.g7.crowdfunding.campaign.application.port.in.ManageCampaignLifecycleUseCase;
import com.g7.crowdfunding.campaign.application.port.in.SuspendCampaignUseCase;
import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.shared.application.exception.ResourceNotFoundException;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.domain.model.Role;

import java.util.UUID;
import java.util.function.Consumer;

public class CampaignLifecycleService implements ManageCampaignLifecycleUseCase, SuspendCampaignUseCase {

    private final CampaignRepository campaigns;
    private final DomainEventOutbox outbox;
    private final UserDirectory userDirectory;
    private final TransactionRunner tx;
    private final ClockPort clock;

    public CampaignLifecycleService(CampaignRepository campaigns, DomainEventOutbox outbox,
                                    UserDirectory userDirectory, TransactionRunner tx, ClockPort clock) {
        this.campaigns = campaigns;
        this.outbox = outbox;
        this.userDirectory = userDirectory;
        this.tx = tx;
        this.clock = clock;
    }

    @Override
    public void publish(UUID campaignId, UUID requesterId) {
        userDirectory.require(requesterId);
        change(campaignId, campaign -> campaign.publish(requesterId, clock.now()));
    }

    @Override
    public void closeManually(UUID campaignId, UUID requesterId) {
        userDirectory.require(requesterId);
        change(campaignId, campaign -> campaign.closeManually(requesterId, clock.now()));
    }

    @Override
    public void cancel(UUID campaignId, UUID requesterId) {
        userDirectory.require(requesterId);
        change(campaignId, campaign -> campaign.cancel(requesterId, clock.now()));
    }

    @Override
    public void suspend(UUID campaignId, UUID adminId) {
        userDirectory.requireRole(adminId, Role.ADMIN);
        change(campaignId, campaign -> campaign.suspend(clock.now()));
    }

    private void change(UUID campaignId, Consumer<Campaign> action) {
        tx.inTransaction(() -> {
            Campaign campaign = campaigns.findById(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignId));
            action.accept(campaign);
            campaigns.save(campaign);
            outbox.append(campaign.pullDomainEvents());
        });
    }
}
