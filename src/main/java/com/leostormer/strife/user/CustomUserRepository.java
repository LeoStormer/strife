package com.leostormer.strife.user;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

public interface CustomUserRepository {
    Optional<User> findOneByUsername(String username);

    User updateUserDetails(ObjectId userId, UserUpdate userUpdate);

    List<User> getFriends(ObjectId userId);

    List<User> getBlockedUsers(ObjectId userId);

    boolean existsByUsername(String username);
}
