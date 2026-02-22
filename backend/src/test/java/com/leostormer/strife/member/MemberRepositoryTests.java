package com.leostormer.strife.member;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractRepositoryTest;
import com.leostormer.strife.TestUtils;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;
import com.leostormer.strife.user.User;
import com.leostormer.strife.user.UserRepository;

public class MemberRepositoryTests extends AbstractRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ServerRepository serverRepository;

    private ObjectId user1Id;
    private ObjectId user2Id;
    private ObjectId user3Id;
    private ObjectId serverId;

    @BeforeEach
    public void setupMembers() {
        User user1 = TestUtils.createUser("user1", "somePassword", userRepository);
        user1Id = user1.getId();
        User user2 = TestUtils.createUser("user2", "anyPassword", userRepository);
        user2Id = user2.getId();
        User user3 = TestUtils.createUser("user3", "password123", userRepository);
        user3Id = user3.getId();

        Server server = TestUtils.createServer(user1, "Server1", "", serverRepository, memberRepository);
        serverId = server.getId();
        TestUtils.createServer(user2, "Other Server", "", serverRepository, memberRepository);

        TestUtils.createMember(user2, server, memberRepository);
        TestUtils.createBannedMember(user3, server, "", memberRepository);
    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
        memberRepository.deleteAll();
        serverRepository.deleteAll();
    }

    @Test
    public void shouldFindByUserIdAndServerId() {
        var result = memberRepository.findByUserIdAndServerId(user1Id, serverId);
        assertTrue(result.isPresent());
        assertTrue(result.get().isOwner());

        result = memberRepository.findByUserIdAndServerId(user2Id, serverId);
        assertTrue(result.isPresent());
        assertFalse(result.get().isOwner());
    }

    @Test
    public void shouldFindServersByUserId() {
        var servers = memberRepository.findServersByUserId(user1Id);
        assertFalse(servers.isEmpty());
        assertEquals(1, servers.size());

        servers = memberRepository.findServersByUserId(user2Id);
        assertEquals(2, servers.size());

        servers = memberRepository.findServersByUserId(user3Id);
        assertTrue(servers.isEmpty());
    }

    @Test
    public void shouldExistsByUserIdAndServerId() {
        assertTrue(memberRepository.existsByUserIdAndServerId(user1Id, serverId));
        assertTrue(memberRepository.existsByUserIdAndServerId(user2Id, serverId));
        assertTrue(memberRepository.existsByUserIdAndServerId(user3Id, serverId));
    }

    @Test
    public void shouldGetIsMember() {
        assertTrue(memberRepository.isMember(user1Id, serverId));
        assertTrue(memberRepository.isMember(user2Id, serverId));
        assertFalse(memberRepository.isMember(user3Id, serverId));
    }

    @Test
    public void shouldRemoveMember() {
        memberRepository.removeMember(user2Id, serverId);
        assertFalse(memberRepository.existsByUserIdAndServerId(user2Id, serverId));
    }

    @Test
    public void shouldBanMember() {
        memberRepository.banMember(user2Id, serverId, "Violation of rules");
        var member = memberRepository.findByUserIdAndServerId(user2Id, serverId);
        assertTrue(member.isPresent());
        assertTrue(member.get().isBanned());
        assertEquals("Violation of rules", member.get().getBanReason());
    }

    @Test
    public void shouldChangeNickname() {
        String newNickname = "TheNewNick";
        memberRepository.changeNickname(user2Id, serverId, newNickname);
        var member = memberRepository.findByUserIdAndServerId(user2Id, serverId);
        assertTrue(member.isPresent());
        assertEquals(newNickname, member.get().getNickname());
    }

    @Test
    public void shouldUpdateMemberRoles() {
    }
}
