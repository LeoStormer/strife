package com.leostormer.strife.conversation;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException() {
        super("Conversation not found");
    }
}
