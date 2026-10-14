package com.g7.crowdfunding.campaign.infrastructure.adapter.in.web;

import java.util.UUID;

public record CampaignCreatedResponse(UUID id, String status) {
}
