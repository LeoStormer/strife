package com.leostormer.strife.server;

import java.util.Map;

import org.bson.types.ObjectId;

import com.leostormer.strife.server.role.Role;
import com.mongodb.lang.Nullable;

public interface CustomServerRepository {
    public Server updateRoles(ObjectId serverId, Map<ObjectId, Role> roles);
    public Server updateServerDetails(ObjectId serverId, @Nullable String name, @Nullable String description);
}
