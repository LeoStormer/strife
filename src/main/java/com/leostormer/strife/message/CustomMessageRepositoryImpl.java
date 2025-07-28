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

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.server.Channel;
import com.leostormer.strife.server.Server;
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
    public List<DirectMessage> getMessages(Conversation conversation, MessageSearchOptions searchOptions) {
        Criteria criteria = Criteria.where("conversation").is(conversation.getId()).and("timestamp");
        Sort sort = Sort.by("timestamp");
        Query query = new Query();
        if (searchOptions.getSearchDirection().equals(MessageSearchDirection.DESCENDING)) {
            query.addCriteria(criteria.lt(searchOptions.getTimestamp())).with(sort.descending());
        } else if (searchOptions.getSearchDirection().equals(MessageSearchDirection.ASCENDING)) {
            query.addCriteria(criteria.gt(searchOptions.getTimestamp())).with(sort.ascending());
        }
        query.limit(searchOptions.getLimit());

        return mongoTemplate.find(query, DirectMessage.class);
    }

    @Override
    public DirectMessage updateDirectMessage(ObjectId id, String messageContent) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        Update update = new Update().set("content", messageContent);

        return mongoTemplate.update(DirectMessage.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public void deleteAllByConversation(Conversation... conversations) {
        ObjectId[] ids = new ObjectId[conversations.length];
        for (int i = 0; i < conversations.length; i++) {
            ids[i] = conversations[i].getId();
        }

        mongoTemplate.remove(new Query().addCriteria(Criteria.where("conversation").in((Object[]) ids)),
                DirectMessage.class);
    }

    @Override
    public boolean existsbyConversation(Conversation conversation) {
        return mongoTemplate.exists(new Query().addCriteria(Criteria.where("conversation").is(conversation.getId())),
                DirectMessage.class);
    }

    @Override
    public Optional<ChannelMessage> findChannelMessageById(ObjectId id) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query().addCriteria(Criteria.where("_id").is(id).and("_class").is("ChannelMessage")),
                ChannelMessage.class));
    }

    @Override
    public ChannelMessage insertMessage(User sender, Server server, Channel channel, String content) {
        ChannelMessage message = new ChannelMessage();
        message.setSender(sender);
        message.setServer(server);
        message.setChannel(channel.getName());
        message.setContent(content);

        return mongoTemplate.insert(message);
    }

    @Override
    public List<ChannelMessage> getMessages(Server server, Channel channel, MessageSearchOptions searchOptions) {
        Criteria criteria = Criteria.where("server").is(server.getId()).and("channel").is(channel.getName())
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
    public ChannelMessage updateChannelMessage(ObjectId id, String messageContent) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        Update update = new Update().set("content", messageContent);

        return mongoTemplate.update(ChannelMessage.class).matching(query).apply(update)
                .withOptions(FindAndModifyOptions.options().returnNew(true)).findAndModifyValue();
    }

    @Override
    public void deleteAllByChannel(Server server, Channel... channels) {
        String[] channelNames = new String[channels.length];
        for (int i = 0; i < channels.length; i++) {
            channelNames[i] = channels[i].getName();
        }

        mongoTemplate.remove(
                new Query().addCriteria(
                        Criteria.where("server").is(server.getId()).and("channel").in((Object[]) channelNames)),
                ChannelMessage.class);
    }

    @Override
    public boolean existsByChannel(Server server, Channel channel) {
        return mongoTemplate.exists(
                new Query()
                        .addCriteria(Criteria.where("server").is(server.getId()).and("channel").is(channel.getName())),
                ChannelMessage.class);
    }
}
