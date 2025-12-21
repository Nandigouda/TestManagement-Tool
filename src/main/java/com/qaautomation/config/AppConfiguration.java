package com.qaautomation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for application beans.
 */
@Slf4j
@Configuration
public class AppConfiguration {

    /**
     * Configures RestClient for making HTTP requests.
     */
    @Bean
    public RestClient restClient() {
        log.debug("Initializing RestClient bean");
        return RestClient.create();
    }

    /**
     * Configures ObjectMapper for JSON serialization/deserialization.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        log.debug("Initializing ObjectMapper bean");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        log.debug("Registered JavaTimeModule for LocalDateTime serialization");
        return mapper;
    }
}
