package com.g7.crowdfunding.campaign.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class CampaignHasContributionsException extends DomainException {

    public CampaignHasContributionsException() {
        super("CAMPAIGN_HAS_CONTRIBUTIONS",
                "A campaign with contributions cannot be cancelled; close it instead");
    }
}
