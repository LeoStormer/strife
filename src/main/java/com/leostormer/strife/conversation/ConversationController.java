package com.leostormer.strife.conversation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.MessageSearchDirection;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.message.MessageView;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserService;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    @Autowired
    UserService userService;

    @Autowired
    ConversationService conversationService;

    // Get all conversations currently joined.
    @GetMapping("")
    public ResponseEntity<List<ConversationView>> getConversations(Principal principal) {
        User user = userService.getUser(principal);

        try {
            return ResponseEntity.ok().body(conversationService.getConversations(user).stream()
                    .map(conversation -> new ConversationView(conversation)).toList());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Start a new conversation.
    @PostMapping("")
    public ResponseEntity<ConversationView> startNewConversation(Principal principal,
            @RequestParam List<ObjectId> otherUserIds) {
        User user = userService.getUser(principal);
        List<User> otherUsers = userService.getUsersById(otherUserIds);
        if (otherUsers.size() < otherUserIds.size())
            return ResponseEntity.notFound().build();

        try {
            return ResponseEntity.ok()
                    .body(new ConversationView(conversationService.startNewConversation(user, otherUsers)));
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Leave a conversation.
    @DeleteMapping("")
    public ResponseEntity<String> leaveConversation(Principal principal, @RequestParam ObjectId conversationId) {
        User user = userService.getUser(principal);

        try {
            conversationService.leaveConversation(user, conversationId);
            return ResponseEntity.ok().body("Conversation successfully left");
        } catch (UnauthorizedActionException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error leaving conversation: " + e.getMessage());
        }
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageView>> getMessages(Principal principal, @PathVariable ObjectId conversationId,
            @PathVariable Date timestamp, @PathVariable MessageSearchDirection searchDirection) {
        User user = userService.getUser(principal);
        MessageSearchOptions searchOptions;
        searchOptions = new MessageSearchOptions(100, timestamp, searchDirection);
        try {
            List<MessageView> messages = conversationService.getMessages(user, conversationId, searchOptions).stream()
                    .map(dm -> new MessageView(dm)).toList();
            return ResponseEntity.ok().body(messages);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{conversationId}/latest")
    public ResponseEntity<List<MessageView>> getLatestMessages(Principal principal,
            @PathVariable ObjectId conversationId) {
        User user = userService.getUser(principal);
        try {
            List<MessageView> messages = conversationService
                    .getMessages(user, conversationId, MessageSearchOptions.latest()).stream()
                    .map(dm -> new MessageView(dm)).toList();
            return ResponseEntity.ok().body(messages);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{conversationId}/earliest")
    public ResponseEntity<List<MessageView>> getEarliestMessages(Principal principal,
            @PathVariable ObjectId conversationId) {
        User user = userService.getUser(principal);
        try {
            List<MessageView> messages = conversationService
                    .getMessages(user, conversationId, MessageSearchOptions.earliest()).stream()
                    .map(dm -> new MessageView(dm)).toList();
            return ResponseEntity.ok().body(messages);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Send a message in a conversation.
    @PostMapping("/{conversationId}")
    public ResponseEntity<MessageView> sendMessage(Principal principal, @PathVariable ObjectId conversationId,
            @RequestBody String messageContent) {
        User user = userService.getUser(principal);

        try {
            return ResponseEntity.ok()
                    .body(new MessageView(conversationService.sendMessage(user, conversationId, messageContent)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Edit a message in a conversation.
    @PutMapping("/{conversationid}")
    public ResponseEntity<MessageView> editMessage(Principal principal, @PathVariable ObjectId conversationId,
            @RequestParam ObjectId messageId, @RequestBody String messageContent) {
        User user = userService.getUser(principal);
        try {
            return ResponseEntity.ok()
                    .body(new MessageView(conversationService.editMessage(user, conversationId, messageId, messageContent)));
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete a message in a conversation.
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<String> deleteMessage(Principal principal, @PathVariable ObjectId conversationId,
            @RequestParam ObjectId messageId) {
        User user = userService.getUser(principal);

        try {
            conversationService.deleteMessage(user, messageId);
            return ResponseEntity.ok().body("Message successfully deleted");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting message: " + e.getMessage());
        }
    }

}
