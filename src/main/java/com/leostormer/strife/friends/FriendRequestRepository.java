package com.leostormer.strife.friends;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, ObjectId> {
    List<FriendRequest> findBySenderId(ObjectId senderId);

    List<FriendRequest> findByReceiverId(ObjectId receiverId);

    List<FriendRequest> findBySenderIdOrReceiverId(ObjectId senderId, ObjectId receiverId);

    //TODO: Gonna have to make a custom query for this
    List<FriendRequest> findBySenderIdOrReceiverIdAndStatus(ObjectId senderId, ObjectId receiverId,
            FriendStatus status);

    Optional<FriendRequest> findOneBySenderIdAndReceiverId(ObjectId senderId, ObjectId receiverId);

    boolean existsBySenderIdAndReceiverId(ObjectId senderId, ObjectId receiverId);
}
