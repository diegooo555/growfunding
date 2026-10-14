package com.g7.crowdfunding.shared.application.port.out;

import com.g7.crowdfunding.shared.domain.event.DomainEvent;

import java.util.Collection;

/**
 * Transactional outbox: events are stored in the same transaction as the aggregate
 * change and dispatched later by a relay, guaranteeing at-least-once delivery.
 */
public interface DomainEventOutbox {

    void append(Collection<? extends DomainEvent> events);
}
