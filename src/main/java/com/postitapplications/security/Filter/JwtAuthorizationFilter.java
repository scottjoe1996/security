package com.postitapplications.security.Filter;

import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.utility.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
        JwtProperties jwtProperties, JwtProvider jwtProvider) {
        super(authenticationManager);
        this.jwtProperties = jwtProperties;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(jwtProperties.getHeader());

        if (isNotValidHeader(header)) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(header);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }

    private boolean isNotValidHeader(String header) {
        return header == null || !header.startsWith(jwtProperties.getPrefix());
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(String header) {
        String token = header.replace(jwtProperties.getPrefix(), "");

        String username = jwtProvider.getUsernameFromToken(token);
        List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthoritiesFromToken(token);

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
