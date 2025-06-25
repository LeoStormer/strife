package com.leostormer.strife.friends;

import com.leostormer.strife.user.UserView;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FriendRequestView {
    private String id;
    private UserView sender;
    private UserView receiver;
    private FriendStatus status;

    public FriendRequestView(FriendRequest friendRequest) {
        this(friendRequest.getId().toString(), new UserView(friendRequest.getSender()),
                new UserView(friendRequest.getReceiver()), friendRequest.getStatus());
    }
}
