package com.postitapplications.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postitapplications.exception.ExceptionResponseBody;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, AuthenticationException exception)
        throws IOException, ServletException {
        ExceptionResponseBody responseBody = getResponseBody(exception);

        httpServletResponse.setStatus(responseBody.getHttpStatus().value());
        OutputStream out = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, responseBody);
        out.flush();
    }

    private ExceptionResponseBody getResponseBody(AuthenticationException exception) {
        if (exception.getClass().equals(UsernameNotFoundException.class)) {
            return new ExceptionResponseBody(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }

        if (exception.getClass().equals(InternalAuthenticationServiceException.class)) {
            return new ExceptionResponseBody(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

        return new ExceptionResponseBody(HttpStatus.UNAUTHORIZED, "Bad Credentials");
    }
}
