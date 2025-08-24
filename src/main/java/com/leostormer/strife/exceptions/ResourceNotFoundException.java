package com.leostormer.strife.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(IExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
    }
}
