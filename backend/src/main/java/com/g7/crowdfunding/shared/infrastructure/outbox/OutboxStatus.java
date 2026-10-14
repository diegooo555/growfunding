package com.g7.crowdfunding.shared.infrastructure.outbox;

public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
