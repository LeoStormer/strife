package com.leostormer.strife.channel;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.server_channel.ChannelUpdateOperation;
import com.leostormer.strife.server.server_channel.ServerChannel;

public interface CustomChannelRepository {
    /**
     * Finds the <code>ServerChannel</code> with the given id.
     * 
     * @param channelId
     * @return the channel with the given id or Optional#empty() if none found.
     */
    Optional<ServerChannel> findServerChannelById(ObjectId channelId);

    /**
     * Returns all <code>ServerChannel</code>s in the given server.
     * 
     * @param serverId
     * @return the list of channels
     */
    List<ServerChannel> findAllByServerId(ObjectId serverId);

    /**
     * Deletes all <code>ServerChannel</code>s in the given server.
     * 
     * @param serverId the id of the server
     */
    void deleteAllByServer(ObjectId serverId);

    /**
     * Performs an update operation on a <code>ServerChannel</code>.
     * 
     * @param serverChannelId
     * @param operation
     */
    public void updateServerChannelSettings(ObjectId serverChannelId, ChannelUpdateOperation operation);

    /**
     * Returns a list of <code>ServerChannel</code>'s that are visible to the given
     * member of a server.
     * 
     * @param serverId the id of the server
     * @param member
     * @return the list of channels
     */
    public List<ServerChannel> getVisibleServerChannels(ObjectId serverId, Member member);

    /**
     * Finds the <code>Conversation</code> with the given id.
     * 
     * @param channelId
     * @return the conversation with the given id or Optional#empty() if none found.
     */
    Optional<Conversation> findConversationById(Object conversationId);

    /**
     * Returns all <code>Conversation</code>s containing the given user.
     * 
     * @param userId the user's id
     * @return the list of conversations
     */
    List<Conversation> getAllUserConversations(ObjectId userId);

    /**
     * Returns all <code>Conversation</code>s containing the given user if the user
     * is present.
     * 
     * @param userId the user's id
     * @return the list of conversations
     */
    List<Conversation> getAllConversationsWhereUserIsPresent(ObjectId userId);

    /**
     * Finds the <code>Conversation</code> containing all given users and only the
     * given users.
     * 
     * @param userIds the users' ids
     * @return the conversation between all the users
     */
    Optional<Conversation> findConversationByUserIds(ObjectId... userIds);

    /**
     * Checks if the <code>Conversation</code> containing all given users and only
     * the given
     * users exists.
     * 
     * @param userIds the users' ids
     * @return whether such converation exists
     */
    boolean conversationExistsByUserIds(ObjectId... userIds);
}
