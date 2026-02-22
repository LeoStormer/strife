package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.member.MemberRoleUpdateOperation;

public class MemberManagerTests extends ServerServiceTestSetup {
    @Test
    public void shouldJoinServer() {
        serverService.joinServer(nonMemberUser, existingServerId);
        assertTrue(memberRepository.isMember(nonMemberUser.getId(), existingServerId));
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
        assertFalse(memberRepository.isMember(moderator.getId(), existingServerId));
        serverService.leaveServer(noPermissionsUser, existingServerId);
        assertFalse(memberRepository.isMember(noPermissionsUser.getId(), existingServerId));
    }

    @Test
    public void shouldNotLeaveServerIfOwner() {
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.leaveServer(owner, existingServerId);
        });
        assertTrue(memberRepository.isMember(owner.getId(), existingServerId));
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
        assertFalse(memberRepository.isMember(noPermissionsUser.getId(), existingServerId));
        serverService.kickMember(owner, moderator.getId(), existingServerId);
        assertFalse(memberRepository.isMember(moderator.getId(), existingServerId));
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
        assertFalse(memberRepository.isMember(noPermissionsUser.getId(), existingServerId));
        serverService.banMember(owner, moderator.getId(), existingServerId, "Test Reason");
        assertFalse(memberRepository.isMember(moderator.getId(), existingServerId));
        serverService.banMember(owner, nonMemberUser.getId(), existingServerId, "I don't like you");
        // can ban users not in the server
        assertFalse(memberRepository.isMember(nonMemberUser.getId(), existingServerId));
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
        assertTrue(memberRepository.findByUserIdAndServerId(bannedUser.getId(), existingServerId).isEmpty());
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
        String newNickname = "New Name";
        serverService.changeNickname(basicMemberUser, basicMemberUser.getId(), existingServerId, newNickname);
        Member member = memberService.getMember(basicMemberUser.getId(), existingServerId).get();
        assertTrue(member.getNickname().equals(newNickname));
    }

    @Test
    public void shouldNotChangeNicknameOfSelfWithoutPermision() {
        String nickname = "New Name";
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickname(noPermissionsUser, noPermissionsUser.getId(), existingServerId, nickname);
        });
    }

    @Test
    public void shouldChangeNicknameOfLowerRankedMember() {
        String newNickname = "New Name";
        serverService.changeNickname(moderator, basicMemberUser.getId(), existingServerId, newNickname);
        Member member = memberService.getMember(basicMemberUser.getId(), existingServerId).get();
        assertTrue(member.getNickname().equals(newNickname));
    }

    @Test
    public void shouldNotChangeNicknameOfOthersWithoutPermission() {
        String newNickname = "New Name";

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickname(noPermissionsUser, basicMemberUser.getId(), existingServerId, newNickname);
        });
        Member member = memberService.getMember(basicMemberUser.getId(), existingServerId).get();
        assertTrue(member.getNickname().equals(basicMemberUser.getUsername()));

        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickname(basicMemberUser, noPermissionsUser.getId(), existingServerId, newNickname);
        });
        Member noPermissionsMember = memberService.getMember(noPermissionsUser.getId(), existingServerId).get();
        assertTrue(noPermissionsMember.getNickname().equals(noPermissionsUser.getUsername()));
    }

    @Test
    public void shouldNotChangeNicknameOfMemberWithHigherRole() {
        String newNickname = "New Name";
        assertThrows(UnauthorizedActionException.class, () -> {
            serverService.changeNickname(moderator, owner.getId(), existingServerId, newNickname);
        });
        Member ownerMember = memberService.getMember(owner.getId(), existingServerId).get();
        assertTrue(ownerMember.getNickname().equals(owner.getUsername()));
    }

    @Test
    public void shouldUpdateRolesOfMember() {
        MemberRoleUpdateOperation operation = new MemberRoleUpdateOperation(List.of(defaultRoleId), List.of());
        serverService.updateMemberRoles(moderator, noPermissionsUser.getId(), existingServerId, operation);
        Member noPermissionsMember = memberService.getMember(noPermissionsUser.getId(), existingServerId).get();
        assertTrue(noPermissionsMember.getRoleIds().stream().anyMatch(id -> id.equals(defaultRoleId)));

        MemberRoleUpdateOperation operation2 = new MemberRoleUpdateOperation(List.of(defaultRoleId),
                List.of(moderatorRoleId));
        serverService.updateMemberRoles(owner, moderator.getId(), existingServerId, operation2);
        Member moderatorMember = memberService.getMember(moderator.getId(), existingServerId).get();
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
