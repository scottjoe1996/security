package com.postitapplications.security.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SecurityControllerConfig {

    @Bean
    @Primary
    public ExternalServiceProperties externalServiceProperties() {
        ExternalServiceProperties externalServiceProperties = new ExternalServiceProperties();
        externalServiceProperties.setUserUrl("http://user-service/test/");
        return externalServiceProperties;
    }
}
