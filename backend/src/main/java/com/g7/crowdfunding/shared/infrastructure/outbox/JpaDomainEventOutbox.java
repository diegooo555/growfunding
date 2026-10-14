package com.g7.crowdfunding.shared.infrastructure.outbox;

import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Writes events to the outbox table inside the caller's transaction.
 */
@Component
public class JpaDomainEventOutbox implements DomainEventOutbox {

    private final OutboxEventJpaRepository repository;
    private final DomainEventSerializer serializer;
    private final ApplicationEventPublisher publisher;
    private final ClockPort clock;

    public JpaDomainEventOutbox(OutboxEventJpaRepository repository, DomainEventSerializer serializer,
                                ApplicationEventPublisher publisher, ClockPort clock) {
        this.repository = repository;
        this.serializer = serializer;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    public void append(Collection<? extends DomainEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        events.forEach(event -> repository.save(new OutboxEventJpaEntity(event.eventId(), event.eventType(),
                event.aggregateId(), serializer.serialize(event), event.occurredAt(), clock.now())));
        publisher.publishEvent(new OutboxEventsAppended(events.size()));
    }
}
