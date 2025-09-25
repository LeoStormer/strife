package com.leostormer.strife.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CustomConversationRepositoryImpl implements CustomConversationRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Conversation> getAllUserConversations(ObjectId userId) {
        return mongoTemplate.find(new Query(Criteria.where("userPresenceMap." + userId.toHexString()).exists(true)),
                Conversation.class);
    }

    @Override
    public List<Conversation> getAllConversationsWhereUserIsPresent(ObjectId userId) {
        return mongoTemplate.find(new Query(Criteria.where("userPresenceMap." + userId.toHexString()).is(true)),
                Conversation.class);
    }

    private Criteria containsGivenUsersAndOnlyGivenUsers(ObjectId... userIds) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("numUsers").is(userIds.length));
        for (ObjectId id : userIds) {
            criteriaList.add(Criteria.where("userPresenceMap." + id.toHexString()).exists(true));
        }

        return new Criteria().andOperator(criteriaList);
    }

    @Override
    public Optional<Conversation> findByUserIds(ObjectId... userIds) {
        return Optional.ofNullable(
                mongoTemplate.findOne(new Query(containsGivenUsersAndOnlyGivenUsers(userIds)), Conversation.class));
    }

    @Override
    public boolean existsByUserIds(ObjectId... userIds) {
        return mongoTemplate.exists(new Query(containsGivenUsersAndOnlyGivenUsers(userIds)), Conversation.class);
    }
}
