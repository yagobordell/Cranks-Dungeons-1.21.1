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

import java.util.Optional;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class AilmentManager {

    private static final Map<UUID, List<Ailment>> ENTITY_AILMENTS = new HashMap<>();
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

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration, Entity source) {
        applyAilment(target, type, baseDamage, duration, source, null, null, 0);
    }

    public static void applyAilment(LivingEntity target, AilmentType type, float baseDamage, double duration, Entity source, Entity parent, UUID chainUuid, int generation) {
        if (target == null) return;

        List<Ailment> ailments = getAilments(target);

        // POISON: Single ailment with stack counter (1-20)
        if (type == AilmentType.POISON) {
            Ailment existingPoison = ailments.stream()
                    .filter(a -> a.getType() == AilmentType.POISON)
                    .findFirst()
                    .orElse(null);
            
            if (existingPoison != null) {
                // Add stack to existing poison (up to cap)
                if (existingPoison.getStacks() < POISON_STACK_CAP) {
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
                StatusEffectInstance instance = StatusEffectHelper.create(
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
                StatusEffectInstance instance = StatusEffectHelper.create(
                        ModEffects.POISON, ticks, amplifier, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case FREEZE -> {
                StatusEffectInstance instance = StatusEffectHelper.create(
                        ModEffects.FREEZE, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case BLEED -> {
                StatusEffectInstance instance = StatusEffectHelper.create(
                        ModEffects.BLEED, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            case SHOCK -> {
                StatusEffectInstance instance = StatusEffectHelper.create(
                        ModEffects.SHOCK, ticks, 0, false, false, true);
                if (instance != null) {
                    target.addStatusEffect(instance);
                }
            }
            default -> {}
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
                        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(newShield)
                            );
                        }
                        damageToApply = 0; // Fully absorbed
                    } else {
                        float absorptionRatio = currentShield / weightedDamage;
                        damageToApply -= damageToApply * absorptionRatio;
                        esProvider.cranksdungeons$setCurrentEnergyShield(0.0f);
                        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(0.0f)
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
        entity.setHealth(newHealth);

        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
            double fire = (ailment.getType() == AilmentType.BURN) ? damage : 0.0;
            double physical = (ailment.getType() == AilmentType.BLEED) ? (double)damage : 0.0;
            DamageNumberSender.broadcast(serverWorld, entity, physical, fire, 0.0, 0.0, 0.0, ailment.getSourceUuid(), false);
        }

        if (ailment.getType() == AilmentType.BURN) {
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.HOSTILE,
                    1.0f, 1.0f);
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.BURN_TICK, SoundCategory.HOSTILE,
                    1.0f, 1.0f + world.getRandom().nextFloat() * 0.2f);
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
                // Store 10% of damage received
                float damageToStore = damageReceived * 0.10f;
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
        return 100.0f;
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
        target.removeStatusEffect(ModEffects.POISON);
        target.removeStatusEffect(ModEffects.BURN);
        target.removeStatusEffect(ModEffects.FREEZE);
        target.removeStatusEffect(ModEffects.BLEED);
        target.removeStatusEffect(ModEffects.SHOCK);
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
            // Skip the entity that triggered this chain to prevent self-damage
            // (they already took the original lightning damage from the hit)
            if (target.getUuid().equals(entity.getUuid())) {
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
        double centerY = target.getY() + target.getHeight() / 2.0;
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
