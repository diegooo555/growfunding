package com.g7.crowdfunding.shared.infrastructure.outbox;

import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Dispatches pending outbox events to their handlers. Each event is handled in its own
 * transaction together with its PROCESSED mark, so handler side effects (in-app notifications)
 * and the outbox state commit atomically. Failures are logged, counted and retried with
 * exponential backoff until {@code max-attempts}, after which the event is marked FAILED.
 */
@Component
public class OutboxRelay {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventJpaRepository repository;
    private final DomainEventSerializer serializer;
    private final DomainEventDispatcher dispatcher;
    private final ClockPort clock;
    private final TransactionTemplate tx;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxRelay(OutboxEventJpaRepository repository, DomainEventSerializer serializer,
                       DomainEventDispatcher dispatcher, ClockPort clock,
                       PlatformTransactionManager transactionManager,
                       @Value("${crowdfunding.outbox.batch-size:50}") int batchSize,
                       @Value("${crowdfunding.outbox.max-attempts:10}") int maxAttempts) {
        this.repository = repository;
        this.serializer = serializer;
        this.dispatcher = dispatcher;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    /**
     * @return number of events successfully dispatched in this run
     */
    public synchronized int dispatchPending() {
        List<UUID> ids = tx.execute(status -> repository.findDispatchableIds(clock.now(),
                PageRequest.of(0, batchSize)));
        int dispatched = 0;
        for (UUID id : ids == null ? List.<UUID>of() : ids) {
            if (dispatchOne(id)) {
                dispatched++;
            }
        }
        return dispatched;
    }

    private boolean dispatchOne(UUID id) {
        try {
            Boolean done = tx.execute(status -> {
                OutboxEventJpaEntity entry = repository.findById(id).orElse(null);
                if (entry == null || entry.getStatus() != OutboxStatus.PENDING) {
                    return false;
                }
                DomainEvent event = serializer.deserialize(entry.getEventType(), entry.getPayload());
                dispatcher.dispatch(event);
                entry.markProcessed(clock.now());
                return true;
            });
            return Boolean.TRUE.equals(done);
        } catch (RuntimeException e) {
            LOG.warn("Outbox event {} could not be dispatched, it will be retried: {}", id, e.toString());
            recordFailure(id, e);
            return false;
        }
    }

    private void recordFailure(UUID id, RuntimeException cause) {
        try {
            tx.executeWithoutResult(status -> repository.findById(id).ifPresent(entry -> {
                entry.registerFailure(cause.toString(), clock.now(), maxAttempts);
                if (entry.getStatus() == OutboxStatus.FAILED) {
                    LOG.error("Outbox event {} marked FAILED after {} attempts", id, entry.getAttempts());
                }
            }));
        } catch (RuntimeException e) {
            LOG.error("Could not record failure for outbox event {}", id, e);
        }
    }
}
