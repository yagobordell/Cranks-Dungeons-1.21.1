package com.cranks.dungeons.util;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.ManaProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.network.EntityShieldSyncPacket;
import com.cranks.dungeons.network.ManaSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.RecoveryPool;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Bounded leech pools; cancellation and resource changes cannot bank unlimited healing. */
public final class LeechManager {
    private record LifePool(boolean shield, RecoveryPool pool) {}
    private static final Map<UUID, LifePool> LIFE = new HashMap<>();
    private static final Map<UUID, RecoveryPool> MANA = new HashMap<>();
    public static void clearEntity(UUID id) { LIFE.remove(id); MANA.remove(id); }
    public static void clearAll() { LIFE.clear(); MANA.clear(); }

    public static void addLeech(LivingEntity entity, float amount) {
        if (entity.getWorld().isClient() || !entity.isAlive()) return;
        boolean shield = entity.getAttributeValue(ModAttributes.KEYSTONE_GHOST_REAVER) > 0;
        double maximum = shield ? entity.getAttributeValue(ModAttributes.ENERGY_SHIELD) : entity.getMaxHealth();
        double current = shield ? ((EnergyShieldProvider)entity).cranksdungeons$getCurrentEnergyShield() : entity.getHealth();
        if (maximum <= 0 || current >= maximum) return;
        var entry = LIFE.get(entity.getUuid());
        if (entry == null || entry.shield != shield) {
            entry = new LifePool(shield, new RecoveryPool()); LIFE.put(entity.getUuid(), entry);
        }
        entry.pool.offer(amount, maximum);
        if (entity.getAttributeValue(ModAttributes.KEYSTONE_VAAL_PACT) > 0)
            recoverLife(entity, shield, (float)entry.pool.drainInstant(maximum, entity.getWorld().getTime()));
    }

    public static void addManaLeech(LivingEntity entity, float amount) {
        if (entity.getWorld().isClient() || !entity.isAlive() || !(entity instanceof ManaProvider mana)) return;
        double maximum = entity.getAttributeValue(ModAttributes.MAX_MANA);
        if (maximum <= 0 || mana.cranksdungeons$getCurrentMana() >= maximum) return;
        MANA.computeIfAbsent(entity.getUuid(), ignored -> new RecoveryPool()).offer(amount, maximum);
    }

    public static void tick(LivingEntity entity) {
        if (entity.getWorld().isClient()) return;
        if (!entity.isAlive()) { clearEntity(entity.getUuid()); return; }
        var life = LIFE.get(entity.getUuid());
        if (life != null) {
            boolean shield = entity.getAttributeValue(ModAttributes.KEYSTONE_GHOST_REAVER) > 0;
            double maximum = shield ? entity.getAttributeValue(ModAttributes.ENERGY_SHIELD) : entity.getMaxHealth();
            double current = shield ? ((EnergyShieldProvider)entity).cranksdungeons$getCurrentEnergyShield() : entity.getHealth();
            boolean instant = entity.getAttributeValue(ModAttributes.KEYSTONE_VAAL_PACT) > 0;
            if (life.shield != shield || maximum <= 0 || (!instant && (current >= maximum || life.pool.empty()))) LIFE.remove(entity.getUuid());
            else if (!instant) com.cranks.dungeons.damage.HealingNumberContext.gradual(
                    () -> recoverLife(entity, shield, (float)life.pool.drain(maximum)));
        }
        var pool = MANA.get(entity.getUuid());
        if (pool != null && entity instanceof ManaProvider mana) {
            float maximum = (float)entity.getAttributeValue(ModAttributes.MAX_MANA);
            float current = mana.cranksdungeons$getCurrentMana();
            if (maximum <= 0 || current >= maximum || pool.empty()) MANA.remove(entity.getUuid());
            else {
                float next = Math.min(maximum, current + (float)pool.drain(maximum));
                mana.cranksdungeons$setCurrentMana(next);
                if (entity instanceof ServerPlayerEntity player && (entity.age % 5 == 0 || next >= maximum))
                    ServerPlayNetworking.send(player, new ManaSyncPacket(next, maximum));
            }
        }
    }

    private static void recoverLife(LivingEntity entity, boolean shield, float amount) {
        if (amount <= 0) return;
        if (shield) addShield(entity, amount);
        else entity.heal(amount);
    }

    public static void addShield(LivingEntity entity, float amount) {
        if (!(entity instanceof EnergyShieldProvider shield) || entity.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) return;
        float maximum = (float)entity.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        float next = Math.min(maximum, shield.cranksdungeons$getCurrentEnergyShield() + amount);
        shield.cranksdungeons$setCurrentEnergyShield(next);
        if (entity.age % 5 == 0 || next >= maximum) {
            if (entity instanceof ServerPlayerEntity player) ServerPlayNetworking.send(player, new EnergyShieldSyncPacket(next, maximum));
            var packet = new EntityShieldSyncPacket(entity.getId(), next, maximum);
            PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, packet));
        }
    }
}
