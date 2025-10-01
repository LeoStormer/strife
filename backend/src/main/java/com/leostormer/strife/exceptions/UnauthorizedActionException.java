package com.leostormer.strife.exceptions;

public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(IExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
    }
}
