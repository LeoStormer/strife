package com.leostormer.strife.friends;

import com.leostormer.strife.user.UserView;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FriendRequestView {
    private String id;
    private UserView user1;
    private UserView user2;
    private FriendRequestResponse user1Response;
    private FriendRequestResponse user2Response;

    public FriendRequestView(FriendRequest friendRequest) {
        this(friendRequest.getId().toString(), new UserView(friendRequest.getUser1()),
                new UserView(friendRequest.getUser2()), friendRequest.getUser1Response(), friendRequest.getUser2Response());
    }
}
