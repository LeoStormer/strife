package com.leostormer.strife.server.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractRepositoryTest;
import com.leostormer.strife.server.PermissionType;
import com.leostormer.strife.server.Permissions;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

public class ChannelRepositoryTests extends AbstractRepositoryTest {
    static Server server;

    static User owner;

    static User moderator;

    static User basicMemberUser;

    static User specialMemberUser;

    static Role moderatorRole;

    static final int NUM_PUBLIC_CHANNELS = 3;

    ObjectId specialChannelId;

    @Autowired
    ChannelRepository channelRepository;

    private static User createUser(String username, UserRepository userRepository) {
        User user = new User();
        user.setUsername(username);
        return userRepository.save(user);
    }

    @BeforeAll
    public static void setupServer(@Autowired ServerRepository serverRepository,
            @Autowired UserRepository userRepository) {
        owner = createUser("owner", userRepository);
        moderator = createUser("moderator", userRepository);
        basicMemberUser = createUser("basic-member", userRepository);
        specialMemberUser = createUser("specialMember", userRepository);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        moderatorRole = new Role(new ObjectId(), "Moderator", 1,
                Permissions.revokePermission(Permissions.ALL, PermissionType.MANAGE_SERVER));
        Role defaultRole = new Role(new ObjectId(), "Member", 0,
                Permissions.getPermissions(PermissionType.VIEW_CHANNELS, PermissionType.SEND_MESSAGES));
        server = new Server();
        server.setName("Test Server");
        server.setDescription("A server for testing");
        server.setRoles(Map.of(
                ownerRole.getId(), ownerRole,
                moderatorRole.getId(), moderatorRole,
                defaultRole.getId(), defaultRole));

        Member ownerMember = Member.fromUser(owner, ownerRole);
        ownerMember.setOwner(true);
        server.setMembers(List.of(
                ownerMember,
                Member.fromUser(moderator, moderatorRole),
                Member.fromUser(basicMemberUser, defaultRole),
                Member.fromUser(specialMemberUser)));
        server = serverRepository.save(server);
    }

    @AfterAll
    public static void cleanupServer(@Autowired ServerRepository serverRepository,
            @Autowired UserRepository userRepository) {
        serverRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    public void setupChannels() {
        for (int i = 0; i < NUM_PUBLIC_CHANNELS; i++) {
            Channel channel = Channel.builder().server(server).name("public-channel-" + i)
                    .description("A public channel for testing").isPublic(true).build();
            channelRepository.save(channel);
        }

        Channel adminOnlyChannel = Channel.builder().server(server).name("admin-only")
                .description("The admin only channel").isPublic(false)
                .rolePermissions(Map.of(moderatorRole.getId(),
                        Permissions.getPermissions(PermissionType.VIEW_CHANNELS, PermissionType.SEND_MESSAGES)))
                .build();
        channelRepository.save(adminOnlyChannel);

        Channel specialChannel = Channel.builder().server(server).name("special-channel")
                .description("A special channel for testing").isPublic(false)
                .userPermissions(Map.of(specialMemberUser.getId(), Permissions.ALL)).build();
        specialChannelId = channelRepository.save(specialChannel).getId();

    }

    @AfterEach
    public void cleanupChannels() {
        channelRepository.deleteAll();
    }

    @Test
    public void shouldFindAllByServerId() {
        List<Channel> channels = channelRepository.findAllByServerId(server.getId());
        assertTrue(channels.size() == 5);
    }

    @Test
    public void shouldDeleteAllByServer() {
        channelRepository.deleteAllByServer(server.getId());
        List<Channel> channels = channelRepository.findAllByServerId(server.getId());
        assertTrue(channels.size() == 0);
    }

    @Test
    public void shouldUpdateChannelSettings() {
        Channel specialChannel = channelRepository.findById(specialChannelId).get();
        String newDescription = "Special channel now for the public";

        ChannelUpdateOperation operation = new ChannelUpdateOperation();
        operation.setDescription(newDescription);
        operation.setIsPublic(true);
        operation.setUserPermissions(Map.of());

        channelRepository.updateChannelSettings(specialChannelId, operation);
        Channel updatedChannel = channelRepository.findById(specialChannelId).get();
        assertEquals(specialChannel.getName(), updatedChannel.getName());
        assertEquals(specialChannel.getCategory(), updatedChannel.getCategory());
        assertEquals(specialChannel.getRolePermissions(), updatedChannel.getRolePermissions());

        assertEquals(newDescription, updatedChannel.getDescription());
        assertTrue(updatedChannel.isPublic());
        assertTrue(updatedChannel.getUserPermissions().isEmpty());
    }

    @Test
    public void shouldGetVisibleChannels() {
        ObjectId serverId = server.getId();
        Member ownerMember = server.getMember(owner).get();
        List<Channel> ownerView = channelRepository.getVisibleChannels(serverId, ownerMember);
        assertTrue(ownerView.size() == 5);

        Member moderatorMember = server.getMember(moderator).get();
        List<Channel> moderatorView = channelRepository.getVisibleChannels(serverId, moderatorMember);
        assertTrue(moderatorView.size() == 4);

        Member basicMember = server.getMember(basicMemberUser).get();
        List<Channel> basicMemberView = channelRepository.getVisibleChannels(serverId, basicMember);
        assertTrue(basicMemberView.size() == NUM_PUBLIC_CHANNELS);

        Member specialMember = server.getMember(specialMemberUser).get();
        List<Channel> specialMemberView = channelRepository.getVisibleChannels(serverId, specialMember);
        assertTrue(specialMemberView.size() == 4);
        assertTrue(specialMemberView.stream().noneMatch(c -> c.getName().equals("admin-only")));
    }
}
