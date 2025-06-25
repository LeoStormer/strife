package com.leostormer.strife.friends;

public class UnauthorizedFriendRequestActionException extends RuntimeException {
    public UnauthorizedFriendRequestActionException() {
        super("You are not authorized to act on this friend request");
    }

    public UnauthorizedFriendRequestActionException(String message) {
        super(message);
    }
}
