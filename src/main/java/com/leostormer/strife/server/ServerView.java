package com.leostormer.strife.server;

import java.util.Map;
import java.util.stream.Collectors;

import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.UserView;

import lombok.Data;

@Data
public class ServerView {
    private String id;

    private String name;

    private String description;

    private UserView owner;

    Map<String, Role> roles;

    public ServerView(Server server) {
        this.id = server.getId().toHexString();
        this.name = server.getName();
        this.description = server.getDescription();
        this.owner = new UserView(server.getOwner());
        this.roles = server.getRoles().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toHexString(), e -> e.getValue()));
    }
}
