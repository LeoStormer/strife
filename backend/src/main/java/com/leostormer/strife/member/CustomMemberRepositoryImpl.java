package com.leostormer.strife.member;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.leostormer.strife.server.Server;

@Repository
public class CustomMemberRepositoryImpl implements CustomMemberRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<Member> findByUserIdAndServerId(ObjectId userId, ObjectId serverId) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId));
        return Optional.ofNullable(mongoTemplate
                .findOne(query, Member.class));
    }

    public boolean existsByUserIdAndServerId(ObjectId userId, ObjectId serverId) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId));
        return mongoTemplate.exists(query, Member.class);
    }

    @Override
    public List<Server> findServersByUserId(ObjectId userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("user").is(userId).and("isBanned").is(false)),
                Aggregation.lookup("servers", "server", "_id", "server"));

        return mongoTemplate.aggregate(aggregation, Member.class, Server.class).getMappedResults();
    }

    @Override
    public boolean isMember(ObjectId userId, ObjectId serverId) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId).and("isBanned").is(false));
        return mongoTemplate.exists(query, Member.class);
    }

    @Override
    public void removeMember(ObjectId userId, ObjectId serverId) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId));
        mongoTemplate.remove(query, Member.class);
    }

    @Override
    public void banMember(ObjectId userId, ObjectId serverId, String banReason) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId));
        Update update = new Update()
                .set("isBanned", true)
                .set("banReason", banReason);
        mongoTemplate.upsert(query, update, Member.class);
    }

    @Override
    public void changeNickname(ObjectId userId, ObjectId serverId, String newNickname) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId));
        Update update = new Update()
                .set("nickname", newNickname);
        mongoTemplate.updateFirst(query, update, Member.class);
    }

    @Override
    public void updateMemberRoles(ObjectId userId, ObjectId serverId, int rolePriority, long permissions,
            List<ObjectId> roleIds) {
        Query query = new Query(Criteria.where("server").is(serverId).and("user").is(userId));
        Update update = new Update()
                .set("rolePriority", rolePriority)
                .set("permissions", permissions)
                .set("roleIds", roleIds);
        mongoTemplate.updateFirst(query, update, Member.class);
    }
}
