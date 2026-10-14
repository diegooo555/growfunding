package com.g7.crowdfunding.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI crowdfundingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("G7 Crowdfunding API")
                .version("v1")
                .description("Send the acting user id in the X-User-Id header (see GET /api/users)."));
    }
}
