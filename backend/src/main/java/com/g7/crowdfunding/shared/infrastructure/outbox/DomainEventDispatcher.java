package com.g7.crowdfunding.shared.infrastructure.outbox;

import com.g7.crowdfunding.shared.application.event.DomainEventHandler;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Routes a domain event to every handler registered for its type.
 */
@Component
public class DomainEventDispatcher {

    private final List<DomainEventHandler<?>> handlers;

    public DomainEventDispatcher(List<DomainEventHandler<?>> handlers) {
        this.handlers = handlers;
    }

    public void dispatch(DomainEvent event) {
        for (DomainEventHandler<?> handler : handlers) {
            invoke(handler, event);
        }
    }

    private static <E extends DomainEvent> void invoke(DomainEventHandler<E> handler, DomainEvent event) {
        if (handler.eventType().isInstance(event)) {
            handler.handle(handler.eventType().cast(event));
        }
    }
}
