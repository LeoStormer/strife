package com.leostormer.strife.user;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

public interface CustomUserRepository {
    Optional<User> findOneByUsername(String username);

    Optional<User> findOneByEmail(String email);

    User updateUserDetails(ObjectId userId, UserUpdate userUpdate);

    List<User> getFriends(ObjectId userId);

    List<User> getBlockedUsers(ObjectId userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void acceptFriendRequest(ObjectId userId, ObjectId otherUserId);

    void removeFriendRequest(ObjectId userId, ObjectId otherUserId);

    void blockUser(ObjectId userId, ObjectId userToBlockId);

    void unblockUser(ObjectId userId, ObjectId userToUnblockId);
}
