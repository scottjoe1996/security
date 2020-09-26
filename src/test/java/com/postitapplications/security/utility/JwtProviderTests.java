package com.postitapplications.security.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.postitapplications.security.configuration.JwtBeanConfig;
import com.postitapplications.security.configuration.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(JwtBeanConfig.class)
public class JwtProviderTests {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private JwtProvider jwtProvider;
    private String testToken;
    private final Authentication mockAuthentication = mock(Authentication.class);
    private final long testTime = System.currentTimeMillis();

    @BeforeEach
    public void setUp() {
        testToken = Jwts.builder()
                        .setSubject("johnSmith123")
                        .claim("authorities", Collections.emptyList())
                        .setIssuedAt(new Date(testTime))
                        .setExpiration(new Date(testTime + jwtProperties.getExpiration() * 1000))
                        .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                        .compact();
    }

    @Test
    public void createTokenFromAuthenticationShouldReturnExpectedToken() {
        when(mockAuthentication.getName()).thenReturn("johnSmith123");
        when(mockAuthentication.getAuthorities()).thenReturn(Collections.emptyList());

        String actualToken = jwtProvider.createTokenFromAuthentication(mockAuthentication);

        assertThat(actualToken).isEqualTo(testToken);
    }

    @Test
    public void getUserNameFromTokenShouldReturnExpectedUsername() {
        String username = jwtProvider.getUsernameFromToken(testToken);

        assertThat(username).isEqualTo("johnSmith123");
    }

    @Test
    public void getUserNameFromTokenShouldThrowJwtExceptionWhenTokenIsNull() {
        Exception exception = assertThrows(JwtException.class, () -> {
            jwtProvider.getUsernameFromToken(null);
        });

        assertThat(exception.getMessage()).isEqualTo(
            "failed to parse given token with error: JWT String argument cannot be null or empty.");
    }

    @Test
    public void getUserNameFromTokenShouldReturnNullIfTokenHasNoSubject() {
        testToken = Jwts.builder()
                        .claim("authorities", Collections.emptyList())
                        .setIssuedAt(new Date(testTime))
                        .setExpiration(new Date(testTime + jwtProperties.getExpiration() * 1000))
                        .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                        .compact();

        String username = jwtProvider.getUsernameFromToken(testToken);

        assertThat(username).isEqualTo(null);
    }

    @Test
    public void getAuthoritiesFromTokenShouldReturnExpectedAuthorities() {
        List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthoritiesFromToken(testToken);

        assertThat(authorities).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getAuthoritiesFromTokenShouldThrowJwtExceptionWhenTokenIsNull() {
        Exception exception = assertThrows(JwtException.class, () -> {
            jwtProvider.getAuthoritiesFromToken(null);
        });

        assertThat(exception.getMessage()).isEqualTo(
            "failed to parse given token with error: JWT String argument cannot be null or empty.");
    }

    @Test
    public void getAuthoritiesFromTokenShouldReturnEmptyListIfTokenHasNoAuthorities() {
        testToken = Jwts.builder()
                        .setSubject("johnSmith123")
                        .setIssuedAt(new Date(testTime))
                        .setExpiration(new Date(testTime + jwtProperties.getExpiration() * 1000))
                        .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                        .compact();

        List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthoritiesFromToken(testToken);

        assertThat(authorities).isEqualTo(Collections.emptyList());
    }
}
