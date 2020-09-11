package com.postitapplications.authentication.security;

import com.postitapplications.authentication.configuration.JwtProperties;
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

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
        JwtProperties jwtProperties) {
        super(authenticationManager);
        this.jwtProperties = jwtProperties;
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

        Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecret().getBytes())
                            .parseClaimsJws(token).getBody();

        String username = claims.getSubject();
        List<String> authorities = (List<String>) claims.get("authorities");

        return new UsernamePasswordAuthenticationToken(username, null,
            authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }
}
