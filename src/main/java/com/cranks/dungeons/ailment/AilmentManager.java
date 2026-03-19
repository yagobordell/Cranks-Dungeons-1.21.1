package com.cranks.dungeons.ailment;

import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import com.cranks.dungeons.util.StatusEffectHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class AilmentManager {

    private static final Map<UUID, List<Ailment>> ENTITY_AILMENTS = new HashMap<>();

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration, Entity source) {
        applyAilment(target, type, baseDamage, duration, source, null, null, 0);
    }

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration, Entity source, Entity parent, UUID chainUuid, int generation) {
        if (target == null) return;

        if (hasAilment(target, type)) {
            return;
        }

        // Rule: Don't re-ignite with the same chain if we've already been hit by it
        if (type == AilmentType.BURN && chainUuid != null && target instanceof AilmentHistoryProvider history) {
            if (history.cranks$wasHitByChain(chainUuid)) {
                return;
            }
        }

        UUID sourceUuid = (source != null) ? source.getUuid() : null;
        UUID parentUuid = (parent != null) ? parent.getUuid() : null;
        UUID actualChainUuid = (chainUuid != null) ? chainUuid : UUID.randomUUID();
        
        // Record this chain in the history
        if (type == AilmentType.BURN && target instanceof AilmentHistoryProvider history) {
            history.cranks$recordBurnChain(actualChainUuid);
        }

        List<Ailment> ailments = getAilments(target);
        Ailment ailment = new Ailment(type, duration, baseDamage, sourceUuid, parentUuid, actualChainUuid, generation);
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
        
        if (!entity.isAlive() || entity.getHealth() <= 0) {
            clearAllAilments(entity);
            return;
        }

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
                    if (ailment.getType() == AilmentType.BURN) {
                        double res = entity.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                        damage *= (float) (1.0 - res);
                        
                        // SPREADING MECHANIC
                        if (ailment.getGeneration() < 5) { // Maximum 5 hops
                            handleBurnSpread(entity, ailment);
                        }
                    }

                    if (damage > 0) {
                        float currentHealth = entity.getHealth();
                        float newHealth = Math.max(0, currentHealth - damage);
                        entity.setHealth(newHealth);
                        
                        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
                            double fire = (ailment.getType() == AilmentType.BURN) ? damage : 0.0;
                            double physical = (ailment.getType() == AilmentType.BLEED || ailment.getType() == AilmentType.POISON) ? (double)damage : 0.0;
                            
                            // Use the stored sourceUuid from the ailment to ensure correct attribution
                            DamageNumberSender.broadcast(serverWorld, entity, physical, fire, 0.0, 0.0, 0.0, ailment.getSourceUuid());
                        }

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
                }
                ailment.resetTimeSinceLastTick();
            }

            if (ailment.isExpired()) {
                toRemove.add(ailment);
            }
        }

        ailments.removeAll(toRemove);
        
        if (entity.isAlive() && entity.getHealth() > 0) {
            ENTITY_AILMENTS.put(entity.getUuid(), ailments);
        } else {
            clearAllAilments(entity);
        }
    }

    private static void handleBurnSpread(LivingEntity entity, Ailment ailment) {
        if (entity.getEntityWorld().isClient()) return;
        
        ServerWorld world = (ServerWorld) entity.getEntityWorld();
        double radius = 3.0;
        Box box = entity.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = world.getEntitiesByClass(LivingEntity.class, box, e -> e != entity && e.isAlive());

        Entity originalSource = null;
        if (ailment.getSourceUuid() != null) {
            originalSource = world.getEntity(ailment.getSourceUuid());
        }

        for (LivingEntity target : nearby) {
            // Rule 1: No spread to original source (player safety)
            if (ailment.getSourceUuid() != null && target.getUuid().equals(ailment.getSourceUuid())) {
                continue;
            }

            // Rule 2: Mobs/Environment never spread fire to ANY Player (COOP Safety)
            if (target instanceof PlayerEntity) {
                continue;
            }

            // Rule 3: Ancestry check (don't spread back to anyone who was part of this chain)
            if (target instanceof AilmentHistoryProvider history && history.cranks$wasHitByChain(ailment.getChainUuid())) {
                continue;
            }
            
            // Decaying damage and duration (percentage based)
            float newDamage = ailment.getBaseDamage() * 0.5f; // 50% damage per hop
            double newDuration = Math.max(1.0, ailment.getRemainingDuration() * 0.5); // 50% duration per hop
            
            if (newDamage < 0.2f) continue;

            applyAilment(target, AilmentType.BURN, newDamage, newDuration, originalSource, entity, ailment.getChainUuid(), ailment.getGeneration() + 1);
        }
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
