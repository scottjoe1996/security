package com.postitapplications.security.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.user.document.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(SpringExtension.class)
@RestClientTest(UserRequest.class)
public class UserRequestTests {

    @Autowired
    private UserRequest userRequest;
    @Autowired
    private MockRestServiceServer mockServer;
    private String userAsString;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        userAsString = objectMapper
            .writeValueAsString(new User(UUID.randomUUID(), "johnSmith123", "password"));
    }

    @Test
    public void getUserByUsernameShouldReturnUserOnSuccessfulResponse() {
        mockServer.expect(requestTo("http://localhost:1040/user/username/johnSmith123"))
                  .andRespond(withSuccess(userAsString, MediaType.APPLICATION_JSON));

        User response = userRequest.getUserByUsername("johnSmith123");

        assertThat(response.getUsername()).isEqualTo("johnSmith123");
        assertThat(response.getPassword()).isEqualTo("password");
    }

    @Test
    public void getUserByUsernameShouldThrowHttpClientErrorExceptionOnAllNonSuccessfulResponses() {
        mockServer.expect(requestTo("http://localhost:1040/user/username/fakeUsername"))
                  .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> {
            userRequest.getUserByUsername("fakeUsername");
        });
    }

    @Test
    public void saveUserShouldReturnUserOnSuccessfulResponse() {
        mockServer.expect(requestTo("http://localhost:1040/user/"))
                  .andRespond(withSuccess(userAsString, MediaType.APPLICATION_JSON));

        User response = userRequest
            .saveUser(new User(UUID.randomUUID(), "johnSmith123", "password"));

        assertThat(response.getUsername()).isEqualTo("johnSmith123");
        assertThat(response.getPassword()).isEqualTo("password");
    }

    @Test
    public void saveUserShouldThrowHttpClientErrorExceptionOnAllNonSuccessfulResponses() {
        mockServer.expect(requestTo("http://localhost:1040/user/"))
                  .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> {
            userRequest.saveUser(null);
        });
    }
}
