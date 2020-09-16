package com.postitapplications.security.Filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.security.configuration.JwtAuthenticationFilterBeanConfig;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.user.document.User;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(JwtAuthenticationFilterBeanConfig.class)
public class JwtAuthenticationFilterTests {

    @Autowired
    private JwtProperties jwtProperties;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private String userAsString;
    private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        userAsString = objectMapper
            .writeValueAsString(new User(UUID.randomUUID(), "johnSmith123", "password"));
    }

    @Test
    public void attemptAuthenticationShouldReturnExpectedAuthenticationWithValidUser() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(userAsString.getBytes());
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuthentication);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties);

        Authentication authentication = jwtAuthenticationFilter
            .attemptAuthentication(mockRequest, mockResponse);

        assertThat(authentication).isEqualTo(mockAuthentication);
    }
}
