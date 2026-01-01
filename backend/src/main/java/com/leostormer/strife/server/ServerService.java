package com.leostormer.strife.server;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.leostormer.strife.server.ServerExceptionMessage.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.exceptions.ResourceNotFoundException;
import com.leostormer.strife.exceptions.UnauthorizedActionException;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.member.MemberRoleUpdateOperation;
import com.leostormer.strife.member.MemberService;
import com.leostormer.strife.message.Message;
import com.leostormer.strife.message.MessageRepository;
import com.leostormer.strife.message.MessageSearchOptions;
import com.leostormer.strife.server.invite.InviteManager;
import com.leostormer.strife.server.invite.InviteRepository;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.server.role.RoleManager;
import com.leostormer.strife.server.server_channel.ChannelManager;
import com.leostormer.strife.server.server_channel.ServerChannel;
import com.leostormer.strife.user.User;
import com.mongodb.lang.Nullable;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ServerService implements RoleManager, ChannelManager, InviteManager {
    @Autowired
    private final ServerRepository serverRepository;

    @Autowired
    private final MessageRepository messageRepository;

    @Autowired
    private final ChannelRepository channelRepository;

    @Autowired
    private final InviteRepository inviteRepository;

    @Autowired
    private final MemberService memberService;

    @Override
    public long getPermissions(ServerChannel channel, Member member) {
        return (channel.isPublic() || member.isOwner()) ? member.getPermissions() : channel.getPermissions(member);
    }

    public void joinServer(User user, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        memberService.joinServer(user, server);
    }

    public void leaveServer(User user, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        memberService.leaveServer(user, server);
    }

    public void kickMember(User commandUser, ObjectId userToKickId, ObjectId serverId) {
        if (!serverRepository.existsById(serverId)) {
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);
        }

        Member commandMember = memberService.getMember(commandUser.getId(), serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToKick = memberService.getMember(userToKickId, serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandMember.isBanned() || memberToKick.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!(commandMember.getRolePriority() > memberToKick.getRolePriority()
                && Permissions.hasPermission(commandMember.getPermissions(), PermissionType.KICK_MEMBERS)))
            throw new UnauthorizedActionException("User is not authorized to kick this member");

        memberService.removeMember(userToKickId, serverId);
    }

    public void banMember(User commandUser, ObjectId userToBanId, ObjectId serverId, String banReason) {
        if (!serverRepository.existsById(serverId)) {
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);
        }

        Member commandMember = memberService.getMember(commandUser.getId(), serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandMember.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        Optional<Member> memberToBan = memberService.getMember(userToBanId, serverId);

        if (memberToBan.isPresent() && !(commandMember.getRolePriority() > memberToBan.get().getRolePriority()
                && Permissions.hasPermission(commandMember.getPermissions(), PermissionType.BAN_MEMBERS))) {
            throw new UnauthorizedActionException("User is not authorized to ban this member");
        }

        memberService.banMember(userToBanId, serverId, banReason);
    }

    public void unbanMember(User commandUser, ObjectId bannedUserId, ObjectId serverId) {
        if (!serverRepository.existsById(serverId)) {
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);
        }

        Member commandMember = memberService.getMember(commandUser.getId(), serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandMember.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(commandMember.getPermissions(), PermissionType.BAN_MEMBERS))
            throw new UnauthorizedActionException("User is not authorized to unban members");

        memberService.unbanMember(bannedUserId, serverId);
    }

    public void changeNickname(User commandUser, ObjectId userToChangeId, ObjectId serverId, String newName) {
        if (!serverRepository.existsById(serverId)) {
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);
        }

        Member commandUsingMember = memberService.getMember(commandUser.getId(), serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        Member memberToUpdate = memberService.getMember(userToChangeId, serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (commandUsingMember.isBanned() || memberToUpdate.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        long permissions = commandUsingMember.getPermissions();
        if (!((commandUser.getId().equals(userToChangeId)
                && Permissions.hasAnyPermissions(permissions, PermissionType.CHANGE_NICKNAME,
                        PermissionType.MANAGE_NICKNAMES))
                || (Permissions.hasPermission(permissions, PermissionType.MANAGE_NICKNAMES)
                        && commandUsingMember.getRolePriority() > memberToUpdate.getRolePriority())))
            throw new UnauthorizedActionException("User is not authorized to change nicknames in this server");

        memberService.changeNickname(userToChangeId, serverId, newName);
    }

    private static List<ObjectId> sanitizeRoleList(Map<ObjectId, Role> validRoles, List<ObjectId> roleList,
            Comparator<ObjectId> ordering) {
        return roleList == null ? List.of()
                : roleList.stream().filter(id -> validRoles.containsKey(id)).sorted(ordering).toList();
    }

    public void updateMemberRoles(User roleGiver, ObjectId roleReceiverId, ObjectId serverId,
            MemberRoleUpdateOperation operation) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        Member roleGiverMember = memberService.getMember(roleGiver.getId(), serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (!Permissions.hasPermission(roleGiverMember.getPermissions(), PermissionType.MANAGE_ROLES))
            throw new UnauthorizedActionException("User is not authorized to manage roles in this server");

        Member memberToUpdate = memberService.getMember(roleReceiverId, serverId)
                .orElseThrow(() -> new UnauthorizedActionException(USER_NOT_MEMBER));

        if (roleGiverMember.isBanned() || memberToUpdate.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

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

        int rolePriority = roleIds.size() > 0 ? roles.get(roleIds.get(0)).getPriority() : 0;
        memberService.updateMemberRoles(roleReceiverId, serverId, rolePriority, permissions, roleIds);
    }

    @Transactional
    public Server createServer(User owner, String serverName, String serverDescription) {
        Server server = new Server();
        server.setName(serverName);
        server.setDescription(serverDescription);
        server.setOwner(owner);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        server.getRoles().put(ownerRole.getId(), ownerRole);
        Role defaultRole = new Role(new ObjectId(), "Member", 0,
                Permissions.getPermissions(PermissionType.SEND_MESSAGES,
                        PermissionType.VIEW_CHANNELS, PermissionType.CHANGE_NICKNAME));
        server.getRoles().put(defaultRole.getId(), defaultRole);
        server = serverRepository.save(server);

        Member ownerMember = Member.from(owner, server, ownerRole, defaultRole);
        ownerMember.setOwner(true);
        memberService.joinServer(owner, server);

        String defaultChannelName = "general";
        String defaultChannelCategory = "General";
        String defaultChannelDescription = "The general discussion channel.";
        createChannel(server, defaultChannelName, defaultChannelCategory, defaultChannelDescription,
                true);

        return server;
    }

    public void updateServerDetails(User commandUser, ObjectId serverId, @Nullable String name,
            @Nullable String description) {
        Member member = serverRepository.getMember(serverId, commandUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        if (!Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_SERVER))
            throw new UnauthorizedActionException("User is not authorized to alter server details");

        if (name == null && description == null)
            return;

        serverRepository.updateServerDetails(serverId, name, description);
    }

    @Transactional
    public void transferServerOwnership(User commandUser, User newOwner, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        if (!commandUser.getId().equals(server.getOwner().getId()))
            throw new UnauthorizedActionException("User is not authorized to transfer server ownership");

        Member newOwnerMember = memberService.getMember(newOwner.getId(), serverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (newOwnerMember.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        Member oldOwner = memberService.getMember(commandUser.getId(), serverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        Map<ObjectId, Role> serverRoles = server.getRoles();
        ObjectId ownerRole = oldOwner.getRoleIds().remove(0);
        oldOwner.setPermissions(Permissions.combinePermissions(
                oldOwner.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));
        oldOwner.setOwner(false);
        oldOwner.setPermissions(Permissions.combinePermissions(
                oldOwner.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));

        newOwnerMember.getRoleIds().add(0, ownerRole);
        newOwnerMember.setOwner(true);
        newOwnerMember.setPermissions(Permissions.combinePermissions(
                newOwnerMember.getRoleIds().stream().map(id -> serverRoles.get(id).getPermissions()).toList()));
        memberService.save(oldOwner, newOwnerMember);

        server.setOwner(newOwner);
        serverRepository.save(server);
    }

    @Transactional
    public void deleteServer(User owner, ObjectId serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVER_NOT_FOUND));

        if (!server.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedActionException("User is not authorized to delete this server");
        }

        ObjectId[] channelIds = channelRepository.findAllByServerId(serverId).stream().map(c -> c.getId())
                .toArray(ObjectId[]::new);
        removeChannel(owner, serverId, channelIds);
        serverRepository.deleteById(serverId);
    }

    public List<Message> getMessages(User user, ObjectId serverId, ObjectId channelId,
            MessageSearchOptions searchOptions) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned())
            throw new UnauthorizedActionException(USER_IS_BANNED);

        ServerChannel channel = getChannelInServer(serverId, channelId);

        if (!Permissions.hasPermission(getPermissions(channel, member), PermissionType.VIEW_CHANNELS))
            throw new UnauthorizedActionException("User is not authorized to view messages in this channel");

        List<Message> messages = messageRepository.getMessages(channelId, searchOptions);
        messages.sort(Message.sortByTimestampAscending);
        return messages;
    }

    public Message sendMessage(User user, ObjectId serverId, ObjectId channelId, String content) {
        if (!serverRepository.existsById(serverId)) {
            throw new ResourceNotFoundException(SERVER_NOT_FOUND);
        }

        Member member = memberService.getMember(user.getId(), serverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        ServerChannel channel = getChannelInServer(serverId, channelId);
        if (!Permissions.hasPermission(getPermissions(channel, member), PermissionType.SEND_MESSAGES)) {
            throw new UnauthorizedActionException("User is not authorized to send messages in this channel");
        }

        return messageRepository.insertMessage(user, channel, content);
    }

    public Message editMessage(User user, ObjectId serverId, ObjectId messageId, String newContent) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("User is not authorized to edit this message");
        }

        return messageRepository.updateMessage(messageId, newContent);
    }

    public void deleteMessage(User user, ObjectId serverId, ObjectId channelId, ObjectId messageId) {
        Member member = serverRepository.getMember(serverId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_MEMBER));

        if (member.isBanned()) {
            throw new UnauthorizedActionException(USER_IS_BANNED);
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        if (!(message.getSender().getId().equals(user.getId())
                || Permissions.hasPermission(member.getPermissions(), PermissionType.MANAGE_MESSAGES))) {
            throw new UnauthorizedActionException("User is not authorized to delete this message");
        }

        messageRepository.deleteById(messageId);
    }

    @Override
    public ServerRepository getServerRepository() {
        return serverRepository;
    }

    @Override
    public ChannelRepository getChannelRepository() {
        return channelRepository;
    }

    @Override
    public InviteRepository getInviteRepository() {
        return inviteRepository;
    }

    @Override
    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    @Override
    public MemberService getMemberService() {
        return memberService;
    }
}
