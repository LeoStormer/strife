package com.leostormer.strife.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.AbstractIntegrationTest;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.user.friends.FriendRequest;
import com.leostormer.strife.user.friends.FriendRequestRepository;
import com.leostormer.strife.user.friends.FriendRequestService;

public class UserServiceTests extends AbstractIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    @Autowired
    FriendRequestService friendRequestService;

    @Autowired
    ChannelRepository conversationRepository;

    @Autowired
    UserService userService;

    private ObjectId user1Id;
    private ObjectId user2Id;
    private ObjectId user3Id;
    private ObjectId user4Id;
    private ObjectId user5Id;

    private User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(username + "@someEmail.com");
        return userRepository.save(user);
    }

    private void createPendingFriendship(User sender, User receiver) {
        FriendRequest friendRequest = FriendRequest.builder().sender(sender).receiver(receiver).build();
        friendRequestRepository.save(friendRequest);
    }

    private void createAcceptedFriendship(User sender, User receiver) {
        FriendRequest friendRequest = FriendRequest.builder().sender(sender).receiver(receiver)
                .accepted(true).build();
        friendRequestRepository.save(friendRequest);
        sender.getFriends().add(receiver.getId());
        receiver.getFriends().add(sender.getId());
        userRepository.saveAll(List.of(sender, receiver));
    }

    private void createBlockedRelationship(User sender, User receiver) {
        Optional<Conversation> result = conversationRepository.findConversationByUserIds(sender.getId(), receiver.getId());
        if (result.isPresent() && !result.get().isLocked()) {
            Conversation conversation = result.get();
            conversation.setLocked(true);
            conversationRepository.save(conversation);
        }
        sender.getBlockedUsers().add(receiver.getId());
        userRepository.save(sender);
    }

    @BeforeEach
    void setUp() {
        User user1 = createUser("user1", "password123");

        User user2 = createUser("user2", "password456");

        User user3 = createUser("user3", "password789");

        User user4 = createUser("user4", "password101112");
        
        // no pre-existing relationships
        User user5 = createUser("user4", "password101112");

        user1Id = user1.getId();
        user2Id = user2.getId();
        user3Id = user3.getId();
        user4Id = user4.getId();
        user5Id = user5.getId();

        createAcceptedFriendship(user1, user2);
        createAcceptedFriendship(user2, user3);

        // Pending
        createPendingFriendship(user2, user4);

        // Blocked
        createBlockedRelationship(user4, user1);
        createBlockedRelationship(user1, user3);
        createBlockedRelationship(user3, user4);
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
        User newUser = new User();
        newUser.setUsername("user1");
        newUser.setPassword("password123");
        assertThrows(UsernameTakenException.class, () -> {
            userService.registerUser(newUser);
        });
    }

    @Test
    void shouldUpdateUserDetails() {
        User user1 = userRepository.findById(user1Id).get();
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
        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();
        UserUpdate updatedUser = new UserUpdate();
        updatedUser.setUsername(user2.getUsername());
        assertThrows(UsernameTakenException.class, () -> {
            userService.updateUserDetails(user1, updatedUser);
        });
    }

    @Test
    void shouldGetAllAcceptedFriends() {
        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();
        User user3 = userRepository.findById(user3Id).get();
        User user4 = userRepository.findById(user4Id).get();
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
    void shouldGetAllPendingFriendRequests() {
        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();
        User user3 = userRepository.findById(user3Id).get();
        User user4 = userRepository.findById(user4Id).get();
        List<FriendRequest> pendingFriends = userService.getPendingFriendRequests(user1);
        assertEquals(0, pendingFriends.size());
        pendingFriends = userService.getPendingFriendRequests(user2);
        assertEquals(1, pendingFriends.size());
        pendingFriends = userService.getPendingFriendRequests(user3);
        assertEquals(0, pendingFriends.size());
        pendingFriends = userService.getPendingFriendRequests(user4);
        assertEquals(1, pendingFriends.size());
    }

    @Test
    void shouldGetAllBlockedUsers() {
        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();
        User user3 = userRepository.findById(user3Id).get();
        User user4 = userRepository.findById(user4Id).get();
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
    void shouldSendFriendRequest() {
        User user1 = userRepository.findById(user1Id).get();
        userService.sendFriendRequest(user1, user5Id);
        Optional<FriendRequest> result = friendRequestRepository.findOneByUserIds(user1Id, user5Id);
        assertTrue(result.isPresent() && !result.get().isAccepted());
    }

    @Test
    void shouldNotSendFriendRequestToSelf() {
        User user1 = userRepository.findById(user1Id).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            userService.sendFriendRequest(user1, user1.getId());
        });
    }

    @Test
    @Transactional
    void shouldAcceptFriendRequest() {
        User user4 = userRepository.findById(user4Id).get();
        FriendRequest pendingfriendRequest = friendRequestRepository.findOneByUserIds(user2Id, user4Id).get();
        userService.acceptFriendRequest(user4, pendingfriendRequest.getId());
        Optional<FriendRequest> result = friendRequestRepository.findOneByUserIds(user2Id, user4Id);
        assertTrue(result.isPresent() && result.get().isAccepted());
        User updatedUser2 = userRepository.findById(user2Id).get();
        User updatedUser4 = userRepository.findById(user4Id).get();
        assertTrue(updatedUser2.isFriend(user4Id));
        assertTrue(updatedUser4.isFriend(user2Id));
    }

    @Test
    @Transactional
    void shouldRemoveSentPendingFriendRequest() {
        User user2 = userRepository.findById(user2Id).get();
        FriendRequest pendingfriendRequest = friendRequestRepository.findOneByUserIds(user2Id, user4Id).get();
        userService.removeFriendRequest(user2, pendingfriendRequest.getId());
        Optional<FriendRequest> result = friendRequestRepository.findOneByUserIds(user2Id, user4Id);
        assertTrue(result.isEmpty());
        User updatedUser2 = userRepository.findById(user2Id).get();
        User updatedUser4 = userRepository.findById(user4Id).get();
        assertFalse(updatedUser2.isFriend(user4Id));
        assertFalse(updatedUser4.isFriend(user2Id));
    }

    @Test
    @Transactional
    void shouldRemoveAcceptedFriendRequest() {
        User user1 = userRepository.findById(user1Id).get();
        FriendRequest acceptedfriendRequest = friendRequestRepository.findOneByUserIds(user2Id, user1Id).get();
        userService.removeFriendRequest(user1, acceptedfriendRequest.getId());
        Optional<FriendRequest> result = friendRequestRepository.findOneByUserIds(user1Id, user2Id);
        assertTrue(result.isEmpty());
        User updatedUser1 = userRepository.findById(user1Id).get();
        User updatedUser2 = userRepository.findById(user2Id).get();
        assertFalse(updatedUser1.isFriend(user2Id));
        assertFalse(updatedUser2.isFriend(user1Id));
    }

    @Test
    @Transactional
    void shouldBlockUser() {
        User user1 = userRepository.findById(user1Id).get();
        userService.blockUser(user1, user2Id);
        User updatedUser1 = userRepository.findById(user1Id).get();
        assertTrue(updatedUser1.hasBlocked(user2Id));
        Optional<Conversation> conversationResult = conversationRepository.findConversationByUserIds(user1Id, user2Id);
        assertTrue(conversationResult.isPresent() && conversationResult.get().isLocked());
    }

    @Test
    @Transactional
    void shouldNotBlockSelf() {
        User user1 = userRepository.findById(user1Id).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            userService.blockUser(user1, user1Id);
        });
        assertFalse(userRepository.findById(user1Id).get().hasBlocked(user1Id));
    }

    @Test
    @Transactional
    void shouldBlockFriendAndRemoveFriendship() {
        User user1 = userRepository.findById(user1Id).get();
        userService.blockUser(user1, user2Id);
        Optional<User> updatedUser1 = userRepository.findById(user1Id);
        Optional<User> updatedUser2 = userRepository.findById(user2Id);
        assertTrue(updatedUser1.isPresent() && updatedUser2.isPresent());
        assertTrue(updatedUser1.get().hasBlocked(user2Id));
        assertFalse(updatedUser1.get().isFriend(user2Id));
        assertFalse(updatedUser2.get().isFriend(user1Id));
    }

    @Test
    @Transactional
    void shouldUnblockUser() {
        User user1 = userRepository.findById(user1Id).get();
        userService.unblockUser(user1, user3Id);
        Optional<User> updatedUser1 = userRepository.findById(user1Id);
        Optional<User> updatedUser3 = userRepository.findById(user3Id);
        assertTrue(updatedUser1.isPresent() && updatedUser3.isPresent());
        assertFalse(updatedUser1.get().hasBlocked(user3Id));
        Optional<Conversation> conversationResult = conversationRepository.findConversationByUserIds(user1Id, user3Id);
        boolean conversationIsLocked = conversationResult.isPresent() && conversationResult.get().isLocked();
        assertTrue(updatedUser3.get().hasBlocked(user1Id) == conversationIsLocked);
    }

    @Test
    @Transactional
    void shouldNotUnblockSelf() {
        User user1 = userRepository.findById(user1Id).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            userService.unblockUser(user1, user1Id);
        });
        Optional<User> updatedUser1 = userRepository.findById(user1Id);
        assertTrue(updatedUser1.isPresent());
        assertFalse(updatedUser1.get().hasBlocked(user1Id));
    }
}
