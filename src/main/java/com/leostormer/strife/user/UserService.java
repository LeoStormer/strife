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
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.conversation.ConversationService;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.friends.FriendRequest;
import com.leostormer.strife.friends.FriendRequestService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private static final String USER_NOT_FOUND = "User not found";
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final FriendRequestService friendRequestService;
    @Autowired
    private final ConversationService conversationService;

    public User getUser(Principal principal) {
        // currently authenticated principal should always correspond to an active user
        // in database
        return getUserByUsername(principal.getName()).get();
    }

    public Optional<User> getUserById(ObjectId userId) {
        return userRepository.findById(userId);
    }

    public List<User> getUsersById(List<ObjectId> userIds) {
        return userRepository.findAllById(userIds);
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
            throw new UnauthorizedActionException("You cannot send a friend request to yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return friendRequestService.sendFriendRequest(sender, receiver);
    }

    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        return friendRequestService.acceptFriendRequest(receiver, requestId);
    }

    public void removeFriendRequest(User user, ObjectId requestId) {
        friendRequestService.removeFriendRequest(user, requestId);
    }

    @Transactional
    public void blockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot block yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        friendRequestService.blockUser(sender, receiver);

        // Users who have blocked each other cant speak to each other
        conversationService.lockConversation(sender, receiver);
    }

    @Transactional
    public void unblockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot unblock yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
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

    public User updateUserDetails(User user, UserUpdate userUpdate) {
        User userData = new User(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(),
                user.getProfilePic(), user.getCreatedDate());

        if (userUpdate.getEmail() != null)
            userData.setEmail(userUpdate.getEmail());

        if (userUpdate.getProfilePic() != null)
            userData.setProfilePic(userUpdate.getProfilePic());

        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty())
            userData.setPassword(passwordEncoder.encode(userUpdate.getPassword()));

        if (userUpdate.getUsername() != null && !userUpdate.getUsername().isEmpty()
                && !user.getUsername().equals(userUpdate.getUsername())) {
            if (userRepository.existsByUsername(userUpdate.getUsername()))
                throw new UsernameTakenException();
            userData.setUsername(userUpdate.getUsername());
        }

        return userRepository.save(userData);
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
