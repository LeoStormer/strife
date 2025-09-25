package com.leostormer.strife.conversation;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

public interface CustomConversationRepository {
    /**
     * Returns all conversations containing the given user.
     * 
     * @param userId the user's id
     * @return the list of conversations
     */
    List<Conversation> getAllUserConversations(ObjectId userId);

    /**
     * Returns all conversations containing the given user if the user is present.
     * 
     * @param userId the user's id
     * @return the list of conversations
     */
    List<Conversation> getAllConversationsWhereUserIsPresent(ObjectId userId);

    /**
     * Finds the conversation containing all given users and only the given users.
     * 
     * @param userIds the users' ids
     * @return the conversation between all the users
     */
    Optional<Conversation> findByUserIds(ObjectId... userIds);

    /**
     * Checks if the conversation containing all given users and only the given
     * users exists.
     * 
     * @param userIds the users' ids
     * @return whether such converation exists
     */
    boolean existsByUserIds(ObjectId... userIds);
}
