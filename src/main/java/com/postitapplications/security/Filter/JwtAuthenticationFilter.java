package com.postitapplications.security.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.exception.exceptions.UserMappingException;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.exception.RestAuthenticationFailureHandler;
import com.postitapplications.security.utility.JwtProvider;
import com.postitapplications.user.document.User;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.json.JsonParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
        JwtProperties jwtProperties, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
        this.jwtProvider = jwtProvider;
        this.setRequiresAuthenticationRequestMatcher(
            new AntPathRequestMatcher(jwtProperties.getUri(), "POST"));
        this.setAuthenticationFailureHandler(new RestAuthenticationFailureHandler());
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
        } catch (IOException | JsonParseException exception) {
            throw new UserMappingException("Failed to map user object from request");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult) {
        String token = jwtProvider.createTokenFromAuthentication(authResult);

        response.addHeader(jwtProperties.getHeader(), jwtProperties.getPrefix() + token);
    }

//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request,
//        HttpServletResponse response, AuthenticationException failed)
//        throws IOException, ServletException {
//        SecurityContextHolder.clearContext();
//        throw failed;
//    }
}
