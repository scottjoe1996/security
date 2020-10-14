package com.postitapplications.security.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.postitapplications.exception.ExceptionResponseBody;
import com.postitapplications.exception.exceptions.ExternalServiceException;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.document.Authorisation;
import com.postitapplications.security.request.UserRequest;
import com.postitapplications.security.utility.JwtProvider;
import com.postitapplications.user.document.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collections;
import java.util.Date;
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
            .postForEntity("/security/login", userToSave, String.class);
        String validJwt = logInResponseEntity.getHeaders().get(jwtProperties.getHeader()).get(0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(jwtProperties.getHeader(), validJwt);
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<List<Authorisation>> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<>() {
                });
        List<Authorisation> authorisations = responseEntity.getBody();

        assertThat(authorisations.get(0).getAuthorisation()).isEqualTo("ROLE_" + savedUser.getId());
    }

    @Test
    public void getAuthoritiesShouldReturnForbiddenStatusCodeWithInvalidJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(jwtProperties.getHeader(), "Bearer invalidJwt");
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody())
            .contains("JWT strings must contain exactly 2 period characters");
    }

    @Test
    public void getAuthoritiesShouldReturnForbiddenStatusCodeWithEmptyJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(jwtProperties.getHeader(), "Bearer ");
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getAuthoritiesShouldReturnForbiddenStatusCodeWithOutDatedJwt() {
        String expiredToken = Jwts.builder().setSubject("johnSmith123")
                                  .claim("authorities", Collections.emptyList())
                                  .setIssuedAt(new Date()).setExpiration(new Date())
                                  .signWith(SignatureAlgorithm.HS512,
                                      jwtProperties.getSecret().getBytes()).compact();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(jwtProperties.getHeader(), "Bearer " + expiredToken);
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody()).contains("JWT expired at");
    }

    @Test
    public void getAuthoritiesShouldReturnForbiddenStatusCodeWithWrongSigningKey() {
        String signingKey = "wrongSigningKey";
        String expiredToken = Jwts.builder().setSubject("johnSmith123")
                                  .claim("authorities", Collections.emptyList())
                                  .setIssuedAt(new Date()).setExpiration(new Date())
                                  .signWith(SignatureAlgorithm.HS512, signingKey.getBytes())
                                  .compact();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(jwtProperties.getHeader(), "Bearer " + expiredToken);
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> responseEntity = testRestTemplate
            .exchange("/security/authorisation", HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody()).contains(
            "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }

    @Test
    public void loginEndpointShouldReturnOKStatusCodeWhenUsernameAndPasswordMatch() {
        when(userRequest.getUserByUsername("johnSmith123")).thenReturn(savedUser);
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/login", userToSave, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void loginEndpointShouldAddExpectedTokenToResponseHeader() {
        JwtProvider jwtProvider = new JwtProvider(jwtProperties);
        when(userRequest.getUserByUsername("johnSmith123")).thenReturn(savedUser);
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/login", userToSave, String.class);
        String header = responseEntity.getHeaders().get(jwtProperties.getHeader()).get(0);
        String token = header.replace(jwtProperties.getPrefix(), "");

        String username = jwtProvider.getUsernameFromToken(token);
        List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthoritiesFromToken(token);

        assertThat(username).isEqualTo("johnSmith123");
        assertThat(authorities.get(0).getAuthority()).isEqualTo("ROLE_" + savedUser.getId());
    }

    @Test
    public void loginEndpointShouldReturnUnAuthorisedStatusCodeWhenUsernameHasNoMatch() {
        when(userRequest.getUserByUsername("johnSmith123"))
            .thenThrow(new ExternalServiceException(HttpStatus.NOT_FOUND, "User was not found"));
        ResponseEntity<String> responseEntity = testRestTemplate
            .postForEntity("/security/login", userToSave, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).contains("Bad credentials");
    }

    @Test
    public void loginEndpointShouldReturnBadRequestStatusCodeWhenRequestBodyIsNotAUserObject() {
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("/security/login",
            new ExceptionResponseBody(HttpStatus.NOT_FOUND, "test"), String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
