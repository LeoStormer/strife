package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.server.role.RoleUpdateOperation;

public class RoleManagerTests extends ServerServiceTestSetup {
    @Test
    public void blankUpdateOperationShouldDoNothing() {
        RoleUpdateOperation doNothing = new RoleUpdateOperation(null, null, null);
        Server server = serverRepository.findById(existingServerId).get();
        serverService.updateRoles(owner, existingServerId, doNothing);
        Server updatedServer = serverRepository.findById(existingServerId).get();

        assertEquals(server.getRoles(), updatedServer.getRoles());
    }

    @Test
    public void shouldAddRoles() {
        Map<ObjectId, Role> oldRoles = serverRepository.findById(existingServerId).get().getRoles();
        Role moderatorRole = oldRoles.get(moderatorRoleId);
        Role weakerModRole = new Role(null, "Weaker Moderator", moderatorRole.getPriority(),
                moderatorRole.getPermissions());

        List<Role> rolesToAdd = List.of(weakerModRole);
        Stream<Role> expectedRoles = Stream.concat(oldRoles.values().stream(), rolesToAdd.stream());
        RoleUpdateOperation addRole = new RoleUpdateOperation(rolesToAdd, null, null);

        serverService.updateRoles(owner, existingServerId, addRole);
        Map<ObjectId, Role> updatedRoles = serverRepository.findById(existingServerId).get().getRoles();
        moderatorRole = updatedRoles.get(moderatorRoleId);
        weakerModRole = updatedRoles.values().stream().filter(r -> r.getName().equals("Weaker Moderator")).findFirst()
                .get();

        assertTrue(expectedRoles.allMatch(r -> updatedRoles.containsKey(r.getId())));
        // When adding a role with the same priority as an existing role, it should be
        // inserted below the existing role
        assertTrue(moderatorRole.getPriority() > weakerModRole.getPriority());
    }

    @Test
    public void shouldRemoveRoles() {
        List<ObjectId> rolesToRemove = List.of(moderatorRoleId, grandAdministratorRoleId);
        Stream<Role> expectedRoles = serverRepository.findById(existingServerId).get().getRoles().values().stream()
                .filter(r -> !rolesToRemove.contains(r.getId()));
        RoleUpdateOperation removeRole = new RoleUpdateOperation(null, rolesToRemove, null);
        serverService.updateRoles(owner, existingServerId, removeRole);
        Map<ObjectId, Role> updatedRoles = serverRepository.findById(existingServerId).get().getRoles();
        assertTrue(expectedRoles.allMatch(r -> updatedRoles.containsKey(r.getId())));
        assertTrue(rolesToRemove.stream().noneMatch(id -> updatedRoles.containsKey(id)));
    }

    @Test
    public void shouldNotUpdateRolesWithoutPermission() {
        List<Role> rolesToAdd = List.of(new Role(null, "Test Role", 1, Permissions.NONE));
        RoleUpdateOperation addRole = new RoleUpdateOperation(rolesToAdd, null, null);
        Server server = serverRepository.findById(existingServerId).get();

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(basicMemberUser, existingServerId, addRole);
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(noPermissionsUser, existingServerId, addRole);
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(nonMemberUser, existingServerId, addRole);
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(bannedUser, existingServerId, addRole);
        });

        Server updatedServer = serverRepository.findById(existingServerId).get();

        assertEquals(server.getRoles(), updatedServer.getRoles());
    }

    @Test
    public void shouldNotAddRolesHigherThanHighestAccessed() {
        Server server = serverRepository.findById(existingServerId).get();
        Role moderatorRole = server.getRoles().get(moderatorRoleId);
        Role newRole = new Role(null, "Better Role", moderatorRole.getPriority() + 1, Permissions.ALL);

        List<Role> rolesToAdd = List.of(newRole);
        RoleUpdateOperation addRole = new RoleUpdateOperation(rolesToAdd, null, null);

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(moderator, existingServerId, addRole);
        });

        Server updatedServer = serverRepository.findById(existingServerId).get();

        assertEquals(server.getRoles(), updatedServer.getRoles());
    }

    @Test
    public void shouldNotRemoveRolesEqualOrHigherThanHighestAccessed() {
        Server server = serverRepository.findById(existingServerId).get();
        Role moderatorRole = server.getRoles().get(moderatorRoleId);

        List<ObjectId> rolesToRemove = List.of(moderatorRole.getId());
        RoleUpdateOperation removeRole = new RoleUpdateOperation(null, rolesToRemove, null);
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(moderator, existingServerId, removeRole);
        });

        Server updatedServer = serverRepository.findById(existingServerId).get();
        assertEquals(server.getRoles(), updatedServer.getRoles());
    }

    @Test
    public void shouldNotUpdateRolesEqualOrHigherThanHighestAccessed() {
        Server server = serverRepository.findById(existingServerId).get();
        Role moderatorRole = server.getRoles().get(moderatorRoleId);
        Role updatedModeratorRole = new Role(moderatorRole.getId(), "Updated Moderator", moderatorRole.getPriority(),
                Permissions.ALL);

        List<Role> rolesToUpdate = List.of(updatedModeratorRole);
        RoleUpdateOperation updateRole = new RoleUpdateOperation(null, null, rolesToUpdate);
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateRoles(moderator, existingServerId, updateRole);
        });

        Server updatedServer = serverRepository.findById(existingServerId).get();
        assertEquals(server.getRoles(), updatedServer.getRoles());
    }
}
