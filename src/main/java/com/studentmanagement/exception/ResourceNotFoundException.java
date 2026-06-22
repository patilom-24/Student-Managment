package com.studentmanagement.exception;

public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
