package com.leostormer.strife.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.server_channel.ChannelUpdateOperation;
import com.leostormer.strife.server.server_channel.ServerChannel;

@Repository
public class CustomChannelRepositoryImpl implements CustomChannelRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<ServerChannel> findServerChannelById(ObjectId channelId) {
        return Optional
                .ofNullable(mongoTemplate.findOne(new Query(Criteria.where("_id").is(channelId)), ServerChannel.class));
    }

    @Override
    public List<ServerChannel> findAllByServerId(ObjectId serverId) {
        return mongoTemplate.find(new Query(Criteria.where("server").is(serverId)), ServerChannel.class);
    }

    @Override
    public List<ServerChannel> getVisibleServerChannels(ObjectId serverId, Member member) {
        if (member.isOwner()) { // Assumes the member is a member of the server
            return findAllByServerId(serverId);
        }

        Criteria isServerCriteria = Criteria.where("server").is(serverId);
        List<Integer> bitPositions = Permissions.getBitPositions(PermissionType.VIEW_CHANNELS,
                PermissionType.ADMINISTRATOR);

        List<Criteria> hasPermissions = new ArrayList<>();
        hasPermissions.add(Criteria.where("isPublic").is(true));
        hasPermissions
                .add(Criteria.where("userPermissions." + member.getUser().getId().toHexString()).bits().anySet(bitPositions));
        member.getRoleIds().forEach(id -> {
            hasPermissions.add(Criteria.where("rolePermissions." + id.toHexString()).bits().anySet(bitPositions));
        });

        Criteria critera = new Criteria().andOperator(isServerCriteria,
                new Criteria().orOperator(hasPermissions));
        Query query = new Query(critera);

        return mongoTemplate.find(query, ServerChannel.class);
    }

    @Override
    public void updateServerChannelSettings(ObjectId channelId, ChannelUpdateOperation operation) {
        Query query = new Query(Criteria.where("_id").is(channelId));
        mongoTemplate.update(ServerChannel.class).matching(query).apply(operation.toUpdateObject()).first();
    }

    @Override
    public void deleteAllByServer(ObjectId serverId) {
        mongoTemplate.remove(new Query(Criteria.where("server").is(serverId)), ServerChannel.class);
    }

    @Override
    public Optional<Conversation> findConversationById(Object conversationId) {
        return Optional.ofNullable(
                mongoTemplate.findOne(new Query(Criteria.where("_id").is(conversationId)), Conversation.class));
    }

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
    public Optional<Conversation> findConversationByUserIds(ObjectId... userIds) {
        return Optional.ofNullable(
                mongoTemplate.findOne(new Query(containsGivenUsersAndOnlyGivenUsers(userIds)), Conversation.class));
    }

    @Override
    public boolean conversationExistsByUserIds(ObjectId... userIds) {
        return mongoTemplate.exists(new Query(containsGivenUsersAndOnlyGivenUsers(userIds)), Conversation.class);
    }

    @Override
    public void lockUserDirectConversation(ObjectId userId, ObjectId otherUserId) {
        Query query = new Query(containsGivenUsersAndOnlyGivenUsers(userId, otherUserId));
        Map<ObjectId, Boolean> userPresenceMap = Map.of(userId, false, otherUserId, false);
        Update update = Update.update("locked", true).setOnInsert("numUsers", 2).setOnInsert("userPresenceMap",
                userPresenceMap);
        mongoTemplate.upsert(query, update, Conversation.class);
    }

    @Override
    public void unlockDirectConversation(ObjectId userId, ObjectId otherUserId) {
        Query query = new Query(containsGivenUsersAndOnlyGivenUsers(userId, otherUserId));
        Update update = Update.update("locked", false);
        mongoTemplate.updateFirst(query, update, Conversation.class);    
    }
}
