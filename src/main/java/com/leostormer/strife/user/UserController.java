package com.leostormer.strife.user;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leostormer.strife.friends.DuplicateFriendRequestException;
import com.leostormer.strife.friends.FriendRequestNotFoundException;
import com.leostormer.strife.friends.FriendRequestView;
import com.leostormer.strife.friends.UnauthorizedFriendRequestActionException;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    @Autowired
    private final UserService userService;

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
        User user = userService.getUserByUsername(principal.getName()).get();
        return ResponseEntity.ok(userService.getBlockedUsers(user).stream().map(UserView::new).toList());
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserView>> getUserFriends(Principal principal) {
        User user = userService.getUserByUsername(principal.getName()).get();
        return ResponseEntity.ok(userService.getFriends(user).stream().map(UserView::new).toList());
    }

    @PostMapping("/friends/friend-request")
    public ResponseEntity<FriendRequestView> sendFriendRequest(Principal principal, @RequestParam ObjectId receiverId) {
        User sender = userService.getUserByUsername(principal.getName()).get();
        try {
            return ResponseEntity.ok(new FriendRequestView(userService.sendFriendRequest(sender, receiverId)));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DuplicateFriendRequestException | UnauthorizedFriendRequestActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/friends/friend-request")
    public ResponseEntity<FriendRequestView> acceptFriendRequest(Principal principal,
            @RequestParam ObjectId requestId) {
        User receiver = userService.getUserByUsername(principal.getName()).get();
        try {
            return ResponseEntity.ok(new FriendRequestView(userService.acceptFriendRequest(receiver, requestId)));
        } catch (FriendRequestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (UnauthorizedFriendRequestActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/friends/friend-request")
    public ResponseEntity<String> removeFriendRequest(Principal principal, @RequestParam ObjectId requestId) {
        User user = userService.getUserByUsername(principal.getName()).get();
        try {
            userService.removeFriendRequest(user, requestId);
            return ResponseEntity.ok("Friend request removed");
        } catch (FriendRequestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UnauthorizedFriendRequestActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error removing friend request: " + e.getMessage());
        }
    }

    @PostMapping("/friends/block")
    public ResponseEntity<String> blockUser(Principal principal, @RequestParam ObjectId receiverId) {
        User sender = userService.getUserByUsername(principal.getName()).get();
        try {
            userService.blockUser(sender, receiverId);
            return ResponseEntity.ok("User blocked successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error blocking user: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserView> registerUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(new UserView(userService.registerUser(user)));
        } catch (UsernameTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
