package com.leostormer.strife.member;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.server.Server;

public interface CustomMemberRepository {
    public Optional<Member> findByUserIdAndServerId(ObjectId userId, ObjectId serverId);
    public List<Server> findServersByUserId(ObjectId userId);
    public boolean existsByUserIdAndServerId(ObjectId userId, ObjectId serverId);
    public boolean isMember(ObjectId userId, ObjectId serverId);
    public void removeMember(ObjectId userId, ObjectId serverId);
    public void banMember(ObjectId userId, ObjectId serverId, String banReason);
    public void changeNickname(ObjectId userId, ObjectId serverId, String newNickname);
    public void updateMemberRoles(ObjectId userId, ObjectId serverId, int rolePriority, long permissions, List<ObjectId> roleIds);
}
