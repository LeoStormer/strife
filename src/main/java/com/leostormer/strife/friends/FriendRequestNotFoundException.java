package com.leostormer.strife.friends;

public class FriendRequestNotFoundException extends RuntimeException {
    public FriendRequestNotFoundException() {
        super("Friend request not found");
    }
    
}
