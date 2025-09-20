package com.leostormer.strife.server.invite;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.leostormer.strife.server.Server;
import com.leostormer.strife.user.User;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invite {
    @Id
    private String id;

    @NotNull
    private int maxUses;

    @NotNull
    private int remainingUses;

    @NotNull
    private Instant expiresAt;

    @DocumentReference(collection = "servers", lazy = true)
    private Server server;

    @DocumentReference(collection = "users", lazy = true)
    private User inviter;

    public static int UNLIMITED_USES = 0;

    public static Instant FAR_FUTURE = Instant.MAX;

    public Invite(User inviter, Server server, long expiresAfterSeconds, int maxUses) {
        this.server = server;
        this.inviter = inviter;
        this.maxUses = maxUses;
        this.remainingUses = maxUses;
        this.expiresAt = Instant.now().plusSeconds(expiresAfterSeconds);
    }
}
