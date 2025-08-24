package com.leostormer.strife.server;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;
import com.mongodb.lang.Nullable;

public interface CustomServerRepository {
    public Server addMember(ObjectId serverId, Member... members);
    public Server updateMember(ObjectId serverId, Member... members);
    public Server removeMember(ObjectId serverId, ObjectId... userIds);
    public boolean isMember(ObjectId serverId, ObjectId userId);
    public Optional<Member> getMember(ObjectId serverId, ObjectId userId);

    public Server updateRoles(ObjectId serverId, Map<ObjectId, Role> roles);
    public Server updateServerDetails(ObjectId serverId, @Nullable String name, @Nullable String description);
}
