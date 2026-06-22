package com.studentmanagement.exception;

public enum ErrorCode {

    RESOURCE_NOT_FOUND("Resource not found"),
    DUPLICATE_RESOURCE("Duplicate resource"),
    VALIDATION_FAILED("Business validation failed"),
    INVALID_REQUEST("Invalid request"),
    INTERNAL_ERROR("Internal server error");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
