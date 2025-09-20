package com.leostormer.strife.server.invite;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.IUsesServerRepository;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.user.User;

public interface InviteManager extends IUsesServerRepository {
    public InviteRepository getInviteRepository();

    @Transactional
    default void joinByInvite(User user, String inviteId) {
        InviteRepository inviteRepository = getInviteRepository();
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        Server server = invite.getServer();
        Optional<Member> member = server.getMember(user);
        if (member.isPresent()) { // User is either already a member, or they are banned
            if (member.get().isBanned()) {
                throw new UnauthorizedActionException(USER_IS_BANNED);
            }

            return;
        }

        int remainingUses = invite.getRemainingUses();
        boolean usesLimited = invite.getMaxUses() > 0;
        boolean inviteExpired = invite.getExpiresAt().isBefore(Instant.now());
        if (inviteExpired || (usesLimited && remainingUses == 0))
            throw new UnauthorizedActionException("Invite not valid");

        remainingUses -= usesLimited ? 1 : 0;
        invite.setRemainingUses(remainingUses);
        inviteRepository.save(invite);
        getServerRepository().addMember(server.getId(), Member.fromUser(user));
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

    default Invite createInvite(User commandUser, ObjectId serverId, long expiresAfter, int maxUses) {
        Server server = getServerRepository().findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));
        Member member = server.getMember(commandUser.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));
        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasAllPermissions(member.getPermissions(), PermissionType.CREATE_INVITE))
            throw new UnauthorizedActionException("User is not authorized to create invites");

        return getInviteRepository().save(new Invite(commandUser, server, expiresAfter, maxUses));
    }

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
