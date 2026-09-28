package com.practice.core.execption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.practice.core.model.ErrorCode;
import com.practice.core.observability.MetricsRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@DisplayName("GlobalExceptionHandler Tests")
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler(new MetricsRecorder(new SimpleMeterRegistry()));

    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest("GET", "/v1/deliveries/123");
    }

    @Test
    @DisplayName("handleMalformedRequest should return 400 with MALFORMED_REQUEST error code")
    void testHandleMalformedRequest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ProblemDetail> response = handler.handleMalformedRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.MALFORMED_REQUEST.name());
        assertThat(body.getInstance()).hasToString("/v1/deliveries/123");
    }

    @Test
    @DisplayName("handleRequestValidations should return 400 with field errors")
    void testHandleRequestValidations() {
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("deliveryRequest", "vehicleId", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ProblemDetail> response = handler.handleRequestValidations(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.VALIDATION_FAILED.name());
        assertThat(body.getProperties()).containsKey("fieldErrors");
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatch should return 400 with INVALID_REQUEST_PARAMETER error code")
    void testHandleMethodArgumentTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", UUID.class, "id", null, new IllegalArgumentException());

        ResponseEntity<ProblemDetail> response = handler.handleMethodArgumentTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.INVALID_REQUEST_PARAMETER.name());
        assertThat(body.getDetail()).contains("id");
    }

    @Test
    @DisplayName("handleApiException should return RESOURCE_NOT_FOUND type for ResourceNotFoundException")
    void testHandleApiExceptionWhenResourceNotFound() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException(ErrorCode.DELIVERY_NOT_FOUND, "Delivery not found");

        ResponseEntity<ProblemDetail> response = handler.handleApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.DELIVERY_NOT_FOUND.name());
        assertThat(body.getTitle()).isEqualTo("Resource Not Found");
    }

    @Test
    @DisplayName("handleApiException should return BUSINESS_VALIDATION type for BusinessValidationException")
    void testHandleApiExceptionWhenBusinessValidation() {
        BusinessValidationException ex =
                new BusinessValidationException(ErrorCode.VEHICLE_ID_REQUIRED, "vehicleId required");

        ResponseEntity<ProblemDetail> response = handler.handleApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Business Validation Failed");
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.VEHICLE_ID_REQUIRED.name());
    }

    @Test
    @DisplayName("handleDataIntegrityViolation should return 409 conflict")
    void testHandleDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key");

        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolations(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.DATA_INTEGRITY_VIOLATION.name());
    }

    @Test
    @DisplayName("handleApiException should return 500 internal server error")
    void testHandleUnwantedException() {
        Exception ex = new IllegalStateException("boom");

        ResponseEntity<ProblemDetail> response = handler.handleUnwantedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties()).containsEntry("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.name());
    }
}
