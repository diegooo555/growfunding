package com.g7.crowdfunding.campaign.domain.model;

public enum CampaignStatus {
    DRAFT,
    ACTIVE,
    CLOSED_BY_DEADLINE,
    CLOSED_MANUALLY,
    CANCELLED,
    SUSPENDED;

    public boolean isTerminal() {
        return this == CLOSED_BY_DEADLINE || this == CLOSED_MANUALLY || this == CANCELLED || this == SUSPENDED;
    }
}
