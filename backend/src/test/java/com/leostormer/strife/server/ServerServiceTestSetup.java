package com.leostormer.strife.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractIntegrationTest;
import com.leostormer.strife.TestUtils;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.member.Member;
import com.leostormer.strife.member.MemberRepository;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.server.server_channel.ServerChannel;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

public class ServerServiceTestSetup extends AbstractIntegrationTest {
    @Autowired
    protected ServerRepository serverRepository;

    @Autowired
    protected ChannelRepository channelRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ServerService serverService;

    protected ObjectId existingServerId;

    protected ObjectId channel1Id;

    protected ObjectId channel2Id;

    protected ObjectId adminOnlyPrivateChannelId;

    protected ObjectId ownerRoleId;

    protected ObjectId grandAdministratorRoleId;

    protected ObjectId moderatorRoleId;

    protected ObjectId defaultRoleId;

    protected static User owner;

    protected static User moderator;

    protected static User basicMemberUser;

    protected static User noPermissionsUser;

    protected static User nonMemberUser;

    protected static User bannedUser;

    @BeforeAll
    public static void setupUsers(@Autowired UserRepository userRepository) {
        owner = TestUtils.createUser("owner", "", userRepository);
        moderator = TestUtils.createUser("moderator", "", userRepository);
        basicMemberUser = TestUtils.createUser("basicUser", "", userRepository);
        noPermissionsUser = TestUtils.createUser("noPermissions", "", userRepository);
        nonMemberUser = TestUtils.createUser("nonMember", "", userRepository);
        bannedUser = TestUtils.createUser("bannedUser", "", userRepository);
    }

    @AfterAll
    public static void clearUsers(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        Role grandAdministratorRole = new Role(new ObjectId(), "Grand Administrator", 2, Permissions.ALL);
        grandAdministratorRoleId = grandAdministratorRole.getId();

        Role moderatorRole = new Role(new ObjectId(), "Moderator", 1,
                Permissions.revokePermission(Permissions.ALL,
                        PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER));
        moderatorRoleId = moderatorRole.getId();

        Role defaultRole = new Role(new ObjectId(), "Member", 0,
                Permissions.getPermissions(PermissionType.SEND_MESSAGES, PermissionType.VIEW_CHANNELS,
                        PermissionType.CHANGE_NICKNAME));
        defaultRoleId = defaultRole.getId();

        Server existingServer = TestUtils.createServer(owner, "TestServer", "A test Server", serverRepository, memberRepository,
                grandAdministratorRole, moderatorRole, defaultRole);

        Role ownerRole = existingServer.getRoles().values().stream().filter(r -> r.getPriority() == Integer.MAX_VALUE)
                .findFirst().get();
        ownerRoleId = ownerRole.getId();
        existingServerId = existingServer.getId();

        TestUtils.createMember(moderator, existingServer, memberRepository, moderatorRole);
        TestUtils.createMember(basicMemberUser, existingServer, memberRepository, defaultRole);
        TestUtils.createMember(noPermissionsUser, existingServer, memberRepository);
        TestUtils.createBannedMember(bannedUser, existingServer, "Because i can", memberRepository);

        channel1Id = channelRepository.save(ServerChannel.builder().server(existingServer).category("General")
                .description("A general channel.").name("general").build()).getId();
        channel2Id = channelRepository.save(ServerChannel.builder().server(existingServer).category("General")
                .description("An events channel.").name("events").build()).getId();

        Map<ObjectId, Long> rolePermissions = new HashMap<>();
        rolePermissions.put(grandAdministratorRoleId, Permissions.ALL);
        rolePermissions.put(moderatorRoleId, Permissions.revokePermission(moderatorRole.getPermissions(),
                PermissionType.ADMINISTRATOR, PermissionType.MANAGE_CHANNELS));
        adminOnlyPrivateChannelId = channelRepository
                .save(ServerChannel.builder().server(existingServer).category("Admin")
                        .description("Admin only channel")
                        .name("admin-discussion").isPublic(false)
                        .rolePermissions(rolePermissions).build())
                .getId();
    }

    @AfterEach
    public void cleanup() {
        channelRepository.deleteAll();
        serverRepository.deleteAll();
    }
}
