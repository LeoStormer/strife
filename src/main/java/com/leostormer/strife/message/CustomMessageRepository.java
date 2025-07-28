package com.leostormer.strife.message;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.server.Channel;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.user.User;

public interface CustomMessageRepository {
    Optional<DirectMessage> findDirectMessageById(ObjectId id);
    DirectMessage insertMessage(User user, Conversation conversation, String content);
    List<DirectMessage> getMessages(Conversation conversation, MessageSearchOptions searchOptions);
    DirectMessage updateDirectMessage(ObjectId id, String messageContent);
    void deleteAllByConversation(Conversation... conversations);
    boolean existsbyConversation(Conversation conversation);

    Optional<ChannelMessage> findChannelMessageById(ObjectId id);
    ChannelMessage insertMessage(User user, Server server, Channel channel, String content);
    List<ChannelMessage> getMessages(Server server, Channel channel, MessageSearchOptions searchOptions);
    ChannelMessage updateChannelMessage(ObjectId id, String messageContent);
    void deleteAllByChannel(Server server, Channel...  channels);
    boolean existsByChannel(Server server, Channel channel);
}
