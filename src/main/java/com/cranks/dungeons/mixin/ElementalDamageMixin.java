package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.UUID;
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
    private static final ThreadLocal<Boolean> cranks$processingElementalDamage = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<Float> cranks$originalDamageAmount = ThreadLocal.withInitial(() -> 0f);

    // Static flag to prevent shock chain damage from triggering elemental
    // processing

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float cranks$overrideDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;
        Entity attackerEntity = source.getAttacker();

        if (attackerEntity instanceof LivingEntity attacker) {
            ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);

            // Check for Ranged Damage Override
            if (CombatUtility.isRanged(weapon, source)) {
                double baseDamage = attacker.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE);
                baseDamage += com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "minecraft:power", 3.0);

                double totalPercent = 1.0 + attacker.getAttributeValue(ModAttributes.ATTACK_DAMAGE_PERCENT)
                        + attacker.getAttributeValue(ModAttributes.RANGED_DAMAGE_PERCENT);

                double drawMultiplier = 1.0;
                if (weapon.getItem() instanceof net.minecraft.item.BowItem) {
                    drawMultiplier = Math.min(1.0, amount / 6.0);
                }

                amount = (float) (baseDamage * totalPercent * drawMultiplier);
            }

            // Normal PvP reduction for melee/other and ranged
            if (attacker instanceof PlayerEntity && target instanceof PlayerEntity) {
                amount *= 0.25f;
            }
            
            // Store the full amount BEFORE conversion so applyElementalDamageOnHit uses it as the base!
            cranks$originalDamageAmount.set(amount);
            
            // PURE DAMAGE CONVERSION REDUCTION
            double toFire = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_FIRE_CONVERSION_PERCENT);
            double toCold = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_COLD_CONVERSION_PERCENT);
            double toLightning = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_LIGHTNING_CONVERSION_PERCENT);
            double toVoid = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT);
            
            double totalConversion = toFire + toCold + toLightning + toVoid;
            if (totalConversion > 1.0) totalConversion = 1.0;
            
            if (totalConversion > 0) {
                amount = (float) (amount * (1.0 - totalConversion));
                if (amount <= 0.0f && totalConversion >= 1.0) {
                    amount = 0.001f; // Keep a tiny amount so the vanilla hit validates
                }
            }
            
            return amount;
        }

        cranks$originalDamageAmount.set(amount);
        return amount;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void applyElementalDamageOnHit(ServerWorld world, DamageSource source, float argAmount,
            CallbackInfoReturnable<Boolean> cir) {
        // Skip if damage didn't succeed or already processing
        if (!cir.getReturnValue()) {
            return;
        }

        LivingEntity target = (LivingEntity) (Object) this;

        if (cranks$processingElementalDamage.get()) {
            return;
        }

        // Skip if this is shock chain damage (prevent double processing)
        if (AilmentManager.isShockChainInProgress()) {
            return;
        }
        
        float actualAmount = cranks$originalDamageAmount.get();
        if (actualAmount <= 0) actualAmount = argAmount;

        // Accumulate bleed damage if target has bleed ailment
        AilmentManager.accumulateBleedDamage(target, actualAmount);

        if (source.getAttacker() instanceof LivingEntity attacker) {
            handleLivingAttacker(world, source, actualAmount, attacker, target);
        } else {
            handleNonPlayerDamage(world, source, actualAmount, target);
        }
    }

    @Unique
    private void handleLivingAttacker(ServerWorld world, DamageSource source, float amount, LivingEntity attacker,
            LivingEntity target) {
        ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);
        ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

        // Check if this hit was a critical strike
        boolean isPvP = attacker instanceof PlayerEntity && target instanceof PlayerEntity;
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
            cranks$processingElementalDamage.set(true);

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

            double selfAfflictedBonus = 0;
            if (AilmentManager.isAfflicted(attacker)) {
                selfAfflictedBonus = attacker.getAttributeValue(ModAttributes.DAMAGE_WHILE_AFFLICTED_PERCENT);
            }

            double elementalBonus = attacker.getAttributeValue(ModAttributes.ELEMENTAL_DAMAGE_PERCENT);
            if (attacker.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.ELEMENTAL_OVERLOAD)) {
                elementalBonus += 0.40;
            }

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

            // === GATHER BASE DAMAGES ===
            double fireBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.FIRE_DAMAGE, weapon,
                    "local_fire_damage", source) + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "minecraft:fire_damage", 4.0);

            double coldBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.COLD_DAMAGE, weapon,
                    "local_cold_damage", source) + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "cranks-dungeons:cold_damage", 4.0);

            double lightningBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.LIGHTNING_DAMAGE,
                    weapon, "local_lightning_damage", source) + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "cranks-dungeons:lightning_damage", 4.0);

            double voidBaseDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.VOID_DAMAGE, weapon,
                    "local_void_damage", source) + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "cranks-dungeons:void_damage", 4.0);

            // === PURE DAMAGE CONVERSIONS ===
            double originalUnreducedPhysical = unreducedPhysicalDamage;
            
            double toFire = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_FIRE_CONVERSION_PERCENT);
            double toCold = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_COLD_CONVERSION_PERCENT);
            double toLightning = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_LIGHTNING_CONVERSION_PERCENT);
            double toVoid = attacker.getAttributeValue(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT);
            
            double totalConversion = toFire + toCold + toLightning + toVoid;
            if (totalConversion > 1.0) {
                toFire /= totalConversion;
                toCold /= totalConversion;
                toLightning /= totalConversion;
                toVoid /= totalConversion;
            }

            double convertedFire = originalUnreducedPhysical * toFire;
            double convertedCold = originalUnreducedPhysical * toCold;
            double convertedLightning = originalUnreducedPhysical * toLightning;
            double convertedVoid = originalUnreducedPhysical * toVoid;
            
            double totalConverted = convertedFire + convertedCold + convertedLightning + convertedVoid;
            unreducedPhysicalDamage -= totalConverted;
            physicalDamage -= totalConverted * pvpMultiplier; // Also reduce physicalDamage to affect poison/bleed base

            fireBaseDamage += convertedFire;
            coldBaseDamage += convertedCold;
            lightningBaseDamage += convertedLightning;
            voidBaseDamage += convertedVoid;

            // === PROPORTIONAL EXTRA DAMAGE (RUNES) ===
            // Calculate total basic damage using UNREDUCED values for proportional scaling
            double totalBasicDamage = unreducedPhysicalDamage + fireBaseDamage + coldBaseDamage + lightningBaseDamage + voidBaseDamage;

            double extraPhysicalPercent = CombatUtility.getAttributeValue(attacker,
                    ModAttributes.PHYSICAL_EXTRA_PERCENT, weapon, "attack_extra_damage", source);
            double finalPhysicalExtraDamage = totalBasicDamage * extraPhysicalPercent
                    + unreducedPhysicalDamage * selfAfflictedBonus;

            double extraFirePercent = CombatUtility.getAttributeValue(attacker, ModAttributes.FIRE_EXTRA_PERCENT,
                    weapon, "fire_extra_damage", source);
            fireBaseDamage += totalBasicDamage * extraFirePercent;

            double extraColdPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.COLD_EXTRA_PERCENT,
                    weapon, "cold_extra_damage", source);
            coldBaseDamage += totalBasicDamage * extraColdPercent;

            double extraLightningPercent = CombatUtility.getAttributeValue(attacker,
                    ModAttributes.LIGHTNING_EXTRA_PERCENT, weapon, "lightning_extra_damage", source);
            lightningBaseDamage += totalBasicDamage * extraLightningPercent;

            double extraVoidPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.VOID_EXTRA_PERCENT,
                    weapon, "void_extra_damage", source);
            voidBaseDamage += totalBasicDamage * extraVoidPercent;

            // Gain % of Physical as Extra Element
            double physAsFire = attacker.getAttributeValue(ModAttributes.PHYSICAL_AS_EXTRA_FIRE_PERCENT);
            fireBaseDamage += originalUnreducedPhysical * physAsFire;

            double physAsCold = attacker.getAttributeValue(ModAttributes.PHYSICAL_AS_EXTRA_COLD_PERCENT);
            coldBaseDamage += originalUnreducedPhysical * physAsCold;

            double physAsLightning = attacker.getAttributeValue(ModAttributes.PHYSICAL_AS_EXTRA_LIGHTNING_PERCENT);
            lightningBaseDamage += originalUnreducedPhysical * physAsLightning;

            double physAsVoid = attacker.getAttributeValue(ModAttributes.PHYSICAL_AS_EXTRA_VOID_PERCENT);
            voidBaseDamage += originalUnreducedPhysical * physAsVoid;

            // === APPLY MULTIPLIERS ===
            double baseMultiplier = weaponSpecificPercent + afflictedBonus + selfAfflictedBonus
                    + elementalBonus + typeBonus + wieldingBonus;

            double fireTotalPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.FIRE_DAMAGE_PERCENT,
                    weapon, "fire_damage_percent", source);
            double fireDamage = fireBaseDamage * (1.0 + fireTotalPercent + baseMultiplier) * critMultiplier;

            double coldTotalPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.COLD_DAMAGE_PERCENT,
                    weapon, "cold_damage_percent", source);
            double coldDamage = coldBaseDamage * (1.0 + coldTotalPercent + baseMultiplier) * critMultiplier;

            double lightningTotalPercent = CombatUtility.getAttributeValue(attacker,
                    ModAttributes.LIGHTNING_DAMAGE_PERCENT, weapon, "lightning_damage_percent", source);
            double lightningDamage = lightningBaseDamage * (1.0 + lightningTotalPercent + baseMultiplier) * critMultiplier;

            double voidTotalPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.VOID_DAMAGE_PERCENT,
                    weapon, "void_damage_percent", source);
            double voidDamage = voidBaseDamage * (1.0 + voidTotalPercent + baseMultiplier) * critMultiplier;

            // Apply global PvP reduction to final bonus values
            fireDamage *= pvpMultiplier;
            coldDamage *= pvpMultiplier;
            lightningDamage *= pvpMultiplier;
            voidDamage *= pvpMultiplier;
            finalPhysicalExtraDamage *= pvpMultiplier;

            if (attacker.getAttributeValue(ModAttributes.KEYSTONE_AVATAR_OF_FIRE) > 0) {
                // Convert 50% of phys, cold, lightning to fire
                fireDamage += (physicalDamage + finalPhysicalExtraDamage) * 0.5;
                fireDamage += coldDamage * 0.5;
                fireDamage += lightningDamage * 0.5;
                
                // Zero out all non-fire damage
                physicalDamage = 0;
                finalPhysicalExtraDamage = 0;
                coldDamage = 0;
                lightningDamage = 0;
                voidDamage = 0;
            }

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
                        (float) finalPhysicalExtraDamage, attacker);
                applyElementalDamage(target, world, reducedExtraPhysical, DamageElement.PHYSICAL);
                totalElementalDamage += reducedExtraPhysical;
                physicalDamage += reducedExtraPhysical;
                if (target.getHealth() <= 0)
                    targetDiedFromElemental = true;
            }

            double armorAppliesPercent = target.getAttributes().hasAttribute(ModAttributes.ARMOR_APPLIES_TO_ELEMENTAL) ? target.getAttributeValue(ModAttributes.ARMOR_APPLIES_TO_ELEMENTAL) : 0.0;

            if (fireDamage > 0) {
                if (armorAppliesPercent > 0) {
                    fireDamage = DamageConversionHelper.calculateArmorReduction(target, (float)fireDamage, 0.0, armorAppliesPercent);
                }
                double resistance = target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                double pen = attacker.getAttributeValue(ModAttributes.ELEMENTAL_PENETRATION_PERCENT)
                        + attacker.getAttributeValue(ModAttributes.FIRE_PENETRATION_PERCENT);
                double finalRes = Math.min(0.75, Math.max(0, resistance * (1.0 - pen)));
                double finalDamage = fireDamage * (1.0 - finalRes);


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
                if (armorAppliesPercent > 0) {
                    coldDamage = DamageConversionHelper.calculateArmorReduction(target, (float)coldDamage, 0.0, armorAppliesPercent);
                }
                double resistance = target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                double pen = attacker.getAttributeValue(ModAttributes.ELEMENTAL_PENETRATION_PERCENT)
                        + attacker.getAttributeValue(ModAttributes.COLD_PENETRATION_PERCENT);
                double finalRes = Math.min(0.75, Math.max(0, resistance * (1.0 - pen)));
                double finalDamage = coldDamage * (1.0 - finalRes);


                if (finalDamage > 0) {
                    applyElementalDamage(target, world, finalDamage, DamageElement.COLD);
                    totalElementalDamage += finalDamage;
                    finalColdDamage = finalDamage;
                    if (target.getHealth() <= 0)
                        targetDiedFromElemental = true;
                }
            }

            if (!targetDiedFromElemental && lightningDamage > 0) {
                if (armorAppliesPercent > 0) {
                    lightningDamage = DamageConversionHelper.calculateArmorReduction(target, (float)lightningDamage, 0.0, armorAppliesPercent);
                }
                double resistance = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                double pen = attacker.getAttributeValue(ModAttributes.ELEMENTAL_PENETRATION_PERCENT)
                        + attacker.getAttributeValue(ModAttributes.LIGHTNING_PENETRATION_PERCENT);
                double finalRes = Math.min(0.75, Math.max(0, resistance * (1.0 - pen)));
                double finalDamage = lightningDamage * (1.0 - finalRes);

                double baseDamageForChain = finalDamage;

                // Trigger shock chain BEFORE applying damage (ensures it works for killing
                // hits)
                boolean shouldChain = hasShockBeforeDamage[0] && baseDamageForChain > 0
                        && !AilmentManager.isShockChainInProgress();

                if (shouldChain) {
                    if (AilmentManager.acquireShockChainLock()) {
                        try {
                            AilmentManager.executeShockChain(target, (float) baseDamageForChain, attacker);
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
                if (armorAppliesPercent > 0) {
                    voidDamage = DamageConversionHelper.calculateArmorReduction(target, (float)voidDamage, 0.0, armorAppliesPercent);
                }
                double resistance = target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                double pen = attacker.getAttributeValue(ModAttributes.ELEMENTAL_PENETRATION_PERCENT)
                        + attacker.getAttributeValue(ModAttributes.VOID_PENETRATION_PERCENT);
                double finalRes = Math.min(0.75, Math.max(0, resistance * (1.0 - pen)));
                double finalDamage = voidDamage * (1.0 - finalRes);


                if (finalDamage > 0) {
                    applyElementalDamage(target, world, finalDamage, DamageElement.VOID);
                    totalElementalDamage += finalDamage;
                    finalVoidDamage = finalDamage;
                }
            }

            // === SEND DAMAGE NUMBERS ===
            // Broadcast if attacker is player OR target is player
            if (attacker instanceof ServerPlayerEntity || target instanceof PlayerEntity) {
                UUID attackerId = (attacker != null) ? attacker.getUuid() : null;
                DamageNumberSender.broadcast(
                        world, target,
                        physicalDamage,
                        finalFireDamage,
                        finalColdDamage,
                        finalLightningDamage,
                        finalVoidDamage,
                        attackerId,
                        isCrit);
            }

            // Play per-element hit sounds
            SoundCategory soundCategory = attacker instanceof PlayerEntity ? SoundCategory.PLAYERS
                    : SoundCategory.HOSTILE;
            if (finalFireDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_FIRE_HIT, soundCategory, 1.5f,
                        1.0f);
            }
            if (finalColdDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_COLD_HIT, soundCategory, 1.0f,
                        1.0f);
            }
            if (finalLightningDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_LIGHTNING_HIT, soundCategory, 0.8f,
                        1.0f);
                // Note: Shock chain is now triggered at the start of the method (line ~58)
                // to ensure it works even when the target dies from the hit
            }
            if (finalVoidDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_VOID_HIT, soundCategory, 0.6f,
                        1.0f);
            }

            // Physical Damage Leeched as Life
            double physicalLeech = CombatUtility.getAttributeValue(attacker, ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE, weapon, "physical_damage_leeched_as_life", source) +
                    com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "minecraft:physical_damage_leeched_as_life", 0.06) +
                    com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, chestplate, "minecraft:physical_damage_leeched_as_life", 0.06);

            if (physicalLeech > 0 && physicalDamage > 0) {
                float healing = physicalDamage * (float) physicalLeech;

                // Halve life steal effectiveness in PvP
                if (isPvP) {
                    healing *= 0.5f;
                }

                com.cranks.dungeons.util.LeechManager.addLeech(attacker, healing);
            }

            // Burn (Scales from Fire Damage)
            if (finalFireDamage > 0) {
                float chanceToBurn = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BURN, weapon, "chance_to_burn", source);
                if (chanceToBurn >= 1.0f || (chanceToBurn > 0.0f && attacker.getRandom().nextFloat() < chanceToBurn)) {
                    float burnMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker,
                            ModAttributes.BURN_DAMAGE_PERCENT, weapon, "burn_damage_percent", source);
                    // Burn deals 50% of fire damage over its duration as base, ignoring crit multiplier
                    float burnBaseDamage = (float) (finalFireDamage / critMultiplier) * 0.5f;
                    if (isCrit) burnBaseDamage *= 1.5f;
                    float finalBurnDamage = burnBaseDamage * (1.0f + burnMagnitudePercent);
                    
                    if (finalBurnDamage >= 1.0f) {
                        double burnDuration = AilmentType.BURN.getBaseDuration()
                                * (1.0 + attacker.getAttributeValue(ModAttributes.BURN_DURATION));
                        AilmentManager.applyAilment(target, AilmentType.BURN, finalBurnDamage, burnDuration, attacker, null, null, 0, finalBurnDamage);
                    }
                }
            }

            // Poison (Scales from Physical and Void Damage)
            if (physicalDamage > 0 || finalVoidDamage > 0) {
                float chanceToPoison = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_POISON, weapon, "chance_to_poison", source);
                if (chanceToPoison >= 1.0f || (chanceToPoison > 0.0f && attacker.getRandom().nextFloat() < chanceToPoison)) {
                    float poisonMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker,
                            ModAttributes.POISON_DAMAGE_PERCENT, weapon, "poison_damage_percent", source);
                    // Poison deals 20% of physical+void damage per second, ignoring crit multiplier
                    float poisonBaseDamage = (float) ((physicalDamage + finalVoidDamage) / critMultiplier) * 0.2f;
                    if (isCrit) poisonBaseDamage *= 1.5f;
                    float finalPoisonDamage = poisonBaseDamage * (1.0f + poisonMagnitudePercent);
                    
                    if (finalPoisonDamage >= 0.5f) {
                        double poisonDuration = AilmentType.POISON.getBaseDuration()
                                * (1.0 + attacker.getAttributeValue(ModAttributes.POISON_DURATION));
                        AilmentManager.applyAilment(target, AilmentType.POISON, finalPoisonDamage, poisonDuration, attacker, null, null, 0, finalPoisonDamage);
                    }
                }
            }

            // Freeze Buildup (Scales from Cold Damage vs Max Health, slower than electrocute)
            if (finalColdDamage > 0) {
                float chanceToFreeze = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_FREEZE, weapon, "chance_to_freeze", source);
                if (chanceToFreeze >= 1.0f || (chanceToFreeze > 0.0f && attacker.getRandom().nextFloat() < chanceToFreeze)) {
                    float freezeMagnitudePercent = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FREEZE_MAGNITUDE_PERCENT, weapon,
                            "freeze_magnitude_percent", source) +
                            (float) com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "minecraft:freeze_magnitude_percent", 0.05) +
                            (float) com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, chestplate, "minecraft:freeze_magnitude_percent", 0.05);
                    
                    float thresholdReduction = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FREEZE_THRESHOLD_REDUCTION, weapon, "freeze_threshold_reduction", source);
                    float ailmentThreshold = target.getMaxHealth() * (1.0f - thresholdReduction);
                    ailmentThreshold = Math.max(1.0f, ailmentThreshold);
                    
                    // Freeze is harder to charge (x2 multiplier) because it lasts longer. Ignores crit multiplier for buildup.
                    float buildupAmount = ((float) (finalColdDamage / critMultiplier) * 2.0f / ailmentThreshold) * freezeMagnitudePercent;
                    if (isCrit) buildupAmount *= 1.5f;
                    
                    if (buildupAmount > 0.05f) {
                        double freezeDuration = AilmentType.FREEZE.getBaseDuration()
                                * (1.0 + attacker.getAttributeValue(ModAttributes.FREEZE_DURATION));
                        AilmentManager.addFreezeBuildup(target, buildupAmount, freezeDuration, attacker);
                    }
                }
            }

            // Bleed (Scales from Physical Damage)
            if (physicalDamage > 0) {
                float chanceToBleed = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BLEED, weapon, "chance_to_bleed", source);
                if (chanceToBleed >= 1.0f || (chanceToBleed > 0.0f && attacker.getRandom().nextFloat() < chanceToBleed)) {
                    float bleedMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.BLEED_DAMAGE_PERCENT, weapon,
                            "bleed_damage_percent", source) +
                            (float) com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "minecraft:BLEED_DAMAGE_PERCENT", 0.10);
                    
                    // Bleed deals 100% of physical damage as burst, ignoring crit multiplier
                    float bleedBaseDamage = (float) (physicalDamage / critMultiplier);
                    if (isCrit) bleedBaseDamage *= 1.5f;
                    float finalBleedDamage = bleedBaseDamage * (1.0f + bleedMagnitudePercent);
                    
                    if (finalBleedDamage >= 1.0f) {
                        double bleedDuration = AilmentType.BLEED.getBaseDuration()
                                * (1.0 + attacker.getAttributeValue(ModAttributes.BLEED_DURATION));
                        AilmentManager.applyAilment(target, AilmentType.BLEED, finalBleedDamage, bleedDuration, attacker, null, null, 0, finalBleedDamage);
                    }
                }
            }

            // Shock (Enables Chain Lightning & Increases Damage Taken based on Magnitude)
            if (finalLightningDamage > 0) {
                float chanceToShock = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_SHOCK, weapon, "chance_to_shock", source);
                if (chanceToShock >= 1.0f || (chanceToShock > 0.0f && attacker.getRandom().nextFloat() < chanceToShock)) {
                    float thresholdReduction = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.SHOCK_THRESHOLD_REDUCTION, weapon, "shock_threshold_reduction", source);
                    float ailmentThreshold = target.getMaxHealth() * (1.0f - thresholdReduction);
                    ailmentThreshold = Math.max(1.0f, ailmentThreshold);
                    
                    float baseMagnitude = ((float) (finalLightningDamage / critMultiplier) / ailmentThreshold) * 5.0f;
                    if (isCrit) baseMagnitude *= 1.5f;
                    
                    float shockMagnitudePercent = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.SHOCK_MAGNITUDE_PERCENT, weapon,
                            "shock_magnitude_percent", source) +
                            (float) com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(attacker, weapon, "minecraft:shock_magnitude_percent", 0.05);
                            
                    float finalMagnitude = baseMagnitude * shockMagnitudePercent;
                    finalMagnitude = Math.min(finalMagnitude, 0.50f); // Cap at 50%
                    
                    if (finalMagnitude > 0.05f) { // Require at least 5% increased damage taken to apply
                        double shockDuration = AilmentType.SHOCK.getBaseDuration()
                                * (1.0 + attacker.getAttributeValue(ModAttributes.SHOCK_DURATION));
                        AilmentManager.applyAilment(target, AilmentType.SHOCK, (float) finalLightningDamage, shockDuration, attacker, null, null, 0, finalMagnitude);
                    }
                }
                
                // Electrocute Buildup (Scales from Lightning Damage vs Max Health, faster than freeze)
                float chanceToShock2 = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_SHOCK, weapon, "chance_to_shock", source);
                if (chanceToShock2 >= 1.0f || (chanceToShock2 > 0.0f && attacker.getRandom().nextFloat() < chanceToShock2)) {
                    float thresholdReduction2 = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.SHOCK_THRESHOLD_REDUCTION, weapon, "shock_threshold_reduction", source);
                    float ailmentThreshold2 = target.getMaxHealth() * (1.0f - thresholdReduction2);
                    ailmentThreshold2 = Math.max(1.0f, ailmentThreshold2);
                    float electrocuteBuildupMultiplier = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.ELECTROCUTE_MAGNITUDE_PERCENT, weapon, "electrocute_magnitude_percent", source);
                    
                    // Electrocute is easier to charge (x8 multiplier to buildup) because it is a shorter stun
                    float buildupAmount = ((float) (finalLightningDamage / critMultiplier) * 8.0f / ailmentThreshold2) * electrocuteBuildupMultiplier;
                    if (isCrit) buildupAmount *= 1.5f;
                    
                    if (buildupAmount > 0) {
                        double electrocuteDuration = AilmentType.ELECTROCUTE.getBaseDuration()
                                * (1.0 + attacker.getAttributeValue(ModAttributes.ELECTROCUTE_DURATION));
                        AilmentManager.addElectrocuteBuildup(target, buildupAmount, electrocuteDuration, attacker);
                    }
                }
            }

        } finally {
            cranks$processingElementalDamage.set(false);
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

            // The breakdown was already reduced by resistances and armor in DamageReductionMixin
            float physical = breakdown.get(DamageElement.PHYSICAL);
            float fire = breakdown.get(DamageElement.FIRE);
            float cold = breakdown.get(DamageElement.COLD);
            float lightning = breakdown.get(DamageElement.LIGHTNING);
            float voidDamage = breakdown.get(DamageElement.VOID);

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
    private void applyElementalDamage(LivingEntity target, ServerWorld world, double damage, DamageElement element) {
        float damageAmount = (float) damage;
        if (damageAmount <= 0)
            return;

        // Energy Shield Mitigation for all living entities
        if (target instanceof com.cranks.dungeons.EnergyShieldProvider esProvider) {
            float currentShield = esProvider.cranksdungeons$getCurrentEnergyShield();

            // Check shield cooldown
            long currentTime = world.getTime();
            long lastDamageTime = esProvider.cranksdungeons$getLastDamageTime();
            boolean isSameHit = (currentTime == lastDamageTime);
            boolean onCooldown = (currentTime - lastDamageTime < 10L);

            if (currentShield > 0 && (isSameHit || !onCooldown)) {
                // Weighted damage (Void deals 2x to shields)
                float weightedDamage = (element == DamageElement.VOID) ? damageAmount * 2.0f : damageAmount;

                if (currentShield >= weightedDamage) {
                    float newShield = currentShield - weightedDamage;
                    esProvider.cranksdungeons$setCurrentEnergyShield(newShield);
                    esProvider.cranksdungeons$setLastDamageTime(currentTime);

                    // Sync to owner if player
                    if (target instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                        float maxShield = (float) target
                                .getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(newShield, maxShield));
                    }

                    // Manual knockback for absorbed hits
                    // Note: Elemental damage usually doesn't apply knockback unless specified,
                    // but we ensure it works if the hit was a player attack.

                    return; // Fully absorbed
                } else {
                    float absorptionRatio = currentShield / weightedDamage;
                    float absorbedHealthDamage = damageAmount * absorptionRatio;
                    damageAmount -= absorbedHealthDamage;

                    esProvider.cranksdungeons$setCurrentEnergyShield(0.0f);
                    esProvider.cranksdungeons$setLastDamageTime(currentTime);

                    if (target instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                        float maxShield = (float) target
                                .getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new com.cranks.dungeons.network.EnergyShieldSyncPacket(0.0f, maxShield));
                    }
                    // Remaining damage falls through to health application below
                }
            }
        }

        // Apply health reduction via helper (no broadcast here, as it's handled at the
        // end of handlePlayerAttacker)
        com.cranks.dungeons.util.ElementalDamageHelper.applyDamage(target, world, (float) damageAmount, element, null,
                false, false);
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
