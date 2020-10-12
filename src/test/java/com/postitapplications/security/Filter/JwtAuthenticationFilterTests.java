package com.postitapplications.security.Filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.exception.exceptions.UserMappingException;
import com.postitapplications.security.configuration.JwtBeanConfig;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.utility.JwtProvider;
import com.postitapplications.user.document.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.bson.json.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(JwtBeanConfig.class)
public class JwtAuthenticationFilterTests {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private JwtProvider jwtProvider;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private String userAsString;
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    private final HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    private final Authentication mockAuthentication = mock(Authentication.class);

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        userAsString = objectMapper
            .writeValueAsString(new User(UUID.randomUUID(), "johnSmith123", "password"));
    }

    @Test
    public void attemptAuthenticationShouldReturnExpectedAuthenticationWithValidUser() {
        mockRequest.setContent(userAsString.getBytes());
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(mockAuthentication);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties,
            jwtProvider);

        Authentication authentication = jwtAuthenticationFilter
            .attemptAuthentication(mockRequest, mockResponse);

        assertThat(authentication).isEqualTo(mockAuthentication);
    }

    @Test
    public void attemptAuthenticationShouldThrowUserMappingExceptionWhenRequestBodyIsInvalid() {
        String invalidBodyString = "invalidBodyString";
        mockRequest.setContent(invalidBodyString.getBytes());
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties,
            jwtProvider);

        Exception exception = assertThrows(UserMappingException.class, () -> {
            jwtAuthenticationFilter.attemptAuthentication(mockRequest, mockResponse);
        });

        assertThat(exception.getMessage()).isEqualTo("Failed to map user object from request");
    }

    @Test
    public void attemptAuthenticationShouldThrowBadCredentialsExceptionExceptionWhenUserFailsToAuthenticate() {
        mockRequest.setContent(userAsString.getBytes());
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(BadCredentialsException.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties,
            jwtProvider);

        assertThrows(BadCredentialsException.class, () -> {
            jwtAuthenticationFilter.attemptAuthentication(mockRequest, mockResponse);
        });
    }

    @Test
    public void successfulAuthenticationShouldAddExpectedTokenToResponseHeader() {
        Collection mockAuthorities = Collections.emptyList();
        when(mockAuthentication.getAuthorities()).thenReturn(mockAuthorities);
        when(mockAuthentication.getName()).thenReturn("johnSmith123");
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtProperties,
            jwtProvider);

        String expectedToken = Jwts.builder().setSubject("johnSmith123")
                                   .claim("authorities", mockAuthorities)
                                   .setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(
                new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000))
                                   .signWith(SignatureAlgorithm.HS512,
                                       jwtProperties.getSecret().getBytes()).compact();

        jwtAuthenticationFilter
            .successfulAuthentication(mockRequest, mockResponse, new MockFilterChain(),
                mockAuthentication);

        verify(mockResponse).addHeader(Mockito.eq(jwtProperties.getHeader()),
            Mockito.contains(jwtProperties.getPrefix() + expectedToken.substring(0, 119)));
    }
}
