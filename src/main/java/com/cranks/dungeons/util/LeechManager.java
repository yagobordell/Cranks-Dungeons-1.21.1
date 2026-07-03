package com.cranks.dungeons.util;

import net.minecraft.entity.LivingEntity;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class LeechManager {

    private static class LeechInstance {
        float remainingHealing;
        float healingPerTick; // Healing to apply per tick (20 ticks per second)

        LeechInstance(float totalHealing, float maxHealth) {
            this.remainingHealing = totalHealing;
            // In PoE, a single leech instance recovers 2% of max health per second (0.1% per tick)
            this.healingPerTick = (maxHealth * 0.02f) / 20.0f;
        }
    }

    private static final Map<UUID, List<LeechInstance>> LEECH_INSTANCES = new ConcurrentHashMap<>();

    public static void addLeech(LivingEntity entity, float amount) {
        if (entity.getEntityWorld().isClient()) return;
        UUID uuid = entity.getUuid();
        LEECH_INSTANCES.computeIfAbsent(uuid, k -> new ArrayList<>()).add(new LeechInstance(amount, entity.getMaxHealth()));
    }

    public static void tick(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;
        
        UUID uuid = entity.getUuid();
        List<LeechInstance> instances = LEECH_INSTANCES.get(uuid);
        if (instances == null || instances.isEmpty()) return;

        // If at full health, all instances are removed (like PoE)
        if (entity.getHealth() >= entity.getMaxHealth()) {
            instances.clear();
            return;
        }

        float maxHealingPerSecond = entity.getMaxHealth() * 0.20f; // Max 20% of max health per second
        float maxHealingPerTick = maxHealingPerSecond / 20.0f;

        float totalHealingThisTick = 0f;

        Iterator<LeechInstance> iterator = instances.iterator();
        while (iterator.hasNext()) {
            LeechInstance instance = iterator.next();
            float healAmount = Math.min(instance.remainingHealing, instance.healingPerTick);
            
            totalHealingThisTick += healAmount;
            instance.remainingHealing -= healAmount;

            if (instance.remainingHealing <= 0) {
                iterator.remove();
            }
        }

        if (totalHealingThisTick > maxHealingPerTick) {
            totalHealingThisTick = maxHealingPerTick;
        }

        if (totalHealingThisTick > 0) {
            entity.heal(totalHealingThisTick);
        }
    }
}
