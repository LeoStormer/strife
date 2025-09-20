package com.leostormer.strife.server.invite;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.leostormer.strife.server.Server;

@Repository
public interface InviteRepository extends MongoRepository<Invite, String> {
    List<Invite> findAllByServer(Server server);
    List<Invite> findAllByServer(ObjectId serverId);
}
