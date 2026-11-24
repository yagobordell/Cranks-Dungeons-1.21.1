package com.cranks.dungeons.party;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {
    private static PartyManager INSTANCE;
    private MinecraftServer server;
    
    private final Map<UUID, Party> playerParties = new ConcurrentHashMap<>();
    private final Map<UUID, PartyInvite> pendingInvites = new ConcurrentHashMap<>();
    
    private PartyManager() {}
    
    private static PartyManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PartyManager();
        }
        return INSTANCE;
    }
    
    // Static helper methods that delegate to singleton
    public static Optional<Party> getParty(UUID player) {
        return getInstance().instanceGetParty(player);
    }
    
    public static boolean isInParty(UUID player) {
        return getInstance().instanceIsInParty(player);
    }
    
    public static boolean areInSameParty(UUID player1, UUID player2) {
        return getInstance().instanceAreInSameParty(player1, player2);
    }
    
    public static Party createParty(UUID leader) {
        return getInstance().instanceCreateParty(leader);
    }
    
    public static boolean disbandParty(UUID leader) {
        return getInstance().instanceDisbandParty(leader);
    }
    
    public static boolean invitePlayer(UUID inviter, UUID invitee) {
        return getInstance().instanceInvitePlayer(inviter, invitee);
    }
    
    public static Optional<PartyInvite> getPendingInvite(UUID invitee) {
        return getInstance().instanceGetPendingInvite(invitee);
    }
    
    public static boolean acceptInvite(UUID invitee, UUID inviter) {
        return getInstance().instanceAcceptInvite(invitee, inviter);
    }
    
    public static boolean kickPlayer(UUID leader, UUID target) {
        return getInstance().instanceKickPlayer(leader, target);
    }
    
    public static boolean leaveParty(UUID player) {
        return getInstance().instanceLeaveParty(player);
    }
    
    public static boolean transferLeadership(UUID currentLeader, UUID newLeader) {
        return getInstance().instanceTransferLeadership(currentLeader, newLeader);
    }
    
    public static void clearPlayerData(UUID playerUuid) {
        getInstance().instanceClearPlayerData(playerUuid);
    }

    public static void clearAll() {
        PartyManager manager = INSTANCE;
        if (manager != null) {
            manager.playerParties.clear();
            manager.pendingInvites.clear();
        }
        INSTANCE = null;
    }

    public static void initialize(MinecraftServer server) {
        PartyManager manager = getInstance();
        manager.server = server;
        manager.playerParties.clear();
        manager.pendingInvites.clear();
        for (Party party : PartyPersistentState.get(server).getParties()) {
            boolean conflicts = party.getMembers().stream().anyMatch(manager.playerParties::containsKey);
            if (conflicts) {
                com.cranks.dungeons.CranksDungeons.LOGGER.warn("[Party] Skipping persisted party with duplicate members; leader={}", party.getLeader());
                continue;
            }
            party.getMembers().forEach(member -> manager.playerParties.put(member, party));
        }
    }

    private void persist() {
        com.cranks.dungeons.mount.LlamaPackManager.invalidate();
        if (server == null) return;
        Set<Party> uniqueParties = Collections.newSetFromMap(new IdentityHashMap<>());
        uniqueParties.addAll(playerParties.values());
        PartyPersistentState.get(server).replaceParties(uniqueParties);
    }
    
    // Instance methods
    private Optional<Party> instanceGetParty(UUID player) {
        return Optional.ofNullable(playerParties.get(player));
    }
    
    private boolean instanceIsInParty(UUID player) {
        return playerParties.containsKey(player);
    }
    
    private boolean instanceAreInSameParty(UUID player1, UUID player2) {
        Party party1 = playerParties.get(player1);
        Party party2 = playerParties.get(player2);
        return party1 != null && party1 == party2;
    }
    
    private Party instanceCreateParty(UUID leader) {
        if (playerParties.containsKey(leader)) {
            return null;
        }
        Party party = new Party(leader);
        playerParties.put(leader, party);
        persist();
        return party;
    }
    
    private boolean instanceDisbandParty(UUID leader) {
        Party party = playerParties.get(leader);
        if (party == null || !party.isLeader(leader)) {
            return false;
        }
        
        for (UUID member : party.getMembers()) {
            playerParties.remove(member);
        }
        pendingInvites.entrySet().removeIf(entry -> entry.getValue().getParty() == party);
        persist();
        return true;
    }
    
    private boolean instanceInvitePlayer(UUID inviter, UUID invitee) {
        Party party = playerParties.get(inviter);
        if (party == null || !party.isLeader(inviter)) {
            return false;
        }
        if (party.isFull()) {
            return false;
        }
        if (playerParties.containsKey(invitee)) {
            return false;
        }
        
        PartyInvite invite = new PartyInvite(inviter, invitee, party);
        pendingInvites.put(invitee, invite);
        return true;
    }
    
    private Optional<PartyInvite> instanceGetPendingInvite(UUID invitee) {
        PartyInvite invite = pendingInvites.get(invitee);
        if (invite != null && invite.isExpired()) {
            pendingInvites.remove(invitee);
            return Optional.empty();
        }
        return Optional.ofNullable(invite);
    }
    
    private boolean instanceAcceptInvite(UUID invitee, UUID inviter) {
        PartyInvite invite = pendingInvites.get(invitee);
        if (invite == null || !invite.getInviter().equals(inviter) || invite.isExpired()) {
            if (invite != null && invite.isExpired()) {
                pendingInvites.remove(invitee);
            }
            return false;
        }
        
        Party party = invite.getParty();
        if (playerParties.containsKey(invitee)
                || playerParties.get(inviter) != party
                || !party.isLeader(inviter)
                || party.isFull()) {
            pendingInvites.remove(invitee);
            return false;
        }
        
        if (party.addMember(invitee)) {
            playerParties.put(invitee, party);
            pendingInvites.remove(invitee);
            persist();
            return true;
        }
        
        return false;
    }
    
    private boolean instanceKickPlayer(UUID leader, UUID target) {
        Party party = playerParties.get(leader);
        if (party == null || !party.isLeader(leader)) {
            return false;
        }
        if (!party.isMember(target) || party.isLeader(target)) {
            return false;
        }
        
        if (party.removeMember(target)) {
            playerParties.remove(target);
            pendingInvites.remove(target);
            pendingInvites.entrySet().removeIf(entry -> entry.getValue().getInviter().equals(target));
            persist();
            return true;
        }
        return false;
    }
    
    private boolean instanceLeaveParty(UUID player) {
        Party party = playerParties.get(player);
        if (party == null) {
            return false;
        }
        
        if (party.isLeader(player)) {
            Optional<UUID> nextLeader = party.getNextLeader();
            if (nextLeader.isPresent()) {
                party.transferLeadership(nextLeader.get());
                party.removeMember(player);
                playerParties.remove(player);
                pendingInvites.entrySet().removeIf(entry -> entry.getValue().getParty() == party);
                persist();
                return true;
            } else {
                return instanceDisbandParty(player);
            }
        } else {
            party.removeMember(player);
            playerParties.remove(player);
            persist();
            return true;
        }
    }
    
    private boolean instanceTransferLeadership(UUID currentLeader, UUID newLeader) {
        Party party = playerParties.get(currentLeader);
        if (party == null || !party.isLeader(currentLeader)) {
            return false;
        }
        if (!party.isMember(newLeader)) {
            return false;
        }
        
        party.transferLeadership(newLeader);
        pendingInvites.entrySet().removeIf(entry -> entry.getValue().getParty() == party);
        persist();
        return true;
    }
    
    private void instanceClearPlayerData(UUID playerUuid) {
        pendingInvites.remove(playerUuid);
        pendingInvites.entrySet().removeIf(entry -> entry.getValue().getInviter().equals(playerUuid));
    }
}
