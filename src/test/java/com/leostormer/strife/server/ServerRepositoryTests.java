package com.leostormer.strife.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

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
    UserRepository userRepository;

    ObjectId existingServerId;

    private User owner;

    private User basicMemberUser;

    private User nonMemberUser;

    private User bannedUser;

    private User createUser(String userName) {
        User user = new User();
        user.setUsername(userName);
        return userRepository.save(user);
    }

    @BeforeEach
    public void setup() {
        owner = createUser("User1");
        basicMemberUser = createUser("User2");
        nonMemberUser = createUser("User3");
        bannedUser = createUser("User4");

        Server server = new Server();
        server.setName("TestServer");
        server.setDescription("A test server.");
        server.setOwner(owner);

        Role ownerRole = new Role(new ObjectId(), "Owner", Integer.MAX_VALUE, Permissions.ALL);
        Role moderatorRole = new Role(new ObjectId(), "Moderator", 1, Permissions.revokePermission(Permissions.ALL,
                PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER));
        Role defaultRole = new Role(new ObjectId(), "Member", 0,
                Permissions.getPermissions(PermissionType.SEND_MESSAGES,
                        PermissionType.VIEW_CHANNELS, PermissionType.CHANGE_NICKNAME));

        server.getRoles().put(ownerRole.getId(), ownerRole);
        server.getRoles().put(moderatorRole.getId(), moderatorRole);
        server.getRoles().put(defaultRole.getId(), defaultRole);

        Member ownerMember = Member.fromUser(owner, ownerRole);
        ownerMember.setOwner(true);
        Member basicMember = Member.fromUser(basicMemberUser, defaultRole);
        Member bannedMember = Member.fromUser(bannedUser);
        bannedMember.setBanned(true);
        bannedMember.setBanReason("Just because");

        server.getMembers().add(ownerMember);
        server.getMembers().add(basicMember);
        server.getMembers().add(bannedMember);
        server = serverRepository.save(server);
        existingServerId = server.getId();
    }

    @AfterEach
    public void cleanup() {
        serverRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldAddMembers() {
        Server server = serverRepository.findById(existingServerId).get();
        List<Member> existingMembers = server.getMembers();

        Server updatedServer = serverRepository.addMember(existingServerId, Member.fromUser(nonMemberUser));
        assertEquals(updatedServer.getOwner().getId(), server.getOwner().getId());
        assertEquals(server.getId(), updatedServer.getId());
        Set<ObjectId> updatedMemberIds = updatedServer.getMembers().stream().map(m -> m.getUserId())
                .collect(Collectors.toSet());

        // existing members are unchanged
        assertTrue(existingMembers.stream().allMatch(m -> updatedMemberIds.contains(m.getUserId())));
        // new member is added
        assertTrue(updatedMemberIds.contains(nonMemberUser.getId()));
    }

    @Test
    public void shouldUpdateMembers() {
        Server server = serverRepository.findById(existingServerId).get();
        Map<ObjectId, Member> originalMembers = server.getMembers().stream()
                .collect(Collectors.toMap(m -> m.getUserId(), m -> m));

        Role moderatorRole = server.getRoles().values().stream().filter(r -> r.getName().equals("Moderator"))
                .findFirst().get();
        Member basicMember = originalMembers.get(basicMemberUser.getId());

        Server updatedServer = serverRepository.updateMember(existingServerId,
                Member.fromUser(basicMemberUser, moderatorRole));
        Member updatedBasicMember = updatedServer.getMembers().stream()
                .filter(m -> m.getUserId().equals(basicMemberUser.getId())).findFirst().get();

        Set<ObjectId> updatedMemberIds = updatedServer.getMembers().stream().map(m -> m.getUserId())
                .collect(Collectors.toSet());
        assertTrue(originalMembers.keySet().stream().allMatch(id -> updatedMemberIds.contains(id)));

        updatedServer.getMembers().stream().filter(m -> !m.getUserId().equals(basicMemberUser.getId())).forEach(m -> {
            Member originalMember = originalMembers.get(m.getUserId());
            assertEquals(originalMember.getNickName(), m.getNickName());
            assertEquals(originalMember.isOwner(), m.isOwner());
            assertEquals(originalMember.isBanned(), m.isBanned());
            assertEquals(originalMember.getBanReason(), m.getBanReason());
            assertEquals(originalMember.getRolePriority(), m.getRolePriority());
        });

        // updated user2 member has new values
        assertEquals(basicMember.getUserId(), updatedBasicMember.getUserId());
        assertEquals(basicMember.getNickName(), updatedBasicMember.getNickName());
        assertEquals(basicMember.isOwner(), updatedBasicMember.isOwner());
        assertEquals(basicMember.isBanned(), updatedBasicMember.isBanned());
        assertTrue(updatedBasicMember.getRoleIds().stream().anyMatch(id -> id.equals(moderatorRole.getId())));
        assertEquals(updatedBasicMember.getRolePriority(), moderatorRole.getPriority());
    }

    @Test
    public void shouldRemoveMembers() {
        Server server = serverRepository.findById(existingServerId).get();
        List<ObjectId> expectedMembers = server.getMembers().stream()
                .filter(m -> !m.getUserId().equals(basicMemberUser.getId())).map(m -> m.getUserId()).toList();
        Server updatedServer = serverRepository.removeMember(existingServerId, basicMemberUser.getId());
        Set<ObjectId> updatedMemberIds = updatedServer.getMembers().stream().map(m -> m.getUserId())
                .collect(Collectors.toSet());

        assertTrue(expectedMembers.stream().allMatch(id -> updatedMemberIds.contains(id)));
        assertFalse(updatedMemberIds.contains(basicMemberUser.getId()));
    }

    @Test
    public void shouldGetMember() {
        Optional<Member> user1Member = serverRepository.getMember(existingServerId, owner.getId());
        Optional<Member> user2Member = serverRepository.getMember(existingServerId, basicMemberUser.getId());
        Optional<Member> user3Member = serverRepository.getMember(existingServerId, nonMemberUser.getId());
        Optional<Member> user4Member = serverRepository.getMember(existingServerId, bannedUser.getId());

        assertTrue(user1Member.isPresent());
        assertEquals(user1Member.get().getNickName(), owner.getUsername());
        assertTrue(user1Member.get().isOwner());

        assertTrue(user2Member.isPresent());
        assertEquals(user2Member.get().getNickName(), basicMemberUser.getUsername());
        assertFalse(user2Member.get().isOwner());

        assertFalse(user3Member.isPresent());

        // A banned user still has a member record
        assertTrue(user4Member.isPresent());
        assertEquals(user4Member.get().getNickName(), bannedUser.getUsername());
        assertFalse(user4Member.get().isOwner());
        assertTrue(user4Member.get().isBanned());
    }

    @Test
    public void shouldGetIsMember() {
        boolean user1IsMember = serverRepository.isMember(existingServerId, owner.getId());
        boolean user2IsMember = serverRepository.isMember(existingServerId, basicMemberUser.getId());
        boolean user3IsMember = serverRepository.isMember(existingServerId, nonMemberUser.getId());
        boolean user4IsMember = serverRepository.isMember(existingServerId, bannedUser.getId());

        assertTrue(user1IsMember);
        assertTrue(user2IsMember);
        assertFalse(user3IsMember);

        // a banned user is not a member even though they have a member record
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
