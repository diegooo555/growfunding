package com.g7.crowdfunding.user.infrastructure.config;

import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.application.port.out.UserRepository;
import com.g7.crowdfunding.user.application.query.UserQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModuleConfig {

    @Bean
    UserDirectory userDirectory(UserRepository users) {
        return new UserDirectory(users);
    }

    @Bean
    UserQueryService userQueryService(UserRepository users) {
        return new UserQueryService(users);
    }
}
