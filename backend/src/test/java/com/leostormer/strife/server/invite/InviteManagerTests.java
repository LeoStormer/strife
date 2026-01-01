package com.leostormer.strife.server.invite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerServiceTestSetup;

public class InviteManagerTests extends ServerServiceTestSetup {
    @Autowired
    private InviteRepository inviteRepository;

    private static final int NUM_INVITES = 6;

    private static final long SEVEN_DAYS = 604800;

    private static final int MAX_ALLOWED_USES = 100;

    @NonNull
    @SuppressWarnings("null")
    private String inviteId;

    @NonNull
    @SuppressWarnings("null")
    private String unlimitedInviteId;

    @NonNull
    @SuppressWarnings("null")
    private String inviteByBannedUserId;

    @NonNull
    @SuppressWarnings("null")
    private String inviteWithNoRemainingUsesId;

    @NonNull
    @SuppressWarnings("null")
    private String expiredInviteId;

    @BeforeEach
    public void setupInvites() {
        Server existingServer = serverRepository.findById(existingServerId).get();
        inviteId = inviteRepository.save(new Invite(owner, existingServer, SEVEN_DAYS, MAX_ALLOWED_USES)).getId();
        expiredInviteId = inviteRepository.save(new Invite(owner, existingServer, -SEVEN_DAYS, MAX_ALLOWED_USES)).getId();
        inviteWithNoRemainingUsesId = inviteRepository.save(new Invite(owner, existingServer, NUM_INVITES, MAX_ALLOWED_USES, 0)).getId();
        unlimitedInviteId = inviteRepository.save(new Invite(bannedUser, existingServer, SEVEN_DAYS, Invite.UNLIMITED_USES)).getId();

        for (int i = 0; i < NUM_INVITES - 4; i++) {
            Invite invite = new Invite(owner, existingServer, SEVEN_DAYS, MAX_ALLOWED_USES);
            inviteRepository.save(invite);
        }

        inviteByBannedUserId = unlimitedInviteId;
    }

    @AfterEach
    public void cleanup() {
        inviteRepository.deleteAll();
    }

    @Test
    @Transactional
    public void shouldJoinByInvite() {
        serverService.joinByInvite(nonMemberUser, inviteId);
        assertTrue(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
        Invite invite = inviteRepository.findById(inviteId).get();
        assertEquals(MAX_ALLOWED_USES - 1, invite.getRemainingUses());
    }

    @Test
    @Transactional
    public void shouldJoinByInviteEvenIfInviteCreatorHasSinceBeenBanned() {
        serverService.joinByInvite(nonMemberUser, inviteByBannedUserId);
        assertTrue(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
    }

    @Test
    @Transactional
    public void shouldJoinByInviteWithUnlimitedUses() {
        serverService.joinByInvite(nonMemberUser, unlimitedInviteId);
        assertTrue(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
        Invite invite = inviteRepository.findById(unlimitedInviteId).get();
        assertEquals(Invite.UNLIMITED_USES, invite.getRemainingUses());
    }

    @Test
    public void shouldNotJoinByInviteIfBanned() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.joinByInvite(bannedUser, inviteId);
        });
        assertFalse(serverRepository.isMember(existingServerId, bannedUser.getId()));
    }

    @Test
    public void shouldNotJoinByExpiredInvite() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.joinByInvite(nonMemberUser, expiredInviteId);
        });
        assertFalse(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
    }

    @Test
    public void shouldNotJoinByInviteWithNoRemainingUses() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.joinByInvite(nonMemberUser, inviteWithNoRemainingUsesId);
        });
        assertFalse(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
    }

    @Test
    public void shouldGetInvites() {
        assertEquals(NUM_INVITES, serverService.getInvites(owner, existingServerId).size());
    }

    @Test
    public void shouldNotGetInvitesWithoutPermission() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getInvites(basicMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getInvites(noPermissionsUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getInvites(nonMemberUser, existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.getInvites(bannedUser, existingServerId);
        });
    }

    @Test
    public void shouldCreateInvite() {
        serverService.createInvite(owner, existingServerId, SEVEN_DAYS, Invite.UNLIMITED_USES);
        serverService.createInvite(moderator, existingServerId, SEVEN_DAYS, Invite.UNLIMITED_USES);
        assertEquals(NUM_INVITES + 2, inviteRepository.findAll().size());
    }

    @Test
    public void shouldNotCreateInviteWithoutPermission() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.createInvite(basicMemberUser, existingServerId, SEVEN_DAYS, MAX_ALLOWED_USES);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.createInvite(noPermissionsUser, existingServerId, SEVEN_DAYS, MAX_ALLOWED_USES);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.createInvite(nonMemberUser, existingServerId, SEVEN_DAYS, MAX_ALLOWED_USES);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.createInvite(bannedUser, existingServerId, SEVEN_DAYS, MAX_ALLOWED_USES);
        });
    }

    @Test
    public void shouldDeleteInvite() {
        serverService.deleteInvite(owner, existingServerId, inviteId);
        assertFalse(inviteRepository.existsById(inviteId));
    }

    @Test
    public void shouldNotDeleteInviteWithoutPermission() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteInvite(basicMemberUser, existingServerId, expiredInviteId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteInvite(noPermissionsUser, existingServerId, expiredInviteId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteInvite(nonMemberUser, existingServerId, expiredInviteId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.deleteInvite(bannedUser, existingServerId, expiredInviteId);
        });
        assertTrue(inviteRepository.existsById(expiredInviteId));
    }
}
