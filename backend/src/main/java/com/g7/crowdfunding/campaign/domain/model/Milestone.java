package com.g7.crowdfunding.campaign.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A relevant moment in the life of a campaign ("hito").
 */
public record Milestone(UUID id, MilestoneType type, Instant reachedAt, String description) {

    public Milestone {
        Objects.requireNonNull(id);
        Objects.requireNonNull(type);
        Objects.requireNonNull(reachedAt);
    }

    public static Milestone of(MilestoneType type, Instant reachedAt, String description) {
        return new Milestone(UUID.randomUUID(), type, reachedAt, description);
    }
}
