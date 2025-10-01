package com.leostormer.strife.message;

import java.util.List;

import org.bson.types.ObjectId;

import com.leostormer.strife.channel.Channel;

import com.leostormer.strife.user.User;

public interface CustomMessageRepository {
    /**
     * Returns all messages associated with a given <code>Channel</code> using the given search options.
     * @param channelId the channel's id
     * @param searchOptions a MessageSearchOptions Object
     * @return 
     * @see MessageSearchOptions
     */
    List<Message> getMessages(ObjectId channelId, MessageSearchOptions searchOptions);

    /**
     * Creates a message with the given content associated with the given channel.
     * @param user the {@link User} sending the message
     * @param channel the {@link Channel}
     * @param content the content of the message
     * @return the saved message
     */
    Message insertMessage(User user, Channel channel, String content);

    /**
     * Edits the content of the message with the given id if found.
     * @param messageId the message's id
     * @param messageContent the content to update with
     * @return the saved message with its new state
     */
    Message updateMessage(ObjectId messageId, String messageContent);

    /**
     * Deletes all messages associated with any of the given channels.
     * @param channelIds an array of channel ids
     */
    void deleteAllByChannel(ObjectId... channelIds);

    /**
     * Checks if any message exists that is associated with the given channel.
     * @param channelId the channel's id
     * @return if any message exists
     */
    boolean existsByChannel(ObjectId channelId);
}
