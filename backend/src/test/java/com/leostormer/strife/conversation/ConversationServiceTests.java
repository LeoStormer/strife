package com.leostormer.strife.conversation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractIntegrationTest;
import com.leostormer.strife.TestUtils;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.Message;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchDirection;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;
import com.leostormer.strife.user.UserService;
import com.leostormer.strife.user.friends.FriendRequestRepository;

public class ConversationServiceTests extends AbstractIntegrationTest {
    public static final int NUM_MESSAGES = 20;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    ChannelRepository conversationRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ConversationService conversationService;

    @Autowired
    UserService userService;

    User user1;

    User user2;

    User user3;

    User user4;

    Conversation conversation1;

    Conversation conversation2;

    @BeforeEach
    void setupRequests() {
        user1 = TestUtils.createUser("User1", "password123", userRepository);
        user2 = TestUtils.createUser("User2", "password456", userRepository);
        user3 = TestUtils.createUser("User3", "password789", userRepository);
        user4 = TestUtils.createUser("User4", "password1234", userRepository);

        TestUtils.createAcceptedFriendship(user1, user2, userRepository, friendRequestRepository);

        TestUtils.createBlockedRelationship(user3, user1, userRepository, conversationRepository);

        TestUtils.createAcceptedFriendship(user1, user4, userRepository, friendRequestRepository);

        conversation1 = conversationRepository.save(new Conversation(user1, user2));
        conversation2 = conversationRepository.save(new Conversation(user2, user3));
    }

    @AfterEach
    void clearUsersAndRelationships() {
        userRepository.deleteAll();
        friendRequestRepository.deleteAll();
        conversationRepository.deleteAll();
        messageRepository.deleteAll();
    }

    private Message[][] initializeMessages() {
        Message[] convo1Messages = new Message[NUM_MESSAGES];
        for (int i = 0; i < NUM_MESSAGES; i++) {
            Message message = new Message();
            message.setSender(i % 2 == 0 ? user1 : user2);
            message.setChannel(conversation1);
            message.setContent("This is a message in conversation 1 " + i);
            convo1Messages[i] = messageRepository.save(message);
        }

        Message[] convo2Messages = new Message[NUM_MESSAGES];
        for (int i = 0; i < NUM_MESSAGES; i++) {
            Message message = new Message();
            message.setSender(i % 2 == 0 ? user2 : user3);
            message.setChannel(conversation2);
            message.setContent("This is a message in conversation 2 " + i);
            convo2Messages[i] = messageRepository.save(message);
        }

        return new Message[][] { convo1Messages, convo2Messages };
    }

    @Test
    void shouldGetAllUserConversations() {
        assertTrue(conversationService.getConversations(user1).size() == 1);
        assertTrue(conversationService.getConversations(user2).size() == 2);
        assertTrue(conversationService.getConversations(user3).size() == 1);
    }

    @Test
    void shouldStartConversationBetweenFriends() {
        User sender = user1;
        User receiver = user4;
        Conversation conversation = conversationService.startNewConversation(sender, List.of(receiver));
        assertTrue(conversationRepository.existsById(conversation.getId()));
        assertTrue(conversation.isPresent(sender));
        assertTrue(conversation.isPresent(receiver));
    }

    @Test
    void shouldStartConversationBetweenStrangers() {
        User sender = user2;
        User receiver = user4;
        Conversation conversation = conversationService.startNewConversation(sender, List.of(receiver));
        assertTrue(conversationRepository.existsById(conversation.getId()));
        assertTrue(conversation.isPresent(sender));
        assertTrue(conversation.isPresent(receiver));
    }

    @Test
    void shouldNotStartConversationBetweenBlockedUsers() {
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.startNewConversation(user3, List.of(user1));
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.startNewConversation(user1, List.of(user3));
        });
    }

    @Test
    void shouldLeaveConversation() {
        conversationService.leaveConversation(user2, conversation1.getId());
        Conversation updatedConversation = conversationService.getConversationById(conversation1.getId()).get();
        assertFalse(updatedConversation.isPresent(user2));
    }

    @Test
    void shouldLeaveConversationAndDelete() {
        conversationService.leaveConversation(user2, conversation1.getId());
        conversationService.leaveConversation(user1, conversation1.getId());
        assertFalse(conversationRepository.existsById(conversation1.getId()));
    }

    @Test
    void shouldLeaveConversationAndKeepIfLocked() {
        conversationService.leaveConversation(user2, conversation1.getId());
        userService.blockUser(user2, user1.getId());
        assertTrue(conversationRepository.existsById(conversation1.getId()));
        conversationService.leaveConversation(user1, conversation1.getId());
        Optional<Conversation> updatedConversation = conversationRepository.findConversationById(conversation1.getId());
        assertTrue(updatedConversation.isPresent());
        assertFalse(updatedConversation.get().isPresent(user2));
        assertFalse(updatedConversation.get().isPresent(user1));
    }

    @Test
    void shouldNotLeaveConversationUserNotPartOf() {
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.leaveConversation(user3, conversation1.getId());
        });
    }

    @Test
    void shouldGetMessagesAfterTimestamp() {
        Message[][] conversationMessages = initializeMessages();
        int conversationIndex = 0;
        int messageIndex = (NUM_MESSAGES - 1) - 10;
        Date timestamp = conversationMessages[conversationIndex][messageIndex].getTimestamp();
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().timestamp(timestamp)
                .searchDirection(MessageSearchDirection.ASCENDING).build();
        List<Message> messages = conversationService.getMessages(user1, conversation1.getId(), searchOptions);

        assertTrue(messages.size() <= searchOptions.getLimit());
        assertTrue(messages.get(0).getTimestamp().compareTo(timestamp) >= 0);
    }

    @Test
    void shouldGetMessagesBeforeTimestamp() {
        Message[][] conversationMessages = initializeMessages();
        int conversationIndex = 0;
        int messageIndex = (NUM_MESSAGES - 1) - 10;
        Date timestamp = conversationMessages[conversationIndex][messageIndex].getTimestamp();
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().timestamp(timestamp)
                .searchDirection(MessageSearchDirection.DESCENDING).build();
        List<Message> messages = conversationService.getMessages(user1, conversation1.getId(), searchOptions);

        assertTrue(messages.size() <= searchOptions.getLimit());
        assertTrue(messages.get(messages.size() - 1).getTimestamp().compareTo(timestamp) <= 0);
    }

    @Test
    void shouldNotGetMessagesFromConversationUserNotPartOf() {
        initializeMessages();
        MessageSearchOptions searchOptions = MessageSearchOptions.builder()
                .searchDirection(MessageSearchDirection.DESCENDING).build();
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.getMessages(user3, conversation1.getId(), searchOptions);
        });
    }

    @Test
    void shouldSendMessage() {
        Message message = conversationService.sendMessage(user2, conversation1.getId(), "Hello");
        assertTrue(messageRepository.existsById(message.getId()));
    }

    @Test
    void shouldNotSendMessageIfUserLeftConversation() {
        ObjectId conversationId = conversation1.getId();
        conversationService.leaveConversation(user2, conversationId);
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.sendMessage(user2, conversationId, "Hello as well");
        });
    }

    @Test
    void shouldNotSendMessageIfUserNotPartOfConversation() {
        ObjectId conversationId = conversation1.getId();
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.sendMessage(user3, conversationId, "I can be anything");
        });
    }

    @Test
    void shouldNotSendMessageIfUserBlocked() {
        userService.blockUser(user2, user1.getId());
        ObjectId conversationId = conversationService.getConversationByUsers(user1, user2).get().getId();
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.sendMessage(user1, conversationId, "I can be anything");
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.sendMessage(user2, conversationId, "I can be anything");
        });
    }

    @Test
    void shouldEditMessageIfUserisSender() {
        initializeMessages();
        Message originalMessage = messageRepository
                .getMessages(conversation1.getId(), MessageSearchOptions.earliest())
                .get(0);
        String messageContent = "I can be anything";
        conversationService.editMessage(user1, conversation1.getId(), originalMessage.getId(), messageContent);
        Message message = messageRepository.findById(originalMessage.getId()).get();

        assertTrue(message.getContent().equals(messageContent));
        assertTrue(message.getChannel().getId().equals(conversation1.getId()));
        assertTrue(message.getId().equals(originalMessage.getId()));
        assertTrue(message.getSender().getId().equals(originalMessage.getSender().getId()));
        assertFalse(message.getContent().equals(originalMessage.getContent()));
    }

    @Test
    void shouldNotEditMessageIfUserIsNotSender() {
        initializeMessages();
        ObjectId messageId = messageRepository.insertMessage(user1, conversation1, "I can be anything").getId();
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.editMessage(user2, conversation1.getId(), messageId, "Any new message");
        });
    }

    @Test
    void shouldNotEditMessageIfUserIsBlocked() {
        initializeMessages();
        ObjectId messageId = messageRepository.insertMessage(user1, conversation1, "I am being rude").getId();
        ObjectId messageId2 = messageRepository.insertMessage(user2, conversation1, "I am being rude too").getId();
        userService.blockUser(user2, user1.getId());

        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.editMessage(user1, conversation1.getId(), messageId, "Any new message");
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.editMessage(user2, conversation1.getId(), messageId2, "Any new message");
        });
    }

    @Test
    void shouldDeleteMessageIfUserIsSender() {
        initializeMessages();
        ObjectId messageId = conversationService
                .getMessages(user1, conversation1.getId(), MessageSearchOptions.earliest()).get(0).getId();
        conversationService.deleteMessage(user1, conversation1.getId(), messageId);
        assertFalse(messageRepository.existsById(messageId));
    }

    @Test
    void shouldNotDeleteMessageIfUserIsNotSender() {
        initializeMessages();
        ObjectId messageId = conversationService
                .getMessages(user1, conversation1.getId(), MessageSearchOptions.earliest()).get(1).getId();
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.deleteMessage(user1, conversation1.getId(), messageId);
        });
    }
}
