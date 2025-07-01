package com.leostormer.strife.friends;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "friend_requests")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FriendRequest {
    @Id
    private ObjectId id;

    @DocumentReference(collection = "users")
    private User user1;

    @DocumentReference(collection = "users")
    private User user2;

    @Builder.Default
    private FriendRequestResponse user1Response = FriendRequestResponse.ACCEPTED;

    @Builder.Default
    private FriendRequestResponse user2Response = FriendRequestResponse.PENDING;

    public boolean isValidUser(User user) {
        return user1.getId().equals(user.getId()) || user2.getId().equals(user.getId());
    }

    public User getOtherUser(User user) {
        if (user1.getId().equals(user.getId())) {
            return user2;
        } else if (user2.getId().equals(user.getId())) {
            return user1;
        } else {
            throw new IllegalArgumentException("User is not part of this friend request");
        }
    }

    public boolean hasBeenBlocked(User user) {
        if (user1.getId().equals(user.getId())) {
            return user2Response.equals(FriendRequestResponse.BLOCKED);
        } else if (user2.getId().equals(user.getId())) {
            return user1Response.equals(FriendRequestResponse.BLOCKED);
        } else {
            throw new IllegalArgumentException("User is not part of this friend request");
        }
    }

    public boolean hasSentBlockRequest(User user) {
        if (user1.getId().equals(user.getId())) {
            return user1Response.equals(FriendRequestResponse.BLOCKED);
        } else if (user2.getId().equals(user.getId())) {
            return user2Response.equals(FriendRequestResponse.BLOCKED);
        } else {
            throw new IllegalArgumentException("User is not part of this friend request");
        }
    }

    public void setUserResponse(User user, FriendRequestResponse response) {
        if (user1.getId().equals(user.getId())) {
            user1Response = response;
        } else if (user2.getId().equals(user.getId())) {
            user2Response = response;
        } else {
            throw new IllegalArgumentException("User is not part of this friend request");
        }
    }
}
