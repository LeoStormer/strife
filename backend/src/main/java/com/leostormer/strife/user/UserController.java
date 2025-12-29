package com.leostormer.strife.user;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.ServerView;
import com.leostormer.strife.user.friends.FriendRequestView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    @Autowired
    private final UserService userService;

    @GetMapping("/servers")
    public ResponseEntity<List<ServerView>> getJoinedServers(Principal principal) {
        // TODO: Implement fetching joined servers
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserView> getUserById(@PathVariable ObjectId userId) {
        Optional<User> optionalUser = userService.getUserById(userId);
        if (optionalUser.isPresent()) {
            return new ResponseEntity<UserView>(new UserView(optionalUser.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<UserView>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<UserView>> getBlockedUsers(Principal principal) {
        User user = userService.getUser(principal);
        return ResponseEntity.ok(userService.getBlockedUsers(user).stream().map(UserView::new).toList());
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserView>> getUserFriends(Principal principal) {
        User user = userService.getUser(principal);
        return ResponseEntity.ok(userService.getFriends(user).stream().map(UserView::new).toList());
    }

    @GetMapping("/friends/friend-request")
    public ResponseEntity<List<FriendRequestView>> getPendingFriendRequests(Principal principal) {
        User user = userService.getUser(principal);
        try {
            return ResponseEntity
                    .ok(userService.getPendingFriendRequests(user).stream().map(FriendRequestView::new).toList());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/friends/friend-request")
    public ResponseEntity<FriendRequestView> sendFriendRequest(Principal principal, @RequestParam ObjectId receiverId) {
        User sender = userService.getUser(principal);
        try {
            return ResponseEntity.ok(new FriendRequestView(userService.sendFriendRequest(sender, receiverId)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/friends/friend-request")
    public ResponseEntity<FriendRequestView> acceptFriendRequest(Principal principal,
            @RequestParam ObjectId requestId) {
        User receiver = userService.getUser(principal);
        try {
            return ResponseEntity.ok(new FriendRequestView(userService.acceptFriendRequest(receiver, requestId)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/friends/friend-request")
    public ResponseEntity<String> removeFriendRequest(Principal principal, @RequestParam ObjectId requestId) {
        User user = userService.getUser(principal);
        try {
            userService.removeFriendRequest(user, requestId);
            return ResponseEntity.ok("Friend request removed");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error removing friend request: " + e.getMessage());
        }
    }

    @PostMapping("/block-user")
    public ResponseEntity<String> blockUser(Principal principal, @RequestParam ObjectId receiverId) {
        User sender = userService.getUser(principal);
        try {
            userService.blockUser(sender, receiverId);
            return ResponseEntity.ok("User blocked successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error blocking user: " + e.getMessage());
        }
    }

    @DeleteMapping("/unblock-user")
    public ResponseEntity<String> unblockUser(Principal principal, @RequestParam ObjectId receiverId) {
        User sender = userService.getUser(principal);
        try {
            userService.unblockUser(sender, receiverId);
            return ResponseEntity.ok("User unblocked successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error unblocking user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserView> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            return ResponseEntity.ok().body(userService.login(loginRequest, request, response).toUserView());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserView> registerUser(@Valid @RequestBody User user, HttpServletRequest request,
            HttpServletResponse response) {
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), user.getPassword());
        try {
            userService.registerUser(user);
        } catch (UsernameTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        try {
            return ResponseEntity.ok(userService.login(loginRequest, request, response).toUserView());
        } catch (Exception e) {
            // User profile was created but failed to login so user should try logging in
            // later.
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<UserView> updateUserDetails(Principal principal, @Valid @RequestBody UserUpdate userData) {
        User user = userService.getUser(principal);
        try {
            return ResponseEntity.ok(new UserView(userService.updateUserDetails(user, userData)));
        } catch (UsernameTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/auth-status")
    public ResponseEntity<Boolean> checkLoginStatus(Principal principal) {
        if (principal != null) {
            return ResponseEntity.ok(true);
        }

        return ResponseEntity.ok(false);
    }

}
