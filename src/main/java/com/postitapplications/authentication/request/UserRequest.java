package com.postitapplications.authentication.request;

import com.postitapplications.authentication.configuration.ExternalServiceProperties;
import com.postitapplications.user.document.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
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
        this.restTemplate = restTemplateBuilder.build();
        this.externalServiceProperties = externalServiceProperties;
    }

    public User getUserByUsername(String username) {
        ResponseEntity<User> responseEntity = restTemplate
            .getForEntity(externalServiceProperties.getUserUrl() + "/username/" + username,
                User.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }

        return null;
    }
}
