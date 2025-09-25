package com.leostormer.strife.conversation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "conversations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    @Id
    private ObjectId id;

    private boolean locked;

    private int numUsers;

    private Map<ObjectId, Boolean> userPresenceMap;

    public Conversation(boolean locked, User... users) {
        this.locked = locked;
        this.numUsers = users.length;
        this.userPresenceMap = new HashMap<>();
        for (User user : users) {
            this.userPresenceMap.put(user.getId(), true);
        }
    }

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

    public Conversation(User user1, User user2, boolean user1IsPresent, boolean user2IsPresent) {
        // Locked if and only if neither user is present
        this(!(user1IsPresent || user2IsPresent), List.of(user1, user2), List.of(user1IsPresent, user2IsPresent));
    }

    public Conversation(User... users) {
        this(false, users);
    }

    public boolean isValidUser(User user) {
        return this.userPresenceMap.containsKey(user.getId());
    }

    public boolean isPresent(User user) {
        return this.userPresenceMap.getOrDefault(user.getId(), false);
    }

    public void setIsPresent(User user, boolean isPresent) {
        this.userPresenceMap.replace(user.getId(), isPresent);
    }

    public boolean isAnyUserPresent() {
        return this.userPresenceMap.values().stream().anyMatch(isPresent -> isPresent.booleanValue() == true);
    }
}
