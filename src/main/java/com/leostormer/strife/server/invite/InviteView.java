package com.leostormer.strife.server.invite;

import java.time.Instant;


import lombok.Data;

@Data
public class InviteView {
    private String id;
    
    private int maxUses;

    private int remainingUses;

    private Instant expiresAt;

    private String server;

    private String inviter;

    public InviteView(Invite invite) {
        this.id = invite.getId();
        this.maxUses = invite.getMaxUses();
        this.remainingUses = invite.getRemainingUses();
        this.expiresAt = invite.getExpiresAt();
        this.server = invite.getServer().getId().toHexString();
        this.inviter = invite.getInviter().getId().toHexString();
    }
}
