package com.leostormer.strife.friends;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setPassword("password123");

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setPassword("password1234");

        userService.registerUser(user1);
        userService.registerUser(user2);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        friendRequestRepository.deleteAll();
    }

    @Test
    void shouldSendFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequest = friendRequestRepository.findById(friendRequest.getId()).get();
        assertNotNull(friendRequest);
        assertTrue(friendRequestRepository.existsById(friendRequest.getId()));
        assertTrue(friendRequest.getUser1Response() == FriendRequestResponse.ACCEPTED);
        assertTrue(friendRequest.getUser2Response() == FriendRequestResponse.PENDING);
    }

    @Test
    void shouldNotSendDuplicateFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        friendRequestService.sendFriendRequest(user1, user2);

        try {
            friendRequestService.sendFriendRequest(user1, user2);
        } catch (Exception e) {
            assertTrue(e instanceof DuplicateFriendRequestException);
        }
    }

    @Test
    void senderShouldNotAcceptFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);

        try {
            friendRequestService.acceptFriendRequest(user1, friendRequest.getId());
        } catch (Exception e) {
            assertTrue(e instanceof UnauthorizedFriendRequestActionException);
        }
    }

    @Test
    void receiverShouldAcceptFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.acceptFriendRequest(user2, friendRequest.getId());

        friendRequest = friendRequestRepository.findById(friendRequest.getId()).orElse(null);
        assertNotNull(friendRequest);
        assertTrue(friendRequest.getUser1Response() == FriendRequestResponse.ACCEPTED);
        assertTrue(friendRequest.getUser2Response() == FriendRequestResponse.ACCEPTED);
    }

    @Test
    void senderShouldRemoveFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.removeFriendRequest(user1, friendRequest.getId());

        assertTrue(friendRequestRepository.findById(friendRequest.getId()).isEmpty());
    }

    @Test
    void receiverShouldRemoveFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.removeFriendRequest(user2, friendRequest.getId());

        assertTrue(friendRequestRepository.findById(friendRequest.getId()).isEmpty());
    }

    @Test
    void shouldBlockUser() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        friendRequestService.blockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId())
                .orElse(null);

        assertNotNull(friendRequest);
        assertTrue(friendRequest.getUser1Response() == FriendRequestResponse.BLOCKED);
    }

    @Test
    void shouldAllowBothUsersToBlockEachOther() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

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
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        friendRequestService.blockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId())
                .orElse(null);

        assertNotNull(friendRequest);
        assertTrue(friendRequest.hasBeenBlocked(user2));
        assertTrue(friendRequest.hasSentBlockRequest(user1));

        // Attempt to remove the blocked request
        try {
            friendRequestService.removeFriendRequest(user2, friendRequest.getId());
        } catch (Exception e) {
            assertTrue(e instanceof UnauthorizedFriendRequestActionException);
        }
    }

    @Test
    void blockedUserShouldNotAcceptFriendRequest() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.blockUser(user1, user2);

        // attempt to accept the friend request after blocking
        try {
            friendRequestService.acceptFriendRequest(user2, friendRequest.getId());
        } catch (Exception e) {
            assertTrue(e instanceof UnauthorizedFriendRequestActionException);
        }
    }

    @Test
    void shouldUnblockUserAndDeleteRequestIfNotBlocked() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        friendRequestService.blockUser(user1, user2);
        friendRequestService.unblockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId()).orElse(null);

        assertNull(friendRequest);
    }

    @Test
    void shouldUnblockUserAndKeepRequestIfBlocked() {
        User user1 = userService.getUserByUsername("testuser1").get();
        User user2 = userService.getUserByUsername("testuser2").get();

        friendRequestService.blockUser(user1, user2);
        friendRequestService.blockUser(user2, user1);

        friendRequestService.unblockUser(user1, user2);
        FriendRequest friendRequest = friendRequestRepository.findOneByUserIds(user1.getId(), user2.getId()).orElse(null);

        assertNotNull(friendRequest);
        assertFalse(friendRequest.hasSentBlockRequest(user1));
    }
}
