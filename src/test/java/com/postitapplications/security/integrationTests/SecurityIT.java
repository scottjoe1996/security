package com.postitapplications.security.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.security.configuration.UserRequestBeanConfig;
import com.postitapplications.security.request.UserRequest;
import com.postitapplications.user.document.User;
import java.util.UUID;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@RestClientTest(UserRequest.class)
@Import(UserRequestBeanConfig.class)
public class SecurityIT {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    private User userToSave;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        userToSave = new User(UUID.randomUUID(), "johnSmith123",
            passwordEncoder.encode("password"));
        String userAsString = objectMapper.writeValueAsString(userToSave);
        mockServer.expect(requestTo("http://user-service/user")).andExpect(method(HttpMethod.POST))
                  .andRespond(withSuccess(userAsString, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://user-service/user/username/johnSmith123"))
                  .andRespond(withSuccess(userAsString, MediaType.APPLICATION_JSON));
    }

    @Test
    public void registerUserShouldReturnExpectedAuthorisationResponseWithSuccessfulRegistration() {
        ResponseEntity<User> responseEntity = restTemplate
            .postForEntity("/security/register", userToSave, User.class);
        User userRegistered = responseEntity.getBody();

        assertThat(userRegistered.getUsername()).isEqualTo("johnSmith123");
    }
}
