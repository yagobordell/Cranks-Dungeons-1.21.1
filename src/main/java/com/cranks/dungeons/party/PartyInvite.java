package com.cranks.dungeons.party;

import java.util.UUID;

public class PartyInvite {
    private final UUID inviter;
    private final UUID invitee;
    private final Party party;
    private final long creationTime;
    
    public static final long INVITE_TIMEOUT_MS = 120000; // 120 seconds
    
    public PartyInvite(UUID inviter, UUID invitee, Party party) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.party = party;
        this.creationTime = System.currentTimeMillis();
    }
    
    public UUID getInviter() {
        return inviter;
    }
    
    public UUID getInvitee() {
        return invitee;
    }
    
    public Party getParty() {
        return party;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > INVITE_TIMEOUT_MS;
    }
    
    public long getRemainingTime() {
        long remaining = INVITE_TIMEOUT_MS - (System.currentTimeMillis() - creationTime);
        return Math.max(0, remaining);
    }
}
