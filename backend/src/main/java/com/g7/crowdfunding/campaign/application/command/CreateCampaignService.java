package com.g7.crowdfunding.campaign.application.command;

import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase;
import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.shared.domain.model.Money;
import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.domain.model.Role;

import java.util.UUID;

public class CreateCampaignService implements CreateCampaignUseCase {

    private final CampaignRepository campaigns;
    private final UserDirectory userDirectory;
    private final TransactionRunner tx;
    private final ClockPort clock;

    public CreateCampaignService(CampaignRepository campaigns, UserDirectory userDirectory,
                                 TransactionRunner tx, ClockPort clock) {
        this.campaigns = campaigns;
        this.userDirectory = userDirectory;
        this.tx = tx;
        this.clock = clock;
    }

    @Override
    public UUID create(CreateCampaignCommand command) {
        userDirectory.requireRole(command.creatorId(), Role.CREATOR);
        String currency = command.currency() == null || command.currency().isBlank()
                ? Money.DEFAULT_CURRENCY : command.currency().toUpperCase();
        Campaign campaign = Campaign.create(command.creatorId(), command.title(), command.description(),
                command.category(), Money.of(command.goalAmount(), currency), command.deadline(),
                command.contributorsVisibility(), clock.now());
        return tx.inTransaction(() -> {
            campaigns.save(campaign);
            return campaign.id();
        });
    }
}
