package com.leostormer.strife.user.friends;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, ObjectId> {
    /**
     * Returns a list of friend requests sent to or by the given user.
     * 
     * @param userId the given user's id
     * @return list of friend requests
     */
    @Query("{ $or : [ { sender: ?0 }, { receiver: ?0 } ] }")
    List<FriendRequest> findAllUserRequests(ObjectId userId);

    /**
     * Returns a list of friend requests sent to or by the given user that have
     * been accepted.
     * 
     * @param userId the given user's id
     * @return list of accepted friend requests
     */
    @Query("{ $and : [ { accepted: true }, { $or : [ { sender: ?0 }, { receiver: ?0 } ] } ] }")
    List<FriendRequest> findAllUserAcceptedRequests(ObjectId userId);

    /**
     * Returns a list of friend requests sent to or by the given user that are
     * still pending.
     * 
     * @param userId the given user's id
     * @return list of pending friend requests
     */
    @Query("{ $and : [ { accepted: false }, { $or : [ { sender: ?0 }, { receiver: ?0 } ] } ] }")
    List<FriendRequest> findAllUserPendingRequests(ObjectId userId);

    /**
     * Finds a friend request between two users whether pending or accepted.
     * Will match regardless of which user sent the request.
     * 
     * @param userId1
     * @param userId2
     * @return the friend request or {@link Optional#empty()} if none found
     */
    @Query("{ $or : [ { sender : ?0, receiver : ?1 }, { sender : ?1, receiver : ?0 } ] }")
    Optional<FriendRequest> findOneByUserIds(ObjectId userId1, ObjectId userId2);

    /**
     * Checks if a friend request exists between two users whether pending or
     * accepted. Will match regardless of which user sent the request.
     * 
     * @param userId1
     * @param userId2
     * @return true if a friend request exists between the two users, false
     *         otherwise
     */
    @Query(value = "{ $or : [ { sender : ?0, receiver : ?1 }, { sender : ?1, receiver : ?0 } ] }", exists = true)
    boolean existsByUserIds(ObjectId userId1, ObjectId userId2);
}
