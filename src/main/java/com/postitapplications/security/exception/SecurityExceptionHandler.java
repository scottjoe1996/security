package com.postitapplications.security.exception;

import com.postitapplications.exception.ExceptionResponseBody;
import com.postitapplications.exception.exceptions.NullOrEmptyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class SecurityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException exception) {
        HttpStatus httpClientStatusCode = exception.getStatusCode();
        ExceptionResponseBody exceptionResponseBody = new ExceptionResponseBody(httpClientStatusCode,
            exception.getMessage());

        return new ResponseEntity<>(exceptionResponseBody, httpClientStatusCode);
    }
}
