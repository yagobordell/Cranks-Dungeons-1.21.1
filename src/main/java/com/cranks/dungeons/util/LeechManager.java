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

    private static void applyHealing(LivingEntity entity, float amount) {
        if (entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.KEYSTONE_GHOST_REAVER) > 0) {
            if (entity instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
                float currentES = provider.cranksdungeons$getCurrentEnergyShield();
                float maxES = (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                if (currentES < maxES) {
                    float newES = Math.min(maxES, currentES + amount);
                    provider.cranksdungeons$setCurrentEnergyShield(newES);
                    if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player && player.networkHandler != null) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(newES, maxES));
                    }
                }
            }
        } else {
            entity.heal(amount);
        }
    }

    public static void addLeech(LivingEntity entity, float amount) {
        if (entity.getEntityWorld().isClient()) return;
        
        // Vaal Pact makes leech instant
        if (entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.KEYSTONE_VAAL_PACT) > 0) {
            applyHealing(entity, amount);
            return;
        }

        boolean ghostReaver = entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.KEYSTONE_GHOST_REAVER) > 0;
        float maxResource = ghostReaver ? (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD) : entity.getMaxHealth();
        if (maxResource <= 0) maxResource = 1; // Prevent div by 0

        UUID uuid = entity.getUuid();
        LEECH_INSTANCES.computeIfAbsent(uuid, k -> new ArrayList<>()).add(new LeechInstance(amount, maxResource));
    }

    public static void tick(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;
        
        UUID uuid = entity.getUuid();
        List<LeechInstance> instances = LEECH_INSTANCES.get(uuid);
        if (instances == null || instances.isEmpty()) return;

        boolean ghostReaver = entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.KEYSTONE_GHOST_REAVER) > 0;
        float currentResource = ghostReaver && entity instanceof com.cranks.dungeons.EnergyShieldProvider provider ? provider.cranksdungeons$getCurrentEnergyShield() : entity.getHealth();
        float maxResource = ghostReaver ? (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD) : entity.getMaxHealth();

        // If at full health/ES, all instances are removed (like PoE)
        if (currentResource >= maxResource) {
            instances.clear();
            return;
        }

        float maxHealingPerSecond = maxResource * 0.20f; // Max 20% of max resource per second
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
            applyHealing(entity, totalHealingThisTick);
        }
    }
}
