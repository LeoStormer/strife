package com.leostormer.strife.message;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.conversation.ConversationRepository;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

@DataMongoTest
@ActiveProfiles("test")
public class MessageRepositoryTests {
    @Autowired
    public MessageRepository messageRepository;

    @Autowired
    public ConversationRepository conversationRepository;

    @Autowired
    public UserRepository userRepository;

    @BeforeAll
    static void setupUsers(@Autowired UserRepository userRepository, @Autowired ConversationRepository conversationRepository) {
        User[] users = new User[3];
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setUsername("User" + (i + 1));
            int passwordDigits = 123 * (i + 1);
            user.setPassword("password" + passwordDigits);
            users[i] = userRepository.save(user);
        }
        
        Conversation convo1 = new Conversation(users[0], users[1], true, true);
        Conversation convo2 = new Conversation(users[1], users[2], true , true);
        Conversation convo3 = new Conversation(users[0], users[2], true, false);
        conversationRepository.saveAll(List.of(convo1, convo2, convo3));
    }

    @AfterAll
    static void clearUsers(@Autowired UserRepository userRepository, @Autowired ConversationRepository conversationRepository) {
        userRepository.deleteAll();
        conversationRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        Conversation conversation = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();

        for (int i = 0; i < 10; i++) {
            DirectMessage message = new DirectMessage();
            message.setConversation(conversation);
            message.setSender(i % 2 == 0 ? user1 : user2);
            message.setContent("This is a message! " + i);
            messageRepository.save(message);
        }
    }

    @AfterEach
    void cleanup() {
        messageRepository.deleteAll();
    }

    @Test
    void shouldInsertDirectMessage() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        Conversation conversation = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();
        String messageContent = "AAAAAAHHHHHH!";
        DirectMessage message = messageRepository.insertMessage(user1, conversation, messageContent);
        assertNotNull(message.getId());
        assertTrue(message.getContent().equals(messageContent));
    }

    @Test
    void shouldFindDirectMessageById() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        Conversation conversation = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();
        DirectMessage message = messageRepository.insertMessage(user1, conversation, "This is a new message!");

        Optional<DirectMessage> message2 = messageRepository.findDirectMessageById(message.getId());
        assertTrue(message2.isPresent());
        assertTrue(message2.get().getId().equals(message.getId()));
        assertTrue(message2.get().getContent().equals(message.getContent()));
    }

    @Test
    void shouldGetDirectMessages() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        Conversation conversation = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();

        MessageSearchOptions searchOptions = MessageSearchOptions.builder().build();
        List<DirectMessage> messages = messageRepository.getMessages(conversation, searchOptions);
        assertTrue(messages.size() == 10);
        assertTrue(messages.get(0).getContent().equals("This is a message! 0"));
    }

    @Test
    void shouldUpdateDirectMessage() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        Conversation conversation = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();
        DirectMessage message = messageRepository.insertMessage(user1, conversation, "This is a new message!");
        DirectMessage updatedMessage = messageRepository.updateDirectMessage(message.getId(), "I have been updated");
        assertTrue(message.getId().equals(updatedMessage.getId()));
        assertFalse(message.getContent().equals(updatedMessage.getContent()));
    }
    
    @Test
    void shouldDeleteAllByConversation() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        Conversation conversation = conversationRepository.findByUserIds(user1.getId(), user2.getId()).get();
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().build();
        assertTrue(messageRepository.getMessages(conversation, searchOptions).size() == 10);
        messageRepository.deleteAllByConversation(conversation);
        assertTrue(messageRepository.getMessages(conversation, searchOptions).size() == 0);
    }

    @Test
    void shouldInsertChannelMessage() {

    }

    @Test
    void shouldFindChannelMessageById() {

    }

    @Test
    void shouldGetChannelMessages() {

    }

    @Test
    void shouldUpdateChannelMessage() {

    }

    @Test
    void shouldDeleteAllByChannel() {

    }
}
