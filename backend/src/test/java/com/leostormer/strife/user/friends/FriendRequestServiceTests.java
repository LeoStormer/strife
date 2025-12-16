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
    static void setUp(@Autowired UserService userService) {
        user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("testuser1@someEmail.com");
        user1.setPassword("password123");

        user2 = new User();
        user2.setUsername("testuser2");
        user1.setEmail("testuser2@someEmail.com");
        user2.setPassword("password1234");

        user1 = userService.registerUser(user1);
        user2 = userService.registerUser(user2);
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
