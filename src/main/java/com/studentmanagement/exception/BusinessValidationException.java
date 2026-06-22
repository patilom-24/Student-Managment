package com.studentmanagement.exception;

public class BusinessValidationException extends ServiceException {

    public BusinessValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }
}
