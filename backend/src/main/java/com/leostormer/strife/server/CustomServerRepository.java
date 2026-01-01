package com.leostormer.strife.server;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.member.Member;
import com.leostormer.strife.server.role.Role;
import com.mongodb.lang.Nullable;

public interface CustomServerRepository {
    public boolean isMember(ObjectId serverId, ObjectId userId);
    public Optional<Member> getMember(ObjectId serverId, ObjectId userId);

    public Server updateRoles(ObjectId serverId, Map<ObjectId, Role> roles);
    public Server updateServerDetails(ObjectId serverId, @Nullable String name, @Nullable String description);
}
