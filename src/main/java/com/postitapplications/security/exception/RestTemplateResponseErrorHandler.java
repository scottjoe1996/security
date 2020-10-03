package com.postitapplications.security.exception;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

import com.postitapplications.exception.exceptions.ExternalServiceException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return (clientHttpResponse.getStatusCode().series() == CLIENT_ERROR
            || clientHttpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        if (isErrorResponseStatus(clientHttpResponse.getStatusCode())) {
            throw new ExternalServiceException(clientHttpResponse.getStatusCode(),
                clientHttpResponse.getBody().toString());
        }
    }

    private boolean isErrorResponseStatus(HttpStatus httpStatus) {
        return httpStatus.series() == HttpStatus.Series.SERVER_ERROR
            || httpStatus.series() == HttpStatus.Series.CLIENT_ERROR;
    }
}
