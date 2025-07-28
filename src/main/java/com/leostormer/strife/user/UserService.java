package com.leostormer.strife.user;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.leostormer.strife.conversation.ConversationService;
import com.leostormer.strife.friends.FriendRequest;
import com.leostormer.strife.friends.FriendRequestService;
import com.leostormer.strife.friends.UnauthorizedFriendRequestActionException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final FriendRequestService friendRequestService;
    @Autowired
    private final ConversationService conversationService;

    public User getUser(Principal principal) {
        // currently authenticated principal should always correspond to an active user in database
        return getUserByUsername(principal.getName()).get();
    }

    public Optional<User> getUserById(ObjectId userId) {
        return userRepository.findById(userId);
    }

    public boolean doesUserExist(ObjectId userId) {
        return userRepository.existsById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findOneByUsername(username);
    }

    public List<User> getBlockedUsers(User user) {
        return friendRequestService.getAllBlockedFriendRequests(user)
                .stream()
                .filter(request -> request.hasSentBlockRequest(user))
                .map(request -> request.getOtherUser(user))
                .toList();
    }

    public List<User> getFriends(User user) {
        return friendRequestService.getAllAcceptedFriendRequests(user)
                .stream()
                .map(request -> request.getOtherUser(user))
                .toList();
    }

    public List<User> getPendingFriends(User user) {
        return friendRequestService.getAllPendingFriendRequests(user)
                .stream()
                .map(request -> request.getOtherUser(user))
                .toList();
    }

    public FriendRequest sendFriendRequest(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedFriendRequestActionException();

        User receiver = userRepository.findById(receiverId).orElseThrow(UserNotFoundException::new);

        return friendRequestService.sendFriendRequest(sender, receiver);
    }

    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        return friendRequestService.acceptFriendRequest(receiver, requestId);
    }

    public void removeFriendRequest(User user, ObjectId requestId) {
        friendRequestService.removeFriendRequest(user, requestId);
    }

    public void blockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedFriendRequestActionException("You cannot block yourself");

        User receiver = userRepository.findById(receiverId).orElseThrow(UserNotFoundException::new);
        friendRequestService.blockUser(sender, receiver);
        
        // Users who have blocked each other cant speak to each other
        conversationService.lockConversation(sender, receiver);
    }

    public void unblockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedFriendRequestActionException("You cannot unblock yourself");

        User receiver = userRepository.findById(receiverId).orElseThrow(UserNotFoundException::new);
        friendRequestService.unblockUser(sender, receiver);
        Optional<FriendRequest> optional = friendRequestService.getFriendRequestByUsers(sender, receiver);
        if (optional.isEmpty())
            // friend request was deleted meaning, sender is neither blocking
            // nor blocked by receiver
            conversationService.unlockConversation(sender, receiver);
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new UsernameTakenException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
