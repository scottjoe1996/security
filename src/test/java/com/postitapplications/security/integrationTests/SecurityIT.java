package com.postitapplications.security.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.postitapplications.exception.exceptions.ExternalServiceException;
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
    public void registerUserShouldReturnGivenStatusCodeWhenUserRequestThrowsAExternalServiceException() {
        when(userRequest.saveUser(any(User.class)))
            .thenThrow(new ExternalServiceException(HttpStatus.BAD_GATEWAY, "errorMessage"));
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", new User(null, "johnSmith123", "password"),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserUsernameIsNull() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", new User(null, null, "password"), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserUsernameIsEmpty() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", new User(null, "", "password"), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserPasswordIsNull() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", new User(null, "johnSmith123", null),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserPasswordIsEmpty() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", new User(null, "johnSmith123", ""), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserIsNull() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/register", null, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
