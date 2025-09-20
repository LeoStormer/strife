package com.leostormer.strife.conversation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractIntegrationTest;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.friends.FriendRequest;
import com.leostormer.strife.friends.FriendRequestRepository;
import com.leostormer.strife.friends.FriendRequestResponse;
import com.leostormer.strife.message.DirectMessage;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchDirection;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;
import com.leostormer.strife.user.UserService;

public class ConversationServiceTests extends AbstractIntegrationTest {
    public static final int NUM_MESSAGES = 20;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    ConversationRepository conversationRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ConversationService conversationService;

    @Autowired
    UserService userService;

    static User user1;

    static User user2;

    static User user3;

    static User user4;

    Conversation conversation1;

    Conversation conversation2;

    @BeforeAll
    static void setupUsers(@Autowired UserService userService) {
        user1 = new User();
        user1.setUsername("User1");
        user1.setPassword("password123");
        user2 = new User();
        user2.setUsername("User2");
        user2.setPassword("password456");
        user3 = new User();
        user3.setUsername("User3");
        user3.setPassword("password789");
        user4 = new User();
        user4.setUsername("User4");
        user4.setPassword("password1234");

        user1 = userService.registerUser(user1);
        user2 = userService.registerUser(user2);
        user3 = userService.registerUser(user3);
        user4 = userService.registerUser(user4);
    }

    @AfterAll
    static void clearUsers(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @BeforeEach
    void setupRequests() {
        FriendRequest user1AndUser2AreFriends = new FriendRequest(null, user1, user2, FriendRequestResponse.ACCEPTED,
                FriendRequestResponse.ACCEPTED);
        FriendRequest user3BlockedUser1 = new FriendRequest(null, user3, user1, FriendRequestResponse.BLOCKED,
                FriendRequestResponse.PENDING);
        Conversation user3AndUser1ConversationBlocked = new Conversation(user3, user1, false, false, true);
        FriendRequest user1AndUser4AreFriends = new FriendRequest(null, user1, user4, FriendRequestResponse.ACCEPTED,
                FriendRequestResponse.ACCEPTED);
        friendRequestRepository.saveAll(List.of(user1AndUser2AreFriends, user3BlockedUser1, user1AndUser4AreFriends));

        conversationRepository.save(user3AndUser1ConversationBlocked);
        conversation1 = conversationRepository.save(new Conversation(user1, user2));
        conversation2 = conversationRepository.save(new Conversation(user2, user3));
    }

    @AfterEach
    void clearUsersAndRelationships() {
        friendRequestRepository.deleteAll();
        conversationRepository.deleteAll();
        messageRepository.deleteAll();
    }

    private DirectMessage[][] initializeMessages() {
        DirectMessage[] convo1Messages = new DirectMessage[NUM_MESSAGES];
        for (int i = 0; i < NUM_MESSAGES; i++) {
            DirectMessage message = new DirectMessage();
            message.setSender(i % 2 == 0 ? user1 : user2);
            message.setConversation(conversation1);
            message.setContent("This is a message in conversation 1 " + i);
            convo1Messages[i] = messageRepository.save(message);
        }

        DirectMessage[] convo2Messages = new DirectMessage[NUM_MESSAGES];
        for (int i = 0; i < NUM_MESSAGES; i++) {
            DirectMessage message = new DirectMessage();
            message.setSender(i % 2 == 0 ? user2 : user3);
            message.setConversation(conversation2);
            message.setContent("This is a message in conversation 2 " + i);
            convo2Messages[i] = messageRepository.save(message);
        }

        return new DirectMessage[][] { convo1Messages, convo2Messages };
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
        Conversation conversation = conversationService.startNewConversation(sender, receiver);
        assertNotNull(conversation);
        assertNotNull(conversation.getId());
        assertTrue(sender.getId().equals(conversation.getUser1().getId()));
        assertTrue(receiver.getId().equals(conversation.getUser2().getId()));
    }

    @Test
    void shouldStartConversationBetweenStrangers() {
        User sender = user2;
        User receiver = user4;
        Conversation conversation = conversationService.startNewConversation(sender, receiver);
        assertTrue(conversationRepository.existsById(conversation.getId()));
        assertTrue(sender.getId().equals(conversation.getUser1().getId()));
        assertTrue(receiver.getId().equals(conversation.getUser2().getId()));
    }

    @Test
    void shouldNotStartConversationBetweenBlockedUsers() {
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.startNewConversation(user1, user3);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.startNewConversation(user3, user1);
        });
    }

    @Test
    void shouldLeaveConversation() {
        conversationService.leaveConversation(user2, conversation1.getId());
        Conversation updatedConversation = conversationService.getConversationById(conversation1.getId()).get();
        assertFalse(updatedConversation.isUser2Participating());
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
        conversationService.leaveConversation(user1, conversation1.getId());
        Optional<Conversation> updatedConversation = conversationRepository.findById(conversation1.getId());
        assertTrue(updatedConversation.isPresent());
        assertFalse(updatedConversation.get().isUser1Participating());
        assertFalse(updatedConversation.get().isUser2Participating());
    }

    @Test
    void shouldNotLeaveConversationUserNotPartOf() {
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.leaveConversation(user3, conversation1.getId());
        });
    }

    @Test
    void shouldGetMessagesAfterTimestamp() {
        DirectMessage[][] conversationMessages = initializeMessages();
        int conversationIndex = 0;
        int messageIndex = (NUM_MESSAGES - 1) - 10;
        Date timestamp = conversationMessages[conversationIndex][messageIndex].getTimestamp();
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().timestamp(timestamp)
                .searchDirection(MessageSearchDirection.ASCENDING).build();
        List<DirectMessage> messages = conversationService.getMessages(user1, conversation1.getId(), searchOptions);

        assertTrue(messages.size() <= searchOptions.getLimit());
        assertTrue(messages.get(0).getTimestamp().compareTo(timestamp) >= 0);
    }

    @Test
    void shouldGetMessagesBeforeTimestamp() {
        DirectMessage[][] conversationMessages = initializeMessages();
        int conversationIndex = 0;
        int messageIndex = (NUM_MESSAGES - 1) - 10;
        Date timestamp = conversationMessages[conversationIndex][messageIndex].getTimestamp();
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().timestamp(timestamp)
                .searchDirection(MessageSearchDirection.DESCENDING).build();
        List<DirectMessage> messages = conversationService.getMessages(user1, conversation1.getId(), searchOptions);

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
        DirectMessage message = conversationService.sendMessage(user2, conversation1.getId(), "Hello");
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
        DirectMessage origianlMessage = messageRepository
                .getDirectMessages(conversation1.getId(), MessageSearchOptions.earliest())
                .get(0);
        String messageContent = "I can be anything";
        conversationService.editMessage(user1, conversation1.getId(), origianlMessage.getId(), messageContent);
        DirectMessage message = messageRepository.findDirectMessageById(origianlMessage.getId()).get();

        assertTrue(message.getContent().equals(messageContent));
        assertTrue(message.getConversation().getId().equals(conversation1.getId()));
        assertTrue(message.getId().equals(origianlMessage.getId()));
        assertTrue(message.getSender().getId().equals(origianlMessage.getSender().getId()));
        assertFalse(message.getContent().equals(origianlMessage.getContent()));
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
        conversationService.deleteMessage(user1, messageId);
        assertFalse(messageRepository.existsById(messageId));
    }

    @Test
    void shouldNotDeleteMessageIfUserIsNotSender() {
        initializeMessages();
        ObjectId messageId = conversationService
                .getMessages(user1, conversation1.getId(), MessageSearchOptions.earliest()).get(1).getId();
        assertThrows(UnauthorizedActionException.class, () -> {
            conversationService.deleteMessage(user1, messageId);
        });
    }
}
