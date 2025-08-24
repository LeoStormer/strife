package com.leostormer.strife.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document(collection = "servers")
@Data
@AllArgsConstructor
public class Server {
    @Id
    private ObjectId id;

    private String name;

    private String description;

    @DocumentReference(collection = "users", lazy = true)
    private User owner;

    private Map<ObjectId, Role> roles;

    private List<Member> members;

    public Server() {
        this.name = "";
        this.description = "";
        this.roles = new HashMap<ObjectId, Role>();
        this.members = new ArrayList<Member>();
    }

    public Optional<Member> getMember(ObjectId userId) {
        return members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
    }

    public Optional<Member> getMember(User user) {
        return getMember(user.getId());
    }
}
