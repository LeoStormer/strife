package com.leostormer.strife.server;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.leostormer.strife.server.ExceptionMessage.*;

import java.util.List;
import java.util.Map;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.channel.ChannelService;
import com.leostormer.strife.channel.ChannelUpdateOperation;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.ChannelMessage;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.member.MemberManager;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.server.role.RoleManager;
import com.leostormer.strife.user.User;
import com.mongodb.lang.Nullable;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ServerService implements MemberManager, RoleManager {
    @Autowired
    private final ServerRepository serverRepository;

    @Autowired
    private final MessageRepository messageRepository;

    @Autowired
    private final ChannelService channelService;

    private long getPermissions(Channel channel, Member member) {
        return (channel.isPublic() || member.isOwner()) ? member.getPermissions() : channel.getPermissions(member);
    }

    private Channel getChannelInServer(ObjectId serverId, ObjectId channelId) {
        Channel channel = channelService.getChannelById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(CHANNEL_NOT_FOUND));

        if (!channel.getServer().getId().equals(serverId))
            throw new ResourceNotFoundException(CHANNEL_NOT_FOUND);

        return channel;
    }

    public Server createServer(User owner, String serverName, String serverDescription) {
        Server server = new Server();
        server.setName(serverName);
        server.setDescription(serverDescription);
        server.setOwner(owner);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        server.getRoles().put(ownerRole.getId(), ownerRole);
        Role defaultRole = new Role(new ObjectId(), "Member", 0, Permissions.getPermissions(PermissionType.SEND_MESSAGES,
                PermissionType.VIEW_CHANNELS, PermissionType.CHANGE_NICKNAME));
        server.getRoles().put(defaultRole.getId(), defaultRole);
        Member ownerMember = Member.fromUser(owner, ownerRole);
        ownerMember.setOwner(true);
        server.getMembers().add(ownerMember);
        server = serverRepository.save(server);

        // Create default Channel
        String defaultChannelName = "general";
        String defaultChannelCategory = "General";
        String defaultChannelDescription = "The general discussion channel.";
        channelService.createChannel(server, defaultChannelName, defaultChannelCategory, defaultChannelDescription,
                true);

        return server;
    }

    public void updateServerDetails(User commandUser, ObjectId serverId, @Nullable String name, @Nullable String description) {
        Member member = serverRepository.getMember(serverId, commandUser.getId()).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);
        
        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_SERVER))
            throw new UnauthorizedActionException("User is not authorized to alter server details");
        
        if (name == null && description == null)
            return;
        
        serverRepository.updateServerDetails(serverId, name, description);
    }

    public void transferServerOwnership(User commandUser, User newOwner, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        if (!commandUser.getId().equals(server.getOwner().getId()))
            throw new UnauthorizedActionException("User is not authorized to transfer server ownership");

        
        Member newOwnerMember = server.getMember(newOwner.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (newOwnerMember.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        Member oldOwner = server.getMember(commandUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        Map<ObjectId, Role> serverRoles = server.getRoles();
        ObjectId ownerRole = oldOwner.getRoleIds().remove(0);
        oldOwner.setPermissions(Permissions.combinePermissions(oldOwner.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));
        oldOwner.setOwner(false);
        
        newOwnerMember.getRoleIds().add(0, ownerRole);
        newOwnerMember.setOwner(true);
        
        server.setOwner(newOwner);
        serverRepository.save(server);
    }

    public void deleteServer(User owner, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        if (!server.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedActionException("User is not authorized to delete this server");
        }

        serverRepository.deleteById(serverId);
        channelService.deleteAllByServer(serverId);
    }

    public List<Channel> getChannels(ObjectId serverId) {
        return channelService.getChannelsByServer(serverId);
    }

    public Channel addChannel(User user, ObjectId serverId, String channelName, String channelCategory,
            String channelDescription, boolean isPublic) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new UnauthorizedActionException(SERVER_NOT_FOUND));

        Member member = server.getMember(user)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to create channels in this server");
        }

        return channelService.createChannel(server, channelName, channelCategory, channelDescription, isPublic);
    }

    public void updateChannelSettings(User commandUser, ObjectId serverId, ObjectId channelId, ChannelUpdateOperation operation) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));
        Member member = server.getMember(commandUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        Channel channel = channelService.getChannelById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(CHANNEL_NOT_FOUND));

        if (!Permissions.hasPermission(getPermissions(channel, member), PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to update this channel");
        }

        Map<ObjectId, Role> serverRoles = server.getRoles();

        Map<ObjectId, Long> rolePermissions = operation.getRolePermissions();
        if (rolePermissions != null && rolePermissions.keySet().stream().anyMatch(id -> !serverRoles.containsKey(id)))
            throw new ResourceNotFoundException(ROLE_NOT_FOUND.getMessage());
        
        Map<ObjectId, Long> userPermissions = operation.getUserPermissions();
        if (userPermissions != null && userPermissions.keySet().stream().anyMatch(userId -> !serverRepository.isMember(serverId, userId)))
            throw new ResourceNotFoundException(USER_NOT_MEMBER);

        channelService.updateChannelSettings(channelId, operation);
    }

    public void removeChannel(User user, ObjectId serverId, ObjectId channelId) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to remove this channel");
        }

        channelService.deleteChannel(channelId);
    }

    public ChannelMessage sendMessage(User user, ObjectId serverId, ObjectId channelId, String content) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));
        Member member = server.getMembers().stream().filter(m -> m.getUserId().equals(user.getId())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Channel channel = getChannelInServer(serverId, channelId);
        if (!Permissions.hasPermission(getPermissions(channel, member), PermissionType.SEND_MESSAGES)) {
            throw new UnauthorizedActionException("User is not authorized to send messages in this channel");
        }

        return messageRepository.insertMessage(user, channel, content);
    }

    public ChannelMessage editMessage(User user, ObjectId serverId, ObjectId messageId, String newContent) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        ChannelMessage message = messageRepository.findChannelMessageById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("User is not authorized to edit this message");
        }

        return messageRepository.updateChannelMessage(messageId, newContent);
    }

    public void deleteMessage(User user, ObjectId serverId, ObjectId channelId, ObjectId messageId) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        ChannelMessage message = messageRepository.findChannelMessageById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!(message.getSender().getId().equals(user.getId())
                || Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_MESSAGES))) {
            throw new UnauthorizedActionException("User is not authorized to delete this message");
        }

        messageRepository.deleteById(messageId);
    }

    @Override
    public ServerRepository getServerRepository() {
        return serverRepository;
    }
}
