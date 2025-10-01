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
import com.leostormer.strife.user.friends.FriendRequest;
import com.leostormer.strife.user.friends.FriendRequestService;

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
        return getUsersById(user.getBlockedUsers().stream().toList());
    }

    public List<User> getFriends(User user) {
        return getUsersById(user.getFriends().stream().toList());
    }

    public List<FriendRequest> getPendingFriendRequests(User user) {
        return friendRequestService.getAllPendingFriendRequests(user);
    }

    public FriendRequest sendFriendRequest(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot send a friend request to yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        return friendRequestService.sendFriendRequest(sender, receiver);
    }

    @Transactional
    public FriendRequest acceptFriendRequest(User receiver, ObjectId requestId) {
        FriendRequest friendRequest = friendRequestService.acceptFriendRequest(receiver, requestId);
        User sender = friendRequest.getOtherUser(receiver);
        receiver.getFriends().add(sender.getId());
        sender.getFriends().add(receiver.getId());
        userRepository.saveAll(List.of(sender, receiver));
        return friendRequest;
    }

    @Transactional
    public void removeFriendRequest(User user, ObjectId requestId) {
        User otherUser = friendRequestService.removeFriendRequest(user, requestId);
        otherUser.getFriends().remove(user.getId());
        user.getFriends().remove(otherUser.getId());
        userRepository.saveAll(List.of(user, otherUser));
    }


    @Transactional
    public void blockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot block yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (sender.isFriend(receiverId)) {
            sender.getFriends().remove(receiver.getId());
            receiver.getFriends().remove(sender.getId());
            friendRequestService.removeFriendRequest(sender.getId(), receiverId);
        }

        conversationService.lockConversation(sender, receiver);
        sender.getBlockedUsers().add(receiverId);
        userRepository.save(sender);
    }

    @Transactional
    public void unblockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot unblock yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
                
        if (!receiver.hasBlocked(sender))
            conversationService.unlockConversation(sender, receiver);


        sender.getBlockedUsers().remove(receiverId);
        userRepository.save(sender);
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new UsernameTakenException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUserDetails(User user, UserUpdate userUpdate) {
        if (userUpdate.getUsername() != null &&  userRepository.existsByUsername(userUpdate.getUsername()))
            throw new UsernameTakenException();
        userUpdate.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        return userRepository.updateUserDetails(user.getId(), userUpdate);
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
