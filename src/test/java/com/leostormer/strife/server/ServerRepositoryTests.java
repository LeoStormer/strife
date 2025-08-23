package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.leostormer.strife.channel.Channel;
import com.leostormer.strife.channel.ChannelRepository;
import com.leostormer.strife.server.member.Member;
import com.leostormer.strife.server.role.Role;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

@DataMongoTest
@ActiveProfiles("test")
public class ServerRepositoryTests {
    @Autowired
    ServerRepository serverRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    UserRepository userRepository;

    ObjectId existingServerId;

    @BeforeEach
    public void setup() {
        User user1 = new User();
        user1.setUsername("User1");
        user1 = userRepository.save(user1);
        User user2 = new User();
        user2.setUsername("User2");
        userRepository.save(user2);
        User user3 = new User();
        user3.setUsername("User3");
        userRepository.save(user3);
        User user4 = new User();
        user4.setUsername("User4");
        userRepository.save(user4);

        Server server = new Server();
        server.setName("TestServer");
        server.setDescription("A test server.");
        server.setOwner(user1);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        Role moderatorRole = new Role(new ObjectId(), "Moderator", 1, Permissions.revokePermission(Permissions.ALL,
                PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER));
        Role defaultRole = new Role(new ObjectId(), "Member", 0, Permissions.getPermissions(PermissionType.SEND_MESSAGES,
                PermissionType.VIEW_CHANNELS, PermissionType.CHANGE_NICKNAME));

        server.getRoles().put(ownerRole.getId(), ownerRole);
        server.getRoles().put(moderatorRole.getId(), moderatorRole);
        server.getRoles().put(defaultRole.getId(), defaultRole);

        Member ownerMember = Member.fromUser(user1, ownerRole);
        ownerMember.setOwner(true);
        Member basicMember = Member.fromUser(user2, defaultRole);
        Member bannedMember = Member.fromUser(user4);
        bannedMember.setBanned(true);
        bannedMember.setBanReason("Just because");

        server.getMembers().add(ownerMember);
        server.getMembers().add(basicMember);
        server.getMembers().add(bannedMember);
        server = serverRepository.save(server);
        existingServerId = server.getId();

        Channel channel = new Channel();
        channel.setName("TestChannel");
        channel.setCategory("Test");
        channel.setDescription("A test channel.");
        channel.setServer(server);
        channel = channelRepository.save(channel);
    }

    @AfterEach
    public void cleanup() {
        serverRepository.deleteAll();
        channelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldAddMembers() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user3 = userRepository.findOneByUsername("User3").get();
        Server server = serverRepository.findById(existingServerId).get();

        Server updatedServer = serverRepository.addMember(existingServerId, Member.fromUser(user3));
        assertEquals(updatedServer.getOwner().getId(), server.getOwner().getId());
        assertEquals(server.getId(), updatedServer.getId());
        assertTrue(updatedServer.getMembers().stream().anyMatch(m -> m.getUserId().equals(user1.getId())));
        assertTrue(updatedServer.getMembers().stream().anyMatch(m -> m.getUserId().equals(user3.getId())));
    }

    @Test
    public void shouldUpdateMembers() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        assertFalse(user1.getId().equals(user2.getId()));
        Server server = serverRepository.findById(existingServerId).get();
        Role moderatorRole = server.getRoles().values().stream().filter(r -> r.getName().equals("Moderator"))
                .findFirst().get();
        Member user2Member = server.getMembers().stream().filter(m -> m.getUserId().equals(user2.getId())).findFirst()
                .get();
        Member updatedMember = serverRepository.updateMember(existingServerId, Member.fromUser(user2, moderatorRole))
                .getMembers().stream().filter(m -> m.getUserId().equals(user2.getId())).findFirst().get();

        Optional<Member> member1 = serverRepository.getMember(existingServerId, user1.getId());
        // Other existing members are unchanged
        assertTrue(member1.isPresent());
        assertTrue(member1.get().getNickName().equals(user1.getUsername()));
        assertTrue(member1.get().getRolePriority() == Integer.MAX_VALUE);

        // updated user2 member has new values
        assertEquals(user2Member.getUserId(), updatedMember.getUserId());
        System.out.println("ABCDE: " + server.getRoles().get(updatedMember.getRoleIds().get(0)).toString());
        assertEquals(updatedMember.getRoleIds().get(0), moderatorRole.getId());
        assertEquals(updatedMember.getRolePriority(), moderatorRole.getPriority());
    }

    @Test
    public void shouldRemoveMembers() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();

        Server updatedServer = serverRepository.removeMember(existingServerId, user2.getId());
        assertEquals(updatedServer.getOwner().getId(), (user1.getId()));
        assertEquals(existingServerId, updatedServer.getId());
        assertTrue(updatedServer.getMembers().stream().anyMatch(m -> m.getUserId().equals(user1.getId())));
        assertTrue(updatedServer.getMembers().stream().noneMatch(m -> m.getUserId().equals(user2.getId())));
    }

    @Test
    public void shouldGetMember() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        User user3 = userRepository.findOneByUsername("User3").get();
        User user4 = userRepository.findOneByUsername("User4").get();

        Optional<Member> user1Member = serverRepository.getMember(existingServerId, user1.getId());
        Optional<Member> user2Member = serverRepository.getMember(existingServerId, user2.getId());
        Optional<Member> user3Member = serverRepository.getMember(existingServerId, user3.getId());
        Optional<Member> user4Member = serverRepository.getMember(existingServerId, user4.getId());

        assertTrue(user1Member.isPresent());
        assertEquals(user1Member.get().getNickName(), user1.getUsername());
        assertTrue(user1Member.get().isOwner());

        assertTrue(user2Member.isPresent());
        assertEquals(user2Member.get().getNickName(), user2.getUsername());
        assertFalse(user2Member.get().isOwner());

        assertFalse(user3Member.isPresent());

        assertTrue(user4Member.isPresent());
        assertEquals(user4Member.get().getNickName(), user4.getUsername());
        assertFalse(user4Member.get().isOwner());
        assertTrue(user4Member.get().isBanned());
    }

    @Test
    public void shouldGetIsMember() {
        User user1 = userRepository.findOneByUsername("User1").get();
        User user2 = userRepository.findOneByUsername("User2").get();
        User user3 = userRepository.findOneByUsername("User3").get();
        User user4 = userRepository.findOneByUsername("User4").get();

        boolean user1IsMember = serverRepository.isMember(existingServerId, user1.getId());
        boolean user2IsMember = serverRepository.isMember(existingServerId, user2.getId());
        boolean user3IsMember = serverRepository.isMember(existingServerId, user3.getId());
        boolean user4IsMember = serverRepository.isMember(existingServerId, user4.getId());
        assertTrue(user1IsMember);
        assertTrue(user2IsMember);
        assertFalse(user3IsMember);
        assertFalse(user4IsMember);
    }

    @Test
    public void shouldUpdateRoles() {
        Server oldServer = serverRepository.findById(existingServerId).get();
        Map<ObjectId, Role> oldRoles = oldServer.getRoles();
        List<Role> oldRolesSorted = oldRoles.values().stream().sorted().toList();
        Map<ObjectId, Role> newRoles = new HashMap<>();
        Role ownerRole = oldRolesSorted.get(0);
        Role oldModeratorRole = oldRolesSorted.get(1);
        Role newModeratorRole = new Role(oldModeratorRole.getId(), "NewModerator", 2, Permissions.ALL);
        Role decorationRole = new Role(new ObjectId(), "DecorationRole", 1, Permissions.NONE);
        Role basicMember = oldRolesSorted.get(2);
        newRoles.put(ownerRole.getId(), ownerRole);
        newRoles.put(newModeratorRole.getId(), newModeratorRole);
        newRoles.put(decorationRole.getId(), decorationRole);
        newRoles.put(basicMember.getId(), basicMember);

        Server updatedServer = serverRepository.updateRoles(existingServerId, newRoles);
        Map<ObjectId, Role> updatedRoles = updatedServer.getRoles();

        BiPredicate<Role, Role> rolesEqual = (r1, r2) -> {
            return r1.getId().equals(r2.getId()) && r1.getName().equals(r2.getName())
                    && r1.getPermissions() == r2.getPermissions() && r1.getPriority() == r2.getPriority();
        };

        assertTrue(rolesEqual.test(ownerRole, updatedRoles.get(ownerRole.getId())));
        assertTrue(rolesEqual.test(newModeratorRole, updatedRoles.get(newModeratorRole.getId())));
        assertTrue(rolesEqual.test(decorationRole, updatedRoles.get(decorationRole.getId())));
        assertTrue(rolesEqual.test(basicMember, updatedRoles.get(basicMember.getId())));
        assertFalse(rolesEqual.test(oldModeratorRole, updatedRoles.get(oldModeratorRole.getId())));
    }
}
