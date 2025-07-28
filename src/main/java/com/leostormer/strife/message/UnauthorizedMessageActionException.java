package com.leostormer.strife.message;

public class UnauthorizedMessageActionException extends RuntimeException {
    public UnauthorizedMessageActionException() {
        super("You are not authorized to act on this message");
    }
}
