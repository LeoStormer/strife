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

    /**
     * Decrements the number of remaining uses by 1 if the invite has a
     * limited number of uses.
     */
    public void decrementUsesIfLimited() {
        if (areUsesLimited() && remainingUses > 0)
            remainingUses--;
    }

    public boolean hasRemainingUses() {
        return !areUsesLimited() || remainingUses > 0;
    }

    public boolean areUsesLimited() {
        return maxUses > 0;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
    
    public Invite(User inviter, Server server, long expiresAfterSeconds, int maxUses, int remainingUses) {
        this.server = server;
        this.inviter = inviter;
        this.maxUses = maxUses;
        this.remainingUses = remainingUses;
        this.expiresAt = Instant.now().plusSeconds(expiresAfterSeconds);
    }

    public Invite(User inviter, Server server, long expiresAfterSeconds, int maxUses) {
        this(inviter, server, expiresAfterSeconds, maxUses, maxUses);
    }
}
