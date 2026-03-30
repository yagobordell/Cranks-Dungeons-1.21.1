package com.cranks.dungeons.party;

import java.util.*;

public class Party {
    private final UUID leader;
    private final List<UUID> members;
    private final long creationTime;
    
    public static final int MAX_MEMBERS = 10;
    
    public Party(UUID leader) {
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
        this.creationTime = System.currentTimeMillis();
    }
    
    public UUID getLeader() {
        return leader;
    }
    
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }
    
    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }
    
    public boolean isMember(UUID player) {
        return members.contains(player);
    }
    
    public boolean addMember(UUID player) {
        if (members.size() >= MAX_MEMBERS || members.contains(player)) {
            return false;
        }
        members.add(player);
        return true;
    }
    
    public boolean removeMember(UUID player) {
        if (player.equals(leader)) {
            return false;
        }
        return members.remove(player);
    }
    
    public boolean isFull() {
        return members.size() >= MAX_MEMBERS;
    }
    
    public int getSize() {
        return members.size();
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public Optional<UUID> getNextLeader() {
        for (UUID member : members) {
            if (!member.equals(leader)) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }
    
    public void transferLeadership(UUID newLeader) {
        if (!members.contains(newLeader)) {
            throw new IllegalArgumentException("New leader must be a party member");
        }
        members.remove(newLeader);
        members.add(0, newLeader);
    }
}
