package com.leostormer.strife.server;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.leostormer.strife.member.Member;
import com.leostormer.strife.member.MemberRepository;
import com.leostormer.strife.server.role.Role;

@Repository
public class CustomServerRepositoryImpl implements CustomServerRepository {
    @Autowired
    public MongoTemplate mongoTemplate;

    @Autowired
    public MemberRepository memberRepository;

    @Override
    public Optional<Member> getMember(ObjectId serverId, ObjectId userId) {
        return memberRepository.findByUserIdAndServerId(userId, serverId);
    }

    @Override
    public boolean isMember(ObjectId serverId, ObjectId userId) {
        return memberRepository.isMember(userId, serverId);
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
