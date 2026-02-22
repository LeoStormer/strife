package com.leostormer.strife.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractRepositoryTest;
import com.leostormer.strife.TestUtils;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.user.friends.FriendRequestRepository;

public class UserRepositoryTests extends AbstractRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    ChannelRepository conversationRepository;

    private ObjectId user1Id;
    private ObjectId user2Id;
    private ObjectId user3Id;

    @BeforeEach
    void setUp() {
        User user1 = TestUtils.createUser("user1", "somePassword", userRepository);
        user1Id = user1.getId();

        User user2 = TestUtils.createUser("user2", "anyPassword", userRepository);
        user2Id = user2.getId();

        User user3 = TestUtils.createUser("user3", "myPassword", userRepository);
        user3Id = user3.getId();

        TestUtils.createAcceptedFriendship(user1, user2, userRepository, friendRequestRepository);
        TestUtils.createBlockedRelationship(user2, user3, true, userRepository, conversationRepository);
        TestUtils.createBlockedRelationship(user3, user1, userRepository, conversationRepository);
    }

    @AfterEach
    void clearDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldExistByUsername() {
        assertTrue(userRepository.existsByUsername("user1"));
    }

    @Test
    void shouldFindUserByUsername() {
        User user = userRepository.findOneByUsername("user1").get();
        assertEquals(user1Id, user.getId());
    }

    @Test
    @SuppressWarnings("null")
    void shouldUpdateUserDetails() {
        String newName = "AwesomeNewName";
        User user = userRepository.findById(user1Id).get();
        UserUpdate userUpdate = new UserUpdate(newName, null, null, null);
        userRepository.updateUserDetails(user1Id, userUpdate);
        User updatedUser = userRepository.findById(user1Id).get();
        assertEquals(newName, updatedUser.getUsername());
        assertEquals(user.getPassword(), updatedUser.getPassword());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        assertEquals(user.getProfilePic(), updatedUser.getProfilePic());
    }

    @Test
    void shouldGetFriends() {
        List<User> user1Friends = userRepository.getFriends(user1Id);
        assertEquals(1, user1Friends.size());
        List<User> user2Friends = userRepository.getFriends(user2Id);
        assertEquals(1, user2Friends.size());
        List<User> user3Friends = userRepository.getFriends(user3Id);
        assertEquals(0, user3Friends.size());
    }

    @Test
    void shouldGetBlockedUsers() {
        List<User> user1BlockedUsers = userRepository.getBlockedUsers(user1Id);
        assertEquals(0, user1BlockedUsers.size());
        List<User> user2BlockedUsers = userRepository.getBlockedUsers(user2Id);
        assertEquals(1, user2BlockedUsers.size());
        List<User> user3BlockedUsers = userRepository.getBlockedUsers(user3Id);
        assertEquals(2, user3BlockedUsers.size());
    }
}
