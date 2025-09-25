package com.leostormer.strife.server;

import java.security.Principal;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.Message;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.message.MessageView;
import com.leostormer.strife.server.invite.InviteView;
import com.leostormer.strife.server.member.MemberRoleUpdateOperation;
import com.leostormer.strife.server.role.RoleUpdateOperation;
import com.leostormer.strife.server.server_channel.ChannelUpdateOperation;
import com.leostormer.strife.server.server_channel.ChannelView;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserService;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@AllArgsConstructor
@RequestMapping("/api/server")
public class ServerController {
    @Autowired
    private final ServerService serverService;

    @Autowired
    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<ServerView> createServer(Principal principal, @RequestParam String serverName,
            @RequestParam String serverDescription) {
        User owner = userService.getUser(principal);
        try {
            return ResponseEntity.ok()
                    .body(new ServerView(serverService.createServer(owner, serverName, serverDescription)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("")
    public ResponseEntity<String> deleteServer(Principal principal, @RequestParam ObjectId serverId) {
        User owner = userService.getUser(principal);
        try {
            serverService.deleteServer(owner, serverId);
            return ResponseEntity.ok().body("Server deleted successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete server: " + e.getMessage());
        }
    }

    @PutMapping("")
    public ResponseEntity<String> updateServerDetails(Principal principal, @RequestParam ObjectId serverId,
            @RequestParam(required = false) String serverName,
            @RequestParam(required = false) String serverDescription) {
        User admin = userService.getUser(principal);
        try {
            serverService.updateServerDetails(admin, serverId, serverName, serverDescription);
            return ResponseEntity.ok().body("Server details updated successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update server details: " + e.getMessage());
        }
    }

    @PutMapping("/{serverId}/transferOwnership")
    public ResponseEntity<String> transferServerOwnership(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId memberId) {
        User owner = userService.getUser(principal);
        try {
            User newOwner = userService.getUserById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            serverService.transferServerOwnership(owner, newOwner, serverId);
            return ResponseEntity.ok().body("Server ownership transferred successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to transfer server ownership: " + e.getMessage());
        }
    }

    @GetMapping("/{serverId}/channels")
    public ResponseEntity<List<ChannelView>> getChannels(Principal principal, @PathVariable ObjectId serverId) {
        User user = userService.getUser(principal);
        try {
            List<ChannelView> channels = serverService.getChannels(user, serverId).stream().map(ChannelView::new)
                    .toList();
            return ResponseEntity.ok().body(channels);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{serverId}/channels")
    public ResponseEntity<ChannelView> addChannel(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam String name, @RequestParam String category, @RequestParam String description,
            @RequestParam(defaultValue = "true") boolean isPublic) {
        User user = userService.getUser(principal);
        try {
            ChannelView channel = new ChannelView(
                    serverService.addChannel(user, serverId, name, category, description, isPublic));
            return ResponseEntity.ok().body(channel);
            // return ResponseEntity.ok().body("Channel created successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{serverId}/channels")
    public ResponseEntity<String> updateChannelSettings(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId channelId, @RequestBody ChannelUpdateOperation operation) {
        User user = userService.getUser(principal);
        try {
            serverService.updateChannelSettings(user, serverId, channelId, operation);
            return ResponseEntity.ok().body("Channel settings updated successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update channel settings: " + e.getMessage());
        }
    }

    @DeleteMapping("/{serverId}/channels")
    public ResponseEntity<String> deleteChannel(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId channelId) {
        User user = userService.getUser(principal);
        try {
            serverService.removeChannel(user, serverId, channelId);
            return ResponseEntity.ok().body("Channel deleted successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete channel: " + e.getMessage());
        }
    }

    @PutMapping("/{serverId}/roles")
    public ResponseEntity<String> updateRoles(Principal principal, @PathVariable ObjectId serverId,
            @RequestBody RoleUpdateOperation operation) {
        User owner = userService.getUser(principal);
        try {
            serverService.updateRoles(owner, serverId, operation);
            return ResponseEntity.ok().body("Server roles updated successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update roles: " + e.getMessage());
        }
    }

    @PostMapping("/{serverId}/join")
    public ResponseEntity<String> joinServer(Principal principal, @PathVariable ObjectId serverId) {
        User user = userService.getUser(principal);
        try {
            serverService.joinServer(user, serverId);
            return ResponseEntity.ok().body("Joined server successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to join server: " + e.getMessage());
        }
    }

    @PostMapping("/{serverId}/join-by-invite")
    public ResponseEntity<String> joinServerByInvite(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam String inviteId) {
        User user = userService.getUser(principal);
        try {
            serverService.joinByInvite(user, inviteId);
            return ResponseEntity.ok().body("Joined server Successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to join server: " + e.getMessage());
        }
    }

    @DeleteMapping("/{serverId}/leave")
    public ResponseEntity<String> leaveServer(Principal principal, @PathVariable ObjectId serverId) {
        User user = userService.getUser(principal);
        try {
            serverService.leaveServer(user, serverId);
            return ResponseEntity.ok().body("Left server successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to leave server: " + e.getMessage());
        }
    }

    @DeleteMapping("/{serverId}/members/kick")
    public ResponseEntity<String> kickMember(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId memberId) {
        User commandUser = userService.getUser(principal);

        try {
            serverService.kickMember(commandUser, memberId, serverId);
            return ResponseEntity.ok().body("Member kicked successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to kick member: " + e.getMessage());
        }
    }

    @PostMapping("/{serverId}/members/ban")
    public ResponseEntity<String> banMember(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId memberId, @RequestBody String banReason) {
        User commandUser = userService.getUser(principal);

        try {
            serverService.banMember(commandUser, memberId, serverId, banReason);
            return ResponseEntity.ok().body("Member banned successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to ban member: " + e.getMessage());
        }
    }

    @DeleteMapping("/{serverId}/members/ban")
    public ResponseEntity<String> unbanMember(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId memberId) {
        User commandUser = userService.getUser(principal);

        try {
            serverService.unbanMember(commandUser, memberId, serverId);
            return ResponseEntity.ok().body("Member unbanned successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to unban member: " + e.getMessage());
        }
    }

    @PutMapping("/{serverId}/members/nickname")
    public ResponseEntity<String> changeNickname(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId memberId, @RequestBody String newNickname) {
        User commandUser = userService.getUser(principal);

        try {
            serverService.changeNickName(commandUser, memberId, serverId, newNickname);
            return ResponseEntity.ok().body("Member nickname changed successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to change nickname: " + e.getMessage());
        }
    }

    @PutMapping("/{serverId}/members/roles")
    public ResponseEntity<String> updateMemberRoles(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId memberId, @RequestBody MemberRoleUpdateOperation operation) {
        User commandUser = userService.getUser(principal);

        try {
            serverService.updateMemberRoles(commandUser, memberId, serverId, operation);
            return ResponseEntity.ok().body("Member roles updated successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update roles: " + e.getMessage());
        }
    }

    @PostMapping("/{serverId}/get-messages")
    public ResponseEntity<List<MessageView>> getMessages(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId channelId, @RequestBody(required = false) MessageSearchOptions searchOptions) {
        User user = userService.getUser(principal);
        searchOptions = searchOptions != null ? searchOptions : MessageSearchOptions.earliest();
        try {
            return ResponseEntity.ok().body(serverService.getMessages(user, serverId, channelId, searchOptions).stream()
                    .map(MessageView::new).toList());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{serverId}/messages")
    public ResponseEntity<MessageView> sendMessage(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId channelId, @RequestBody String content) {
        User user = userService.getUser(principal);
        try {
            Message message = serverService.sendMessage(user, serverId, channelId, content);
            return ResponseEntity.ok().body(new MessageView(message));
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{serverId}/messages")
    public ResponseEntity<MessageView> editMessage(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId messageId, @RequestBody String content) {
        User user = userService.getUser(principal);
        try {
            Message message = serverService.editMessage(user, serverId, messageId, content);
            return ResponseEntity.ok().body(new MessageView(message));
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{serverId}/messages")
    public ResponseEntity<String> deleteMessage(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam ObjectId channelId, @RequestParam ObjectId messageId) {
        User user = userService.getUser(principal);
        try {
            serverService.deleteMessage(user, serverId, channelId, messageId);
            return ResponseEntity.ok().body("Message deleted successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete message: " + e.getMessage());
        }
    }

    @GetMapping("/{serverId}/invites")
    public ResponseEntity<List<InviteView>> getInvites(Principal principal, @PathVariable ObjectId serverId) {
        User commandUser = userService.getUser(principal);
        try {
            return ResponseEntity.ok()
                    .body(serverService.getInvites(commandUser, serverId).stream().map(InviteView::new).toList());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{serverId}/invites")
    public ResponseEntity<InviteView> createInvite(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam(defaultValue = "604800") long expiresAfter, @RequestParam(defaultValue = "0") int maxUses) {
        User commandUser = userService.getUser(principal);
        try {
            return ResponseEntity.ok()
                    .body(new InviteView(serverService.createInvite(commandUser, serverId, expiresAfter, maxUses)));
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{serverId}/invites")
    public ResponseEntity<String> deleteInvite(Principal principal, @PathVariable ObjectId serverId,
            @RequestParam String inviteId) {
        User commandUser = userService.getUser(principal);
        try {
            serverService.deleteInvite(commandUser, serverId, inviteId);
            return ResponseEntity.ok().body("Invite deleted successfully");
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete invite: " + e.getMessage());
        }
    }
}
