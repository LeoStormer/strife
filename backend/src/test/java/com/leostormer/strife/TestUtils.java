package com.leostormer.strife;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.conversation.Conversation;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.member.MemberRepository;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;
import com.leostormer.strife.user.friends.FriendRequest;
import com.leostormer.strife.user.friends.FriendRequestRepository;

public class TestUtils {

    public static User createUser(String username, String password, UserRepository userRepository) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(username + "@someEmail.com");
        return userRepository.save(user);
    }

    public static void createPendingFriendship(User sender, User receiver,
            FriendRequestRepository friendRequestRepository) {
        FriendRequest friendRequest = new FriendRequest(sender, receiver);
        friendRequestRepository.save(friendRequest);
    }

    @SuppressWarnings("null")
    public static void createAcceptedFriendship(User sender, User receiver, UserRepository userRepository,
            FriendRequestRepository friendRequestRepository) {
        FriendRequest friendRequest = new FriendRequest(sender, receiver, true);
        friendRequestRepository.save(friendRequest);
        sender.getFriends().add(receiver.getId());
        receiver.getFriends().add(sender.getId());
        userRepository.saveAll(List.of(sender, receiver));
    }

    public static void createBlockedRelationship(User sender, User receiver, UserRepository userRepository,
            ChannelRepository conversationRepository) {
        createBlockedRelationship(sender, receiver, false, userRepository, conversationRepository);
    }

    public static void createBlockedRelationship(User sender, User receiver, boolean isMutual, UserRepository userRepository,
            ChannelRepository conversationRepository) {
        Optional<Conversation> result = conversationRepository.findConversationByUserIds(sender.getId(),
                receiver.getId());
        if (result.isPresent() && !result.get().isLocked()) {
            Conversation conversation = result.get();
            conversation.setLocked(true);
            conversationRepository.save(conversation);
        }
        sender.getBlockedUsers().add(receiver.getId());
        userRepository.save(sender);
        if (!isMutual) {
            return;
        }
        receiver.getBlockedUsers().add(sender.getId());
        userRepository.save(receiver);
    }

    public static Server createServer(User owner, String serverName, String serverDescription, ServerRepository serverRepository, MemberRepository memberRepository, Role... roles) {
        Server server = new Server();
        server.setName(serverName);
        server.setDescription(serverDescription);
        server.setOwner(owner);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        server.getRoles().put(ownerRole.getId(), ownerRole);

        for (Role role : roles) {
            server.getRoles().put(role.getId(), role);
        }

        server = serverRepository.save(server);
        memberRepository.save(Member.from(owner, server, ownerRole));
        return server;
    }

    public static Member createMember(User user, Server server, MemberRepository memberRepository) {
        return memberRepository.save(Member.from(user, server));
    }
    public static Member createMember(User user, Server server, MemberRepository memberRepository, Role... roles) {
        return memberRepository.save(Member.from(user, server, roles));
    }

    public static Member createBannedMember(User user, Server server, String banReason, MemberRepository memberRepository) {
        return memberRepository.save(Member.createBannedMember(user, server, banReason));
    }
}
