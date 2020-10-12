package com.postitapplications.security.utility;

import com.postitapplications.security.configuration.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createTokenFromAuthentication(Authentication authentication) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                   .setSubject(authentication.getName())
                   .claim("authorities", authentication.getAuthorities().stream()
                                                   .map(GrantedAuthority::getAuthority)
                                                   .collect(Collectors.toList()))
                   .setIssuedAt(new Date(now))
                   .setExpiration(new Date(now + jwtProperties.getExpiration() * 1000))
                   .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret().getBytes())
                   .compact();
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        List<String> authorities = getAuthoritiesFromClaims(claims);
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private List<String> getAuthoritiesFromClaims(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");
        return Objects.requireNonNullElse(authorities, Collections.emptyList());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(jwtProperties.getSecret().getBytes())
                       .parseClaimsJws(token).getBody();
        } catch(JwtException | IllegalArgumentException exception) {
            throw new JwtException(String.format("failed to parse given token with error: %s",
                exception.getMessage()));
        }
    }
}
