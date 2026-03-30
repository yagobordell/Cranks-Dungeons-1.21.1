package com.cranks.dungeons.client.party;

import com.cranks.dungeons.party.PartyMemberData;
import java.util.*;

public class PartyClientData {
    private static final Map<UUID, PartyMemberData> partyMembers = new LinkedHashMap<>();
    private static UUID leaderUuid;
    private static boolean inParty = false;
    
    public static void setPartyData(List<PartyMemberData> members, UUID leader) {
        partyMembers.clear();
        for (PartyMemberData member : members) {
            partyMembers.put(member.uuid(), member);
        }
        leaderUuid = leader;
        inParty = !members.isEmpty();
    }
    
    public static void updateMemberHealth(UUID uuid, float health, float maxHealth, float energyShield, float maxEnergyShield) {
        PartyMemberData existing = partyMembers.get(uuid);
        if (existing != null) {
            partyMembers.put(uuid, new PartyMemberData(
                uuid, existing.name(), health, maxHealth, energyShield, maxEnergyShield
            ));
        }
    }
    
    public static void leaveParty() {
        partyMembers.clear();
        leaderUuid = null;
        inParty = false;
    }
    
    public static boolean isInParty() {
        return inParty;
    }
    
    public static List<PartyMemberData> getMembers() {
        return new ArrayList<>(partyMembers.values());
    }
    
    public static UUID getLeaderUuid() {
        return leaderUuid;
    }
    
    public static boolean isLeader(UUID uuid) {
        return uuid.equals(leaderUuid);
    }
}
