package com.g7.crowdfunding.campaign.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class CampaignNotAcceptingContributionsException extends DomainException {

    public CampaignNotAcceptingContributionsException(String message) {
        super("CAMPAIGN_NOT_ACCEPTING_CONTRIBUTIONS", message);
    }
}
