package com.postitapplications.security.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.postitapplications.exception.exceptions.ExternalServiceException;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.document.Authorisation;
import com.postitapplications.security.request.UserRequest;
import com.postitapplications.user.document.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityIT {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @MockBean
    private UserRequest userRequest;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtProperties jwtProperties;
    private User userToSave;
    private User savedUser;

    @BeforeEach
    public void setUp() {
        userToSave = new User(UUID.randomUUID(), "johnSmith123", "password");
        savedUser = new User(UUID.randomUUID(), "johnSmith123", passwordEncoder.encode("password"));
    }

    @Test
    public void registerUserShouldReturnSavedUserIdWithSuccessfulRegistration() {
        when(userRequest.saveUser(any(User.class))).thenReturn(savedUser);
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", userToSave, String.class);
        String userId = responseEntity.getBody();

        assertThat(userId).isEqualTo(savedUser.getId().toString());
    }

    @Test
    public void registerUserShouldReturnCreatedStatusCodeWithSuccessfulRegistration() {
        when(userRequest.saveUser(any(User.class))).thenReturn(savedUser);
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", userToSave, String.class);
        HttpStatus responseStatusCode = responseEntity.getStatusCode();

        assertThat(responseStatusCode).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void registerUserShouldReturnGivenStatusCodeWhenUserRequestThrowsAExternalServiceException() {
        when(userRequest.saveUser(any(User.class)))
            .thenThrow(new ExternalServiceException(HttpStatus.BAD_GATEWAY, "errorMessage"));
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", new User(null, "johnSmith123", "password"),
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserUsernameIsNull() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", new User(null, null, "password"), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserUsernameIsEmpty() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", new User(null, "", "password"), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserPasswordIsNull() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", new User(null, "johnSmith123", null), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserPasswordIsEmpty() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", new User(null, "johnSmith123", ""), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void registerUserShouldReturnBadRequestStatusCodeWhenUserIsNull() {
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/user", null, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getAuthoritiesShouldReturnExpectedAuthoritiesWithValidJwt() {
        when(userRequest.getUserByUsername("johnSmith123")).thenReturn(savedUser);
        ResponseEntity<String> logInResponseEntity = testRestTemplate
            .postForEntity("/auth", userToSave, String.class);
        String validJwt = logInResponseEntity.getHeaders().get(jwtProperties.getHeader()).get(0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(jwtProperties.getHeader(), validJwt);
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<List<Authorisation>> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {
            });
        List<Authorisation> authorities = responseEntity.getBody();

        assertThat(authorities.get(0).getAuthorisation()).isEqualTo("ROLE_" + savedUser.getId());
    }

    @Test
    public void getAuthoritiesShouldReturnUnAuthorisedStatusCodeWithInvalidJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<List<Authorisation>> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {
            });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
