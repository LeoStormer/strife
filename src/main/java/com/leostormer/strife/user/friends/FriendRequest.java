package com.leostormer.strife.user.friends;

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

    /**
     * The user who sent this request
     */
    @DocumentReference(collection = "users", lazy = true)
    private User sender;

    /**
     * The user who received this request
     */
    @DocumentReference(collection = "users", lazy = true)
    private User receiver;

    @Builder.Default
    private boolean accepted = false;

    public boolean isValidUser(User user) {
        return sender.getId().equals(user.getId()) || receiver.getId().equals(user.getId());
    }

    public User getOtherUser(User user) {
        if (sender.getId().equals(user.getId())) {
            return receiver;
        } else if (receiver.getId().equals(user.getId())) {
            return sender;
        } else {
            throw new IllegalArgumentException("User is not part of this friend request");
        }
    }
}
