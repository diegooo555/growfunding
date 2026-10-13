package com.g7.crowdfunding.campaign.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class InvalidCampaignStateException extends DomainException {

    public InvalidCampaignStateException(String message) {
        super("INVALID_CAMPAIGN_STATE", message);
    }
}
