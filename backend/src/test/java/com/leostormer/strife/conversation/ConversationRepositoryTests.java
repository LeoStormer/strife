package com.leostormer.strife.conversation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractRepositoryTest;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

public class ConversationRepositoryTests extends AbstractRepositoryTest {
    @Autowired
    public ChannelRepository conversationRepository;

    @Autowired
    public UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    private User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }

    @BeforeEach
    @SuppressWarnings("null")
    void setup() {
        user1 = createUser("User1", "password123");
        user2 = createUser("User2", "password1234");
        user3 = createUser("User3", "password12345");
        user4 = createUser("User4", "password123456");

        Conversation user1AndUser2ArePresent = new Conversation(user1, user2);
        Conversation user1AndUser3ArePresent = new Conversation(user1, user3);
        Conversation user2AndUser3ArePresent = new Conversation(user2, user3);
        Conversation user4IsPresentUser1Left = new Conversation(user4, user1, true, false);
        Conversation user4IsPresentUser2Left = new Conversation(user4, user2, true, false);
        Conversation user3BlockedUser4 = new Conversation(true, List.of(user3, user4), List.of(false, true));
        Conversation groupChat = new Conversation(user1, user2, user3);
        Conversation groupChat2 = new Conversation(false, List.of(user1, user2, user4), List.of(true, false, false));
        conversationRepository
                .saveAll(List.of(user1AndUser2ArePresent, user1AndUser3ArePresent, user2AndUser3ArePresent,
                        user4IsPresentUser1Left, user4IsPresentUser2Left, user3BlockedUser4, groupChat, groupChat2));
    }

    @AfterEach
    void cleanup() {
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldGetAllUserConversations() {
        assertTrue(conversationRepository.getAllUserConversations(user1.getId()).size() == 5);
        assertTrue(conversationRepository.getAllUserConversations(user2.getId()).size() == 5);
        assertTrue(conversationRepository.getAllUserConversations(user3.getId()).size() == 4);
        assertTrue(conversationRepository.getAllUserConversations(user4.getId()).size() == 4);
    }

    @Test
    void shouldGetAllConversationsWhereUserIsPresent() {
        assertTrue(conversationRepository.getAllConversationsWhereUserIsPresent(user1.getId()).size() == 4);
        assertTrue(conversationRepository.getAllConversationsWhereUserIsPresent(user2.getId()).size() == 3);
        assertTrue(conversationRepository.getAllConversationsWhereUserIsPresent(user3.getId()).size() == 3);
        assertTrue(conversationRepository.getAllConversationsWhereUserIsPresent(user4.getId()).size() == 3);
    }

    @Test
    void shouldFindConversationsByUserIds() {
        Optional<Conversation> request = conversationRepository.findConversationByUserIds(user1.getId(), user2.getId());
        Optional<Conversation> request2 = conversationRepository.findConversationByUserIds(user2.getId(),
                user1.getId());
        assertTrue(request.isPresent());
        assertTrue(request2.isPresent());
        assertTrue(request.get().getId().equals(request2.get().getId()));
    }

    @Test
    void shouldExistByUserIdsWithoutCaringAboutOrder() {
        assertTrue(conversationRepository.conversationExistsByUserIds(user1.getId(), user2.getId()));
        assertTrue(conversationRepository.conversationExistsByUserIds(user2.getId(), user1.getId()));
        assertTrue(conversationRepository.conversationExistsByUserIds(user1.getId(), user3.getId()));
        assertTrue(conversationRepository.conversationExistsByUserIds(user3.getId(), user1.getId()));
        assertTrue(conversationRepository.conversationExistsByUserIds(user2.getId(), user3.getId()));
        assertTrue(conversationRepository.conversationExistsByUserIds(user3.getId(), user2.getId()));
    }
}
