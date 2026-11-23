package com.g7.crowdfunding.shared.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g7.crowdfunding.shared.application.event.DomainEventHandler;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Forwards a domain event to Kafka as one more handler of the outbox relay. If the broker is down
 * the handler fails, the relay rolls the dispatch back and retries with backoff: nothing is lost
 * (at-least-once delivery, consumers must be idempotent).
 */
public class KafkaDomainEventForwarder<E extends DomainEvent> implements DomainEventHandler<E> {

    static final String EVENT_TYPE_HEADER = "eventType";
    static final String CORRELATION_HEADER = "correlationId";

    private final Class<E> type;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaDomainEventForwarder(Class<E> type, KafkaTemplate<String, String> kafka, ObjectMapper objectMapper,
                                     String topic) {
        this.type = type;
        this.kafka = kafka;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public Class<E> eventType() {
        return type;
    }

    @Override
    public void handle(E event) {
        // Key = aggregate id: all events of a campaign land in the same partition, in order.
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.aggregateId().toString(), toJson(event));
        record.headers().add(EVENT_TYPE_HEADER, event.eventType().getBytes(StandardCharsets.UTF_8));
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            record.headers().add(CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        try {
            kafka.send(record).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Could not forward " + event.eventType() + " to Kafka", e);
        }
    }

    private String toJson(E event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event " + event.eventType(), e);
        }
    }
}
