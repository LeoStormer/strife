package com.leostormer.strife.friends;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository
        extends MongoRepository<FriendRequest, ObjectId>, CustomFriendRequestRepository {

    List<FriendRequest> findByUser1_Id(ObjectId user1Id);
    List<FriendRequest> findByUser2_Id(ObjectId user2Id);

    @Query("{ $or : [ { user1 : ?0, user2 : ?1 }, { user1 : ?1, user2 : ?0 } ] }")
    Optional<FriendRequest> findOneByUserIds(ObjectId userId1, ObjectId userId2);

    @Query(value = "{ $or : [ { user1 : ?0, user2 : ?1 }, { user1 : ?1, user2 : ?0 } ] }", exists = true)
    boolean existsByUserIds(ObjectId userId1, ObjectId userId2);
}
