package com.cranks.dungeons.ailment;

import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.util.StatusEffectHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class AilmentManager {

    private static final Map<UUID, List<Ailment>> ENTITY_AILMENTS = new HashMap<>();

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration) {
        if (target == null) return;

        if (hasAilment(target, type)) {
            return;
        }

        List<Ailment> ailments = getAilments(target);
        Ailment ailment = new Ailment(type, duration, baseDamage);
        ailments.add(ailment);
        ENTITY_AILMENTS.put(target.getUuid(), ailments);

        applyStatusEffect(target, type, duration);
    }

    private static void applyStatusEffect(LivingEntity target, AilmentType type, double duration) {
        int ticks = (int)(duration * 20);
        switch (type) {
            case BURN -> {
                StatusEffectInstance instance = StatusEffectHelper.create(
                        ModEffects.BURN, ticks, 0, false, true, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
        }
    }

    public static boolean hasAilment(LivingEntity target, AilmentType type) {
        List<Ailment> ailments = getAilments(target);
        for (Ailment ailment : ailments) {
            if (ailment.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public static List<Ailment> getAilments(LivingEntity target) {
        return ENTITY_AILMENTS.getOrDefault(target.getUuid(), new ArrayList<>());
    }

    public static void tickAilments(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;

        List<Ailment> ailments = getAilments(entity);
        if (ailments.isEmpty()) return;

        List<Ailment> toRemove = new ArrayList<>();
        float tickDamagePercent = getDamagePercent(entity);

        for (Ailment ailment : ailments) {
            ailment.addTimeSinceLastTick(0.05);
            ailment.setRemainingDuration(ailment.getRemainingDuration() - 0.05);

            if (ailment.isReadyToTick()) {
                float damage = ailment.getDamagePerTick(tickDamagePercent);
                if (damage > 0) {
                    float currentHealth = entity.getHealth();
                    float newHealth = Math.max(0, currentHealth - damage);
                    entity.setHealth(newHealth);
                    
                    if (ailment.getType() == AilmentType.BURN) {
                        ServerWorld world = (ServerWorld) entity.getEntityWorld();
                        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.HOSTILE,
                                1.0f, 1.0f);
                        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.HOSTILE,
                                0.4f, 1.5f + world.getRandom().nextFloat() * 0.5f);
                    }
                }
                ailment.resetTimeSinceLastTick();
            }

            if (ailment.isExpired()) {
                toRemove.add(ailment);
            }
        }

        ailments.removeAll(toRemove);
        ENTITY_AILMENTS.put(entity.getUuid(), ailments);
    }

    private static float getDamagePercent(LivingEntity entity) {
        return 100.0f;
    }

    public static void clearAilment(LivingEntity target, AilmentType type) {
        List<Ailment> ailments = getAilments(target);
        ailments.removeIf(a -> a.getType() == type);
        ENTITY_AILMENTS.put(target.getUuid(), ailments);
    }

    public static void clearAllAilments(LivingEntity target) {
        ENTITY_AILMENTS.remove(target.getUuid());
    }

    public static double getAilmentDuration(LivingEntity target, AilmentType type) {
        List<Ailment> ailments = getAilments(target);
        for (Ailment ailment : ailments) {
            if (ailment.getType() == type) {
                return ailment.getRemainingDuration();
            }
        }
        return 0;
    }

    public static void onEntityDeath(LivingEntity entity) {
        clearAllAilments(entity);
    }

    public static void onEntityDespawn(LivingEntity entity) {
        clearAllAilments(entity);
    }
}
