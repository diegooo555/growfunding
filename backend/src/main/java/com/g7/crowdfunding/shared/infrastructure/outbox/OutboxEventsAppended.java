package com.g7.crowdfunding.shared.infrastructure.outbox;

/**
 * In-process signal published when events were written to the outbox; used to trigger the
 * relay right after commit instead of waiting for the next scheduled poll.
 */
public record OutboxEventsAppended(int count) {
}
