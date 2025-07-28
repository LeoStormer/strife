package com.leostormer.strife.conversation;

public class UnauthorizedConversationActionException extends RuntimeException {
    
    public UnauthorizedConversationActionException() {
        super("You are not authorized to act on this conversation");
    }

    public UnauthorizedConversationActionException(String message) {
        super(message);
    }
}
