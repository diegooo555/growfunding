package com.g7.crowdfunding.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
public class AsyncSchedulingConfig {

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "crowdfunding.scheduling.enabled", havingValue = "true", matchIfMissing = true)
    static class SchedulingEnabled {
    }
}
