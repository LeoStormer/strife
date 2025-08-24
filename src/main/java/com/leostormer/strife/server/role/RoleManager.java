package com.leostormer.strife.server.role;

import org.bson.types.ObjectId;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.user.User;

import static com.leostormer.strife.server.ExceptionMessage.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public interface RoleManager extends IUsesServerRepository {

    default void updateRoles(User user, ObjectId serverId, RoleUpdateOperation operation) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member member = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_ROLES))
            throw new UnauthorizedActionException("User is not authorized to manage roles in this server");

        int highestRolePriority = member.getRolePriority();
        Map<ObjectId, Role> serverRoles = server.getRoles();
        List<Role> rolesToAdd = operation.getRolesToAdd();
        List<ObjectId> rolesToRemove = operation.getRolesToRemove().stream().filter(id -> serverRoles.containsKey(id))
                .toList();
        List<Role> rolesToUpdate = operation.getRolesToUpdate().stream().filter(r -> serverRoles.containsKey(r.getId()))
                .toList();

        if (rolesToRemove.stream().anyMatch(id -> serverRoles.get(id).getPriority() >= highestRolePriority)
                || rolesToUpdate.stream()
                        .anyMatch(r -> serverRoles.get(r.getId()).getPriority() >= highestRolePriority))
            throw new UnauthorizedActionException("User is not authorized to manage roles at or above their highest");

        int delta = rolesToAdd.size() - rolesToRemove.size();
        int newHighestRolePriority = highestRolePriority + delta;
        if (Stream.concat(rolesToUpdate.stream(), rolesToAdd.stream())
                .anyMatch(r -> r.getPriority() >= newHighestRolePriority))
            throw new UnauthorizedActionException("User is not authorised to manage roles at or above their highest");

        rolesToAdd.forEach(r -> r.setId(new ObjectId()));

        Stream.concat(rolesToUpdate.stream(), rolesToAdd.stream()).forEach(r -> serverRoles.put(r.getId(), r));
        rolesToRemove.stream().forEach(id -> serverRoles.remove(id));

        AtomicInteger currentPriority = new AtomicInteger();
        serverRoles.values().stream().filter((r) -> r.getPriority() != Integer.MAX_VALUE)// ignore owner role
                .sorted((r1, r2) -> -1 * r1.compareTo(r2)).sequential().forEach(r -> {
                    r.setPriority(currentPriority.getAndIncrement());
                });
        serverRepository.updateRoles(serverId, serverRoles);
    }

}
