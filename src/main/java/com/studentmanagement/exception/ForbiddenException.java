package com.studentmanagement.exception;

public class ForbiddenException extends ServiceException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
