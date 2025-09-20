package com.leostormer.strife.conversation;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "conversations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    private static final String errorString = "User is not a valid member of this conversation";

    @Id
    private ObjectId id;

    @DocumentReference(collection = "users", lazy = true)
    private User user1;

    @DocumentReference(collection = "users", lazy = true)
    private User user2;

    private boolean user1Participating;

    private boolean user2Participating;

    private boolean locked;

    public Conversation(User user1, User user2, boolean user1Participating, boolean user2Participating,
            boolean locked) {
        this.user1 = user1;
        this.user2 = user2;
        this.user1Participating = user1Participating;
        this.user2Participating = user2Participating;
        this.locked = locked;
    }

    public Conversation(User user1, User user2, boolean user1Participating, boolean user2Participating) {
        this(user1, user2, user1Participating, user2Participating, false);
    }

    public Conversation(User user1, User user2) {
        this(user1, user2, true, true, false);
    }

    public boolean isValidUser(User user) {
        return user1.getId().equals(user.getId()) || user2.getId().equals(user.getId());
    }

    public boolean isUserParticipating(User user) {
        if (user1.getId().equals(user.getId())) {
            return user1Participating == true;
        } else if (user2.getId().equals(user.getId())) {
            return user2Participating == true;
        } else {
            throw new IllegalArgumentException(errorString);
        }
    }

    public void setUserParticipating(User user, boolean participating) {
        if (user1.getId().equals(user.getId())) {
            user1Participating = participating;
        } else if (user2.getId().equals(user.getId())) {
            user2Participating = participating;
        } else {
            throw new IllegalArgumentException(errorString);
        }
    }
}
