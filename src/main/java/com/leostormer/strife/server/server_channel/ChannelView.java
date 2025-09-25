package com.leostormer.strife.server.server_channel;

import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class ChannelView {
    private String id;

    private String serverId;

    private String name;

    private String category;

    private String description;

    private boolean isPublic = true;

    private Map<String, Long> rolePermissions = Map.of();

    private Map<String, Long> userPermissions = Map.of();

    private Map<String, Long> convertMap(Map<ObjectId, Long> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toHexString(), Map.Entry::getValue));
    }

    public ChannelView(ServerChannel channel) {
        this.id = channel.getId().toHexString();
        this.serverId = channel.getServer().getId().toHexString();
        this.name = channel.getName();
        this.category = channel.getCategory();
        this.description = channel.getDescription();
        this.isPublic = channel.isPublic();
        this.rolePermissions = convertMap(channel.getRolePermissions());
        this.userPermissions = convertMap(channel.getUserPermissions());
    }
}
