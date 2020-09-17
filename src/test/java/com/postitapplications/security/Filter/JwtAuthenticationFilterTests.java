package com.postitapplications.security.Filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.security.configuration.JwtAuthenticationFilterBeanConfig;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.user.document.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(mockAuthentication);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties);

        Authentication authentication = jwtAuthenticationFilter
            .attemptAuthentication(mockRequest, mockResponse);

        assertThat(authentication).isEqualTo(mockAuthentication);
    }

    @Test
    public void attemptAuthenticationShouldThrowJsonParseExceptionWhenRequestBodyIsInvalid() {
        String invalidBodyString = "invalidBodyString";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(invalidBodyString.getBytes());
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties);

        Exception exception = assertThrows(JsonParseException.class, () -> {
            jwtAuthenticationFilter.attemptAuthentication(mockRequest, mockResponse);
        });

        assertThat(exception.getMessage()).isEqualTo("Failed to map user object from request");
    }

    @Test
    public void attemptAuthenticationShouldThrowBadCredentialsExceptionExceptionWhenUserFailsToAuthenticate() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(userAsString.getBytes());
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(BadCredentialsException.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties);

        assertThrows(BadCredentialsException.class, () -> {
            jwtAuthenticationFilter.attemptAuthentication(mockRequest, mockResponse);
        });
    }

    @Test
    public void successfulAuthenticationShouldAddExpectedTokenToResponseHeader() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockFilterChain = new MockFilterChain();
        Authentication authentication = mock(Authentication.class);
        Collection mockAuthorities = Collections.emptyList();
        when(authentication.getAuthorities()).thenReturn(mockAuthorities);
        when(authentication.getName()).thenReturn("johnSmith123");
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties);

        String expectedToken = Jwts.builder()
                                   .setSubject("johnSmith123")
                                   .claim("authorities", mockAuthorities)
                                   .setIssuedAt(new Date(System.currentTimeMillis()))
                                   .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000))
                                   .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                                   .compact();

        jwtAuthenticationFilter.successfulAuthentication(mockRequest, mockResponse,
            mockFilterChain, authentication);

        verify(mockResponse).addHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + expectedToken);
    }
}
