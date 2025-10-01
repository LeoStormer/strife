package com.leostormer.strife.server;

import com.leostormer.strife.exceptions.IExceptionMessage;

public enum ServerExceptionMessage implements IExceptionMessage {
    SERVER_NOT_FOUND("Server not found"),
    CHANNEL_NOT_FOUND("Channel not found"),
    MESSAGE_NOT_FOUND("Message not found"),
    ROLE_NOT_FOUND("Role not found"),
    USER_IS_BANNED("User is banned from this server"),
    USER_NOT_MEMBER("User is not a member of this server");

    private String message;

    private ServerExceptionMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
