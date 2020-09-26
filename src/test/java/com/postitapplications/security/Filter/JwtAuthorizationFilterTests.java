package com.postitapplications.security.Filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.postitapplications.security.configuration.JwtBeanConfig;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.utility.JwtProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(JwtBeanConfig.class)
public class JwtAuthorizationFilterTests {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private JwtProvider jwtProvider;
    private JwtAuthorizationFilter jwtAuthorizationFilter;
    private String mockJwtToken;
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    private final HttpServletResponse mockResponse = new MockHttpServletResponse();
    private final FilterChain mockFilterChain = mock(FilterChain.class);
    private final SecurityContext mockSecurityContext = mock(SecurityContext.class);

    @BeforeEach
    public void setUp() {
        jwtAuthorizationFilter = new JwtAuthorizationFilter(authenticationManager, jwtProperties, jwtProvider);
        mockJwtToken = Jwts.builder().setSubject("johnSmith123")
                           .claim("authorities", Collections.emptyList())
                           .setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(
                new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000))
                           .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                           .compact();
    }

    @Test()
    public void doFilterInternalShouldSkipToNextFilterWhenAuthorizationRequestHeaderIsNull()
        throws IOException, ServletException {
        jwtAuthorizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, atMostOnce()).doFilter(mockRequest, mockResponse);
    }

    @Test()
    public void doFilterInternalShouldSkipToNextFilterWhenAuthorizationRequestHeaderIsInvalid()
        throws IOException, ServletException {
        mockRequest.addHeader(jwtProperties.getHeader(), "notValid");

        jwtAuthorizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, atMostOnce()).doFilter(mockRequest, mockResponse);
    }

    @Test()
    public void doFilterInternalShouldSetExpectedAuthentication()
        throws IOException, ServletException {
        mockRequest.addHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + mockJwtToken);
        UsernamePasswordAuthenticationToken expectedAuthentication =
            new UsernamePasswordAuthenticationToken("johnSmith123", null, Collections.emptyList());
        doNothing().when(mockSecurityContext).setAuthentication(any(Authentication.class));
        SecurityContextHolder.setContext(mockSecurityContext);

        jwtAuthorizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockSecurityContext, atMostOnce()).setAuthentication(expectedAuthentication);
        verify(mockFilterChain, atMostOnce()).doFilter(mockRequest, mockResponse);
    }

    @Test()
    public void doFilterInternalShouldThrowJwtExceptionIfTokenIsNull() {
        mockRequest.addHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + null);

        Exception exception = assertThrows(JwtException.class, () -> {
            jwtAuthorizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
        });

        assertThat(exception.getMessage()).isEqualTo("failed to parse given token with error: JWT strings must contain exactly 2 period characters. Found: 0");
    }

    @Test()
    public void doFilterInternalShouldThrowJwtExceptionIfTokenIsEmpty() {
        mockRequest.addHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + "");

        Exception exception = assertThrows(JwtException.class, () -> {
            jwtAuthorizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
        });

        assertThat(exception.getMessage()).isEqualTo("failed to parse given token with error: JWT String argument cannot be null or empty");
    }
}
