package com.cranks.dungeons.ailment;

import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.particle.ModParticles;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import com.cranks.dungeons.util.StatusEffectHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.cranks.dungeons.network.ShockArcPayload;
import com.cranks.dungeons.network.AilmentSyncPayload;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffect;

import java.util.Optional;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.UUID;

public class AilmentManager {

    private static final Map<java.util.UUID, List<Ailment>> ENTITY_AILMENTS = new ConcurrentHashMap<>();
    private static final Map<java.util.UUID, Float> FREEZE_BUILDUP = new ConcurrentHashMap<>();
    private static final Map<java.util.UUID, Float> ELECTROCUTE_BUILDUP = new ConcurrentHashMap<>();
    private static final Map<java.util.UUID, Long> FREEZE_BUILDUP_LAST_HIT = new ConcurrentHashMap<>();
    private static final Map<java.util.UUID, Long> ELECTROCUTE_BUILDUP_LAST_HIT = new ConcurrentHashMap<>();
    private static final int POISON_STACK_CAP = 20;
    
    // Track entities currently being processed by shock chain to prevent recursion
    private static final Set<UUID> PROCESSING_SHOCK_CHAIN = new HashSet<>();
    
    // Global flag to prevent ANY shock chain from triggering while one is in progress
    // This stops cross-entity ping-pong (A chains to B, B chains back to A)
    private static boolean GLOBAL_SHOCK_CHAIN_LOCK = false;
    
    // Track when the lock was acquired for timeout purposes
    private static long LOCK_ACQUIRE_TIME = 0;
    
    // Lock timeout in milliseconds (prevent stuck locks)
    private static final long LOCK_TIMEOUT = 100; // 100ms should be plenty for a single chain
    
    // Track which entity triggered the current chain to prevent self-chaining
    private static UUID CURRENT_CHAIN_SOURCE = null;
    
    // Track entities currently being damaged by ailments (to prevent void damage numbers from showing on death)
    private static final Set<UUID> POISON_DAMAGE_IN_PROGRESS = new HashSet<>();
    private static final Set<UUID> BLEED_DAMAGE_IN_PROGRESS = new HashSet<>();
    
    public static boolean isPoisonDamageInProgress(UUID entityUuid) {
        return POISON_DAMAGE_IN_PROGRESS.contains(entityUuid);
    }
    
    public static void setPoisonDamageInProgress(UUID entityUuid, boolean inProgress) {
        if (inProgress) {
            POISON_DAMAGE_IN_PROGRESS.add(entityUuid);
        } else {
            POISON_DAMAGE_IN_PROGRESS.remove(entityUuid);
        }
    }

    public static boolean isBleedDamageInProgress(UUID entityUuid) {
        return BLEED_DAMAGE_IN_PROGRESS.contains(entityUuid);
    }
    
    public static void setBleedDamageInProgress(UUID entityUuid, boolean inProgress) {
        if (inProgress) {
            BLEED_DAMAGE_IN_PROGRESS.add(entityUuid);
        } else {
            BLEED_DAMAGE_IN_PROGRESS.remove(entityUuid);
        }
    }

    public static float getFreezeBuildup(UUID entityUuid) {
        return FREEZE_BUILDUP.getOrDefault(entityUuid, 0.0f);
    }

    public static float getElectrocuteBuildup(UUID entityUuid) {
        return ELECTROCUTE_BUILDUP.getOrDefault(entityUuid, 0.0f);
    }

    public static void addFreezeBuildup(LivingEntity target, float amount, double duration, Entity source) {
        if (hasAilment(target, AilmentType.FREEZE)) {
            com.cranks.dungeons.CranksDungeons.LOGGER.info("[FREEZE_DEBUG] addFreezeBuildup BLOCKED - entity already has FREEZE ailment");
            return;
        }
        float current = getFreezeBuildup(target.getUuid());
        float newBuildup = current + amount;
        com.cranks.dungeons.CranksDungeons.LOGGER.info("[FREEZE_DEBUG] addFreezeBuildup: current={} + amount={} = newBuildup={} ({}%)", 
            String.format("%.4f", current), String.format("%.4f", amount), String.format("%.4f", newBuildup), (int)(newBuildup * 100));
        
        if (newBuildup >= 1.0f) {
            com.cranks.dungeons.CranksDungeons.LOGGER.info("[FREEZE_DEBUG] THRESHOLD REACHED! Removing buildup and applying FREEZE ailment");
            FREEZE_BUILDUP.remove(target.getUuid());
            FREEZE_BUILDUP_LAST_HIT.remove(target.getUuid());
            applyAilment(target, AilmentType.FREEZE, 0.0f, duration, source);
        } else {
            FREEZE_BUILDUP.put(target.getUuid(), newBuildup);
            FREEZE_BUILDUP_LAST_HIT.put(target.getUuid(), target.getEntityWorld().getTime());
        }
        syncAilments(target);
    }

    public static void addElectrocuteBuildup(LivingEntity target, float amount, double duration, Entity source) {
        if (hasAilment(target, AilmentType.ELECTROCUTE)) return;
        float current = getElectrocuteBuildup(target.getUuid());
        float newBuildup = current + amount;
        
        if (newBuildup >= 1.0f) {
            ELECTROCUTE_BUILDUP.remove(target.getUuid());
            ELECTROCUTE_BUILDUP_LAST_HIT.remove(target.getUuid());
            applyAilment(target, AilmentType.ELECTROCUTE, 0.0f, duration, source);
        } else {
            ELECTROCUTE_BUILDUP.put(target.getUuid(), newBuildup);
            ELECTROCUTE_BUILDUP_LAST_HIT.put(target.getUuid(), target.getEntityWorld().getTime());
        }
        syncAilments(target);
    }

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration, Entity source) {
        applyAilment(target, type, baseDamage, duration, source, null, null, 0);
    }

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration, Entity source, Entity parent, UUID chainUuid, int generation) {
        if (target == null) return;

        List<Ailment> ailments = getAilments(target);

        // POISON: Single ailment with stack counter (1-20 base, plus modifiers)
        if (type == AilmentType.POISON) {
            int maxStacks = POISON_STACK_CAP;
            if (source instanceof LivingEntity living) {
                maxStacks += (int) living.getAttributeValue(ModAttributes.POISON_MAX_STACKS);
            }

            Ailment existingPoison = ailments.stream()
                    .filter(a -> a.getType() == AilmentType.POISON)
                    .findFirst()
                    .orElse(null);
            
            if (existingPoison != null) {
                // Add stack to existing poison (up to cap)
                if (existingPoison.getStacks() < maxStacks) {
                    existingPoison.addStack(duration, baseDamage);
                    ENTITY_AILMENTS.put(target.getUuid(), ailments);
                    applyStatusEffect(target, type, existingPoison.getRemainingDuration(), existingPoison.getStacks());
                }
                return;
            }
            // No existing poison, create new one with 1 stack
        } else {
            // BURN and other ailments: don't re-apply if already present
            if (hasAilment(target, type)) {
                return;
            }
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

        Ailment ailment = new Ailment(type, duration, baseDamage, sourceUuid, parentUuid, actualChainUuid, generation);
        ailments.add(ailment);
        ENTITY_AILMENTS.put(target.getUuid(), ailments);

        // Play apply sound and particles
        if (type == AilmentType.FREEZE) {
            // Play sound multiple times to artificially boost the peak volume
            target.getEntityWorld().playSound(null, target.getBlockPos(), com.cranks.dungeons.registry.ModSounds.FREEZE_APPLY, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            target.getEntityWorld().playSound(null, target.getBlockPos(), com.cranks.dungeons.registry.ModSounds.FREEZE_APPLY, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.9f);
            target.getEntityWorld().playSound(null, target.getBlockPos(), com.cranks.dungeons.registry.ModSounds.FREEZE_APPLY, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.1f);
            if (target.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.5), target.getZ(), 30, target.getWidth() / 2.0, target.getHeight() / 2.0, target.getWidth() / 2.0, 0.05);
            }
        } else if (type == AilmentType.ELECTROCUTE) {
            target.getEntityWorld().playSound(null, target.getBlockPos(), com.cranks.dungeons.registry.ModSounds.ELECTROCUTED_APPLY, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            if (target.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getBodyY(0.5), target.getZ(), 20, target.getWidth() / 2.0, target.getHeight() / 2.0, target.getWidth() / 2.0, 0.1);
            }
        }

        // Apply status effect - pass stack count for poison (1-20), or 0 for burn
        int displayValue = (type == AilmentType.POISON) ? ailment.getStacks() : 0;
        applyStatusEffect(target, type, duration, displayValue);
    }

    private static void applyStatusEffect(LivingEntity target, AilmentType type, double duration) {
        applyStatusEffect(target, type, duration, 0);
    }

    private static void applyStatusEffect(LivingEntity target, AilmentType type, double duration, int stackCount) {
        int ticks = (int)(duration * 20);
        switch (type) {
            case BURN -> {
                StatusEffectInstance instance = new StatusEffectInstance(
                        ModEffects.BURN, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case POISON -> {
                // Remove existing poison effect first to force update
                target.removeStatusEffect(ModEffects.POISON);
                // For poison, use stack count as amplifier
                // amplifier 0 = +1, 1 = +2, etc. (from potion.potency translations)
                int amplifier = Math.max(0, stackCount - 1);
                StatusEffectInstance instance = new StatusEffectInstance(
                        ModEffects.POISON, ticks, amplifier, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case FREEZE -> {
                StatusEffectInstance instance = new StatusEffectInstance(
                        ModEffects.FREEZE, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case BLEED -> {
                StatusEffectInstance instance = new StatusEffectInstance(
                        ModEffects.BLEED, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case SHOCK -> {
                StatusEffectInstance instance = new StatusEffectInstance(
                        ModEffects.SHOCK, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case ELECTROCUTE -> {
                StatusEffectInstance instance = new StatusEffectInstance(
                        ModEffects.ELECTROCUTE, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            default -> {}
        }
    }

    public static void syncAilments(LivingEntity entity) {
        if (entity.getEntityWorld().isClient() || !(entity.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld)) return;

        List<AilmentSyncPayload.AilmentEntry> entries = new java.util.ArrayList<>();
        
        checkAndAddSyncEntry(entity, ModEffects.BURN, Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/burn.png"), entries);
        checkAndAddSyncEntry(entity, ModEffects.POISON, Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/poison.png"), entries);
        checkAndAddSyncEntry(entity, ModEffects.FREEZE, Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/freeze.png"), entries);
        checkAndAddSyncEntry(entity, ModEffects.SHOCK, Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/shock.png"), entries);
        checkAndAddSyncEntry(entity, ModEffects.BLEED, Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/bleed.png"), entries);
        checkAndAddSyncEntry(entity, ModEffects.ELECTROCUTE, Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/electrocute.png"), entries);

        float freezeBuildup = getFreezeBuildup(entity.getUuid());
        if (freezeBuildup > 0 && !entity.hasStatusEffect(ModEffects.FREEZE)) {
            int displayPercent = (int)(freezeBuildup * 100);
            com.cranks.dungeons.CranksDungeons.LOGGER.info("[FREEZE_DEBUG] SYNC: freezeBuildup={} -> displayPercent={}%", 
                String.format("%.4f", freezeBuildup), displayPercent);
            entries.add(new AilmentSyncPayload.AilmentEntry(Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/freeze_buildup.png"), displayPercent));
        }

        float electrocuteBuildup = getElectrocuteBuildup(entity.getUuid());
        if (electrocuteBuildup > 0 && !entity.hasStatusEffect(ModEffects.ELECTROCUTE)) {
            entries.add(new AilmentSyncPayload.AilmentEntry(Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "textures/mob_effect/electrocute_buildup.png"), (int)(electrocuteBuildup * 100)));
        }

        AilmentSyncPayload payload = new AilmentSyncPayload(entity.getId(), entries);
        
        // Send to all players tracking this entity
        net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(entity).forEach(player -> {
            ServerPlayNetworking.send(player, payload);
        });
        
        // Also send to the entity themselves if it's a player
        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static void checkAndAddSyncEntry(LivingEntity entity, net.minecraft.registry.entry.RegistryEntry<StatusEffect> effect, Identifier texture, List<AilmentSyncPayload.AilmentEntry> entries) {
        StatusEffectInstance instance = entity.getStatusEffect(effect);
        if (instance != null) {
            entries.add(new AilmentSyncPayload.AilmentEntry(texture, instance.getAmplifier() + 1));
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

    public static boolean isAfflicted(LivingEntity target) {
        if (target == null) return false;
        // Check custom ailment list
        if (!getAilments(target).isEmpty()) return true;
        // Also check if they are on fire (vanilla or mod burn)
        return target.isOnFire();
    }

    public static List<Ailment> getAilments(LivingEntity target) {
        return ENTITY_AILMENTS.getOrDefault(target.getUuid(), new ArrayList<>());
    }

    public static void refreshAilmentDuration(LivingEntity target, AilmentType type) {
        List<Ailment> ailments = getAilments(target);
        for (Ailment ailment : ailments) {
            if (ailment.getType() == type) {
                ailment.setRemainingDuration(ailment.getMaxDuration());
                // Also refresh the status effect visually
                applyStatusEffect(target, type, ailment.getMaxDuration(), type == AilmentType.POISON ? ailment.getStacks() : 0);
            }
        }
    }

    public static void tickAilments(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;
        
        if (!entity.isAlive() || entity.getHealth() <= 0) {
            clearAllAilments(entity);
            return;
        }

        // Decay buildups
        boolean buildupChanged = false;
        long currentTime = entity.getEntityWorld().getTime();
        
        if (FREEZE_BUILDUP.containsKey(entity.getUuid())) {
            long lastHit = FREEZE_BUILDUP_LAST_HIT.getOrDefault(entity.getUuid(), 0L);
            if (currentTime - lastHit > 40) { // 2 seconds grace period
                float current = FREEZE_BUILDUP.get(entity.getUuid());
                float newBuildup = current - 0.0025f; // Decay 5% per second (0.0025 per tick)
                com.cranks.dungeons.CranksDungeons.LOGGER.info("[FREEZE_DEBUG] DECAY: current={} -> newBuildup={} ({}%)", 
                    String.format("%.4f", current), String.format("%.4f", newBuildup), (int)(newBuildup * 100));
                if (newBuildup <= 0) {
                    FREEZE_BUILDUP.remove(entity.getUuid());
                    FREEZE_BUILDUP_LAST_HIT.remove(entity.getUuid());
                } else {
                    FREEZE_BUILDUP.put(entity.getUuid(), newBuildup);
                }
                buildupChanged = true;
            }
        }
        
        if (ELECTROCUTE_BUILDUP.containsKey(entity.getUuid())) {
            long lastHit = ELECTROCUTE_BUILDUP_LAST_HIT.getOrDefault(entity.getUuid(), 0L);
            if (currentTime - lastHit > 40) { // 2 seconds grace period
                float current = ELECTROCUTE_BUILDUP.get(entity.getUuid());
                float newBuildup = current - 0.01f; // Decay 20% per second (0.01 per tick)
                if (newBuildup <= 0) {
                    ELECTROCUTE_BUILDUP.remove(entity.getUuid());
                    ELECTROCUTE_BUILDUP_LAST_HIT.remove(entity.getUuid());
                } else {
                    ELECTROCUTE_BUILDUP.put(entity.getUuid(), newBuildup);
                }
                buildupChanged = true;
            }
        }

        // Periodic sync for duration progress and reaction cooldowns
        if (entity.getEntityWorld().getTime() % 10 == 0 || buildupChanged) {
            syncAilments(entity);
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
                        
                        if (ailment.getGeneration() < 5) {
                            handleBurnSpread(entity, ailment);
                        }

                        if (damage > 0) {
                            applySingleAilmentDamage(entity, damage, ailment);
                        }
                    } else if (ailment.getType() == AilmentType.POISON) {
                        // Handle POISON damage (single ailment with stacks)
                        applyPoisonDamage(entity, ailment, damage);
                    } else if (ailment.getType() == AilmentType.BLEED) {
                        // BLEED doesn't deal tick damage - only burst damage on expiration
                        // Skip damage application
                    } else {
                        // Handle other ailments
                        applySingleAilmentDamage(entity, damage, ailment);
                    }
                }
                ailment.resetTimeSinceLastTick();
            }

            if (ailment.isExpired()) {
                if (ailment.getType() == AilmentType.POISON) {
                    // For poison: remove one stack and reset timer
                    ailment.removeStack();
                    if (ailment.getStacks() <= 0) {
                        // Only remove when all stacks are gone
                        toRemove.add(ailment);
                        entity.removeStatusEffect(ModEffects.POISON);
                    } else {
                        // Update status effect with new stack count
                        applyStatusEffect(entity, AilmentType.POISON, ailment.getRemainingDuration(), ailment.getStacks());
                        syncAilments(entity);
                    }
                } else if (ailment.getType() == AilmentType.BLEED) {
                    // For bleed: deal stored damage when it expires
                    float storedDamage = ailment.getStoredDamage();
                    if (storedDamage > 0) {
                        applyBleedBurstDamage(entity, storedDamage, ailment);
                    }
                    toRemove.add(ailment);
                    entity.removeStatusEffect(ModEffects.BLEED);
                } else {
                    toRemove.add(ailment);
                    if (ailment.getType() == AilmentType.BURN) {
                        entity.removeStatusEffect(ModEffects.BURN);
                    } else if (ailment.getType() == AilmentType.FREEZE) {
                        entity.removeStatusEffect(ModEffects.FREEZE);
                    } else if (ailment.getType() == AilmentType.SHOCK) {
                        entity.removeStatusEffect(ModEffects.SHOCK);
                    } else if (ailment.getType() == AilmentType.ELECTROCUTE) {
                        entity.removeStatusEffect(ModEffects.ELECTROCUTE);
                    }
                }
            }
        }

        ailments.removeAll(toRemove);
        
        if (entity.isAlive() && entity.getHealth() > 0) {
            ENTITY_AILMENTS.put(entity.getUuid(), ailments);
        } else {
            clearAllAilments(entity);
        }
    }

    private static void applySingleAilmentDamage(LivingEntity entity, float damage, Ailment ailment) {
        float damageToApply = damage;

        // Energy Shield Mitigation for players (Burn/Shock/Cold/Lightning/Void etc. ailments)
        // Poison and Bleed bypass Energy Shield based on PoE 2 rules
        if (entity instanceof PlayerEntity player && entity instanceof com.cranks.dungeons.EnergyShieldProvider esProvider) {
            if (ailment.getType() != AilmentType.POISON && ailment.getType() != AilmentType.BLEED) {
                float currentShield = esProvider.cranksdungeons$getCurrentEnergyShield();
                if (currentShield > 0) {
                    float weightedDamage = (ailment.getType() == AilmentType.BURN || ailment.getType() == AilmentType.SHOCK || ailment.getType() == AilmentType.FREEZE) ? damageToApply : damageToApply;
                    // Note: Void ailment doesn't exist yet but if it did, weighted *= 2.0f
                    
                    if (currentShield >= weightedDamage) {
                        float newShield = currentShield - weightedDamage;
                        esProvider.cranksdungeons$setCurrentEnergyShield(newShield);
                        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                            float maxShield = (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(newShield, maxShield)
                            );
                        }
                        damageToApply = 0; // Fully absorbed
                    } else {
                        float absorptionRatio = currentShield / weightedDamage;
                        damageToApply -= damageToApply * absorptionRatio;
                        esProvider.cranksdungeons$setCurrentEnergyShield(0.0f);
                        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                            float maxShield = (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(0.0f, maxShield)
                            );
                        }
                    }
                }
            }
        }

        if (damageToApply <= 0 && damage > 0) {
            // Damage was fully absorbed by ES. Still broadcast damage numbers.
            if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
                double fire = (ailment.getType() == AilmentType.BURN) ? damage : 0.0;
                double physical = (ailment.getType() == AilmentType.BLEED) ? (double)damage : 0.0;
                com.cranks.dungeons.network.DamageNumberSender.broadcast(serverWorld, entity, physical, fire, 0.0, 0.0, 0.0, ailment.getSourceUuid(), false);
            }
            return;
        }

        float currentHealth = entity.getHealth();
        float newHealth = Math.max(0, currentHealth - damageToApply);

        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
            double fire = (ailment.getType() == AilmentType.BURN) ? damage : 0.0;
            double physical = (ailment.getType() == AilmentType.BLEED) ? (double)damage : 0.0;

            // Apply lethal damage via the damage system so death-prevention mods can catch it
            if (currentHealth - damageToApply <= 0) {
                DamageNumberSender.broadcast(serverWorld, entity, physical, fire, 0.0, 0.0, 0.0, ailment.getSourceUuid(), false);
                
                net.minecraft.entity.damage.DamageSource source;
                if (ailment.getType() == AilmentType.BURN) {
                    // Use generic source so it's quiet (no vanilla fire sounds),
                    // but it's mapped to Fire Elemental in DamageConversionHelper if target has BURN.
                    source = serverWorld.getDamageSources().generic();
                } else {
                    source = serverWorld.getDamageSources().magic();
                }
                entity.damage(serverWorld, source, damageToApply);
            } else {
                // Apply non-lethal damage directly for cleaner numbers
                entity.setHealth(newHealth);
                DamageNumberSender.broadcast(serverWorld, entity, physical, fire, 0.0, 0.0, 0.0, ailment.getSourceUuid(), false);
            }
        }

        if (ailment.getType() == AilmentType.BURN) {
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            
            // Always play the custom burn tick
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.BURN_TICK, SoundCategory.HOSTILE, 1.0f, 1.0f + world.getRandom().nextFloat() * 0.2f);

            // Always play standard hurt sound
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
    }

    private static void applyBleedBurstDamage(LivingEntity entity, float storedDamage, Ailment ailment) {
        if (entity.getEntityWorld().isClient()) return;
        
        ServerWorld world = (ServerWorld) entity.getEntityWorld();
        float currentHealth = entity.getHealth();
        
        // Broadcast damage numbers as physical (dark red)
        DamageNumberSender.broadcastBleed(world, entity, storedDamage, ailment.getSourceUuid());
        
        // Mark bleed damage as in progress to prevent void numbers from showing on kill
        setBleedDamageInProgress(entity.getUuid(), true);
        try {
            if (currentHealth - storedDamage <= 0) {
                // Entity will die - use proper damage source to ensure loot drops
                net.minecraft.entity.damage.DamageSource damageSource = world.getDamageSources().magic();
                entity.damage(world, damageSource, storedDamage);
            } else {
                // Entity won't die - apply damage directly
                entity.setHealth(currentHealth - storedDamage);
            }
        } finally {
            setBleedDamageInProgress(entity.getUuid(), false);
        }
        
        // Bleed burst sound effect
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                ModSounds.BLEED_BURST, SoundCategory.HOSTILE,
                0.8f, 0.8f + world.getRandom().nextFloat() * 0.3f);
    }

    public static void accumulateBleedDamage(LivingEntity entity, float damageReceived) {
        if (entity.getEntityWorld().isClient()) return;
        if (!hasAilment(entity, AilmentType.BLEED)) return;
        
        List<Ailment> ailments = getAilments(entity);
        for (Ailment ailment : ailments) {
            if (ailment.getType() == AilmentType.BLEED) {
                // Store 40% of damage received (Buffed from 10%)
                float damageToStore = damageReceived * 0.40f;
                ailment.addStoredDamage(damageToStore);
                
                // Play bleed hit sound when entity gets hit while bleeding
                ServerWorld world = (ServerWorld) entity.getEntityWorld();
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.BLEED_HIT, SoundCategory.HOSTILE,
                        0.8f, 0.9f + world.getRandom().nextFloat() * 0.2f);
                break;
            }
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
            if (ailment.getSourceUuid() != null && target.getUuid().equals(ailment.getSourceUuid())) {
                continue;
            }
            if (target instanceof PlayerEntity) {
                continue;
            }
            if (target instanceof AilmentHistoryProvider history && history.cranks$wasHitByChain(ailment.getChainUuid())) {
                continue;
            }
            
            float newDamage = ailment.getBaseDamage() * 0.5f; 
            double newDuration = Math.max(1.0, ailment.getRemainingDuration() * 0.5); 
            
            if (newDamage < 0.2f) continue;

            applyAilment(target, AilmentType.BURN, newDamage, newDuration, originalSource, entity, ailment.getChainUuid(), ailment.getGeneration() + 1);
        }
    }

    private static void applyPoisonDamage(LivingEntity entity, Ailment poisonAilment, float damageFromTick) {
        if (entity.getEntityWorld().isClient()) return;
        
        double res = entity.getAttributeValue(ModAttributes.VOID_RESISTANCE);
        int stacks = poisonAilment.getStacks();
        // Escalation: multiplicative scaling (+20% compound growth per stack)
        // 1 stack = 1.0, 2 stacks = 1.2, 3 stacks = 1.44, 20 stacks = ~32
        double damageMultiplier = Math.pow(1.2, stacks - 1);
        // Damage = damagePerStack × multiplier × (1 - resistance)
        float finalPoisonDamage = (float) (poisonAilment.getDamagePerStack() * (1.0 - res) * damageMultiplier);

        if (finalPoisonDamage > 0) {
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            
            // Check if damage will kill the entity
            float currentHealth = entity.getHealth();
            if (currentHealth - finalPoisonDamage <= 0) {
                // Entity will die - use proper damage source to ensure loot drops
                // Send poison damage number BEFORE applying killing blow so it shows green instead of purple void
                DamageNumberSender.broadcastPoison(world, entity, finalPoisonDamage, poisonAilment.getSourceUuid());
                // Mark poison damage as in progress to prevent void numbers from showing
                setPoisonDamageInProgress(entity.getUuid(), true);
                try {
                    net.minecraft.entity.damage.DamageSource damageSource = world.getDamageSources().magic();
                    entity.damage(world, damageSource, finalPoisonDamage);
                } finally {
                    setPoisonDamageInProgress(entity.getUuid(), false);
                }
            } else {
                // Entity won't die - apply damage directly to avoid vanilla damage numbers
                entity.setHealth(currentHealth - finalPoisonDamage);
                
                // Poison sound effect (custom)
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.POISON_TICK, SoundCategory.HOSTILE,
                        0.8f, 1.0f);
                
                // Player hurt sound
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.HOSTILE,
                        0.8f, 1.0f);

                DamageNumberSender.broadcastPoison(world, entity, finalPoisonDamage, poisonAilment.getSourceUuid());
            }
            
            // Update status effect with current stack count
            applyStatusEffect(entity, AilmentType.POISON, poisonAilment.getRemainingDuration(), stacks);
        }
    }

    private static float getDamagePercent(LivingEntity entity) {
        return 1.0f;
    }

    public static void clearAilment(LivingEntity target, AilmentType type) {
        List<Ailment> ailments = getAilments(target);
        ailments.removeIf(a -> a.getType() == type);
        ENTITY_AILMENTS.put(target.getUuid(), ailments);
        
        // Remove status effect
        if (type == AilmentType.POISON) {
            target.removeStatusEffect(ModEffects.POISON);
        } else if (type == AilmentType.BURN) {
            target.removeStatusEffect(ModEffects.BURN);
        } else if (type == AilmentType.FREEZE) {
            target.removeStatusEffect(ModEffects.FREEZE);
        } else if (type == AilmentType.BLEED) {
            target.removeStatusEffect(ModEffects.BLEED);
        } else if (type == AilmentType.SHOCK) {
            target.removeStatusEffect(ModEffects.SHOCK);
        }
    }

    public static void clearAllAilments(LivingEntity target) {
        ENTITY_AILMENTS.remove(target.getUuid());
        FREEZE_BUILDUP.remove(target.getUuid());
        ELECTROCUTE_BUILDUP.remove(target.getUuid());
        target.removeStatusEffect(ModEffects.POISON);
        target.removeStatusEffect(ModEffects.BURN);
        target.removeStatusEffect(ModEffects.FREEZE);
        target.removeStatusEffect(ModEffects.BLEED);
        target.removeStatusEffect(ModEffects.SHOCK);
        target.removeStatusEffect(ModEffects.ELECTROCUTE);
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

    public static void handleShockChain(LivingEntity entity, float lightningDamage, Entity source) {
        if (entity.getEntityWorld().isClient()) return;
        if (!hasAilment(entity, AilmentType.SHOCK)) return;
        if (lightningDamage <= 0) return;
        
        // Note: Global lock is managed by the caller (ElementalDamageMixin)
        // This prevents cross-entity ping-pong (A chains to B, B chains back to A)
        
        // NOTE: Removed source type matching to ensure ALL lightning damage chains
        // This ensures consistent behavior whether shock was applied by player or mob
        
        executeShockChain(entity, lightningDamage, source);
    }
    
    /**
     * Execute shock chain without checking if entity has shock (for killing hits where ailment was already cleared)
     */
    public static void executeShockChain(LivingEntity entity, float lightningDamage, Entity source) {
        if (entity.getEntityWorld().isClient()) return;
        if (lightningDamage <= 0) return;
        
        ServerWorld world = (ServerWorld) entity.getEntityWorld();
        double radius = 10.0;
        Box box = entity.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = world.getEntitiesByClass(LivingEntity.class, box, e -> 
            e.isAlive() && hasAilment(e, AilmentType.SHOCK)
        );
        
        for (LivingEntity target : nearby) {
            // Skip the entity that triggered this chain to prevent self-damage and duplicate numbers
            if (target == entity) {
                continue;
            }
            
            // Apply lightning damage using the elemental damage system
            // Apply resistance reduction
            double resistance = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
            double finalDamage = lightningDamage * (1.0 - resistance);
            
            if (finalDamage > 0) {
                // Use magic damage to apply damage with full hit reactions
                // The global lock is already set, which prevents both recursive chains 
                // AND double elemental processing (mixin checks isShockChainInProgress)
                net.minecraft.entity.damage.DamageSource damageSource = world.getDamageSources().magic();
                target.damage(world, damageSource, (float) finalDamage);
                
                // Play custom shock chain sound
                world.playSound(null, target.getX(), target.getY(), target.getZ(),
                        ModSounds.SHOCK_CHAIN, SoundCategory.HOSTILE,
                        0.7f, 1.0f + world.getRandom().nextFloat() * 0.3f);
                
                // Broadcast damage numbers as lightning damage  
                DamageNumberSender.broadcast(world, target, 0, 0, 0, finalDamage, 0, source != null ? source.getUuid() : null, false);
                
                // Spawn shock chain particles at target position
                spawnShockChainParticles(world, target);

                // Send ShockArc visual to all nearby players
                Vec3d startPos = new Vec3d(entity.getX(), entity.getY() + entity.getStandingEyeHeight() * 0.8, entity.getZ());
                Vec3d endPos = new Vec3d(target.getX(), target.getY() + target.getStandingEyeHeight() * 0.8, target.getZ());
                ShockArcPayload payload = new ShockArcPayload(startPos, endPos, 0xFFFFFF); // White lightning

                for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
                    if (player.squaredDistanceTo(startPos) < 64 * 64) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
                    }
                }
                
            }
        }
    }

    public static boolean isShockChainInProgress() {
        // Check if lock has timed out
        if (GLOBAL_SHOCK_CHAIN_LOCK && System.currentTimeMillis() - LOCK_ACQUIRE_TIME > LOCK_TIMEOUT) {
            System.err.println("[CranksDungeons] Shock chain lock timed out, forcing release");
            releaseShockChainLock();
            return false;
        }
        return GLOBAL_SHOCK_CHAIN_LOCK;
    }
    
    public static boolean acquireShockChainLock() {
        // Check if previous lock timed out
        if (GLOBAL_SHOCK_CHAIN_LOCK && System.currentTimeMillis() - LOCK_ACQUIRE_TIME > LOCK_TIMEOUT) {
            System.err.println("[CranksDungeons] Previous shock chain lock timed out, resetting");
            releaseShockChainLock();
        }
        
        if (GLOBAL_SHOCK_CHAIN_LOCK) {
            return false;
        }
        GLOBAL_SHOCK_CHAIN_LOCK = true;
        LOCK_ACQUIRE_TIME = System.currentTimeMillis();
        return true;
    }
    
    public static void releaseShockChainLock() {
        GLOBAL_SHOCK_CHAIN_LOCK = false;
        LOCK_ACQUIRE_TIME = 0;
    }

    private static void spawnShockChainParticles(ServerWorld world, LivingEntity target) {
        double centerX = target.getX();
        double centerY = target.getY() + target.getStandingEyeHeight() * 0.8;
        double centerZ = target.getZ();

        // Spawn 4 different shock particles in a small burst (single iteration)
        double offsetX = (world.getRandom().nextDouble() - 0.5) * 1.0;
        double offsetY = (world.getRandom().nextDouble() - 0.5) * 1.0;
        double offsetZ = (world.getRandom().nextDouble() - 0.5) * 1.0;

        // Spawn all 4 shock particle types with slight offsets
        world.spawnParticles(ModParticles.SHOCK_SPARK_1, centerX + offsetX, centerY + offsetY, centerZ + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
        world.spawnParticles(ModParticles.SHOCK_SPARK_2, centerX + offsetX * 0.8, centerY + offsetY * 0.8, centerZ + offsetZ * 0.8, 1, 0.1, 0.1, 0.1, 0.05);
        world.spawnParticles(ModParticles.SHOCK_SPARK_3, centerX + offsetX * 1.2, centerY + offsetY * 1.2, centerZ + offsetZ * 1.2, 1, 0.1, 0.1, 0.1, 0.05);
        world.spawnParticles(ModParticles.SHOCK_SPARK_4, centerX + offsetX * 0.6, centerY + offsetY * 0.6, centerZ + offsetZ * 0.6, 1, 0.1, 0.1, 0.1, 0.05);
    }
    

}
