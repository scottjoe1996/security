package com.postitapplications.security.exception;

import com.postitapplications.exception.ExceptionResponseBody;
import com.postitapplications.exception.exceptions.ExternalServiceException;
import com.postitapplications.exception.exceptions.ValidationException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class SecurityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceException(ExternalServiceException exception) {
        HttpStatus httpStatus = exception.getStatusCode();
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(httpStatus,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, httpStatus);
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<Object> handleValidationExceptionException(Exception exception) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(badRequest,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, badRequest);
    }

    @ExceptionHandler(value = JwtException.class)
    public ResponseEntity<Object> handleJwtException(Exception exception) {
        HttpStatus forbidden = HttpStatus.FORBIDDEN;
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(forbidden,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, forbidden);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException exception) {
        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(unauthorized,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, unauthorized);
    }

    @ExceptionHandler(value = InternalAuthenticationServiceException.class)
    public ResponseEntity<Object> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException exception) {
        HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(internalServerError,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, internalServerError);
    }
}
