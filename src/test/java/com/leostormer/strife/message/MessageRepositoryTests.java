package com.leostormer.strife.message;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
        User[] users = new User[4];
        for (int i = 0; i < 4; i++) {
            User user = new User();
            user.setUsername("User" + (i + 1));
            int passwordDigits = 123 * (i + 1);
            user.setPassword("password" + passwordDigits);
            users[i] = userRepository.save(user);
        }
        
        for (int j = 0; j < users.length - 1; j++) {
            Conversation conversation = new Conversation(users[j], users[j + 1], true, true);
            conversationRepository.save(conversation);
        }
    }

    @AfterAll
    static void clearUsers(@Autowired UserRepository userRepository, @Autowired ConversationRepository conversationRepository) {
        userRepository.deleteAll();
        conversationRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        User[] users = new User[4];
        for (int i = 0; i < users.length; i++) {
            users[i] = userRepository.findOneByUsername("User"+(i + 1)).get();
        }

        for (int i = 0; i < 10; i++) {
            DirectMessage message = new DirectMessage();
            message.setContent("This is a message! " + i);

            message.setSender(i % 2 == 0 ? users[0] : users[1]);
        }
    }

    @AfterEach
    void cleanup() {
        messageRepository.deleteAll();
    }

    @Test
    void shouldInsertDirectMessage() {
        User user4 = userRepository.findOneByUsername("User4").get();
        User user3 = userRepository.findOneByUsername("User3").get();
        Conversation conversation = conversationRepository.findByUserIds(user4.getId(), user3.getId()).get();
        String messageContent = "AAAAAAHHHHHH!";
        DirectMessage message = messageRepository.insertMessage(user4, conversation, messageContent);
        assertNotNull(message.getId());
        assertTrue(message.getContent().equals(messageContent));
    }

    @Test
    void shouldFindDirectMessageById() {

    }

    @Test
    void shouldGetDirectMessages() {

    }

    @Test
    void shouldUpdateDirectMessage() {

    }
    
    @Test
    void shouldDeleteAllByConversation() {

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
