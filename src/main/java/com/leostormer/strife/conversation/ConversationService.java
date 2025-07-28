package com.leostormer.strife.conversation;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leostormer.strife.message.DirectMessage;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.message.UnauthorizedMessageActionException;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConversationService {
    @Autowired
    private final ConversationRepository conversationRepository;

    @Autowired
    private final MessageRepository messageRepository;

    public Optional<Conversation> getConversationByUsers(User user1, User user2) {
        return conversationRepository.findByUserIds(user1.getId(), user2.getId());
    }

    public Optional<Conversation> getConversationById(ObjectId id) {
        return conversationRepository.findById(id);
    }

    public List<Conversation> getConversations(User user) {
        return conversationRepository.getAllUserConversations(user.getId());
    }

    public void lockConversation(Conversation conversation) {
        conversation.setLocked(true);
        conversationRepository.save(conversation);
    }

    public void lockConversation(User user1, User user2) {
        Conversation conversation = getConversationByUsers(user1, user2)
                .orElse(new Conversation(user1, user2, false, false));
        lockConversation(conversation);
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

    public Conversation startNewConversation(User user1, User user2) {
        if (user1.getId().equals(user2.getId()))
            throw new UnauthorizedConversationActionException("You cannot start a conversation with yourself");

        Optional<Conversation> result = conversationRepository.findByUserIds(user1.getId(), user2.getId());
        if (result.isEmpty())
            return conversationRepository.save(new Conversation(user1, user2, true, true));

        Conversation conversation = result.get();
        if (conversation.isLocked())
            throw new UnauthorizedConversationActionException();

        if (conversation.isUserParticipating(user1))
            throw new UnauthorizedConversationActionException(
                    "You cannot start a conversation you're already participating in");

        conversation.setUserParticipating(user1, true);
        return conversationRepository.save(conversation);
    }

    public void leaveConversation(User user, ObjectId conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(ConversationNotFoundException::new);
        if (!conversation.isValidUser(user))
            throw new UnauthorizedConversationActionException();

        if (!conversation.isUserParticipating(user))
            return;

        // make user leave conversation
        conversation.setUserParticipating(user, false);

        if (conversation.isLocked() || conversation.isUser1Participating() || conversation.isUser2Participating()) {
            conversationRepository.save(conversation);
        } else {
            delete(conversation);
        }
    }

    public List<DirectMessage> getMessages(User user, ObjectId conversationId, MessageSearchOptions searchOptions) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(ConversationNotFoundException::new);
        if (!conversation.isValidUser(user))
            throw new UnauthorizedConversationActionException();

        List<DirectMessage> dms = messageRepository.getMessages(conversation, searchOptions);
        dms.sort((dm1, dm2) -> dm1.getTimestamp().compareTo(dm2.getTimestamp()));
        return dms;
    }

    public DirectMessage sendMessage(User sender, ObjectId conversationId, String messageContent) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(ConversationNotFoundException::new);

        if (conversation.isLocked() || !conversation.isValidUser(sender) || !conversation.isUserParticipating(sender))
            throw new UnauthorizedConversationActionException();

        return messageRepository.insertMessage(sender, conversation, messageContent);
    }

    public DirectMessage editMessage(User sender, ObjectId conversationId, ObjectId messageId, String messageContent) {
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        if (conversation.isEmpty() || conversation.get().isLocked())
            throw new UnauthorizedConversationActionException();

        DirectMessage message = messageRepository.findDirectMessageById(messageId).orElseThrow();
        if (!message.getSender().getId().equals(sender.getId()))
            throw new UnauthorizedMessageActionException();

        return messageRepository.updateDirectMessage(messageId, messageContent);
    }

    public void deleteAll(List<Conversation> conversations) {
        messageRepository.deleteAllByConversation(conversations.toArray(new Conversation[conversations.size()]));
        conversationRepository.deleteAllById(conversations.stream().map(conversation -> conversation.getId()).toList());
    }

    public void deleteAllConversationsByUser(User user) {
        deleteAll(conversationRepository.getAllUserConversations(user.getId()));
    }

    public void delete(Conversation... conversations) {
        messageRepository.deleteAllByConversation(conversations);
        for (Conversation conversation : conversations) {
            conversationRepository.delete(conversation);
        }
    }

    public void deleteMessage(User sender, ObjectId messageId) {
        DirectMessage message = messageRepository.findDirectMessageById(messageId).orElseThrow();
        if (!message.getSender().getId().equals(sender.getId()))
            throw new UnauthorizedMessageActionException();

        messageRepository.delete(message);
    }
}
