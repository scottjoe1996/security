package com.postitapplications.security.request;

import com.postitapplications.security.configuration.ExternalServiceProperties;
import com.postitapplications.user.document.User;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserRequest {

    private final RestTemplate restTemplate;
    private final ExternalServiceProperties externalServiceProperties;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserRequest(RestTemplateBuilder restTemplateBuilder,
        ExternalServiceProperties externalServiceProperties, BCryptPasswordEncoder passwordEncoder) {
        this.restTemplate = restTemplateBuilder.build();
        this.externalServiceProperties = externalServiceProperties;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) throws RestClientException {
//        ResponseEntity<User> response = restTemplate
//            .getForEntity(externalServiceProperties.getUserUrl() + "/username/" + username,
//                User.class);
//
//        return response.getBody();

        if (username.equals("johnSmith123")) {
            return new User(UUID.randomUUID(), "johnSmith123", passwordEncoder.encode("password"));
        }

        return null;
    }
}
