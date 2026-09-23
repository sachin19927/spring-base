package com.practice.core.execption;

import com.practice.core.model.ErrorCode;
import org.springframework.http.HttpStatus;

public final class BusinessValidationException extends ApiException {
    public BusinessValidationException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }
}
