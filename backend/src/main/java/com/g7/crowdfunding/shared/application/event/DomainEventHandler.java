package com.g7.crowdfunding.shared.application.event;

import com.g7.crowdfunding.shared.domain.event.DomainEvent;

/**
 * Unattended reaction to a domain event, invoked by the outbox relay.
 */
public interface DomainEventHandler<E extends DomainEvent> {

    Class<E> eventType();

    void handle(E event);
}
