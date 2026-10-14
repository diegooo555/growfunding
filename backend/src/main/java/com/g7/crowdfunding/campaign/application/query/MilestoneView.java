package com.g7.crowdfunding.campaign.application.query;

import java.time.Instant;

public record MilestoneView(String type, Instant reachedAt, String description) {
}
