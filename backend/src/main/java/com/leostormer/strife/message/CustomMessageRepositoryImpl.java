package com.leostormer.strife.message;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.user.User;

@Repository
public class CustomMessageRepositoryImpl implements CustomMessageRepository {
    @Autowired
    public MongoTemplate mongoTemplate;

    @Override
    public Message insertMessage(User sender, Channel channel, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setChannel(channel);
        message.setContent(content);

        return mongoTemplate.insert(message);
    }

    @Override
    @SuppressWarnings("null")
    public List<Message> getMessages(ObjectId channelId, MessageSearchOptions searchOptions) {
        Criteria criteria = Criteria.where("channel").is(channelId)
                .and("timestamp");
        Sort sort = Sort.by("timestamp");
        Query query = new Query();
        if (searchOptions.getSearchDirection().equals(MessageSearchDirection.DESCENDING)) {
            query.addCriteria(criteria.lt(searchOptions.getTimestamp())).with(sort.descending());
        } else if (searchOptions.getSearchDirection().equals(MessageSearchDirection.ASCENDING)) {
            query.addCriteria(criteria.gt(searchOptions.getTimestamp())).with(sort.ascending());
        }
        query.limit(searchOptions.getLimit());

        return mongoTemplate.find(query, Message.class);
    }

    @Override
    public Message updateMessage(ObjectId messageId, String messageContent) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(messageId));
        Update update = new Update().set("content", messageContent);

        return mongoTemplate.update(Message.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    @SuppressWarnings("null")
    public void deleteAllByChannel(ObjectId... channelIds) {
        mongoTemplate.remove(
                new Query(Criteria.where("channel").in((Object[]) channelIds)), Message.class);
    }

    @Override
    public boolean existsByChannel(ObjectId channelId) {
        return mongoTemplate.exists(
                new Query(Criteria.where("channel").is(channelId)), Message.class);
    }
}
