package com.leostormer.strife.server.channel;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

public interface ChannelManager extends IUsesServerRepository {
    public ChannelRepository getChannelRepository();

    public long getPermissions(Channel channel, Member member);

    default Channel createChannel(Server server, String name, String category, String description, boolean isPublic) {
        Channel channel = new Channel();
        channel.setServer(server);
        channel.setName(name);
        channel.setCategory(category);
        channel.setDescription(description);
        channel.setPublic(isPublic);
        return getChannelRepository().save(channel);
    }

    default Channel getChannelInServer(ObjectId serverId, ObjectId channelId) {
        Channel channel = getChannelRepository().findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(CHANNEL_NOT_FOUND));

        if (!channel.getServer().getId().equals(serverId))
            throw new ResourceNotFoundException(CHANNEL_NOT_FOUND);

        return channel;
    }

    default List<Channel> getChannels(User user, ObjectId serverId) {
        Member member = getServerRepository().getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        ChannelRepository channelRepository = getChannelRepository();

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.VIEW_CHANNELS))
            throw new UnauthorizedActionException("User is not authorized to view channels");

        return channelRepository.getVisibleChannels(serverId, member);
    }

    default Channel addChannel(User user, ObjectId serverId, String channelName, String channelCategory,
            String channelDescription, boolean isPublic) {
        Server server = getServerRepository().findById(serverId)
                .orElseThrow(() -> new UnauthorizedActionException(SERVER_NOT_FOUND));

        Member member = server.getMember(user)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to create channels in this server");
        }

        return createChannel(server, channelName, channelCategory, channelDescription, isPublic);
    }

    default void updateChannelSettings(User commandUser, ObjectId serverId, ObjectId channelId,
            ChannelUpdateOperation operation) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));
        Member member = server.getMember(commandUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        ChannelRepository channelRepository = getChannelRepository();
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        Channel channel = getChannelInServer(serverId, channelId);

        if (!Permissions.hasAllPermissions(getPermissions(channel, member), PermissionType.VIEW_CHANNELS,
                PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to update this channel");
        }

        Map<ObjectId, Role> serverRoles = server.getRoles();

        Map<ObjectId, Long> rolePermissions = operation.getRolePermissions();
        if (rolePermissions != null && rolePermissions.keySet().stream().anyMatch(id -> !serverRoles.containsKey(id)))
            throw new ResourceNotFoundException(ROLE_NOT_FOUND.getMessage());

        Map<ObjectId, Long> userPermissions = operation.getUserPermissions();
        if (userPermissions != null
                && userPermissions.keySet().stream().anyMatch(userId -> !serverRepository.isMember(serverId, userId)))
            throw new ResourceNotFoundException(USER_NOT_MEMBER);

        channelRepository.updateChannelSettings(channelId, operation);
    }

    default void removeChannel(User user, ObjectId serverId, ObjectId channelId) {
        Member member = getServerRepository().getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Channel channel = getChannelInServer(serverId, channelId);
        if (!Permissions.hasAllPermissions(getPermissions(channel, member), PermissionType.VIEW_CHANNELS,
                PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to remove this channel");
        }

        getChannelRepository().deleteById(channelId);
    }
}
