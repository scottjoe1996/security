package com.postitapplications.security.configuration;

import com.postitapplications.security.utility.JwtProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JwtBeanConfig {

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    public JwtProvider jwtProvider() {
        return new JwtProvider(jwtProperties());
    }
}
