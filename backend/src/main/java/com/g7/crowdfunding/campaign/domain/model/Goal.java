package com.g7.crowdfunding.campaign.domain.model;

import com.g7.crowdfunding.campaign.domain.exception.InvalidCampaignException;
import com.g7.crowdfunding.shared.domain.model.Money;

/**
 * Funding target of a campaign. Always strictly positive.
 */
public record Goal(Money amount) {

    public Goal {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidCampaignException("Goal amount must be greater than zero");
        }
    }

    public String currency() {
        return amount.currency();
    }

    public boolean isReachedBy(Money raised) {
        return raised.isGreaterThanOrEqual(amount);
    }
}
