package com.leostormer.strife.server;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.lang.NonNull;

import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document(collection = "servers")
@Data
@AllArgsConstructor
public class Server {
    @Id
    @NonNull
    private ObjectId id;

    private String name;

    private String description;

    private String icon;

    @DocumentReference(collection = "users", lazy = true)
    private User owner;

    private Map<ObjectId, Role> roles;

    @SuppressWarnings("null")
    public Server() {
        this.name = "";
        this.description = "";
        this.roles = new HashMap<ObjectId, Role>();
    }
}
