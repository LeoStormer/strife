package com.leostormer.strife.server.member;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.types.ObjectId;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

public interface MemberManager extends IUsesServerRepository {
    default void joinServer(User user, ObjectId serverId) {
        ServerRepository serverRepository = getServerRepository();

        if (!serverRepository.existsById(serverId))
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);

        Optional<Member> existingMember = serverRepository.getMember(serverId, user.getId());
        if (existingMember.isEmpty()) {
            serverRepository.addMember(serverId, Member.fromUser(user));
            return;
        }

        if (existingMember.get().isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }
    }

    default void leaveServer(User user, ObjectId serverId) {
        ServerRepository serverRepository = getServerRepository();
        if (!serverRepository.existsById(serverId))
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);

        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (member.isOwner())
            throw new UnauthorizedActionException("Server owner cannot leave the server");

        serverRepository.removeMember(serverId, user.getId());
    }

    default void kickMember(User commandUser, ObjectId userToKickId, ObjectId serverId) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member commandMember = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(commandUser.getId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToKick = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(userToKickId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandMember.isBanned() || memberToKick.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!(commandMember.getRolePriority() > memberToKick.getRolePriority()
                && Permissions.hasPermission(commandMember.getPermissions(), PermissionType.KICK_MEMBERS)))
            throw new UnauthorizedActionException("User is not authorized to kick this member");

        serverRepository.removeMember(serverId, userToKickId);
    }

    default void banMember(User commandUser, ObjectId userToBanId, ObjectId serverId, String banReason) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member commandMember = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(commandUser.getId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToBan = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(userToBanId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandMember.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!(commandMember.getRolePriority() > memberToBan.getRolePriority()
                && Permissions.hasPermission(commandMember.getPermissions(), PermissionType.BAN_MEMBERS)))
            throw new UnauthorizedActionException("User is not authorized to ban this member");

        serverRepository.updateMember(serverId, Member.createBannedMember(memberToBan, banReason));
    }

    default void unbanMember(User commandUser, ObjectId bannedUserId, ObjectId serverId) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member commandMember = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(commandUser.getId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToUnban = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(bannedUserId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandMember.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!memberToUnban.isBanned())
            return;

        if (!Permissions.hasPermission(commandMember.getPermissions(), PermissionType.BAN_MEMBERS))
            throw new UnauthorizedActionException("User is not authorized to unban members");

        serverRepository.removeMember(serverId, bannedUserId);
    }

    default void changeNickName(User commandUser, ObjectId userToChangeId, ObjectId serverId, String newName) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member commandUsingMember = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(commandUser.getId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToUpdate = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(userToChangeId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandUsingMember.isBanned() || memberToUpdate.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!(commandUser.getId().equals(userToChangeId)
                || (Permissions.hasPermission(commandUsingMember.getPermissions(), PermissionType.CHANGE_NICKNAME)
                        && commandUsingMember.getRolePriority() > memberToUpdate.getRolePriority())))
            throw new UnauthorizedActionException("User is not authorized to change nicknames in this server");

        memberToUpdate.setNickName(newName);
        serverRepository.updateMember(serverId, memberToUpdate);
    }

    private static List<ObjectId> sanitizeRoleList(Map<ObjectId, Role> validRoles, List<ObjectId> roleList,
            Comparator<ObjectId> ordering) {
        return roleList.stream().filter(id -> validRoles.containsKey(id)).sorted(ordering).toList();
    }

    default void updateMemberRoles(User roleGiver, ObjectId roleReceiverId, ObjectId serverId,
            MemberRoleUpdateOperation operation) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member roleGiverMember = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(roleGiver.getId()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToUpdate = server.getMembers().stream()
                .filter(m -> m.getUserId().equals(roleReceiverId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (roleGiverMember.isBanned() || memberToUpdate.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(roleGiverMember.getPermissions(), PermissionType.MANAGE_ROLES))
            throw new UnauthorizedActionException("User is not authorized to manage roles in this server");

        Map<ObjectId, Role> roles = server.getRoles();
        Comparator<ObjectId> orderByDescendingPriority = (id1, id2) -> roles.get(id1)
                .compareTo(roles.get(id2));

        List<ObjectId> rolesToAdd = sanitizeRoleList(roles, operation.getRolesToAdd(), orderByDescendingPriority);
        List<ObjectId> rolesToRemove = sanitizeRoleList(roles, operation.getRolesToRemove(), orderByDescendingPriority);

        int highestRolePriority = roleGiverMember.isOwner() ? Integer.MAX_VALUE
                : roles.get(roleGiverMember.getRoleIds().get(0)).getPriority();
        int highestAddPriority = rolesToAdd.isEmpty() ? 0 : roles.get(rolesToAdd.get(0)).getPriority();
        int highestRemovePriority = rolesToRemove.isEmpty() ? 0 : roles.get(rolesToRemove.get(0)).getPriority();

        if (highestAddPriority >= highestRolePriority || highestRemovePriority >= highestRolePriority)
            throw new UnauthorizedActionException(
                    "User is not authorized to grant or revoke roles greater than / equal to their highest role");

        List<ObjectId> roleIds = Stream
                .concat(memberToUpdate.getRoleIds().stream().filter(id -> !rolesToRemove.contains(id)),
                        rolesToAdd.stream())
                .distinct()
                .sorted(orderByDescendingPriority).toList();

        memberToUpdate.setRoleIds(roleIds);
        long permissions = roleIds.stream().reduce(
                Permissions.NONE,
                (accumulator, id) -> accumulator | roles.get(id).getPermissions(),
                (accumulator, accumulator2) -> accumulator | accumulator2);

        memberToUpdate.setPermissions(permissions);
        memberToUpdate.setRolePriority(roles.get(roleIds.get(0)).getPriority());
        serverRepository.updateMember(server.getId(), memberToUpdate);
    }
}
