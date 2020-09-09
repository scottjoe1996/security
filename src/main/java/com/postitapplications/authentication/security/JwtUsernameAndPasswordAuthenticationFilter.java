package com.postitapplications.authentication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.authentication.configuration.JwtProperties;
import com.postitapplications.exception.exceptions.UserNotAuthorised;
import com.postitapplications.user.document.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class JwtUsernameAndPasswordAuthenticationFilter extends
    UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager,
        JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
        this.setRequiresAuthenticationRequestMatcher(
            new AntPathRequestMatcher(jwtProperties.getUri(), "POST"));
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {
        User user = getUserFromRequest(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            user.getUsername(), user.getPassword(), Collections.emptyList());

        return authenticationManager.authenticate(authToken);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        try {
            return new ObjectMapper().readValue(request.getInputStream(), User.class);
        } catch (IOException e) {
            throw new UserNotAuthorised(
                String.format("Failed to authorise request with error: %s", e.getMessage()));
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult) {
        String token = createToken(authResult);

        response.addHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + token);
    }

    private String createToken(Authentication authResult) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                   .setSubject(authResult.getName())
                   .claim("authorities", authResult.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                   .setIssuedAt(new Date(now))
                   .setExpiration(new Date(now + jwtProperties.getExpiration() * 1000))
                   .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                   .compact();
    }
}