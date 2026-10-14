package com.g7.crowdfunding.contribution.application.query;

import com.g7.crowdfunding.campaign.application.port.out.CampaignReadModelPort;
import com.g7.crowdfunding.campaign.application.query.CampaignRecord;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import com.g7.crowdfunding.contribution.application.exception.ContributorsNotVisibleException;
import com.g7.crowdfunding.contribution.application.port.in.ContributionQueries;
import com.g7.crowdfunding.contribution.application.port.out.ContributionReadModelPort;
import com.g7.crowdfunding.shared.application.exception.ResourceNotFoundException;
import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.application.port.out.UserRepository;
import com.g7.crowdfunding.user.domain.model.User;

import java.util.List;
import java.util.UUID;

public class ContributionQueryService implements ContributionQueries {

    private final ContributionReadModelPort readModel;
    private final CampaignReadModelPort campaignReadModel;
    private final UserDirectory userDirectory;
    private final UserRepository users;

    public ContributionQueryService(ContributionReadModelPort readModel, CampaignReadModelPort campaignReadModel,
                                    UserDirectory userDirectory, UserRepository users) {
        this.readModel = readModel;
        this.campaignReadModel = campaignReadModel;
        this.userDirectory = userDirectory;
        this.users = users;
    }

    @Override
    public List<ContributionHistoryView> historyOf(UUID contributorId) {
        userDirectory.require(contributorId);
        return readModel.findByContributor(contributorId);
    }

    @Override
    public List<CampaignContributionView> contributorsOf(UUID campaignId, UUID requesterId) {
        CampaignRecord campaign = campaignReadModel.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignId));
        if (campaign.contributorsVisibility() != ContributorsVisibility.PUBLIC && !isPrivileged(campaign, requesterId)) {
            throw new ContributorsNotVisibleException();
        }
        return readModel.findByCampaign(campaignId).stream().map(CampaignContributionView::masked).toList();
    }

    private boolean isPrivileged(CampaignRecord campaign, UUID requesterId) {
        if (requesterId == null) {
            return false;
        }
        if (campaign.creatorId().equals(requesterId)) {
            return true;
        }
        return users.findById(requesterId).map(User::isAdmin).orElse(false);
    }
}
