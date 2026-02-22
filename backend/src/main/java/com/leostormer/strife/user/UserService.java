package com.leostormer.strife.user;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.conversation.ConversationService;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.member.MemberService;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.user.friends.FriendRequest;
import com.leostormer.strife.user.friends.FriendRequestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    @Autowired
    private SecurityContextRepository securityContextRepository;
    @Autowired
    private final MemberService memberService;

    public List<Server> getJoinedServers(User user) {
        return memberService.getServersByUserId(user.getId());
    }

    public User getUser(Principal principal) {
        // currently authenticated principal should always correspond to an
        // active user in database
        return userRepository.findOneByEmail(principal.getName()).get();
    }

    @SuppressWarnings("null")
    public Optional<User> getUserById(ObjectId userId) {
        return userRepository.findById(userId);
    }

    @SuppressWarnings("null")
    public List<User> getUsersById(List<ObjectId> userIds) {
        return userRepository.findAllById(userIds);
    }

    @SuppressWarnings("null")
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

    @SuppressWarnings("null")
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
        userRepository.acceptFriendRequest(friendRequest.getSender().getId(), friendRequest.getReceiver().getId());
        return friendRequest;
    }

    @Transactional
    public void removeFriendRequest(User user, ObjectId requestId) {
        User otherUser = friendRequestService.removeFriendRequest(user, requestId);
        userRepository.removeFriendRequest(user.getId(), otherUser.getId());
    }

    @Transactional
    @SuppressWarnings("null")
    public void blockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot block yourself");

        if (!userRepository.existsById(receiverId))
            throw new ResourceNotFoundException(USER_NOT_FOUND);

        if (sender.isFriend(receiverId)) {
            friendRequestService.removeFriendRequest(sender.getId(), receiverId);
        }

        conversationService.lockConversation(sender.getId(), receiverId);
        userRepository.blockUser(sender.getId(), receiverId);
    }

    @Transactional
    @SuppressWarnings("null")
    public void unblockUser(User sender, ObjectId receiverId) {
        if (sender.getId().equals(receiverId))
            throw new UnauthorizedActionException("You cannot unblock yourself");

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        if (!receiver.hasBlocked(sender))
            conversationService.unlockConversation(sender.getId(), receiverId);

        userRepository.unblockUser(sender.getId(), receiverId);
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail()))
            throw new UsernameTakenException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUserDetails(User user, UserUpdate userUpdate) {
        if (userUpdate.getUsername() != null && userRepository.existsByUsername(userUpdate.getUsername()))
            throw new UsernameTakenException();
        userUpdate.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        return userRepository.updateUserDetails(user.getId(), userUpdate);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findOneByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return toUserDetails(user);
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }

    public User login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findOneByEmail(loginRequest.email()).orElseThrow(() -> new UsernameNotFoundException("Authentication failed"));
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword()))
            throw new BadCredentialsException("Authentication failed");
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(toUserDetails(user), null, List.of());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return user;
    }
}
