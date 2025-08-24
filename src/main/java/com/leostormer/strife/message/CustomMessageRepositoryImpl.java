package com.leostormer.strife.message;

import java.util.List;
import java.util.Optional;

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
import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.user.User;

@Repository
public class CustomMessageRepositoryImpl implements CustomMessageRepository {
    @Autowired
    public MongoTemplate mongoTemplate;

    @Override
    public Optional<DirectMessage> findDirectMessageById(ObjectId id) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query().addCriteria(Criteria.where("_id").is(id).and("_class").is("DirectMessage")),
                DirectMessage.class));
    }

    @Override
    public DirectMessage insertMessage(User sender, Conversation conversation, String content) {
        DirectMessage message = new DirectMessage();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(content);

        return mongoTemplate.insert(message);
    }

    @Override
    public List<DirectMessage> getDirectMessages(ObjectId conversationId, MessageSearchOptions searchOptions) {
        Criteria criteria = Criteria.where("conversation").is(conversationId).and("timestamp");
        Sort sort = Sort.by("timestamp");
        Query query = new Query();
        if (searchOptions.getSearchDirection().equals(MessageSearchDirection.DESCENDING)) {
            query.addCriteria(criteria.lte(searchOptions.getTimestamp())).with(sort.descending());
        } else if (searchOptions.getSearchDirection().equals(MessageSearchDirection.ASCENDING)) {
            query.addCriteria(criteria.gte(searchOptions.getTimestamp())).with(sort.ascending());
        }
        query.limit(searchOptions.getLimit());

        return mongoTemplate.find(query, DirectMessage.class);
    }

    @Override
    public DirectMessage updateDirectMessage(ObjectId messageId, String messageContent) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(messageId));
        Update update = new Update().set("content", messageContent);

        return mongoTemplate.update(DirectMessage.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public void deleteAllByConversation(ObjectId... conversationIds) {
        mongoTemplate.remove(new Query().addCriteria(Criteria.where("conversation").in((Object[]) conversationIds)),
                DirectMessage.class);
    }

    @Override
    public boolean existsbyConversation(ObjectId conversationId) {
        return mongoTemplate.exists(new Query().addCriteria(Criteria.where("conversation").is(conversationId)),
                DirectMessage.class);
    }

    @Override
    public Optional<ChannelMessage> findChannelMessageById(ObjectId id) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query().addCriteria(Criteria.where("_id").is(id).and("_class").is("ChannelMessage")),
                ChannelMessage.class));
    }

    @Override
    public ChannelMessage insertMessage(User sender, Channel channel, String content) {
        ChannelMessage message = new ChannelMessage();
        message.setSender(sender);
        message.setChannel(channel);
        message.setContent(content);

        return mongoTemplate.insert(message);
    }

    @Override
    public List<ChannelMessage> getChannelMessages(ObjectId channelId, MessageSearchOptions searchOptions) {
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

        return mongoTemplate.find(query, ChannelMessage.class);
    }

    @Override
    public ChannelMessage updateChannelMessage(ObjectId messageId, String messageContent) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(messageId));
        Update update = new Update().set("content", messageContent);

        return mongoTemplate.update(ChannelMessage.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public void deleteAllByChannel(ObjectId... channelIds) {
        mongoTemplate.remove(
                new Query().addCriteria(
                        Criteria.where("channel").in((Object[]) channelIds)),
                ChannelMessage.class);
    }

    @Override
    public boolean existsByChannel(ObjectId channelId) {
        return mongoTemplate.exists(
                new Query()
                        .addCriteria(Criteria.where("channel").is(channelId)),
                ChannelMessage.class);
    }
}
