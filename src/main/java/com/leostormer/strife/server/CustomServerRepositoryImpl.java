package com.leostormer.strife.server;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;

@Repository
public class CustomServerRepositoryImpl implements CustomServerRepository {
    @Autowired
    public MongoTemplate mongoTemplate;
    private static String[] memberClassFields = Stream.of(Member.class.getDeclaredFields())
            .map(f -> "members." + f.getName()).toArray(String[]::new);

    @Override
    public Optional<Member> getMember(ObjectId serverId, ObjectId userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(serverId)),
                Aggregation.unwind("members"),
                Aggregation.match(Criteria.where("members.userId").is(userId)),
                Aggregation.project(memberClassFields));

        AggregationResults<Member> results = mongoTemplate.aggregate(aggregation, Server.class, Member.class);
        return Optional.ofNullable(results.getUniqueMappedResult());
    }

    @Override
    public Server addMember(ObjectId serverId, Member... members) {
        Query query = new Query(Criteria.where("_id").is(serverId));
        Update update = new Update().push("members").each((Object[]) members);

        return mongoTemplate.update(Server.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public Server updateMember(ObjectId serverId, Member... members) {
        removeMember(serverId, Stream.of(members).map(m -> m.getUserId()).toArray(ObjectId[]::new));
        return addMember(serverId, members);
    }

    @Override
    public Server removeMember(ObjectId serverId, ObjectId... userIds) {
        Query query = new Query(Criteria.where("_id").is(serverId));
        Update update = new Update().pull("members", new Query(Criteria.where("userId").in((Object[]) userIds)));

        return mongoTemplate.update(Server.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public boolean isMember(ObjectId serverId, ObjectId userId) {
        Query query = new Query(Criteria.where("_id").is(serverId)
                .and("members").elemMatch(Criteria.where("userId").is(userId).and("isBanned").is(false)));
        return mongoTemplate.exists(query, Server.class);
    }

    @Override
    public Server updateRoles(ObjectId serverId, Map<ObjectId, Role> roles) {
        Query query = new Query(Criteria.where("_id").is(serverId));
        Update update = new Update().set("roles", roles);

        return mongoTemplate.update(Server.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public Server updateServerDetails(ObjectId serverId, String name, String description) {
        Query query = new Query(Criteria.where("_id").is(serverId));
        Update update = new Update();

        if (name != null)
            update = update.set("name", name);

        if (description != null)
            update = update.set("description", description);

        return mongoTemplate.update(Server.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }
}
