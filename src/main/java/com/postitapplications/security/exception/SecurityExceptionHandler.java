package com.postitapplications.security.exception;

import com.postitapplications.exception.ExceptionResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class SecurityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RestClientException.class)
    public ResponseEntity<Object> handleBadGatewayException(RestClientException exception) {
        HttpStatus badGateway = HttpStatus.BAD_GATEWAY;
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(badGateway,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, badGateway);
    }
}
