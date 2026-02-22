package com.leostormer.strife.user.friends;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractIntegrationTest;
import com.leostormer.strife.TestUtils;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;
import com.leostormer.strife.user.UserService;

public class FriendRequestServiceTests extends AbstractIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    FriendRequestService friendRequestService;

    static User user1;

    static User user2;

    @BeforeAll
    static void setUp(@Autowired UserRepository userRepository) {
        user1 = TestUtils.createUser("testuser1", "password123", userRepository);
        user2 = TestUtils.createUser("testuser2", "password1234", userRepository);
    }

    @AfterAll
    static void clearUsers(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        friendRequestRepository.deleteAll();
    }

    @Test
    void shouldSendFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        assertTrue(friendRequestRepository.existsById(friendRequest.getId()));
        assertFalse(friendRequest.isAccepted());
    }

    @Test
    void shouldNotSendDuplicateFriendRequest() {
        friendRequestService.sendFriendRequest(user1, user2);

        assertThrows(UnauthorizedActionException.class, () -> {
            friendRequestService.sendFriendRequest(user1, user2);
        });
    }

    @Test
    void senderShouldNotAcceptFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);

        assertThrows(UnauthorizedActionException.class, () -> {
            friendRequestService.acceptFriendRequest(user1, friendRequest.getId());
        });
    }

    @Test
    void receiverShouldAcceptFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.acceptFriendRequest(user2, friendRequest.getId());

        friendRequest = friendRequestRepository.findById(friendRequest.getId()).orElse(null);
        assertNotNull(friendRequest);
        assertTrue(friendRequest.isAccepted());
    }

    @Test
    void senderShouldRemoveFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.removeFriendRequest(user1, friendRequest.getId());

        assertFalse(friendRequestRepository.existsById(friendRequest.getId()));
    }

    @Test
    void receiverShouldRemoveFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.removeFriendRequest(user2, friendRequest.getId());

        assertFalse(friendRequestRepository.existsById(friendRequest.getId()));
    }
}
