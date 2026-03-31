package com.cranks.dungeons.client.party;

import com.cranks.dungeons.party.PartyMemberData;
import java.util.*;

public class PartyClientData {
    private static final Map<UUID, PartyMemberData> partyMembers = new LinkedHashMap<>();
    private static final Map<UUID, Long> damageFlashTimers = new HashMap<>();
    private static final Map<UUID, Long> healFlashTimers = new HashMap<>();
    private static final Map<UUID, Long> shieldFlashTimers = new HashMap<>();
    private static final Map<UUID, Float> lastHealthValues = new HashMap<>();
    private static final Map<UUID, Integer> lastFullHearts = new HashMap<>();
    private static final Map<UUID, Boolean> lastHadHalfHeart = new HashMap<>();
    private static final Map<UUID, Float> lastShieldValues = new HashMap<>();
    private static UUID leaderUuid;
    private static boolean inParty = false;

    public static final long DAMAGE_FLASH_DURATION = 1000; // 1000ms for 3 blinks
    public static final long HEAL_FLASH_DURATION = 500; // 500ms for 2 blinks
    public static final long SHIELD_FLASH_DURATION = 1000; // 1000ms for shield blinks
    public static final long SHIELD_BLINK_INTERVAL = 250; // 250ms toggle interval

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
            // Check for damage (health decreased)
            Float lastHealth = lastHealthValues.get(uuid);
            if (lastHealth != null) {
                float healthDiff = health - lastHealth;
                if (healthDiff < -0.01f) {
                    // Damage taken! Start flash
                    damageFlashTimers.put(uuid, System.currentTimeMillis());
                }
            }
            lastHealthValues.put(uuid, health);

            // Check for new heart appearing (visual change) - both full and half hearts
            int currentFullHearts = (int) (health / 2.0f);
            boolean currentHasHalfHeart = (health % 2.0f) >= 1.0f;
            
            Integer lastFull = lastFullHearts.get(uuid);
            Boolean lastHalf = lastHadHalfHeart.get(uuid);
            
            if (lastFull != null && lastHalf != null) {
                // Flash if new full heart OR if half heart just appeared
                if (currentFullHearts > lastFull || 
                    (currentFullHearts == lastFull && currentHasHalfHeart && !lastHalf)) {
                    healFlashTimers.put(uuid, System.currentTimeMillis());
                }
            }
            
            lastFullHearts.put(uuid, currentFullHearts);
            lastHadHalfHeart.put(uuid, currentHasHalfHeart);

            // Check for shield change (damage or recharge)
            Float lastShield = lastShieldValues.get(uuid);
            if (lastShield != null && Math.abs(energyShield - lastShield) > 0.01f) {
                // Shield changed! Start shield flash
                shieldFlashTimers.put(uuid, System.currentTimeMillis());
            }
            lastShieldValues.put(uuid, energyShield);

            partyMembers.put(uuid, new PartyMemberData(
                uuid, existing.name(), health, maxHealth, energyShield, maxEnergyShield
            ));
        }
    }
    
    public static void leaveParty() {
        partyMembers.clear();
        damageFlashTimers.clear();
        healFlashTimers.clear();
        shieldFlashTimers.clear();
        lastHealthValues.clear();
        lastFullHearts.clear();
        lastHadHalfHeart.clear();
        lastShieldValues.clear();
        leaderUuid = null;
        inParty = false;
    }

    public static boolean isShieldFlashing(UUID uuid) {
        Long flashStart = shieldFlashTimers.get(uuid);
        if (flashStart == null) return false;

        long currentTime = System.currentTimeMillis();
        long timeSinceChange = currentTime - flashStart;

        // Check if flash has expired
        if (timeSinceChange > SHIELD_FLASH_DURATION) {
            shieldFlashTimers.remove(uuid);
            return false;
        }

        // Shield blink pattern: toggle every 250ms
        long phase = (timeSinceChange / SHIELD_BLINK_INTERVAL) % 2;
        return phase == 0;
    }

    public static boolean isHealFlashing(UUID uuid) {
        Long flashStart = healFlashTimers.get(uuid);
        if (flashStart == null) return false;

        long currentTime = System.currentTimeMillis();
        long timeSinceHeal = currentTime - flashStart;

        // Check if flash has expired (2 blinks for healing)
        if (timeSinceHeal > HEAL_FLASH_DURATION) {
            healFlashTimers.remove(uuid);
            return false;
        }

        // Blink pattern: toggle every ~167ms for 2 blinks in 500ms
        long phase = (timeSinceHeal / 167) % 2;
        return phase == 0;
    }
    
    public static boolean isDamageFlashing(UUID uuid) {
        Long flashStart = damageFlashTimers.get(uuid);
        if (flashStart == null) return false;

        long currentTime = System.currentTimeMillis();
        long timeSinceDamage = currentTime - flashStart;

        // Check if flash has expired
        if (timeSinceDamage > DAMAGE_FLASH_DURATION) {
            damageFlashTimers.remove(uuid);
            return false;
        }

        // Blink pattern: toggle every ~167ms for 3 blinks in 1000ms
        long phase = (timeSinceDamage / 167) % 2;
        return phase == 0;
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
