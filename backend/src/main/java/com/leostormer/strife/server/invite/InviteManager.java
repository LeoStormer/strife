package com.leostormer.strife.server.invite;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.member.MemberService;
import com.leostormer.strife.server.IUsesMemberService;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.user.User;

public interface InviteManager extends IUsesServerRepository, IUsesMemberService {
    public InviteRepository getInviteRepository();

    @Transactional
    @SuppressWarnings("null")
    default void joinByInvite(User user, String inviteId) {
        InviteRepository inviteRepository = getInviteRepository();
        MemberService memberService = getMemberService();
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        Server server = invite.getServer();
        Optional<Member> member = memberService.getMember(server.getId(), user.getId());
        if (member.isPresent()) { // User is either already a member, or they are banned
            if (member.get().isBanned()) {
                throw new UnauthorizedActionException(USER_IS_BANNED);
            }

            return;
        }

        if (invite.isExpired() || !invite.hasRemainingUses())
            throw new UnauthorizedActionException("Invite not valid");

        invite.decrementUsesIfLimited();
        inviteRepository.save(invite);
        memberService.joinServer(user, server);
    }

    default List<Invite> getInvites(User commandUser, ObjectId serverId) {
        Member member = getServerRepository().getMember(serverId, commandUser.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_SERVER))
            throw new UnauthorizedActionException("User is not authorized to view invites");

        return getInviteRepository().findAllByServer(serverId);
    }

    @SuppressWarnings("null")
    default Invite createInvite(User commandUser, ObjectId serverId, long expiresAfter, int maxUses) {
        ServerRepository serverRepository = getServerRepository();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));
        Member member = serverRepository.getMember(serverId, commandUser.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasAllPermissions(member.getPermissions(), PermissionType.CREATE_INVITE))
            throw new UnauthorizedActionException("User is not authorized to create invites");

        return getInviteRepository().save(new Invite(commandUser, server, expiresAfter, maxUses));
    }

    @SuppressWarnings("null")
    default void deleteInvite(User commandUser, ObjectId serverId, String inviteId) {
        Member member = getServerRepository().getMember(serverId, commandUser.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasAllPermissions(member.getPermissions(), PermissionType.MANAGE_SERVER,
                PermissionType.CREATE_INVITE))
            throw new UnauthorizedActionException("User is not authorized to delete invites");

        getInviteRepository().deleteById(inviteId);
    }
}
