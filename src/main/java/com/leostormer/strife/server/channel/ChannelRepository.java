package com.leostormer.strife.server.channel;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends MongoRepository<Channel, ObjectId>, CustomChannelRepository {
    List<Channel> findAllByServerId(ObjectId serverId);

    void deleteAllByServer(ObjectId serverId);
}
