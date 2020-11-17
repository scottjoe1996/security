package com.postitapplications.security.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.postitapplications.exception.exceptions.ExternalServiceException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class RestTemplateResponseErrorHandlerTests {
    JSONObject mockResponse = new JSONObject();

    @Test
    public void hasErrorShouldReturnTrueIfHttpResponseHas4XXStatusCode() throws IOException {
        RestTemplateResponseErrorHandler errorHandler = new RestTemplateResponseErrorHandler();
        ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(new byte[0],
            HttpStatus.NOT_FOUND);

        boolean result = errorHandler.hasError(clientHttpResponse);

        assertTrue(result);
    }

    @Test
    public void hasErrorShouldReturnTrueIfHttpResponseHas5XXStatusCode() throws IOException {
        RestTemplateResponseErrorHandler errorHandler = new RestTemplateResponseErrorHandler();
        ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(new byte[0],
            HttpStatus.BAD_GATEWAY);

        boolean result = errorHandler.hasError(clientHttpResponse);

        assertTrue(result);
    }

    @Test
    public void hasErrorShouldReturnFalseIfHttpResponseHasNeither4xxOr5xxStatusCode()
        throws IOException {
        RestTemplateResponseErrorHandler errorHandler = new RestTemplateResponseErrorHandler();
        ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(new byte[0],
            HttpStatus.OK);

        boolean result = errorHandler.hasError(clientHttpResponse);

        assertFalse(result);
    }

    @Test
    public void handleErrorShouldThrowExternalServiceExceptionWithNotFoundStatusCode() {
        RestTemplateResponseErrorHandler errorHandler = new RestTemplateResponseErrorHandler();
        ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(
            mockResponse.toString().getBytes(StandardCharsets.UTF_8), HttpStatus.NOT_FOUND);

        ExternalServiceException exception = assertThrows(ExternalServiceException.class, () -> {
            errorHandler.handleError(clientHttpResponse);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void handleErrorShouldThrowExternalServiceExceptionWithBadGatewayStatusCode() {
        RestTemplateResponseErrorHandler errorHandler = new RestTemplateResponseErrorHandler();
        ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(
            mockResponse.toString().getBytes(StandardCharsets.UTF_8), HttpStatus.BAD_GATEWAY);

        ExternalServiceException exception = assertThrows(ExternalServiceException.class, () -> {
            errorHandler.handleError(clientHttpResponse);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    public void handleErrorShouldNotThrowExternalServiceException() {
        RestTemplateResponseErrorHandler errorHandler = new RestTemplateResponseErrorHandler();
        ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(new byte[0],
            HttpStatus.OK);

        assertDoesNotThrow(() -> errorHandler.handleError(clientHttpResponse));
    }
}