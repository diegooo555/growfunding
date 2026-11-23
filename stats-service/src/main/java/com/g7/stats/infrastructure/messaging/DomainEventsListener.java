package com.g7.stats.infrastructure.messaging;

import com.g7.stats.application.StatsProjector;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Inbound adapter: receives the domain events published by the crowdfunding core and keeps the
 * correlation id of the originating request in the MDC while projecting them.
 */
@Component
public class DomainEventsListener {

    private final StatsProjector projector;

    public DomainEventsListener(StatsProjector projector) {
        this.projector = projector;
    }

    @KafkaListener(topics = "${stats.topic}")
    public void onEvent(ConsumerRecord<String, String> record) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", header(record, "correlationId"))) {
            projector.apply(header(record, "eventType"), record.value());
        }
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? "" : new String(header.value(), StandardCharsets.UTF_8);
    }
}
