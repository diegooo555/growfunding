package com.g7.crowdfunding.shared.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g7.crowdfunding.campaign.domain.event.CampaignClosed;
import com.g7.crowdfunding.campaign.domain.event.ClosingSoon;
import com.g7.crowdfunding.campaign.domain.event.ContributionRegistered;
import com.g7.crowdfunding.campaign.domain.event.GoalReached;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Publishes the campaign domain events to Kafka (through the outbox relay) for other services.
 */
@Configuration
@ConditionalOnProperty(name = "crowdfunding.events.kafka.enabled", havingValue = "true")
public class KafkaEventsConfig {

    @Bean
    NewTopic domainEventsTopic(@Value("${crowdfunding.events.kafka.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    KafkaDomainEventForwarder<ContributionRegistered> contributionRegisteredForwarder(
            KafkaTemplate<String, String> kafka, ObjectMapper mapper, @Value("${crowdfunding.events.kafka.topic}") String topic) {
        return new KafkaDomainEventForwarder<>(ContributionRegistered.class, kafka, mapper, topic);
    }

    @Bean
    KafkaDomainEventForwarder<GoalReached> goalReachedForwarder(
            KafkaTemplate<String, String> kafka, ObjectMapper mapper, @Value("${crowdfunding.events.kafka.topic}") String topic) {
        return new KafkaDomainEventForwarder<>(GoalReached.class, kafka, mapper, topic);
    }

    @Bean
    KafkaDomainEventForwarder<ClosingSoon> closingSoonForwarder(
            KafkaTemplate<String, String> kafka, ObjectMapper mapper, @Value("${crowdfunding.events.kafka.topic}") String topic) {
        return new KafkaDomainEventForwarder<>(ClosingSoon.class, kafka, mapper, topic);
    }

    @Bean
    KafkaDomainEventForwarder<CampaignClosed> campaignClosedForwarder(
            KafkaTemplate<String, String> kafka, ObjectMapper mapper, @Value("${crowdfunding.events.kafka.topic}") String topic) {
        return new KafkaDomainEventForwarder<>(CampaignClosed.class, kafka, mapper, topic);
    }
}
