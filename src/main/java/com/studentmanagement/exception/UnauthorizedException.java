package com.studentmanagement.exception;

public class UnauthorizedException extends ServiceException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
