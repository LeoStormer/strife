package com.leostormer.strife.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.conversation.ConversationRepository;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

@DataMongoTest
@ActiveProfiles("test")
public class MessageRepositoryTests {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ConversationRepository conversationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChannelRepository channelRepository;

    static ObjectId channelId;

    static ObjectId conversationId;

    ObjectId existingDirectMessageId;

    ObjectId existingChannelMessageId;

    @BeforeAll
    static void setupUsers(@Autowired UserRepository userRepository,
            @Autowired ConversationRepository conversationRepository, @Autowired ChannelRepository channelRepository) {
        User[] users = new User[3];
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setUsername("User" + (i + 1));
            int passwordDigits = 123 * (i + 1);
            user.setPassword("password" + passwordDigits);
            users[i] = userRepository.save(user);
        }

        Conversation conversation = new Conversation(users[0], users[1], true, true);
        conversation = conversationRepository.save(conversation);
        conversationId = conversation.getId();

        Channel channel = new Channel();
        channel.setName("Test");
        channelId = channelRepository.save(channel).getId();
    }

    @AfterAll
    static void clearUsers(@Autowired UserRepository userRepository,
            @Autowired ConversationRepository conversationRepository, @Autowired ChannelRepository channelRepository) {
        userRepository.deleteAll();
        conversationRepository.deleteAll();
        channelRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        User[] users = { userRepository.findOneByUsername("User1").get(),
                userRepository.findOneByUsername("User2").get(),
                userRepository.findOneByUsername("User3").get() };
        Conversation conversation = conversationRepository.findById(conversationId).get();

        DirectMessage existingDirectMessage = new DirectMessage();
        existingDirectMessage.setConversation(conversation);
        existingDirectMessage.setSender(users[0]);
        existingDirectMessage.setContent("This is a message! 0");
        existingDirectMessageId = messageRepository.save(existingDirectMessage).getId();

        for (int i = 1; i < 10; i++) {
            DirectMessage message = new DirectMessage();
            message.setConversation(conversation);
            message.setSender(users[i % 2]);
            message.setContent("This is a message! " + i);
            messageRepository.save(message);
        }

        Channel channel = channelRepository.findById(channelId).get();
        ChannelMessage existingChannelMessage = new ChannelMessage();
        existingChannelMessage.setChannel(channel);
        existingChannelMessage.setContent("This is a message! 0");
        existingChannelMessage.setSender(users[0]);
        existingChannelMessageId = messageRepository.save(existingChannelMessage).getId();

        for (int i = 1; i < 10; i++) {
            ChannelMessage message = new ChannelMessage();
            message.setChannel(channel);
            message.setSender(users[i % 3]);
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
        Conversation conversation = conversationRepository.findById(conversationId).get();
        String messageContent = "AAAAAAHHHHHH!";
        DirectMessage message = messageRepository.insertMessage(user1, conversation, messageContent);
        assertTrue(messageRepository.existsById(message.getId()));
        assertTrue(message.getContent().equals(messageContent));
    }

    @Test
    void shouldFindDirectMessageById() {
        User user1 = userRepository.findOneByUsername("User1").get();
        Optional<DirectMessage> messageFromRepository = messageRepository
                .findDirectMessageById(existingDirectMessageId);
        assertTrue(messageFromRepository.isPresent());
        assertEquals(user1.getId(), messageFromRepository.get().getSender().getId());
        assertEquals("This is a message! 0", messageFromRepository.get().getContent());
        assertEquals(conversationId, messageFromRepository.get().getConversation().getId());
    }

    @Test
    void shouldGetDirectMessages() {
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().build();
        List<DirectMessage> messages = messageRepository.getDirectMessages(conversationId, searchOptions);
        assertTrue(messages.size() == 10);
        assertTrue(messages.get(0).getTimestamp().compareTo(searchOptions.getTimestamp()) >= 0);
    }

    @Test
    void shouldUpdateDirectMessage() {
        String newMessageContent = "I have been updated";
        DirectMessage originalMessage = messageRepository.findDirectMessageById(existingDirectMessageId).get();
        messageRepository.updateDirectMessage(existingDirectMessageId, newMessageContent);
        DirectMessage updatedMessage = messageRepository.findDirectMessageById(existingDirectMessageId).get();
        assertNotEquals(originalMessage.getContent(), updatedMessage.getContent());
        assertEquals(originalMessage.getSender().getId(), updatedMessage.getSender().getId());
        assertEquals(originalMessage.getConversation().getId(), updatedMessage.getConversation().getId());
        assertEquals(newMessageContent, updatedMessage.getContent());
    }

    @Test
    void shouldExistByConversation() {
        assertTrue(messageRepository.existsbyConversation(conversationId));
    }

    @Test
    void shouldDeleteAllByConversation() {
        assertTrue(messageRepository.existsbyConversation(conversationId));

        messageRepository.deleteAllByConversation(conversationId);

        assertFalse(messageRepository.existsbyConversation(conversationId));
    }

    @Test
    void shouldInsertChannelMessage() {
        User user1 = userRepository.findOneByUsername("User1").get();
        Channel channel = channelRepository.findById(channelId).get();
        String messageContent = "content";
        ChannelMessage message = messageRepository.insertMessage(user1, channel, messageContent);
        assertTrue(messageRepository.existsById(message.getId()));
        assertEquals(message.getChannel().getId(), channel.getId());
        assertEquals(message.getSender().getId(), user1.getId());
        assertEquals(message.getContent(), messageContent);
    }

    @Test
    void shouldFindChannelMessageById() {
        User user1 = userRepository.findOneByUsername("User1").get();
        Optional<ChannelMessage> message = messageRepository.findChannelMessageById(existingChannelMessageId);
        assertTrue(message.isPresent());
        assertEquals(user1.getId(), message.get().getSender().getId());
        assertEquals(channelId, message.get().getChannel().getId());
    }

    @Test
    void shouldGetChannelMessages() {
        MessageSearchOptions searchOptions = MessageSearchOptions.builder().build();
        List<ChannelMessage> messages = messageRepository.getChannelMessages(channelId, searchOptions);
        assertTrue(messages.size() == 10);
        assertTrue(messages.get(0).getTimestamp().compareTo(searchOptions.getTimestamp()) >= 0);
    }

    @Test
    void shouldUpdateChannelMessage() {
        String newMessageContent = "I have been updated";
        ChannelMessage originalMessage = messageRepository.findChannelMessageById(existingChannelMessageId).get();
        messageRepository.updateChannelMessage(existingChannelMessageId, newMessageContent);
        ChannelMessage updatedMessage = messageRepository.findChannelMessageById(existingChannelMessageId).get();
        assertNotEquals(originalMessage.getContent(), updatedMessage.getContent());
        assertEquals(originalMessage.getSender().getId(), updatedMessage.getSender().getId());
        assertEquals(originalMessage.getChannel().getId(), updatedMessage.getChannel().getId());
        assertEquals(newMessageContent, updatedMessage.getContent());
    }

    @Test
    void shouldExistByChannel() {
        assertTrue(messageRepository.existsByChannel(channelId));
    }

    @Test
    void shouldDeleteAllByChannel() {
        assertTrue(messageRepository.existsByChannel(channelId));

        messageRepository.deleteAllByChannel(channelId);

        assertFalse(messageRepository.existsByChannel(channelId));
    }
}
