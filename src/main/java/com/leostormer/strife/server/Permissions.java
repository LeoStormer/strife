package com.leostormer.strife.server;

import java.util.List;

import com.leostormer.strife.server.role.Role;

public class Permissions {
    public static final long NONE = 0L;
    public static final long ALL = ~NONE;

    public static boolean hasPermission(long permissions, PermissionType permissionType) {
        return (permissions & PermissionType.ADMINISTRATOR.getValue()) != 0
                || (permissions & permissionType.getValue()) != 0;
    }

    public static long grantPermission(long currentPermissions, PermissionType... permissionTypes) {
        long newPermissions = currentPermissions;
        for (PermissionType type : permissionTypes)
            newPermissions |= type.getValue();
        return newPermissions;
    }

    public static long combinePermissions(long permissions1, long permissions2) {
        return permissions1 | permissions2;
    }

    public static long combinePermissions(List<Long> permissions) {
        return permissions.stream().reduce(NONE,
                (subPermissions, currentPermission) -> combinePermissions(subPermissions, currentPermission),
                (perm1, perm2) -> combinePermissions(perm1, perm2));
    }

    public static long revokePermission(long currentPermissions, PermissionType... permissionTypes) {
        long permissionAccumulator = 0L;
        for (PermissionType type : permissionTypes) {
            permissionAccumulator |= type.getValue();
        }

        return currentPermissions &= ~permissionAccumulator; // Clear the specified permissions
    }

    public static long getPermissions(PermissionType... permissionTypes) {
        return grantPermission(NONE, permissionTypes);
    }

    public static long getPermissions(List<Role> roles) {
        return roles.stream().reduce(Permissions.NONE,
                (subPermissions, role) -> Permissions.combinePermissions(subPermissions, role.getPermissions()),
                (perm1, perm2) -> Permissions.combinePermissions(perm1, perm2));
    }
}
