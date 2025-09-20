package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.member.MemberRoleUpdateOperation;

public class MemberManagerTests extends ServerServiceTestSetup {
    @Test
    public void shouldJoinServer() {
        serverService.joinServer(nonMemberUser, existingServerId);
        assertTrue(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
    }

    @Test
    public void shouldNotJoinServerIfBanned() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.joinServer(bannedUser, existingServerId);
        });
    }

    @Test
    public void shouldLeaveServer() {
        serverService.leaveServer(moderator, existingServerId);
        assertFalse(serverRepository.isMember(existingServerId, moderator.getId()));
        serverService.leaveServer(noPermissionsUser, existingServerId);
        assertFalse(serverRepository.isMember(existingServerId, noPermissionsUser.getId()));
    }

    @Test
    public void shouldNotLeaveServerIfOwner() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.leaveServer(owner, existingServerId);
        });
        assertTrue(serverRepository.isMember(existingServerId, owner.getId()));
    }

    @Test
    public void shouldNotLeaveServerIfBanned() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.leaveServer(bannedUser, existingServerId);
        });
    }

    @Test
    public void shouldKickMember() {
        serverService.kickMember(moderator, noPermissionsUser.getId(), existingServerId);
        assertFalse(serverRepository.isMember(existingServerId, noPermissionsUser.getId()));
        serverService.kickMember(owner, moderator.getId(), existingServerId);
        assertFalse(serverRepository.isMember(existingServerId, moderator.getId()));
    }

    @Test
    public void shouldNotKickMemberWithoutPermission() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(basicMemberUser, noPermissionsUser.getId(), existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(nonMemberUser, noPermissionsUser.getId(), existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(bannedUser, noPermissionsUser.getId(), existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(noPermissionsUser, basicMemberUser.getId(), existingServerId);
        });
    }

    @Test
    public void shouldNotKickMemberWithEqualOrHigherRole() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(basicMemberUser, moderator.getId(), existingServerId);
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(moderator, owner.getId(), existingServerId);
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(moderator, moderator.getId(), existingServerId);
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.kickMember(owner, owner.getId(), existingServerId);
        });
    }

    @Test
    public void shouldBanMember() {
        serverService.banMember(moderator, noPermissionsUser.getId(), existingServerId, "Test Reason");
        assertFalse(serverRepository.isMember(existingServerId, noPermissionsUser.getId()));
        serverService.banMember(owner, moderator.getId(), existingServerId, "Test Reason");
        assertFalse(serverRepository.isMember(existingServerId, moderator.getId()));
        serverService.banMember(owner, nonMemberUser.getId(), existingServerId, "I don't like you");
        // can ban users not in the server
        assertFalse(serverRepository.isMember(existingServerId, nonMemberUser.getId()));
    }

    @Test
    public void shouldNotBanMemberWithoutPermission() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(basicMemberUser, noPermissionsUser.getId(), existingServerId, "Test");
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(nonMemberUser, noPermissionsUser.getId(), existingServerId, "Test");
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(bannedUser, noPermissionsUser.getId(), existingServerId, "Test");
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(noPermissionsUser, basicMemberUser.getId(), existingServerId, "Test");
        });
    }

    @Test
    public void shouldNotBanMemberWithEqualOrHigherRole() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(basicMemberUser, moderator.getId(), existingServerId, "Test");
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(moderator, owner.getId(), existingServerId, "Test");
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(moderator, moderator.getId(), existingServerId, "Test");
        });

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.banMember(owner, owner.getId(), existingServerId, "Test");
        });
    }

    @Test
    public void shouldUnbanMember() {
        serverService.unbanMember(moderator, bannedUser.getId(), existingServerId);
        assertTrue(serverRepository.getMember(existingServerId, bannedUser.getId()).isEmpty());
    }

    @Test
    public void shouldNotUnbanMemberWithoutPermission() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.unbanMember(basicMemberUser, bannedUser.getId(), existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.unbanMember(noPermissionsUser, bannedUser.getId(), existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.unbanMember(nonMemberUser, bannedUser.getId(), existingServerId);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.unbanMember(bannedUser, bannedUser.getId(), existingServerId);
        });
    }

    @Test
    public void shouldChangeNicknameOfSelf() {
        String newNickName = "New Name";
        serverService.changeNickName(basicMemberUser, basicMemberUser.getId(), existingServerId, newNickName);
        Member member = serverRepository.getMember(existingServerId, basicMemberUser.getId()).get();
        assertTrue(member.getNickName().equals(newNickName));
    }

    @Test
    public void shouldNotChangeNicknameOfSelfWithoutPermision() {
        String nickname = "New Name";
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickName(noPermissionsUser, noPermissionsUser.getId(), existingServerId, nickname);
        });
    }

    @Test
    public void shouldChangeNicknameOfLowerRankedMember() {
        String newNickName = "New Name";
        serverService.changeNickName(moderator, basicMemberUser.getId(), existingServerId, newNickName);
        Member member = serverRepository.getMember(existingServerId, basicMemberUser.getId()).get();
        assertTrue(member.getNickName().equals(newNickName));
    }

    @Test
    public void shouldNotChangeNicknameOfOthersWithoutPermission() {
        String newNickName = "New Name";

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickName(noPermissionsUser, basicMemberUser.getId(), existingServerId, newNickName);
        });
        Member member = serverRepository.getMember(existingServerId, basicMemberUser.getId()).get();
        assertTrue(member.getNickName().equals(basicMemberUser.getUsername()));

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickName(basicMemberUser, noPermissionsUser.getId(), existingServerId, newNickName);
        });
        Member noPermissionsMember = serverRepository.getMember(existingServerId, noPermissionsUser.getId()).get();
        assertTrue(noPermissionsMember.getNickName().equals(noPermissionsUser.getUsername()));
    }

    @Test
    public void shouldNotChangeNicknameOfMemberWithHigherRole() {
        String newNickName = "New Name";
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickName(moderator, owner.getId(), existingServerId, newNickName);
        });
        Member ownerMember = serverRepository.getMember(existingServerId, owner.getId()).get();
        assertTrue(ownerMember.getNickName().equals(owner.getUsername()));
    }

    @Test
    public void shouldUpdateRolesOfMember() {
        MemberRoleUpdateOperation operation = new MemberRoleUpdateOperation(List.of(defaultRoleId), List.of());
        serverService.updateMemberRoles(moderator, noPermissionsUser.getId(), existingServerId, operation);
        Member noPermissionsMember = serverRepository.getMember(existingServerId, noPermissionsUser.getId()).get();
        assertTrue(noPermissionsMember.getRoleIds().stream().anyMatch(id -> id.equals(defaultRoleId)));

        MemberRoleUpdateOperation operation2 = new MemberRoleUpdateOperation(List.of(defaultRoleId),
                List.of(moderatorRoleId));
        serverService.updateMemberRoles(owner, moderator.getId(), existingServerId, operation2);
        Member moderatorMember = serverRepository.getMember(existingServerId, moderator.getId()).get();
        assertTrue(moderatorMember.getRoleIds().stream().anyMatch(id -> id.equals(defaultRoleId)));
        assertTrue(moderatorMember.getRoleIds().stream().noneMatch(id -> id.equals(moderatorRoleId)));
    }

    @Test
    public void shouldNotUpdateRolesOfMemberWithoutPermission() {
        MemberRoleUpdateOperation operation = new MemberRoleUpdateOperation(List.of(defaultRoleId), List.of());
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(basicMemberUser, basicMemberUser.getId(), existingServerId, operation);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(noPermissionsUser, basicMemberUser.getId(), existingServerId, operation);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(nonMemberUser, basicMemberUser.getId(), existingServerId, operation);
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(bannedUser, basicMemberUser.getId(), existingServerId, operation);
        });
    }

    @Test
    public void shouldNotUpdateRolesEqualOrHigherThanAccessed() {
        MemberRoleUpdateOperation addHigherRole = new MemberRoleUpdateOperation(List.of(grandAdministratorRoleId),
                List.of());
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(moderator, moderator.getId(), existingServerId, addHigherRole);
        });

        MemberRoleUpdateOperation removeOwnHighestRole = new MemberRoleUpdateOperation(List.of(),
                List.of(moderatorRoleId));
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(moderator, moderator.getId(), existingServerId, removeOwnHighestRole);
        });

        MemberRoleUpdateOperation ownerRemovesOwnerRole = new MemberRoleUpdateOperation(List.of(),
                List.of(ownerRoleId));
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.updateMemberRoles(owner, owner.getId(), existingServerId, ownerRemovesOwnerRole);
        });
    }
}
