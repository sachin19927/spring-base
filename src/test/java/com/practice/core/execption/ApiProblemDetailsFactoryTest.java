package com.practice.core.execption;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("ApiProblemDetailsFactory Tests")
public class ApiProblemDetailsFactoryTest {

    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest("GET", "/v1/deliveries/123");
    }

    @Test
    @DisplayName("create() should populate status, type, title, detail and instance")
    void testCreatePopulatesBaseFields() {
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.VALIDATION_FAILED,
                "Validation Failed",
                "One or more request fields are invalid",
                "VALIDATION_FAILED",
                request);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getType()).isEqualTo(ApiProblemType.VALIDATION_FAILED.toUri());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation Failed");
        assertThat(problemDetail.getDetail()).isEqualTo("One or more request fields are invalid");
        assertThat(problemDetail.getInstance()).hasToString("/v1/deliveries/123");
    }

    @Test
    @DisplayName("create() should set error code and timestamp properties")
    void testCreateSetsErrorCodeAndTimeStamp() {
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Business Validation Failed",
                "vehicleId required",
                "VEHICLE_ID_REQUIRED",
                request);

        assertThat(problemDetail.getProperties()).containsEntry("errorCode", "VEHICLE_ID_REQUIRED");
        assertThat(problemDetail.getProperties()).containsKey("timeStamp");
    }

    @Test
    @DisplayName("create() should emit fieldErrors property when list is null")
    void testCreateOmitsFieldErrorsWhenNull() {
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Business Validation Failed",
                "vehicleId required",
                "VEHICLE_ID_REQUIRED",
                request);

        assertThat(problemDetail.getProperties()).doesNotContainKey("fieldErrors");
    }

    @Test
    @DisplayName("create() should omit fieldErrors property when list is empty")
    void testCreateOmitsFieldErrorsWhenEmpty() {
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Validation Failed",
                "One or more request fields are invalid",
                "VALIDATION_FAILED",
                request,
                List.of());

        assertThat(problemDetail.getProperties()).doesNotContainKey("fieldErrors");
    }

    @Test
    @DisplayName("create() should include fieldErrors property when list is non-empty")
    void testCreateIncludesFieldErrorsWhenPresent() {
        List<ApiFieldError> fieldErrors = List.of(new ApiFieldError("vehicleId", "must not be blank"));
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Validation Failed",
                "One or more request fields are invalid",
                "VALIDATION_FAILED",
                request,
                fieldErrors);

        assertThat(problemDetail.getProperties()).containsEntry("fieldErrors", fieldErrors);
    }

    @Test
    @DisplayName("resolveTraceId() should use traceId request attribute when present")
    void testResolveTraceIdUsesRequestAttribute() {
        request.setAttribute("traceId", "trace-from-attribute");
        request.setAttribute("X-Request-Id", "trace-from-header");
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Validation Failed",
                "detail",
                "CODE",
                request);

        assertThat(problemDetail.getProperties()).containsEntry("traceId", "trace-from-attribute");
    }

    @Test
    @DisplayName("resolveTraceId() should fall back to X-Request-Id header when attribute is blank")
    void testResolveTraceIdFallsBackToHeader() {
        request.addHeader("X-Request-Id", "trace-from-header");
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Validation Failed",
                "detail",
                "CODE",
                request);

        assertThat(problemDetail.getProperties()).containsEntry("traceId", "trace-from-header");
    }

    @Test
    @DisplayName("resolveTraceId() should fall back to request id when attribute and header are absent")
    void testResolveTraceIdFallsBackToRequestId() {
        ProblemDetail problemDetail = ApiProblemDetailsFactory.create(
                HttpStatus.BAD_REQUEST,
                ApiProblemType.BUSINESS_VALIDATION,
                "Validation Failed",
                "detail",
                "CODE",
                request);

        assertThat(problemDetail.getProperties()).containsEntry("traceId", request.getRequestId());
    }
}
