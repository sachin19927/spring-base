package com.practice.core.execption;

public class BusinessValidationException extends  RuntimeException{
    public BusinessValidationException(String message){
        super(message);
    }
}
