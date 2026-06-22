package com.studentmanagement.exception;

public abstract class ServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    protected ServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
