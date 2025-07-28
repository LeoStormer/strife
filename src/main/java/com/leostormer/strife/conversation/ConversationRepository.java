package com.leostormer.strife.conversation;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ConversationRepository extends MongoRepository<Conversation, ObjectId> {
    @Query("{ $or : [ { user1: ?0, user1Participating: true }, { user2: ?0, user2Participating: true } ] }")
    List<Conversation> getAllUserConversations(ObjectId userId);
    
    @Query("{ $or: [ { user1: ?0, user2: ?1 }, { user1: ?1, user2: ?0 } ] }")
    Optional<Conversation> findByUserIds(ObjectId user1Id, ObjectId user2Id);

    @Query(value = "{ $or: [ { user1: ?0, user2: ?1 }, { user1: ?1, user2: ?0 } ] }", exists = true)
    boolean existsByUserIds(ObjectId user1Id, ObjectId user2Id);
}
