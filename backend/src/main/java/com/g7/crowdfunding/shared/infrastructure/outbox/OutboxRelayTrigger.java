package com.g7.crowdfunding.shared.infrastructure.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Low-latency path: once the business transaction commits, dispatch asynchronously. The
 * scheduled relay remains the safety net if this attempt fails or the process dies.
 */
@Component
@ConditionalOnProperty(name = "crowdfunding.outbox.dispatch-after-commit", havingValue = "true", matchIfMissing = true)
public class OutboxRelayTrigger {

    private final OutboxRelay relay;

    public OutboxRelayTrigger(OutboxRelay relay) {
        this.relay = relay;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventsAppended(OutboxEventsAppended signal) {
        relay.dispatchPending();
    }
}
