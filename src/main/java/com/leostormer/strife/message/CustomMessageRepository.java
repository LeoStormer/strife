package com.leostormer.strife.message;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.server.channel.Channel;
import com.leostormer.strife.user.User;

public interface CustomMessageRepository {
    Optional<DirectMessage> findDirectMessageById(ObjectId id);

    DirectMessage insertMessage(User user, Conversation conversation, String content);

    List<DirectMessage> getDirectMessages(ObjectId conversationId, MessageSearchOptions searchOptions);

    DirectMessage updateDirectMessage(ObjectId messageId, String messageContent);

    void deleteAllByConversation(ObjectId... conversationIds);

    boolean existsbyConversation(ObjectId conversationId);

    Optional<ChannelMessage> findChannelMessageById(ObjectId id);

    ChannelMessage insertMessage(User user, Channel channel, String content);

    List<ChannelMessage> getChannelMessages(ObjectId channelId, MessageSearchOptions searchOptions);

    ChannelMessage updateChannelMessage(ObjectId messageId, String messageContent);

    void deleteAllByChannel(ObjectId... channelIds);

    boolean existsByChannel(ObjectId channelId);
}
