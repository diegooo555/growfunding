package com.g7.crowdfunding.campaign.application.query;

import java.util.List;

public record CampaignDetailView(CampaignSummaryView summary, String description, List<MilestoneView> milestones) {
}
