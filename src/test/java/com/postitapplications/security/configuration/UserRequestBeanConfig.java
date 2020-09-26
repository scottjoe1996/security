package com.postitapplications.security.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@TestConfiguration
public class UserRequestBeanConfig {

    @Bean
    public ExternalServiceProperties externalServiceProperties() {
        ExternalServiceProperties externalServiceProperties = new ExternalServiceProperties();
        externalServiceProperties.setUserUrl("http://user-service/user/");
        return externalServiceProperties;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
