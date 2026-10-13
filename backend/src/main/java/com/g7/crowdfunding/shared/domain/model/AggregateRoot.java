package com.g7.crowdfunding.shared.domain.model;

import com.g7.crowdfunding.shared.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for aggregates that collect domain events. The application layer pulls
 * them after a successful state change and writes them to the transactional outbox.
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<DomainEvent> peekDomainEvents() {
        return List.copyOf(domainEvents);
    }
}
