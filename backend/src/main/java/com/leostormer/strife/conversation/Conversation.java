package com.leostormer.strife.conversation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Conversation extends Channel {
    /**
     * Whether users can send messages in this channel.
     */
    private boolean locked;

    /**
     * The number of users in this conversation.
     */
    private int numUsers;

    /**
     * Maps from userId to whether that user is still present in this conversation.
     */
    private Map<ObjectId, Boolean> userPresenceMap;

    /**
     * Constructs a conversation such that all given users are present.
     * @param locked whether the conversation is locked
     * @param users the users in this conversation
     */
    public Conversation(boolean locked, User... users) {
        this.locked = locked;
        this.numUsers = users.length;
        this.userPresenceMap = new HashMap<>();
        for (User user : users) {
            this.userPresenceMap.put(user.getId(), true);
        }
    }

    /**
     * Constructs a conversation from a list of users and a list indicating whether each one is present
     * @param locked whether the conversation is locked
     * @param users the users in this conversation
     * @param usersPresent whether each user is present
     */
    public Conversation(boolean locked, List<User> users, List<Boolean> usersPresent) {
        this.locked = locked;
        this.numUsers = users.size();
        this.userPresenceMap = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Boolean userParticipating = i < usersPresent.size() ? usersPresent.get(i) : true;
            this.userPresenceMap.put(user.getId(), userParticipating);
        }
    }

    /**
     * Constructs a conversation between two users. If neither user is present the conversation is locked.
     * @param user1 the first user
     * @param user2 the second user
     * @param user1IsPresent whether user1 is present
     * @param user2IsPresent whether user2 is present
     */
    public Conversation(User user1, User user2, boolean user1IsPresent, boolean user2IsPresent) {
        // Locked if and only if neither user is present
        this(!(user1IsPresent || user2IsPresent), List.of(user1, user2), List.of(user1IsPresent, user2IsPresent));
    }

    /**
     * Constructs a conversation such that all given users are present and the conversation is not locked.
     * @param users
     */
    public Conversation(User... users) {
        this(false, users);
    }

    /**
     * Returns whether the given user is part of the conversation regardless of whether they are present or not.
     * @param user the given user
     * @return whether they are part of the conversation
     */
    public boolean isValidUser(User user) {
        return this.userPresenceMap.containsKey(user.getId());
    }

    /**
     * Returns whether a given user is present in the conversation.
     * @param user the given user
     * @return whether they are present
     */
    public boolean isPresent(User user) {
        return this.userPresenceMap.getOrDefault(user.getId(), false);
    }

    /**
     * Sets whether a given user is present in the conversation if they are a valid user to conversation
     * @param user the given user
     * @param isPresent whether they are present
     */
    public void setIsPresent(User user, boolean isPresent) {
        this.userPresenceMap.replace(user.getId(), isPresent);
    }

    /**
     * Returns whether any user is present in the conversation
     * @return whether any is present
     */
    public boolean isAnyUserPresent() {
        return this.userPresenceMap.values().stream().anyMatch(isPresent -> isPresent.booleanValue() == true);
    }
}
