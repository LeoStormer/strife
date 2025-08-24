package com.leostormer.strife.friends;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;
import com.leostormer.strife.user.UserService;

@SpringBootTest
@ActiveProfiles("test")
public class FriendRequestServiceTests {

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
        user1.setPassword("password123");

        user2 = new User();
        user2.setUsername("testuser2");
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
        assertTrue(friendRequest.getUser1Response() == FriendRequestResponse.ACCEPTED);
        assertTrue(friendRequest.getUser2Response() == FriendRequestResponse.PENDING);
    }

    @Test
    void shouldNotSendDuplicateFriendRequest() {
        friendRequestService.sendFriendRequest(user1, user2);

        assertThrows(DuplicateFriendRequestException.class, () -> {
            friendRequestService.sendFriendRequest(user1, user2);
        });
    }

    @Test
    void senderShouldNotAcceptFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);

        assertThrows(UnauthorizedFriendRequestActionException.class, () -> {
            friendRequestService.acceptFriendRequest(user1, friendRequest.getId());
        });
    }

    @Test
    void receiverShouldAcceptFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.acceptFriendRequest(user2, friendRequest.getId());

        friendRequest = friendRequestRepository.findById(friendRequest.getId()).orElse(null);
        assertNotNull(friendRequest);
        assertTrue(friendRequest.getUser1Response() == FriendRequestResponse.ACCEPTED);
        assertTrue(friendRequest.getUser2Response() == FriendRequestResponse.ACCEPTED);
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

    @Test
    void shouldBlockUser() {
        friendRequestService.blockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId())
                .orElse(null);

        assertNotNull(friendRequest);
        assertTrue(friendRequest.getUser1Response() == FriendRequestResponse.BLOCKED);
    }

    @Test
    void shouldAllowBothUsersToBlockEachOther() {
        friendRequestService.blockUser(user1, user2);
        friendRequestService.blockUser(user2, user1);

        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId())
                .orElse(null);

        assertNotNull(friendRequest);
        assertTrue(friendRequest.hasBeenBlocked(user1));
        assertTrue(friendRequest.hasBeenBlocked(user2));
    }

    @Test
    void blockedUserShouldNotRemoveBlockedFriendRequest() {
        friendRequestService.blockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId())
                .orElse(null);

        assertNotNull(friendRequest);
        assertTrue(friendRequest.hasBeenBlocked(user2));
        assertTrue(friendRequest.hasSentBlockRequest(user1));

        // Attempt to remove the blocked request
        assertThrows(UnauthorizedFriendRequestActionException.class, () -> {
            friendRequestService.removeFriendRequest(user2, friendRequest.getId());
        });
    }

    @Test
    void blockedUserShouldNotAcceptFriendRequest() {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.blockUser(user1, user2);

        // attempt to accept the friend request after blocking
        assertThrows(UnauthorizedFriendRequestActionException.class, () -> {
            friendRequestService.acceptFriendRequest(user2, friendRequest.getId());
        });
    }

    @Test
    void shouldUnblockUserAndDeleteRequestIfNotBlocked() {
        friendRequestService.blockUser(user1, user2);
        friendRequestService.unblockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId()).orElse(null);

        assertNull(friendRequest);
    }

    @Test
    void shouldUnblockUserAndKeepRequestIfBlocked() {
        friendRequestService.blockUser(user1, user2);
        friendRequestService.blockUser(user2, user1);

        friendRequestService.unblockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId()).orElse(null);

        assertNotNull(friendRequest);
        assertFalse(friendRequest.hasSentBlockRequest(user1));
    }
}
