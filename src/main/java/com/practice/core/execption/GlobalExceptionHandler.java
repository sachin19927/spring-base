package com.practice.core.execption;

import com.practice.core.model.ErrorCode;
import com.practice.core.observability.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MetricsRecorder recorder;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedRequest(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        recordApiError(ErrorCode.MALFORMED_REQUEST);
        return ResponseEntity.badRequest()
                .body(ApiProblemDetailsFactory.create(
                        HttpStatus.BAD_REQUEST,
                        ApiProblemType.MALFORMED_REQUEST,
                        "Malformed Request",
                        "The request body is invalid",
                        ErrorCode.MALFORMED_REQUEST.name(),
                        request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleRequestValidations(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(), Objects.requireNonNull(error.getDefaultMessage())))
                .toList();
        recordApiError(ErrorCode.VALIDATION_FAILED);
        return ResponseEntity.badRequest()
                .body(ApiProblemDetailsFactory.create(
                        HttpStatus.BAD_REQUEST,
                        ApiProblemType.VALIDATION_FAILED,
                        "Validation Failed",
                        "One or more request fields are invalid",
                        ErrorCode.VALIDATION_FAILED.name(),
                        request,
                        fieldErrors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        recordApiError(ErrorCode.INVALID_REQUEST_PARAMETER);
        return ResponseEntity.badRequest()
                .body(ApiProblemDetailsFactory.create(
                        HttpStatus.BAD_REQUEST,
                        ApiProblemType.INVALID_REQUEST_PARAMETER,
                        "Invalid Request Parameter",
                        "Invalid value for parameter: " + ex.getName(),
                        ErrorCode.INVALID_REQUEST_PARAMETER.name(),
                        request));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException ex, HttpServletRequest request) {
        ApiProblemType problemType = ex instanceof ResourceNotFoundException
                ? ApiProblemType.RESOURCE_NOT_FOUND
                : ApiProblemType.BUSINESS_VALIDATION;
        String title = ex instanceof ResourceNotFoundException ? "Resource Not Found" : "Business Validation Failed";
        recordApiError(ex.getErrorCode());
        return ResponseEntity.badRequest()
                .body(ApiProblemDetailsFactory.create(
                        HttpStatus.BAD_REQUEST,
                        problemType,
                        title,
                        ex.getMessage(),
                        ex.getErrorCode().name(),
                        request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolations(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation while processing request {}", request.getRequestURI(), ex);
        recordApiError(ErrorCode.DATA_INTEGRITY_VIOLATION);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiProblemDetailsFactory.create(
                        HttpStatus.CONFLICT,
                        ApiProblemType.DATA_INTEGRITY_VIOLATION,
                        "Data Integrity Violation",
                        "The request violates data integrity constraints",
                        ErrorCode.DATA_INTEGRITY_VIOLATION.name(),
                        request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnwantedException(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred while processing request {}", request.getRequestURI(), ex);
        recordApiError(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiProblemDetailsFactory.create(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ApiProblemType.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred",
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        request));
    }

    private void recordApiError(ErrorCode errorCode) {
        recorder.increment(MetricConstants.API_ERROR, MetricTag.of(MetricTagKey.ERROR_TYPE, errorCode.name()));
    }
}
