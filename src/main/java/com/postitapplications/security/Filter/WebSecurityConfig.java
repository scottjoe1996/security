package com.postitapplications.security.Filter;

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
import org.springframework.web.servlet.HandlerExceptionResolver;

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
}
