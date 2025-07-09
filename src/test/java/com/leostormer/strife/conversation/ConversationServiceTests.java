package com.leostormer.strife.conversation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.friends.FriendRequest;
import com.leostormer.strife.friends.FriendRequestRepository;
import com.leostormer.strife.message.DirectMessage;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.message.UnauthorizedMessageActionException;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;
import com.leostormer.strife.user.UserService;

@SpringBootTest
@ActiveProfiles("test")
public class ConversationServiceTests {
    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    ConversationRepository conversationRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserService userService;

    @Autowired
    ConversationService conversationService;

    @BeforeEach
    void setup() {
        User user1 = new User();
        user1.setUsername("User1");
        user1.setPassword("password123");
        User user2 = new User();
        user2.setUsername("User2");
        user2.setPassword("password456");
        User user3 = new User();
        user3.setUsername("User3");
        user3.setPassword("password789");

        user1 = userService.registerUser(user1);
        user2 = userService.registerUser(user2);
        user3 = userService.registerUser(user3);
        FriendRequest frienedRequest = userService.sendFriendRequest(user1, user2.getId());
        userService.acceptFriendRequest(user2, frienedRequest.getId());
        userService.blockUser(user3, user1.getId());
    }

    @AfterEach
    void clearUsersAndRelationships() {
        userRepository.deleteAll();
        friendRequestRepository.deleteAll();
        conversationRepository.deleteAll();
        messageRepository.deleteAll();
    }

    private void initializeConversations() {
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        User user3 = userService.getUserByUsername("User3").get();

        conversationService.startNewConversation(user1, user2);
        conversationService.startNewConversation(user2, user3);
    }

    private void initializeConversationsAndMessages() {
        initializeConversations();
        long startTime = 100000;
        long secondsIncrement = 15;

        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        User user3 = userService.getUserByUsername("User3").get();
        Conversation convo1 = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();
        Conversation convo2 = conversationRepository.findByUserIds(user2.getId(), user3.getId()).get();

        for (int i = 0; i < 50; i++) {
            Date messageDate = new Date(startTime + secondsIncrement * i);
            DirectMessage message = new DirectMessage();
            message.setSender(i % 2 == 0 ? user1 : user2);
            message.setConversation(convo1);
            message.setContent("This is a message in conversation 1 " + i);
            message.setTimestamp(messageDate);
            messageRepository.save(message);
        }

        for (int i = 0; i < 50; i++) {
            Date messageDate = new Date(startTime + secondsIncrement * (i + 30));
            DirectMessage message = new DirectMessage();
            message.setSender(i % 2 == 0 ? user2 : user3);
            message.setConversation(convo2);
            message.setContent("This is a message in conversation 2 " + i);
            message.setTimestamp(messageDate);
            messageRepository.save(message);
        }
    }

    @Test
    void shouldGetAllUserConversations() {
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        User user3 = userService.getUserByUsername("User3").get();
        initializeConversations();
        // System.out.println("ABCDE: " + conversationService.getConversations(user1).size());
        assertTrue(conversationService.getConversations(user1).size() == 1);
        assertTrue(conversationService.getConversations(user2).size() == 2);
        assertTrue(conversationService.getConversations(user3).size() == 1);
    }

    @Test
    void shouldStartConversationBetweenFriends() {
        User sender = userService.getUserByUsername("User1").get();
        User receiver = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.startNewConversation(sender, receiver);
        assertNotNull(conversation);
        assertNotNull(conversation.getId());
        assertTrue(sender.getId().equals(conversation.getUser1().getId()));
        assertTrue(receiver.getId().equals(conversation.getUser2().getId()));
    }

    @Test
    void shouldStartConversationBetweenStrangers() {
        User sender = userService.getUserByUsername("User2").get();
        User receiver = userService.getUserByUsername("User3").get();
        Conversation conversation = conversationService.startNewConversation(sender, receiver);
        assertTrue(conversationRepository.existsById(conversation.getId()));
        assertTrue(sender.getId().equals(conversation.getUser1().getId()));
        assertTrue(receiver.getId().equals(conversation.getUser2().getId()));
    }

    @Test
    void shouldNotStartConversationBetweenBlockedUsers() {
        User user1 = userService.getUserByUsername("User1").get();
        User user3 = userService.getUserByUsername("User3").get();
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.startNewConversation(user1, user3);
        });
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.startNewConversation(user3, user1);
        });
    }

    @Test
    void shouldLeaveConversation() {
        initializeConversationsAndMessages();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        conversationService.leaveConversation(user2, conversation.getId());
        Conversation updatedConversation = conversationService.getConversationById(conversation.getId()).get();
        assertFalse(updatedConversation.isUser2Participating());
    }

    @Test
    void shouldNotLeaveConversationUserNotPartOf() {
        initializeConversations();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        User user3 = userService.getUserByUsername("User3").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.leaveConversation(user3, conversation.getId());
        });
    }

    @Test
    void shouldGetConversationEarliestMessages() {
        initializeConversationsAndMessages();
    }

    @Test
    void shouldGetConversationLatestMessages() {
        initializeConversationsAndMessages();
    }

    @Test
    void shouldGetMessagesAfterTimestamp() {
        initializeConversationsAndMessages();
    }

    @Test
    void shouldGetMessagesBeforeTimestamp() {
        initializeConversationsAndMessages();
    }

    @Test
    void shouldNotGetMessagesFromConversationUserNotPartOf() {
        initializeConversationsAndMessages();
    }

    @Test
    void shouldSendMessage() {
        initializeConversations();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        DirectMessage message = conversationService.sendMessage(user2, conversation.getId(), "Hello");
        assertTrue(messageRepository.existsById(message.getId()));
    }

    @Test
    void shouldNotSendMessageIfUserLeftConversation() {
        initializeConversations();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        ObjectId conversationId = conversationService.getConversationByUsers(user1, user2).get().getId();
        conversationService.leaveConversation(user2, conversationId);
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.sendMessage(user2, conversationId, "Hello as well");
        });
    }

    @Test
    void shouldNotSendMessageIfUserNotPartOfConversation() {
        initializeConversations();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        ObjectId conversationId = conversationService.getConversationByUsers(user1, user2).get().getId();
        User user3 = userService.getUserByUsername("User3").get();
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.sendMessage(user3, conversationId, "I can be anything");
        });
    }

    @Test
    void shouldNotSendMessageIfUserBlocked() {
        initializeConversations();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        userService.blockUser(user2, user1.getId());
        ObjectId conversationId = conversationService.getConversationByUsers(user1, user2).get().getId();
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.sendMessage(user1, conversationId, "I can be anything");
        });
        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.sendMessage(user2, conversationId, "I can be anything");
        });
    }

    @Test
    void shouldEditMessageIfUserisSender() {
        initializeConversationsAndMessages();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        System.out.println("ABCDE: "+ conversation.getId());
        DirectMessage origianlMessage = messageRepository.getMessages(conversation, MessageSearchOptions.earliest())
                .get(0);
        String messageContent = "I can be anything";
        conversationService.editMessage(user1, conversation.getId(), origianlMessage.getId(), messageContent);
        DirectMessage message = messageRepository.findDirectMessageById(origianlMessage.getId()).get();
        assertTrue(message.getContent().equals(messageContent));
        assertTrue(message.getConversation().getId().equals(conversation.getId()));
        assertTrue(message.getId().equals(origianlMessage.getId()));
        assertTrue(message.getSender().getId().equals(origianlMessage.getSender().getId()));
        assertFalse(message.getContent().equals(origianlMessage.getContent()));
    }

    @Test
    void shouldNotEditMessageIfUserIsNotSender() {
        initializeConversationsAndMessages();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        ObjectId messageId = messageRepository.insertMessage(user1, conversation, "I can be anything").getId();
        assertThrows(UnauthorizedMessageActionException.class, () -> {
            conversationService.editMessage(user2, conversation.getId(), messageId, "Any new message");
        });
    }

    @Test
    void shouldNotEditMessageIfUserIsBlocked() {
        initializeConversationsAndMessages();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        ObjectId messageId = messageRepository.insertMessage(user1, conversation, "I am being rude").getId();
        ObjectId messageId2 = messageRepository.insertMessage(user2, conversation, "I am being rude too").getId();
        userService.blockUser(user2, user1.getId());

        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.editMessage(user1, conversation.getId(), messageId, "Any new message");
        });

        assertThrows(UnauthorizedConversationActionException.class, () -> {
            conversationService.editMessage(user2, conversation.getId(), messageId2, "Any new message");
        });
    }

    @Test
    void shouldDeleteMessageIfUserIsSender() {
        initializeConversationsAndMessages();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        ObjectId messageId = conversationService.getMessages(user1, conversation.getId(), MessageSearchOptions.earliest()).get(0).getId();
        conversationService.deleteMessage(user1, messageId);
        assertFalse(messageRepository.existsById(messageId));
    }

    @Test
    void shouldNotDeleteMessageIfUserIsNotSender() {
        initializeConversationsAndMessages();
        User user1 = userService.getUserByUsername("User1").get();
        User user2 = userService.getUserByUsername("User2").get();
        Conversation conversation = conversationService.getConversationByUsers(user1, user2).get();
        ObjectId messageId = conversationService.getMessages(user1, conversation.getId(), MessageSearchOptions.earliest()).get(1).getId();
        assertThrows(UnauthorizedMessageActionException.class, () -> {conversationService.deleteMessage(user1, messageId);});
    }
}
