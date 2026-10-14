package com.g7.crowdfunding.campaign.application.query;

import java.util.Locale;

public enum CampaignSort {
    DEADLINE_ASC,
    NEWEST,
    PROGRESS_DESC,
    RAISED_DESC;

    /**
     * Parses values such as {@code deadline,asc}, {@code createdAt,desc} or enum names.
     */
    public static CampaignSort parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEADLINE_ASC;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "deadline", "deadline,asc", "deadline_asc" -> DEADLINE_ASC;
            case "newest", "createdat,desc", "created_at,desc", "publishedat,desc" -> NEWEST;
            case "progress", "progress,desc", "progress_desc" -> PROGRESS_DESC;
            case "raised", "raised,desc", "raisedamount,desc", "raised_desc" -> RAISED_DESC;
            default -> throw new IllegalArgumentException("Unsupported sort: " + raw);
        };
    }
}
