package com.postitapplications.security.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.postitapplications.security.SecurityApplication;
import com.postitapplications.user.document.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityIT {

    @Autowired
    private TestRestTemplate testRestTemplate;
//    @MockBean
//    private RestTemplateBuilder restTemplateBuilder;
//    @MockBean
//    private UserRequest userRequest;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    private User userToSave;

    @BeforeEach
    public void setUp() {
        userToSave = new User(null, "johnSmith123",
            passwordEncoder.encode("password"));
//        RestTemplate mockRestTemplate = mock(RestTemplate.class);
//        when(mockRestTemplate.postForEntity("http://user-service/user/", userToSave,
//            User.class)).thenReturn(new ResponseEntity<>(userToSave, HttpStatus.CREATED));
//        when(restTemplateBuilder.build()).thenReturn(new RestTemplate());
//        when(userRequest.saveUser(any(User.class))).thenReturn(userToSave);
    }

    @Test
    public void registerUserShouldReturnExpectedAuthorisationResponseWithSuccessfulRegistration() {
        ResponseEntity<User> responseEntity = testRestTemplate
            .postForEntity("/security/register", userToSave, User.class);
        User userRegistered = responseEntity.getBody();

        assertThat(userRegistered.getUsername()).isEqualTo("johnSmith123");
    }
}
