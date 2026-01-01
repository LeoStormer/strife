package com.leostormer.strife.server.server_channel;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerChannel extends Channel {
    @DocumentReference(collection = "servers", lazy = true)
    private Server server;

    private String name;

    private String category;

    private String description;

    @Builder.Default
    private boolean isPublic = true;

    /**
     * List of roles that are allowed to access this channel.
     * Is ignored if the channel is public
     */
    @Builder.Default
    private Map<ObjectId, Long> rolePermissions = Map.of();

    /**
     * List of users that are allowed to access this channel.
     * Is ignored if the channel is public
     */
    @Builder.Default
    private Map<ObjectId, Long> userPermissions = Map.of();

    public long getPermissions(Role role) {
        return rolePermissions.getOrDefault(role.getId(), Permissions.NONE);
    }

    public long getPermissions(User user) {
        return userPermissions.getOrDefault(user.getId(), Permissions.NONE);
    }

    public long getPermissions(Member member) {
        long permissions = Permissions.NONE;
        for (ObjectId roleId : member.getRoleIds()) {
            permissions |= rolePermissions.getOrDefault(roleId, Permissions.NONE);
        }

        return permissions | userPermissions.getOrDefault(member.getUser().getId(), Permissions.NONE);
    }

    public void setPermissions(Role role, long permissions) {
        rolePermissions.put(role.getId(), permissions);
    }

    public void setPermissions(User user, long permissions) {
        userPermissions.put(user.getId(), permissions);
    }
}
