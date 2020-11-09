package com.postitapplications.security.Filter;

import com.google.common.collect.ImmutableList;
import com.postitapplications.security.configuration.JwtProperties;
import com.postitapplications.security.utility.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImp userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public WebSecurityConfig(UserDetailsServiceImp userDetailsService,
        BCryptPasswordEncoder passwordEncoder, JwtProperties jwtProperties,
        JwtProvider jwtProvider, HandlerExceptionResolver handlerExceptionResolver) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProperties = jwtProperties;
        this.jwtProvider = jwtProvider;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, jwtProperties.getUri()).permitAll()
                .antMatchers(HttpMethod.POST, "/security/user").permitAll()
                .anyRequest().authenticated()
            .and()
                .addFilterBefore(new ExceptionHandlerFilter(handlerExceptionResolver), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtProperties, jwtProvider))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtProperties, jwtProvider))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ImmutableList.of("*"));
        configuration.setAllowedMethods(ImmutableList.of("HEAD",
            "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
