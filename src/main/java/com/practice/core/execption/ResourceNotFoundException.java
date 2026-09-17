package com.practice.core.execption;

import com.practice.core.model.ErrorCode;
import org.springframework.http.HttpStatus;

public final class ResourceNotFoundException extends  ApiException{
    public ResourceNotFoundException(ErrorCode errorCode, String message){
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }
}
