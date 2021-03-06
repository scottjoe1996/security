package com.postitapplications.security.request;

import com.postitapplications.exception.exceptions.ExternalServiceException;
import com.postitapplications.security.configuration.ExternalServiceProperties;
import com.postitapplications.security.exception.RestTemplateResponseErrorHandler;
import com.postitapplications.user.document.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserRequest {

    private final RestTemplate restTemplate;
    private final ExternalServiceProperties externalServiceProperties;

    @Autowired
    public UserRequest(RestTemplateBuilder restTemplateBuilder,
        ExternalServiceProperties externalServiceProperties) {
        this.restTemplate = restTemplateBuilder
                                .errorHandler(new RestTemplateResponseErrorHandler())
                                .build();
        this.externalServiceProperties = externalServiceProperties;
    }

    public User getUserByUsername(String username) {
        ResponseEntity<User> response = restTemplate
            .getForEntity(externalServiceProperties.getUserUrl() + "/username/" + username,
                User.class);

        return response.getBody();
    }

    public User saveUser(User user) {
        String userServiceUrl = externalServiceProperties.getUserUrl();
        ResponseEntity<User> response = restTemplate
            .postForEntity(userServiceUrl, user, User.class);

        return response.getBody();
    }
}
