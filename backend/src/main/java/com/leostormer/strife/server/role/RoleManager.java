package com.leostormer.strife.server.role;

import org.bson.types.ObjectId;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.user.User;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RoleManager extends IUsesServerRepository {
    static final Comparator<Role> ascendingOrder = (r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority());

    private RoleUpdateOperation sanitizeOperation(Map<ObjectId, Role> serverRoles, RoleUpdateOperation operation) {
        List<Role> rolesToAdd = operation.getRolesToAdd();
        rolesToAdd = rolesToAdd == null ? List.of() : rolesToAdd;

        List<ObjectId> rolesToRemove = operation.getRolesToRemove();
        rolesToRemove = rolesToRemove == null ? List.of()
                : rolesToRemove.stream().filter(id -> serverRoles.containsKey(id))
                        .toList();

        List<Role> rolesToUpdate = operation.getRolesToUpdate();
        rolesToUpdate = rolesToUpdate == null ? List.of()
                : rolesToUpdate.stream().filter(r -> serverRoles.containsKey(r.getId()))
                        .toList();

        return new RoleUpdateOperation(rolesToAdd, rolesToRemove, rolesToUpdate);
    }

    private List<Role> mergeLists(List<Role> existingRoles, List<Role> rolesToAdd) {
        // Both lists are assumed to be sorted in ascending order
        // merges both lists into a single sorted list so that
        // if two roles have the same priority the one from rolesToAdd comes first.
        int i = 0;
        int j = 0;

        List<Role> mergedList = new ArrayList<>();
        while (i < existingRoles.size() && j < rolesToAdd.size()) {
            Role role1 = existingRoles.get(i);
            Role role2 = rolesToAdd.get(j);

            if (role1.getPriority() < role2.getPriority()) {
                mergedList.add(role1);
                i++;
            } else {
                mergedList.add(role2);
                j++;
            }
        }

        while (i < existingRoles.size()) {
            mergedList.add(existingRoles.get(i));
            i++;
        }

        while (j < rolesToAdd.size()) {
            mergedList.add(rolesToAdd.get(j));
            j++;
        }

        return mergedList;
    }

    @SuppressWarnings("null")
    default void updateRoles(User user, ObjectId serverId, RoleUpdateOperation operation) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_ROLES))
            throw new UnauthorizedActionException("User is not authorized to manage roles in this server");

        int highestRolePriority = member.getRolePriority();
        Map<ObjectId, Role> serverRoles = server.getRoles();
        RoleUpdateOperation santizedOperation = sanitizeOperation(serverRoles, operation);
        List<Role> rolesToAdd = santizedOperation.getRolesToAdd();
        List<ObjectId> rolesToRemove = santizedOperation.getRolesToRemove();
        List<Role> rolesToUpdate = santizedOperation.getRolesToUpdate();

        if (rolesToRemove.stream().anyMatch(id -> serverRoles.get(id).getPriority() >= highestRolePriority)
                || rolesToUpdate.stream()
                        .anyMatch(r -> serverRoles.get(r.getId()).getPriority() >= highestRolePriority))
            throw new UnauthorizedActionException("User is not authorized to manage roles at or above their highest");

        int delta = rolesToAdd.size() - rolesToRemove.size();
        int newHighestRolePriority = member.isOwner() ? Integer.MAX_VALUE : highestRolePriority + delta;
        if (Stream.concat(rolesToUpdate.stream(), rolesToAdd.stream())
                .anyMatch(r -> r.getPriority() >= newHighestRolePriority))
            throw new UnauthorizedActionException("User is not authorised to manage roles at or above their highest");

        rolesToAdd.forEach(r -> r.setId(new ObjectId()));
        rolesToAdd = Stream.concat(rolesToUpdate.stream(), rolesToAdd.stream()).sorted(ascendingOrder).toList();

        Set<ObjectId> filteredIds = Stream.concat(rolesToRemove.stream(), rolesToAdd.stream().map(r -> r.getId()))
                .collect(Collectors.toSet());
        List<Role> existingRoles = serverRoles.values().stream().filter(r -> !filteredIds.contains(r.getId()))
                .sorted(ascendingOrder).toList();
        List<Role> mergedList = mergeLists(existingRoles, rolesToAdd);

        Map<ObjectId, Role> updatedRoles = new HashMap<>();
        for (int i = 0; i < mergedList.size(); i++) {
            Role r = mergedList.get(i);
            if (r.getPriority() != Integer.MAX_VALUE) // ignore owner role
                r.setPriority(i);
            updatedRoles.put(r.getId(), r);
        }

        serverRepository.updateRoles(serverId, updatedRoles);
    }

}
