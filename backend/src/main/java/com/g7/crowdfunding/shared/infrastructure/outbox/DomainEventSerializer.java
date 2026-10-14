package com.g7.crowdfunding.shared.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g7.crowdfunding.shared.application.event.DomainEventHandler;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON (de)serialization of domain events. Event types are resolved from the registered handlers.
 */
@Component
public class DomainEventSerializer {

    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends DomainEvent>> types = new ConcurrentHashMap<>();

    public DomainEventSerializer(ObjectMapper objectMapper, List<DomainEventHandler<?>> handlers) {
        this.objectMapper = objectMapper;
        handlers.forEach(handler -> register(handler.eventType()));
    }

    public final void register(Class<? extends DomainEvent> type) {
        types.put(type.getSimpleName(), type);
    }

    public String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event " + event.eventType(), e);
        }
    }

    public DomainEvent deserialize(String eventType, String payload) {
        Class<? extends DomainEvent> type = types.get(eventType);
        if (type == null) {
            throw new IllegalStateException("Unknown event type: " + eventType);
        }
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot deserialize event " + eventType, e);
        }
    }
}
