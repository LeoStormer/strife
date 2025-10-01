package com.leostormer.strife.user;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException() {
        super("Username already exists");
    }
}
