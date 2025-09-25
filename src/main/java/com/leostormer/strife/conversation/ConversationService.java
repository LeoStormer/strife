package com.leostormer.strife.conversation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.DirectMessage;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConversationService {
    @Autowired
    private final ConversationRepository conversationRepository;

    @Autowired
    private final MessageRepository messageRepository;

    private static final String CONVERSATION_NOT_FOUND = "Conversation not found";

    private static final String MESSAGE_NOT_FOUND = "Message not found";

    private static final String DEFAULT_UNAUTHORIZED_MESSAGE = "You are not authorized to act on this conversation";

    public Optional<Conversation> getConversationByUsers(User user1, User user2) {
        return conversationRepository.findByUserIds(user1.getId(), user2.getId());
    }

    public Optional<Conversation> getConversationById(ObjectId id) {
        return conversationRepository.findById(id);
    }

    public List<Conversation> getConversations(User user) {
        return conversationRepository.getAllConversationsWhereUserIsPresent(user.getId());
    }

    public void lockConversation(User user1, User user2) {
        Conversation conversation = getConversationByUsers(user1, user2)
                .orElse(new Conversation(true, List.of(user1, user2), List.of(false, false)));
        conversation.setLocked(true);
        conversationRepository.save(conversation);
    }

    public void unlockConversation(Conversation conversation) {
        conversation.setLocked(false);
        conversationRepository.save(conversation);
    }

    public void unlockConversation(User user1, User user2) {
        Optional<Conversation> optional = getConversationByUsers(user1, user2);
        if (optional.isPresent())
            unlockConversation(optional.get());
    }

    public Conversation startNewConversation(User user1, List<User> otherUsers) {
        if (otherUsers.stream().anyMatch(u -> u.getId().equals(user1.getId())))
            throw new UnauthorizedActionException("You cannot start a conversation with yourself");

        User[] usersInConversation = Stream.concat(otherUsers.stream(), Stream.of(user1)).toArray(User[]::new);
        Optional<Conversation> result = conversationRepository.findByUserIds(Stream.of(usersInConversation).map(u -> u.getId()).toArray(ObjectId[]::new));
        if (result.isEmpty())
            return conversationRepository.save(new Conversation(usersInConversation));

        Conversation conversation = result.get();
        if (conversation.isLocked())
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        if (conversation.isPresent(user1))
            throw new UnauthorizedActionException(
                    "You cannot start a conversation you're already participating in");

        conversation.setIsPresent(user1, true);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public void deleteConversation(ObjectId conversationId) {
        messageRepository.deleteAllByConversation(conversationId);
        conversationRepository.deleteById(conversationId);
    }

    public void leaveConversation(User user, ObjectId conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(CONVERSATION_NOT_FOUND));

        if (!conversation.isValidUser(user))
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        if (!conversation.isPresent(user))
            return;

        // make user leave conversation
        conversation.setIsPresent(user, false);

        if (conversation.isLocked() || conversation.isAnyUserPresent()) {
            conversationRepository.save(conversation);
        } else {
            deleteConversation(conversation.getId());
        }
    }

    public List<DirectMessage> getMessages(User user, ObjectId conversationId, MessageSearchOptions searchOptions) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(CONVERSATION_NOT_FOUND));

        if (!conversation.isValidUser(user))
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        List<DirectMessage> dms = messageRepository.getDirectMessages(conversationId, searchOptions);
        dms.sort((dm1, dm2) -> dm1.getTimestamp().compareTo(dm2.getTimestamp()));
        return dms;
    }

    public DirectMessage sendMessage(User sender, ObjectId conversationId, String messageContent) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(CONVERSATION_NOT_FOUND));

        if (conversation.isLocked() || !conversation.isValidUser(sender) || !conversation.isPresent(sender))
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        return messageRepository.insertMessage(sender, conversation, messageContent);
    }

    public DirectMessage editMessage(User sender, ObjectId conversationId, ObjectId messageId, String messageContent) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(CONVERSATION_NOT_FOUND));

        if (conversation.isLocked())
            throw new UnauthorizedActionException(DEFAULT_UNAUTHORIZED_MESSAGE);

        DirectMessage message = messageRepository.findDirectMessageById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(sender.getId()))
            throw new UnauthorizedActionException("User is not authorized to edit this message");

        return messageRepository.updateDirectMessage(messageId, messageContent);
    }

    public void deleteMessage(User sender, ObjectId messageId) {
        DirectMessage message = messageRepository.findDirectMessageById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(sender.getId()))
            throw new UnauthorizedActionException("User is not authorized to delete this message");

        messageRepository.delete(message);
    }
}
