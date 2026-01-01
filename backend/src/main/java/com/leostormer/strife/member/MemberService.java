package com.leostormer.strife.member;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.user.User;


import static com.leostormer.strife.server.ServerExceptionMessage.*;

@Service
public class MemberService {
    @Autowired
    MemberRepository memberRepository;

    @SuppressWarnings("null")
    public void save(Member... members) {
        memberRepository.saveAll(List.of(members));
    }
    
    public Optional<Member> getMember(ObjectId userId, ObjectId serverId) {
        return memberRepository.findByUserIdAndServerId(userId, serverId);
    }

    public List<Server> getServersByUserId(ObjectId userId) {
        return memberRepository.findServersByUserId(userId);
    }

    public void joinServer(User user, Server server) {
        Optional<Member> existingMember = memberRepository.findByUserIdAndServerId(user.getId(), server.getId());
        if (existingMember.isPresent()) {
            if (existingMember.get().isBanned()) {
                throw new UnauthorizedActionException(USER_IS_BANNED);
            }
            return;
        }

        memberRepository.save(Member.from(user, server));
    }

    public void leaveServer(User user, Server server) {
        Member member = memberRepository.findByUserIdAndServerId(user.getId(), server.getId())
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (member.isOwner())
            throw new UnauthorizedActionException("Server owner cannot leave the server");

        memberRepository.deleteById(member.getId());
    }

    public void removeMember(ObjectId userId, ObjectId serverId) {
        memberRepository.removeMember(userId, serverId);
    }

    public void banMember(ObjectId userToBanId, ObjectId serverId, String banReason) {
        memberRepository.banMember(userToBanId, serverId, banReason);
    }

    public void unbanMember(ObjectId bannedUserId, ObjectId serverId) {
        Optional<Member> memberToUnban = getMember(bannedUserId, serverId);

        if (!memberToUnban.isPresent() || !memberToUnban.get().isBanned())
            return;

        memberRepository.removeMember(bannedUserId, serverId);
    }

    public void changeNickname(ObjectId userToChangeId, ObjectId serverId, String newName) {
        memberRepository.changeNickname(userToChangeId, serverId, newName);
    }

    public void updateMemberRoles(ObjectId roleReceiverId, ObjectId serverId, int rolePriority, long permissions,
            List<ObjectId> roleIds) {
        memberRepository.updateMemberRoles(roleReceiverId, serverId, rolePriority, permissions, roleIds);
    }
}
