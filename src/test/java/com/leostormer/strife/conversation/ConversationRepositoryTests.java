package com.leostormer.strife.conversation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

@DataMongoTest
@ActiveProfiles("test")
public class ConversationRepositoryTests {
    @Autowired
    public ConversationRepository conversationRepository;

    @Autowired
    public UserRepository userRepository;

    @BeforeEach
    void setup() {
        User user1 = new User();
        user1.setUsername("User1");
        user1.setPassword("password123");
        user1 = userRepository.save(user1);
        User user2 = new User();
        user2.setUsername("User2");
        user2.setPassword("password1234");
        user2 = userRepository.save(user2);
        User user3 = new User();
        user3.setUsername("User3");
        user3.setPassword("password12345");
        user3 = userRepository.save(user3);
        User user4 = new User();
        user4.setUsername("User4");
        user4.setPassword("password123456");
        user4 = userRepository.save(user4);

        conversationRepository
                .saveAll(List.of(new Conversation(user1, user2, true, true), new Conversation(user1, user3, true, true),
                        new Conversation(user2, user3, true, true), new Conversation(user4, user1, true, false),
                        new Conversation(user4, user2, true, false), new Conversation(user4, user3, true, false)));
    }

    @AfterEach
    void cleanup() {
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldGetAllUserConversations() {
        User[] users = new User[4];
        for (int i = 0; i < users.length; i++) {
            users[i] = userRepository.findOneByUsername("User" + (i + 1)).get();
        }

        assertTrue(conversationRepository.getAllUserConversations(users[0].getId()).size() == 2);
        assertTrue(conversationRepository.getAllUserConversations(users[1].getId()).size() == 2);
        assertTrue(conversationRepository.getAllUserConversations(users[2].getId()).size() == 2);
        assertTrue(conversationRepository.getAllUserConversations(users[3].getId()).size() == 3);
    }

    @Test
    void shouldFindConversationsByUserIds() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();

        Optional<Conversation> request = conversationRepository.findByUserIds(user1.getId(), user2.getId());
        Optional<Conversation> request2 = conversationRepository.findByUserIds(user2.getId(), user1.getId());
        assertTrue(request.isPresent());
        assertTrue(request2.isPresent());
        assertTrue(request.get().getId().equals(request2.get().getId()));
    }

    @Test
    void shouldExistByUserIdsWithoutCaringAboutOrder() {
        User[] users = new User[3];
        for (int i = 0; i < users.length; i++) {
            users[i] = userRepository.findOneByUsername("User" + (i + 1)).get();
        }

        assertTrue(conversationRepository.existsByUserIds(users[0].getId(), users[1].getId()));
        assertTrue(conversationRepository.existsByUserIds(users[1].getId(), users[0].getId()));
        assertTrue(conversationRepository.existsByUserIds(users[0].getId(), users[2].getId()));
        assertTrue(conversationRepository.existsByUserIds(users[2].getId(), users[0].getId()));
        assertTrue(conversationRepository.existsByUserIds(users[1].getId(), users[2].getId()));
        assertTrue(conversationRepository.existsByUserIds(users[2].getId(), users[1].getId()));
    }
}
