package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.channel.Channel;
import com.leostormer.strife.server.member.Member;

public class ServerServiceTests extends ServerServiceTestSetup {
    @Test
    public void shouldCreateServer() {
        String serverName = "New Server";
        String serverDescription = "A New Server";
        Server server = serverService.createServer(owner, serverName, serverDescription);
        assertTrue(serverRepository.existsById(server.getId()));
        assertEquals(server.getName(), serverName);
        assertEquals(server.getDescription(), serverDescription);
        assertEquals(server.getOwner().getId(), owner.getId());
    }

    @Test
    public void shouldDeleteServerIfOwner() {
        serverService.deleteServer(owner, existingServerId);
        assertFalse(serverRepository.existsById(existingServerId));
        List<Channel> channels = channelRepository.findAllByServerId(existingServerId);
        assertTrue(channels.size() == 0);
    }

    @Test
    public void shouldNotDeleteServerIfNotOwner() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(basicMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(noPermissionsUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(nonMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteServer(bannedUser, existingServerId);
        });
        assertTrue(serverRepository.existsById(existingServerId));
    }

    @Test
    public void shouldUpdateServerDetails() {
        String newName = "NewServerName";
        String newDescription = "A new server description";

        serverService.updateServerDetails(owner, existingServerId, newName, newDescription);
        Server server = serverRepository.findById(existingServerId).get();
        assertEquals(newName, server.getName());
        assertEquals(newDescription, server.getDescription());
    }

    @Test
    public void shouldUpdateServerDetailsPartially() {
        String newName = "NewServerName";

        Server server = serverRepository.findById(existingServerId).get();
        serverService.updateServerDetails(owner, existingServerId, newName, null);
        Server updatedServer = serverRepository.findById(existingServerId).get();
        assertEquals(newName, updatedServer.getName());
        assertEquals(server.getDescription(), updatedServer.getDescription());
    }

    @Test
    public void shouldNotUpdateServerDetailsWithoutPermisison() {
        String newName = "NewServerName";
        String newDescription = "A new server description";

        Server server = serverRepository.findById(existingServerId).get();
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateServerDetails(basicMemberUser, existingServerId, newName, newDescription);
        });
        assertThrows(ResourceNotFoundException.class, () -> {
            serverService.updateServerDetails(nonMemberUser, existingServerId, newName, newDescription);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateServerDetails(noPermissionsUser, existingServerId, newName, newDescription);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateServerDetails(bannedUser, existingServerId, newName, newDescription);
        });

        Server updatedServer = serverRepository.findById(existingServerId).get();
        assertEquals(server.getName(), updatedServer.getName());
        assertEquals(server.getDescription(), updatedServer.getDescription());
    }

    @Test
    public void shouldTransferServerOwnerShip() {
        serverService.transferServerOwnership(owner, moderator, existingServerId);
        Server server = serverRepository.findById(existingServerId).get();
        Member modMember = server.getMember(moderator).get();
        Member oldOwner = server.getMember(owner).get();
        assertEquals(moderator.getId(), server.getOwner().getId());
        assertTrue(modMember.isOwner());
        assertFalse(oldOwner.isOwner());
    }

    @Test
    public void shouldNotTransferServerOwnershipIfNotOwner() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(moderator, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(basicMemberUser, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(noPermissionsUser, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(nonMemberUser, moderator, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.transferServerOwnership(bannedUser, moderator, existingServerId);
        });

        Server server = serverRepository.findById(existingServerId).get();
        assertEquals(owner.getId(), server.getOwner().getId());
    }
}
