package com.g7.crowdfunding.campaign.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class InvalidCampaignException extends DomainException {

    public InvalidCampaignException(String message) {
        super("INVALID_CAMPAIGN", message);
    }
}
