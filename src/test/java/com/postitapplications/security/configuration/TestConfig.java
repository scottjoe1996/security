package com.postitapplications.security.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public ExternalServiceProperties externalServiceProperties() {
        ExternalServiceProperties externalServiceProperties = new ExternalServiceProperties();
        externalServiceProperties.setUserUrl("http://user-service/user/");
        return externalServiceProperties;
    }
}
