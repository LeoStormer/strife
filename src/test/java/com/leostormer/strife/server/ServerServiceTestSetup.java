package com.leostormer.strife.server;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.server.channel.Channel;
import com.leostormer.strife.server.channel.ChannelRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ServerServiceTestSetup {
    @Autowired
    protected ServerRepository serverRepository;

    @Autowired
    protected ChannelRepository channelRepository;

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

    private static User createUser(String userName, UserRepository userRepository) {
        User user = new User();
        user.setUsername(userName);
        return userRepository.save(user);
    }

    @BeforeAll
    public static void setupUsers(@Autowired UserRepository userRepository) {
        owner = createUser("owner", userRepository);
        moderator = createUser("moderator", userRepository);
        basicMemberUser = createUser("basicUser", userRepository);
        noPermissionsUser = createUser("noPermissions", userRepository);
        nonMemberUser = createUser("nonMember", userRepository);
        bannedUser = createUser("bannedUser", userRepository);
    }

    @AfterAll
    public static void clearUsers(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        Server existingServer = new Server();
        existingServer.setName("TestServer");
        existingServer.setDescription("A test Server");
        existingServer.setOwner(owner);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        existingServer.getRoles().put(ownerRole.getId(), ownerRole);
        ownerRoleId = ownerRole.getId();

        Role grandAdministratorRole = new Role(new ObjectId(), "Grand Administrator", 2, Permissions.ALL);
        existingServer.getRoles().put(grandAdministratorRole.getId(), grandAdministratorRole);
        grandAdministratorRoleId = grandAdministratorRole.getId();

        Role moderatorRole = new Role(new ObjectId(), "Moderator", 1,
                Permissions.revokePermission(Permissions.ALL,
                        PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER));
        existingServer.getRoles().put(moderatorRole.getId(), moderatorRole);
        moderatorRoleId = moderatorRole.getId();

        Role defaultRole = new Role(new ObjectId(), "Member", 0,
                Permissions.getPermissions(PermissionType.SEND_MESSAGES, PermissionType.VIEW_CHANNELS));
        existingServer.getRoles().put(defaultRole.getId(), defaultRole);
        defaultRoleId = defaultRole.getId();

        Member ownerMember = Member.fromUser(owner, ownerRole);
        ownerMember.setOwner(true);
        existingServer.getMembers().add(ownerMember);

        Member moderatorMember = Member.fromUser(moderator, moderatorRole);
        existingServer.getMembers().add(moderatorMember);

        Member basicMember = Member.fromUser(basicMemberUser, defaultRole);
        existingServer.getMembers().add(basicMember);

        Member noPermissionMember = Member.fromUser(noPermissionsUser);
        existingServer.getMembers().add(noPermissionMember);

        Member bannedMember = Member.fromUser(bannedUser);
        bannedMember.setBanned(true);
        bannedMember.setBanReason("Because i can");
        existingServer.getMembers().add(bannedMember);

        existingServer = serverRepository.save(existingServer);
        existingServerId = existingServer.getId();

        channel1Id = channelRepository.save(Channel.builder().server(existingServer).category("General")
                .description("A general channel.").name("general").build()).getId();
        channel2Id = channelRepository.save(Channel.builder().server(existingServer).category("General")
                .description("An events channel.").name("events").build()).getId();

        Map<ObjectId, Long> rolePermissions = new HashMap<>();
        rolePermissions.put(grandAdministratorRoleId, Permissions.ALL);
        rolePermissions.put(moderatorRoleId, Permissions.revokePermission(moderatorRole.getPermissions(),
                PermissionType.ADMINISTRATOR, PermissionType.MANAGE_CHANNELS));
        adminOnlyPrivateChannelId = channelRepository
                .save(Channel.builder().server(existingServer).category("Admin")
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
