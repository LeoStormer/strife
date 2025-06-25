package com.leostormer.strife.friends;

public class DuplicateFriendRequestException extends RuntimeException {
    public DuplicateFriendRequestException() {
        super("A friend request between these users already exists.");
    }

    public DuplicateFriendRequestException(String message) {
        super(message);
    }
}
