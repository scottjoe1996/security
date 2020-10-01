package com.postitapplications.security.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.postitapplications.security.request.UserRequest;
import com.postitapplications.user.document.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityIT {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @MockBean
    private UserRequest userRequest;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    private User savedUser;

    @BeforeEach
    public void setUp() {
        savedUser = new User(UUID.randomUUID(), "johnSmith123", "password");
    }

    @Test
    public void registerUserShouldReturnSavedUserIdWithSuccessfulRegistration() {
        when(userRequest.saveUser(any(User.class))).thenReturn(savedUser);
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", savedUser, String.class);
        String userId = responseEntity.getBody();

        assertThat(userId).isEqualTo(savedUser.getId().toString());
    }

    @Test
    public void registerUserShouldReturnCreatedStatusCodeWithSuccessfulRegistration() {
        when(userRequest.saveUser(any(User.class))).thenReturn(savedUser);
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", savedUser, String.class);
        HttpStatus responseStatusCode = responseEntity.getStatusCode();

        assertThat(responseStatusCode).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void registerUserShouldReturnGivenStatusCodeWhenUserRequestThrowsAHttpClientErrorException() {
        when(userRequest.saveUser(any(User.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", null, String.class);
        HttpStatus responseStatusCode = responseEntity.getStatusCode();

        assertThat(responseStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
