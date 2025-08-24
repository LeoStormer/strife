package com.leostormer.strife.channel;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CustomChannelRepositoryImpl implements CustomChannelRepository{
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public void updateChannelSettings(ObjectId channelId, ChannelUpdateOperation operation) {
        Query query = new Query(Criteria.where("_id").is(channelId));
        mongoTemplate.update(Channel.class).matching(query).apply(operation.toUpdateObject()).first();
    }
}
