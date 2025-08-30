package com.leostormer.strife.server.channel;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.member.Member;

public class CustomChannelRepositoryImpl implements CustomChannelRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void updateChannelSettings(ObjectId channelId, ChannelUpdateOperation operation) {
        Query query = new Query(Criteria.where("_id").is(channelId));
        mongoTemplate.update(Channel.class).matching(query).apply(operation.toUpdateObject()).first();
    }

    @Override
    public List<Channel> getVisibleChannels(ObjectId serverId, Member member) {
        Criteria isServerCriteria = Criteria.where("server").is(serverId);
        if (member.isOwner()) { // Assumes the member is a member of the server
            return mongoTemplate.find(new Query(isServerCriteria), Channel.class);
        }

        List<Integer> bitPositions = Permissions.getBitPositions(PermissionType.VIEW_CHANNELS,
                PermissionType.ADMINISTRATOR);

        List<Criteria> hasPermissions = new ArrayList<>();
        hasPermissions.add(Criteria.where("isPublic").is(true));
        hasPermissions
                .add(Criteria.where("userPermissions." + member.getUserId().toHexString()).bits().anySet(bitPositions));
        member.getRoleIds().forEach(id -> {
            hasPermissions.add(Criteria.where("rolePermissions." + id.toHexString()).bits().anySet(bitPositions));
        });

        Criteria critera = new Criteria().andOperator(isServerCriteria,
                new Criteria().orOperator(hasPermissions));
        Query query = new Query(critera);

        return mongoTemplate.find(query, Channel.class);
    }
}
