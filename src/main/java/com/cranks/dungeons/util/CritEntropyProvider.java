package com.cranks.dungeons.util;

import net.minecraft.util.math.random.Random;

/**
 * Interface implemented on LivingEntity to support Path of Exile 2-style
 * deterministic entropy accumulation for critical strikes.
 *
 * Prevents lucky streaks and unlucky droughts, guaranteeing consistent
 * crit distribution matching the player's exact crit chance percentage.
 */
public interface CritEntropyProvider {
    float cranks$getCritEntropy();
    void cranks$setCritEntropy(float entropy);
    long cranks$getLastCritAttackTime();
    void cranks$setLastCritAttackTime(long time);

    /**
     * Rolls a critical strike using the Path of Exile 2 entropy accumulation system.
     *
     * @param critChance The attacker's effective crit chance (0.0 to 1.0+)
     * @param currentTime Current game world time in ticks
     * @param random Attacker's RNG instance for seeding out-of-combat state
     * @return true if this hit is a critical strike, false otherwise
     */
    boolean cranks$rollCriticalStrike(float critChance, long currentTime, Random random);
}
