package com.studentmanagement.exception;

public class DuplicateResourceException extends ServiceException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }
}
