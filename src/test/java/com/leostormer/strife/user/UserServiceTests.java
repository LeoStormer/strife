package com.leostormer.strife.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractIntegrationTest;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.friends.FriendRequest;
import com.leostormer.strife.friends.FriendRequestRepository;
import com.leostormer.strife.friends.FriendRequestService;

public class UserServiceTests extends AbstractIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    FriendRequestService friendRequestService;

    @Autowired
    UserService userService;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password456");
        user2 = userRepository.save(user2);

        user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password789");
        user3 = userRepository.save(user3);

        user4 = new User();
        user4.setUsername("user4");
        user4.setPassword("password101112");
        user4 = userRepository.save(user4);

        // Accepted
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(user1, user2);
        friendRequestService.acceptFriendRequest(user2, friendRequest.getId());

        FriendRequest friendRequest2 = friendRequestService.sendFriendRequest(user2, user3);
        friendRequestService.acceptFriendRequest(user3, friendRequest2.getId());

        // Pending
        friendRequestService.sendFriendRequest(user2, user4);

        // Blocked
        friendRequestService.sendFriendRequest(user1, user4);
        friendRequestService.blockUser(user4, user1);
        friendRequestService.blockUser(user1, user3);
        friendRequestService.blockUser(user3, user4);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        friendRequestRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        userService.registerUser(user);
    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {
        user1.setUsername("user1");
        user1.setPassword("password123");
        assertThrows(UsernameTakenException.class, () -> {
            userService.registerUser(user1);
        });
    }

    @Test
    void shouldUpdateUserDetails() {
        UserUpdate userUpdate = new UserUpdate();
        userUpdate.setPassword("SecurePassword!@#ASD");
        userService.updateUserDetails(user1, userUpdate);
        User updatedUser = userRepository.findById(user1.getId()).get();
        assertEquals(user1.getUsername(), updatedUser.getUsername());
        assertEquals(user1.getEmail(), updatedUser.getEmail());
        assertEquals(user1.getProfilePic(), updatedUser.getProfilePic());
        assertNotEquals(user1.getPassword(), updatedUser.getPassword());
    }

    @Test
    public void shouldNotUpdateUserDetailsWithExistingUsername() {
        UserUpdate updatedUser = new UserUpdate();
        updatedUser.setUsername(user2.getUsername());
        assertThrows(UsernameTakenException.class, () -> {
            userService.updateUserDetails(user1, updatedUser);
        });
    }

    @Test
    void shouldGetAllAcceptedFriends() {
        List<User> friends = userService.getFriends(user1);
        assertTrue(friends.size() == 1);
        friends = userService.getFriends(user2);
        assertTrue(friends.size() == 2);
        friends = userService.getFriends(user3);
        assertTrue(friends.size() == 1);
        friends = userService.getFriends(user4);
        assertTrue(friends.size() == 0);
    }

    @Test
    void shouldGetAllPendingFriends() {
        List<User> pendingFriends = userService.getPendingFriends(user1);
        assertTrue(pendingFriends.size() == 0);
        pendingFriends = userService.getPendingFriends(user2);
        assertTrue(pendingFriends.size() == 1);
        pendingFriends = userService.getPendingFriends(user3);
        assertTrue(pendingFriends.size() == 0);
        pendingFriends = userService.getPendingFriends(user4);
        assertTrue(pendingFriends.size() == 1);
    }

    @Test
    void shouldGetAllBlockedUsers() {
        List<User> blockedUsers = userService.getBlockedUsers(user1);
        assertTrue(blockedUsers.size() == 1);
        blockedUsers = userService.getBlockedUsers(user2);
        assertTrue(blockedUsers.size() == 0);
        blockedUsers = userService.getBlockedUsers(user3);
        assertTrue(blockedUsers.size() == 1);
        blockedUsers = userService.getBlockedUsers(user4);
        assertTrue(blockedUsers.size() == 1);
    }

    @Test
    void shouldNotSendFriendRequestToSelf() {
        assertThrows(UnauthorizedActionException.class, () -> {
            userService.sendFriendRequest(user1, user1.getId());
        });
    }

    @Test
    void shouldNotBlockSelf() {
        assertThrows(UnauthorizedActionException.class, () -> {
            userService.blockUser(user1, user1.getId());
        });
    }

    @Test
    void shouldNotUnblockSelf() {
        assertThrows(UnauthorizedActionException.class, () -> {
            userService.unblockUser(user1, user1.getId());
        });
    }
}
