package com.leostormer.strife.server;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

import java.util.List;
import java.util.Map;

import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.message.Message;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.server.invite.InviteManager;
import com.leostormer.strife.server.invite.InviteRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.member.MemberManager;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.server.role.RoleManager;
import com.leostormer.strife.server.server_channel.ChannelManager;
import com.leostormer.strife.server.server_channel.ServerChannel;
import com.leostormer.strife.user.User;
import com.mongodb.lang.Nullable;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ServerService implements MemberManager, RoleManager, ChannelManager, InviteManager {
    @Autowired
    private final ServerRepository serverRepository;

    @Autowired
    private final MessageRepository messageRepository;

    @Autowired
    private final ChannelRepository channelRepository;

    @Autowired
    private final InviteRepository inviteRepository;

    @Override
    public long getPermissions(ServerChannel channel, Member member) {
        return (channel.isPublic() || member.isOwner()) ? member.getPermissions() : channel.getPermissions(member);
    }

    @Transactional
    public Server createServer(User owner, String serverName, String serverDescription) {
        Server server = new Server();
        server.setName(serverName);
        server.setDescription(serverDescription);
        server.setOwner(owner);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        server.getRoles().put(ownerRole.getId(), ownerRole);
        Role defaultRole = new Role(new ObjectId(), "Member", 0,
                Permissions.getPermissions(PermissionType.SEND_MESSAGES,
                        PermissionType.VIEW_CHANNELS, PermissionType.CHANGE_NICKNAME));
        server.getRoles().put(defaultRole.getId(), defaultRole);
        Member ownerMember = Member.fromUser(owner, ownerRole, defaultRole);
        ownerMember.setOwner(true);
        server.getMembers().add(ownerMember);
        server = serverRepository.save(server);

        String defaultChannelName = "general";
        String defaultChannelCategory = "General";
        String defaultChannelDescription = "The general discussion channel.";
        createChannel(server, defaultChannelName, defaultChannelCategory, defaultChannelDescription,
                true);

        return server;
    }

    public void updateServerDetails(User commandUser, ObjectId serverId, @Nullable String name,
            @Nullable String description) {
        Member member = serverRepository.getMember(serverId, commandUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

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
        oldOwner.setPermissions(Permissions.combinePermissions(
                oldOwner.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));
        oldOwner.setOwner(false);
        oldOwner.setPermissions(Permissions.combinePermissions(
                oldOwner.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));

        newOwnerMember.getRoleIds().add(0, ownerRole);
        newOwnerMember.setOwner(true);
        newOwnerMember.setPermissions(Permissions.combinePermissions(
                newOwnerMember.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));

        server.setOwner(newOwner);
        serverRepository.save(server);
    }

    @Transactional
    public void deleteServer(User owner, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        if (!server.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedActionException("User is not authorized to delete this server");
        }

        ObjectId[] channelIds = channelRepository.findAllByServerId(serverId).stream().map(c -> c.getId())
                .toArray(ObjectId[]::new);
        removeChannel(owner, serverId, channelIds);
        serverRepository.deleteById(serverId);
    }

    public List<Message> getMessages(User user, ObjectId serverId, ObjectId channelId,
            MessageSearchOptions searchOptions) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        ServerChannel channel = getChannelInServer(serverId, channelId);

        if (!Permissions.hasPermission(getPermissions(channel, member), PermissionType.VIEW_CHANNELS))
            throw new UnauthorizedActionException("User is not authorized to view messages in this channel");

        List<Message> messages = messageRepository.getMessages(channelId, searchOptions);
        messages.sort(Message.sortByTimestampAscending);
        return messages;
    }

    public Message sendMessage(User user, ObjectId serverId, ObjectId channelId, String content) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member member = server.getMember(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        ServerChannel channel = getChannelInServer(serverId, channelId);
        if (!Permissions.hasPermission(getPermissions(channel, member), PermissionType.SEND_MESSAGES)) {
            throw new UnauthorizedActionException("User is not authorized to send messages in this channel");
        }

        return messageRepository.insertMessage(user, channel, content);
    }

    public Message editMessage(User user, ObjectId serverId, ObjectId messageId, String newContent) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("User is not authorized to edit this message");
        }

        return messageRepository.updateMessage(messageId, newContent);
    }

    public void deleteMessage(User user, ObjectId serverId, ObjectId channelId, ObjectId messageId) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Message message = messageRepository.findById(messageId)
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

    @Override
    public ChannelRepository getChannelRepository() {
        return channelRepository;
    }

    @Override
    public InviteRepository getInviteRepository() {
        return inviteRepository;
    }

    @Override
    public MessageRepository getMessageRepository() {
        return messageRepository;
    }
}
