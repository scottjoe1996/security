package com.postitapplications.authentication.request;

import com.postitapplications.user.document.User;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserRequest {

    private final RestTemplate restTemplate;
    private final Environment environment;

    public UserRequest(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public User getUserByUsername(String username) {
        ResponseEntity<User> responseEntity = restTemplate
            .getForEntity(environment.getProperty("userUrl") + "/username/" + username, User.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }

        return null;
    }
}
