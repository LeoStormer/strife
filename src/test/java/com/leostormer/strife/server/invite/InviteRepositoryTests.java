package com.leostormer.strife.server.invite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.leostormer.strife.AbstractRepositoryTest;
import com.leostormer.strife.server.Server;
import com.leostormer.strife.server.ServerRepository;

public class InviteRepositoryTests extends AbstractRepositoryTest {
    @Autowired
    InviteRepository inviteRepository;

    @Autowired
    ServerRepository serverRepository;

    private static Server server;
    
    private static Server server2;

    private static final int NUM_INVITES = 10;

    @BeforeAll
    public static void setup(@Autowired ServerRepository serverRepository, @Autowired InviteRepository inviteRepository) {
        server = serverRepository.save(new Server());
        server2 = serverRepository.save(new Server());

        for (int i = 0; i < NUM_INVITES; i++) {
            Invite invite = new Invite();
            invite.setServer(server);
            inviteRepository.save(invite);
        }

        Invite invite = new Invite();
        invite.setServer(server2);
        inviteRepository.save(invite);
    }

    @AfterAll
    public static void cleanup(@Autowired ServerRepository serverRepository, @Autowired InviteRepository inviteRepository) {
        serverRepository.deleteAll();
        inviteRepository.deleteAll();
    }

    @Test
    public void shouldFindInvitesByServer() {
        assertEquals(NUM_INVITES, inviteRepository.findAllByServer(server).size());
        assertEquals(1, inviteRepository.findAllByServer(server2).size());
    }

    @Test
    public void shouldFindInvitesByServerId() {
        assertEquals(NUM_INVITES, inviteRepository.findAllByServer(server.getId()).size());
        assertEquals(1, inviteRepository.findAllByServer(server2.getId()).size());
    }
}
