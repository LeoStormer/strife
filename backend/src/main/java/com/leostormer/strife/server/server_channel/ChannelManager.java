package com.leostormer.strife.server.server_channel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

public interface ChannelManager extends IUsesServerRepository {
    public ChannelRepository getChannelRepository();
    public MessageRepository getMessageRepository();

    public long getPermissions(ServerChannel channel, Member member);

    default ServerChannel createChannel(Server server, String name, String category, String description, boolean isPublic) {
        ServerChannel channel = new ServerChannel();
        channel.setServer(server);
        channel.setName(name);
        channel.setCategory(category);
        channel.setDescription(description);
        channel.setPublic(isPublic);
        return getChannelRepository().save(channel);
    }

    default ServerChannel getChannelInServer(ObjectId serverId, ObjectId channelId) {
        ServerChannel channel = getChannelRepository().findServerChannelById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(CHANNEL_NOT_FOUND));

        if (!channel.getServer().getId().equals(serverId))
            throw new ResourceNotFoundException(CHANNEL_NOT_FOUND);

        return channel;
    }

    default List<ServerChannel> getChannels(User user, ObjectId serverId) {
        Member member = getServerRepository().getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        ChannelRepository channelRepository = getChannelRepository();

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.VIEW_CHANNELS))
            throw new UnauthorizedActionException("User is not authorized to view channels");

        return channelRepository.getVisibleServerChannels(serverId, member);
    }

    default ServerChannel getDefaultChannel(User user, ObjectId serverId) {
        Member member = getServerRepository().getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        ChannelRepository channelRepository = getChannelRepository();

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.VIEW_CHANNELS))
            throw new UnauthorizedActionException("User is not authorized to view channels");

        return channelRepository.getFirstVisibleServerChannel(serverId, member);
    }

    @SuppressWarnings("null")
    default ServerChannel addChannel(User user, ObjectId serverId, String channelName, String channelCategory,
            String channelDescription, boolean isPublic) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to create channels in this server");
        }

        return createChannel(server, channelName, channelCategory, channelDescription, isPublic);
    }

    @SuppressWarnings("null")
    default void updateChannelSettings(User commandUser, ObjectId serverId, ObjectId channelId,
            ChannelUpdateOperation operation) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));
        Member member = serverRepository.getMember(serverId, commandUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        ChannelRepository channelRepository = getChannelRepository();
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        ServerChannel channel = getChannelInServer(serverId, channelId);

        if (!Permissions.hasAllPermissions(getPermissions(channel, member), PermissionType.VIEW_CHANNELS,
                PermissionType.MANAGE_CHANNELS)) {
            throw new UnauthorizedActionException("User is not authorized to update this channel");
        }

        Map<ObjectId, Role> serverRoles = server.getRoles();

        Map<ObjectId, Long> rolePermissions = operation.getRolePermissions();
        if (rolePermissions != null && rolePermissions.keySet().stream().anyMatch(id -> !serverRoles.containsKey(id)))
            throw new ResourceNotFoundException(ROLE_NOT_FOUND);

        Map<ObjectId, Long> userPermissions = operation.getUserPermissions();
        if (userPermissions != null
                && userPermissions.keySet().stream().anyMatch(userId -> !serverRepository.isMember(serverId, userId)))
            throw new ResourceNotFoundException(USER_NOT_MEMBER);

        channelRepository.updateServerChannelSettings(channelId, operation);
    }

    @Transactional
    @SuppressWarnings("null")
    default void removeChannel(User user, ObjectId serverId, ObjectId... channelIds) {
        Member member = getServerRepository().getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Stream.of(channelIds).forEach(id -> {
            ServerChannel channel = getChannelInServer(serverId, id);
            if (!Permissions.hasAllPermissions(getPermissions(channel, member), PermissionType.VIEW_CHANNELS,
                    PermissionType.MANAGE_CHANNELS)) {
                throw new UnauthorizedActionException("User is not authorized to remove this channel");
            }
        });

        getMessageRepository().deleteAllByChannel(channelIds);
        getChannelRepository().deleteAllById(Stream.of(channelIds).toList());;
    }
}
