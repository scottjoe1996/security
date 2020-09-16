package com.postitapplications.security.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JwtAuthenticationFilterBeanConfig {

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }
}
