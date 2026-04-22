package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.CombatUtility;
import com.cranks.dungeons.util.CriticalHitState;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ElementalDamageMixin {

    @Shadow
    public abstract boolean damage(ServerWorld world, DamageSource source, float amount);

    @Shadow
    public abstract void heal(float amount);

    @Shadow
    public abstract double getAttributeValue(
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Unique
    private boolean cranks$processingElementalDamage = false;

    // Static flag to prevent shock chain damage from triggering elemental
    // processing

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float cranks$reducePvPDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (source.getAttacker() instanceof PlayerEntity && target instanceof PlayerEntity) {
            return amount * 0.25f;
        }
        return amount;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void applyElementalDamageOnHit(ServerWorld world, DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        // Skip if damage didn't succeed or already processing
        if (!cir.getReturnValue()) {
            return;
        }

        LivingEntity target = (LivingEntity) (Object) this;

        if (cranks$processingElementalDamage) {
            return;
        }

        // Skip if this is shock chain damage (prevent double processing)
        if (AilmentManager.isShockChainInProgress()) {
            return;
        }

        // Accumulate bleed damage if target has bleed ailment
        AilmentManager.accumulateBleedDamage(target, amount);

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            handlePlayerAttacker(world, source, amount, attacker, target);
        } else {
            handleNonPlayerDamage(world, source, amount, target);
        }
    }

    @Unique
    private void handlePlayerAttacker(ServerWorld world, DamageSource source, float amount, PlayerEntity attacker,
            LivingEntity target) {
        ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);
        ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

        // Check if this hit was a critical strike
        boolean isPvP = target instanceof PlayerEntity;
        double pvpMultiplier = isPvP ? 0.25 : 1.0;

        boolean isCrit = CriticalHitState.isCriticalHit();
        float critMultiplier = isCrit ? (float) attacker.getAttributeValue(ModAttributes.CRIT_MULTIPLIER) : 1.0f;
        // Halve the bonus multiplier in PvP
        if (isCrit && isPvP) {
            critMultiplier = 1.0f + (critMultiplier - 1.0f) * 0.5f;
        }

        // Use unreduced physical damage for internal scaling calculations to avoid
        // double-dipping the 75% PvP reduction
        float unreducedPhysicalDamage = (float) (amount / pvpMultiplier);
        float physicalDamage = amount;

        try {
            cranks$processingElementalDamage = true;

            // Check for shock IMMEDIATELY at start - BEFORE any damage calculations or
            // applications
            // This ensures we capture the shock state before any damage kills the entity
            final boolean[] hasShockBeforeDamage = { AilmentManager.hasAilment(target, AilmentType.SHOCK) };

            // Get weapon-specific bonus once
            double weaponSpecificPercent = CombatUtility.getWeaponSpecificDamageBonus(attacker, weapon);

            double afflictedBonus = 0;
            if (AilmentManager.isAfflicted(target)) {
                afflictedBonus = attacker.getAttributeValue(ModAttributes.DAMAGE_AGAINST_AFFLICTED_PERCENT);
            }

            double elementalBonus = attacker.getAttributeValue(ModAttributes.ELEMENTAL_DAMAGE_PERCENT);

            double typeBonus = 0;
            if (CombatUtility.isMelee(weapon, source)) {
                typeBonus = attacker.getAttributeValue(ModAttributes.MELEE_DAMAGE_PERCENT);
            } else if (CombatUtility.isRanged(weapon, source)) {
                typeBonus = attacker.getAttributeValue(ModAttributes.RANGED_DAMAGE_PERCENT);
            }
            
            double wieldingBonus = 0;
            if (CombatUtility.isTwoHanding(attacker, weapon)) {
                wieldingBonus = attacker.getAttributeValue(ModAttributes.TWO_HANDING_DAMAGE_PERCENT);
            } else if (CombatUtility.isDualWielding(attacker, weapon)) {
                wieldingBonus = attacker.getAttributeValue(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT);
            } else if (CombatUtility.isOneHanding(attacker, weapon)) {
                wieldingBonus = attacker.getAttributeValue(ModAttributes.ONE_HANDED_DAMAGE_PERCENT);
            }

            // === FIRE DAMAGE ===
            double fireBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.FIRE_DAMAGE, weapon,
                    "fire_damage", source);
            fireBaseDamage += getEnchantmentValue(weapon, "minecraft:fire_damage", 4.0);

            double fireTotalPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.FIRE_DAMAGE_PERCENT,
                    weapon, "fire_damage_percent", source);

            double fireDamage = fireBaseDamage
                    * (1.0 + fireTotalPercent + weaponSpecificPercent + afflictedBonus + elementalBonus + typeBonus + wieldingBonus)
                    * critMultiplier;

            // === COLD DAMAGE ===
            double coldBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.COLD_DAMAGE, weapon,
                    "cold_damage", source);
            coldBaseDamage += getEnchantmentValue(weapon, "minecraft:cold_damage", 4.0);

            double coldTotalPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.COLD_DAMAGE_PERCENT,
                    weapon, "cold_damage_percent", source);

            double coldDamage = coldBaseDamage
                    * (1.0 + coldTotalPercent + weaponSpecificPercent + afflictedBonus + elementalBonus + typeBonus + wieldingBonus)
                    * critMultiplier;

            // === LIGHTNING DAMAGE ===
            double lightningBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.LIGHTNING_DAMAGE,
                    weapon, "lightning_damage", source);
            lightningBaseDamage += getEnchantmentValue(weapon, "minecraft:lightning_damage", 4.0);

            double lightningTotalPercent = CombatUtility.getAttributeValue(attacker,
                    ModAttributes.LIGHTNING_DAMAGE_PERCENT, weapon, "lightning_damage_percent", source);

            double lightningDamage = lightningBaseDamage * (1.0 + lightningTotalPercent + weaponSpecificPercent
                    + afflictedBonus + elementalBonus + typeBonus + wieldingBonus) * critMultiplier;

            // === VOID DAMAGE ===
            double voidBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.VOID_DAMAGE, weapon,
                    "void_damage", source);
            voidBaseDamage += getEnchantmentValue(weapon, "minecraft:void_damage", 4.0);

            double voidTotalPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.VOID_DAMAGE_PERCENT,
                    weapon, "void_damage_percent", source);

            double voidDamage = voidBaseDamage
                    * (1.0 + voidTotalPercent + weaponSpecificPercent + afflictedBonus + elementalBonus + typeBonus + wieldingBonus)
                    * critMultiplier;

            // === PROPORTIONAL EXTRA DAMAGE (RUNES) ===
            // Calculate total basic damage using UNREDUCED values for proportional scaling
            double totalBasicDamage = unreducedPhysicalDamage + fireDamage + coldDamage + lightningDamage + voidDamage;

            double extraPhysicalPercent = CombatUtility.getAttributeValue(attacker,
                    ModAttributes.PHYSICAL_EXTRA_PERCENT, weapon, "attack_extra_damage", source);
            double finalPhysicalExtraDamage = totalBasicDamage * extraPhysicalPercent;

            double extraFirePercent = CombatUtility.getAttributeValue(attacker, ModAttributes.FIRE_EXTRA_PERCENT,
                    weapon, "fire_extra_damage", source);
            fireDamage += totalBasicDamage * extraFirePercent;

            double extraColdPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.COLD_EXTRA_PERCENT,
                    weapon, "cold_extra_damage", source);
            coldDamage += totalBasicDamage * extraColdPercent;

            double extraLightningPercent = CombatUtility.getAttributeValue(attacker,
                    ModAttributes.LIGHTNING_EXTRA_PERCENT, weapon, "lightning_extra_damage", source);
            lightningDamage += totalBasicDamage * extraLightningPercent;

            double extraVoidPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.VOID_EXTRA_PERCENT,
                    weapon, "void_extra_damage", source);
            voidDamage += totalBasicDamage * extraVoidPercent;

            // Apply global PvP reduction to final bonus values
            fireDamage *= pvpMultiplier;
            coldDamage *= pvpMultiplier;
            lightningDamage *= pvpMultiplier;
            voidDamage *= pvpMultiplier;
            finalPhysicalExtraDamage *= pvpMultiplier;

            double totalElementalDamage = 0;

            double finalFireDamage = 0;
            double finalColdDamage = 0;
            double finalLightningDamage = 0;
            double finalVoidDamage = 0;

            // Track if target died from elemental damage to ensure proper death handling
            boolean targetDiedFromElemental = false;
            float healthBeforeElemental = target.getHealth();

            if (finalPhysicalExtraDamage > 0) {
                float reducedExtraPhysical = DamageConversionHelper.calculatePhysicalReduction(target,
                        (float) finalPhysicalExtraDamage);
                applyElementalDamage(target, world, reducedExtraPhysical, DamageElement.PHYSICAL);
                totalElementalDamage += reducedExtraPhysical;
                physicalDamage += reducedExtraPhysical;
                if (target.getHealth() <= 0)
                    targetDiedFromElemental = true;
            }

            if (fireDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                double finalDamage = fireDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    // Use proper damage method with fire damage source to ensure death handling
                    applyElementalDamage(target, world, finalDamage, DamageElement.FIRE);
                    totalElementalDamage += finalDamage;
                    finalFireDamage = finalDamage;
                    if (target.getHealth() <= 0)
                        targetDiedFromElemental = true;
                }
            }

            if (!targetDiedFromElemental && coldDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                double finalDamage = coldDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    applyElementalDamage(target, world, finalDamage, DamageElement.COLD);
                    totalElementalDamage += finalDamage;
                    finalColdDamage = finalDamage;
                    if (target.getHealth() <= 0)
                        targetDiedFromElemental = true;
                }
            }

            if (!targetDiedFromElemental && lightningDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                double finalDamage = lightningDamage * (1.0 - resistance);

                // Trigger shock chain BEFORE applying damage (ensures it works for killing
                // hits)
                boolean shouldChain = hasShockBeforeDamage[0] && finalDamage > 0
                        && !AilmentManager.isShockChainInProgress();

                if (shouldChain) {
                    if (AilmentManager.acquireShockChainLock()) {
                        try {
                            AilmentManager.executeShockChain(target, (float) finalDamage, attacker);
                        } catch (Exception e) {
                            System.err.println("[CranksDungeons] Error in shock chain: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            AilmentManager.releaseShockChainLock();
                        }
                    } else {
                        System.err.println("[CranksDungeons] Failed to acquire shock chain lock (already in progress)");
                    }
                }

                if (finalDamage > 0) {
                    applyElementalDamage(target, world, finalDamage, DamageElement.LIGHTNING);
                    totalElementalDamage += finalDamage;
                    finalLightningDamage = finalDamage;
                    if (target.getHealth() <= 0)
                        targetDiedFromElemental = true;
                }
            }

            if (!targetDiedFromElemental && voidDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                double finalDamage = voidDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    applyElementalDamage(target, world, finalDamage, DamageElement.VOID);
                    totalElementalDamage += finalDamage;
                    finalVoidDamage = finalDamage;
                }
            }

            // === SEND DAMAGE NUMBERS ===
            // Broadcast to all nearby players so everyone can see the damage numbers
            if (attacker instanceof ServerPlayerEntity serverPlayer) {
                DamageNumberSender.broadcast(
                        world, target,
                        physicalDamage,
                        finalFireDamage,
                        finalColdDamage,
                        finalLightningDamage,
                        finalVoidDamage,
                        serverPlayer.getUuid(),
                        isCrit);
            }

            // Play per-element hit sounds
            if (finalFireDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_FIRE_HIT, SoundCategory.PLAYERS, 1.5f,
                        1.0f);
            }
            if (finalColdDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_COLD_HIT, SoundCategory.PLAYERS, 1.0f,
                        1.0f);
            }
            if (finalLightningDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_LIGHTNING_HIT, SoundCategory.PLAYERS, 0.8f,
                        1.0f);
                // Note: Shock chain is now triggered at the start of the method (line ~58)
                // to ensure it works even when the target dies from the hit
            }
            if (finalVoidDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_VOID_HIT, SoundCategory.PLAYERS, 0.6f,
                        1.0f);
            }

            // Life Steal
            double lifeSteal = CombatUtility.getAttributeValue(attacker, ModAttributes.LIFE_STEAL, weapon, "life_steal",
                    source) +
                    getEnchantmentValue(weapon, "minecraft:life_steal", 0.06) +
                    getEnchantmentValue(chestplate, "minecraft:life_steal", 0.06);

            if (lifeSteal > 0) {
                float totalDamage = physicalDamage + (float) totalElementalDamage;
                float healing = totalDamage * (float) lifeSteal;

                // Halve life steal effectiveness in PvP
                if (isPvP) {
                    healing *= 0.5f;
                }

                attacker.heal(healing);
            }

            // Burn
            double chanceToBurn = CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BURN, weapon,
                    "chance_to_burn", source) +
                    getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.05) +
                    getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.05);

            if (Math.random() < chanceToBurn) {
                float burnBaseDamage = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.BURN_BASE_DAMAGE,
                        weapon, "burn_base_damage", source);
                float burnDamagePercent = (float) CombatUtility.getAttributeValue(attacker,
                        ModAttributes.BURN_DAMAGE_PERCENT, weapon, "burn_damage_percent", source);
                double burnDuration = AilmentType.BURN.getBaseDuration()
                        * (1.0 + attacker.getAttributeValue(ModAttributes.BURN_DURATION));
                AilmentManager.applyAilment(target, AilmentType.BURN, burnBaseDamage * (1.0f + burnDamagePercent),
                        burnDuration, attacker);
            }

            // Poison
            double chanceToPoison = CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_POISON, weapon,
                    "chance_to_poison", source) +
                    getEnchantmentValue(weapon, "minecraft:chance_to_poison", 0.05) +
                    getEnchantmentValue(chestplate, "minecraft:chance_to_poison", 0.05);

            if (Math.random() < chanceToPoison) {
                float poisonBaseDamage = (float) CombatUtility.getAttributeValue(attacker,
                        ModAttributes.POISON_BASE_DAMAGE, weapon, "poison_base_damage", source);
                float poisonDamagePercent = (float) CombatUtility.getAttributeValue(attacker,
                        ModAttributes.POISON_DAMAGE_PERCENT, weapon, "poison_damage_percent", source);
                double poisonDuration = AilmentType.POISON.getBaseDuration()
                        * (1.0 + attacker.getAttributeValue(ModAttributes.POISON_DURATION));
                AilmentManager.applyAilment(target, AilmentType.POISON, poisonBaseDamage * (1.0f + poisonDamagePercent),
                        poisonDuration, attacker);
            }

            // Freeze
            double chanceToFreeze = CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_FREEZE, weapon,
                    "chance_to_freeze", source) +
                    getEnchantmentValue(weapon, "minecraft:chance_to_freeze", 0.05) +
                    getEnchantmentValue(chestplate, "minecraft:chance_to_freeze", 0.05);

            if (Math.random() < chanceToFreeze) {
                double freezeDuration = AilmentType.FREEZE.getBaseDuration()
                        * (1.0 + attacker.getAttributeValue(ModAttributes.FREEZE_DURATION));
                AilmentManager.applyAilment(target, AilmentType.FREEZE, 0.0f, freezeDuration, attacker);
            }

            // Bleed
            double chanceToBleed = CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BLEED, weapon,
                    "chance_to_bleed", source) +
                    getEnchantmentValue(weapon, "minecraft:chance_to_bleed", 0.05) +
                    getEnchantmentValue(chestplate, "minecraft:chance_to_bleed", 0.05);

            if (Math.random() < chanceToBleed) {
                double bleedDuration = AilmentType.BLEED.getBaseDuration()
                        * (1.0 + attacker.getAttributeValue(ModAttributes.BLEED_DURATION));
                AilmentManager.applyAilment(target, AilmentType.BLEED, 0.0f, bleedDuration, attacker);
            }

            // Shock
            double chanceToShock = CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_SHOCK, weapon,
                    "chance_to_shock", source) +
                    getEnchantmentValue(weapon, "minecraft:chance_to_shock", 0.05) +
                    getEnchantmentValue(chestplate, "minecraft:chance_to_shock", 0.05);

            if (Math.random() < chanceToShock) {
                double shockDuration = CombatUtility.getAttributeValue(attacker, ModAttributes.SHOCK_DURATION, weapon,
                        "shock_duration", source);
                AilmentManager.applyAilment(target, AilmentType.SHOCK, 0.0f, shockDuration, attacker);
            }

        } finally {
            cranks$processingElementalDamage = false;
        }
    }

    @Unique
    private void handleNonPlayerDamage(ServerWorld world, DamageSource source, float amount, LivingEntity target) {
        // Skip broadcasting void damage numbers if this is ailment damage (they send
        // their own correct numbers)
        if (AilmentManager.isPoisonDamageInProgress(target.getUuid())
                || AilmentManager.isBleedDamageInProgress(target.getUuid())) {
            return;
        }

        // For non-player damage, we retrieve the stored breakdown and broadcast to
        // nearby players
        if (target instanceof DamageBreakdownProvider provider) {
            DamageConversionHelper.DamageBreakdown breakdown = provider.cranks$getLastDamageBreakdown();
            if (breakdown == null) {
                breakdown = DamageConversionHelper.getBreakdown(source, amount, target);
            }

            // Check for shock BEFORE calculating damage
            boolean hasShockBeforeDamage = AilmentManager.hasAilment(target, AilmentType.SHOCK);

            // Apply resistances to get the actual final values
            float physical = breakdown.get(DamageElement.PHYSICAL);
            float fire = (float) (breakdown.get(DamageElement.FIRE)
                    * (1.0 - target.getAttributeValue(ModAttributes.FIRE_RESISTANCE)));
            float cold = (float) (breakdown.get(DamageElement.COLD)
                    * (1.0 - target.getAttributeValue(ModAttributes.COLD_RESISTANCE)));
            float lightning = (float) (breakdown.get(DamageElement.LIGHTNING)
                    * (1.0 - target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE)));
            float voidDamage = (float) (breakdown.get(DamageElement.VOID)
                    * (1.0 - target.getAttributeValue(ModAttributes.VOID_RESISTANCE)));

            // Trigger shock chain BEFORE damage is applied (works for killing hits)
            if (lightning > 0 && hasShockBeforeDamage && !AilmentManager.isShockChainInProgress()) {
                if (AilmentManager.acquireShockChainLock()) {
                    try {
                        AilmentManager.executeShockChain(target, lightning, source.getAttacker());
                    } finally {
                        AilmentManager.releaseShockChainLock();
                    }
                }
            }

            DamageNumberSender.broadcast(world, target, physical, fire, cold, lightning, voidDamage);
        }
    }

    @Unique
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) {
                return baseValue;
            }
        }
        return 0.0;
    }

    @Unique
    private void applyElementalDamage(LivingEntity target, ServerWorld world, double damage, DamageElement element) {
        float damageAmount = (float) damage;
        if (damageAmount <= 0)
            return;

        // Energy Shield Mitigation for players
        if (target instanceof PlayerEntity player
                && target instanceof com.cranks.dungeons.EnergyShieldProvider esProvider) {
            float currentShield = esProvider.cranksdungeons$getCurrentEnergyShield();

            // Check shield cooldown - allow the same-frame hit (currentTime ==
            // lastDamageTime)
            // but block if it's a different hit within the cooldown window.
            long currentTime = world.getTime();
            long lastDamageTime = esProvider.cranksdungeons$getLastDamageTime();
            boolean isSameHit = (currentTime == lastDamageTime);
            boolean onCooldown = (currentTime - lastDamageTime < 10L); // 10L = SHIELD_COOLDOWN_TICKS

            if (currentShield > 0 && (isSameHit || !onCooldown)) {
                // Weighted damage (Void deals 2x to shields)
                float weightedDamage = (element == DamageElement.VOID) ? damageAmount * 2.0f : damageAmount;

                if (currentShield >= weightedDamage) {
                    float newShield = currentShield - weightedDamage;
                    esProvider.cranksdungeons$setCurrentEnergyShield(newShield);

                    // Sync to client
                    if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(newShield));
                    }
                    return; // Fully absorbed
                } else {
                    float absorptionRatio = currentShield / weightedDamage;
                    float absorbedHealthDamage = damageAmount * absorptionRatio;
                    damageAmount -= absorbedHealthDamage;

                    esProvider.cranksdungeons$setCurrentEnergyShield(0.0f);
                    if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(0.0f));
                    }
                    // Remaining damage falls through to health application below
                }
            }
        }

        // Apply health reduction
        if (target.getHealth() - damageAmount <= 0) {
            DamageSource damageSource = getElementalDamageSource(world, element);
            target.damage(world, damageSource, damageAmount);
        } else {
            target.setHealth(target.getHealth() - damageAmount);
        }
    }

    @Unique
    private DamageSource getElementalDamageSource(ServerWorld world, DamageElement element) {
        return switch (element) {
            case PHYSICAL -> world.getDamageSources().generic();
            case FIRE -> world.getDamageSources().onFire();
            case COLD -> world.getDamageSources().freeze();
            case LIGHTNING -> world.getDamageSources().lightningBolt();
            case VOID -> world.getDamageSources().outOfWorld();
            default -> world.getDamageSources().generic();
        };
    }
}