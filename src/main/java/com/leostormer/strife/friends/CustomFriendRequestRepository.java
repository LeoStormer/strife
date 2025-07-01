package com.leostormer.strife.friends;

import java.util.List;

import org.bson.types.ObjectId;

public interface CustomFriendRequestRepository {
    List<FriendRequest> findAllUserRequests(ObjectId userId);
    List<FriendRequest> findAllUserBlockedRequests(ObjectId userId);
    List<FriendRequest> findAllUserAcceptedRequests(ObjectId userId);
    List<FriendRequest> findAllUserPendingRequests(ObjectId userId);
}
