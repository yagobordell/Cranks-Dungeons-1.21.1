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
    public abstract boolean damage(DamageSource source, float amount);

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

    @Unique
    private static final ThreadLocal<Boolean> cranks$hadShock = ThreadLocal.withInitial(() -> false);

    // Static flag to prevent shock chain damage from triggering elemental
    // processing

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float cranks$overrideDamage(float amount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;
        cranks$hadShock.set(AilmentManager.hasAilment(target, com.cranks.dungeons.ailment.AilmentType.SHOCK));
        cranks$originalDamageAmount.set(amount);
        return amount;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void applyElementalDamageOnHit(DamageSource source, float argAmount,
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
            if (!target.getWorld().isClient() && target.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                handleLivingAttacker(serverWorld, source, actualAmount, attacker, target);
            }
        } else {
            if (!target.getWorld().isClient() && target.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                handleNonPlayerDamage(serverWorld, source, actualAmount, target);
            }
        }
    }

    @Unique
    private void handleLivingAttacker(ServerWorld world, DamageSource source, float amount, LivingEntity attacker,
            LivingEntity target) {
        ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);

        boolean isPvP = attacker instanceof net.minecraft.entity.player.PlayerEntity && target instanceof net.minecraft.entity.player.PlayerEntity;
        boolean isCrit = com.cranks.dungeons.util.CriticalHitState.isCriticalHit();
        float critMultiplier = isCrit ? (float) attacker.getAttributeValue(ModAttributes.CRIT_MULTIPLIER) : 1.0f;
        if (isCrit && isPvP) {
            critMultiplier = 1.0f + (critMultiplier - 1.0f) * 0.5f;
        }

        try {
            cranks$processingElementalDamage.set(true);
            final boolean[] hasShockBeforeDamage = { cranks$hadShock.get() };

            DamageConversionHelper.DamageBreakdown breakdown = null;
            if (target instanceof com.cranks.dungeons.util.DamageBreakdownProvider provider) {
                breakdown = provider.cranks$getLastDamageBreakdown();
            }
            if (breakdown == null) {
                breakdown = DamageConversionHelper.getBreakdown(source, amount, target);
            }

            float physicalDamage = breakdown.get(DamageElement.PHYSICAL);
            float finalFireDamage = breakdown.get(DamageElement.FIRE);
            float finalColdDamage = breakdown.get(DamageElement.COLD);
            float finalLightningDamage = breakdown.get(DamageElement.LIGHTNING);
            float finalVoidDamage = breakdown.get(DamageElement.VOID);

            double physicalLeech = CombatUtility.getAttributeValue(attacker, ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE, weapon, "physical_damage_leeched_as_life", source);
            double totalLeech = 0;
            if (physicalDamage > 0) totalLeech += physicalDamage * physicalLeech;
            
            double lifeLeechMult = 1.0 + CombatUtility.getAttributeValue(attacker, ModAttributes.INCREASED_LIFE_LEECH_PERCENT, weapon, "increased_life_leech_percent", source);
            totalLeech *= lifeLeechMult;

            if (totalLeech > 0) {
                float maxLeechInstance = attacker.getMaxHealth() * 0.10f;
                totalLeech = Math.min(totalLeech, maxLeechInstance);
                com.cranks.dungeons.util.LeechManager.addLeech(attacker, (float) totalLeech);
            }

            double manaLeechPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_MANA, weapon, "physical_damage_leeched_as_mana", source);
            double totalManaLeech = 0;
            if (physicalDamage > 0) totalManaLeech += physicalDamage * manaLeechPercent;

            double manaLeechMult = 1.0 + CombatUtility.getAttributeValue(attacker, ModAttributes.INCREASED_MANA_LEECH_PERCENT, weapon, "increased_mana_leech_percent", source);
            totalManaLeech *= manaLeechMult;
            
            double recoveryRate = attacker.getAttributeValue(ModAttributes.MANA_RECOVERY_RATE);
            totalManaLeech *= (1.0 + recoveryRate);

            if (totalManaLeech > 0 && attacker instanceof com.cranks.dungeons.ManaProvider manaProvider) {
                float currentMana = manaProvider.cranksdungeons$getCurrentMana();
                float maxMana = (float) attacker.getAttributeValue(ModAttributes.MAX_MANA);
                if (currentMana < maxMana) {
                    float maxLeechInstance = maxMana * 0.10f;
                    float appliedLeech = Math.min((float) totalManaLeech, maxLeechInstance);
                    float newMana = Math.min(maxMana, currentMana + appliedLeech);
                    manaProvider.cranksdungeons$setCurrentMana(newMana);
                    
                    if (attacker instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer, new com.cranks.dungeons.network.ManaSyncPacket(newMana, maxMana));
                    }
                }
            }

            if (finalFireDamage > 0) {
                float increasedFlammability = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BURN, weapon, "chance_to_burn", source);
                float flammabilityMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FLAMMABILITY_MAGNITUDE_PERCENT, weapon, "flammability_magnitude_percent", source);
                float flammabilityAdded = (float) (finalFireDamage / critMultiplier / Math.max(1.0f, target.getMaxHealth())) * 0.20f;
                if (isCrit) flammabilityAdded *= 1.5f;
                float finalFlammability = flammabilityAdded * (1.0f + increasedFlammability + flammabilityMagnitudePercent);

                if (finalFlammability > 0.01f) {
                    float burnMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.BURN_MAGNITUDE_PERCENT, weapon, "burn_magnitude_percent", source);
                    float burnBaseDamage = (float) (finalFireDamage / critMultiplier) * 0.5f;
                    if (isCrit) burnBaseDamage *= 1.5f;
                    float finalBurnDamage = burnBaseDamage * (1.0f + burnMagnitudePercent);
                    
                    if (finalBurnDamage >= 1.0f) {
                        double burnDuration = com.cranks.dungeons.ailment.AilmentType.BURN.getBaseDuration()
                                * (1.0 + burnMagnitudePercent);
                        AilmentManager.addFlammability(target, finalFlammability, burnDuration, attacker, finalBurnDamage);
                    }
                }
            }

            if (physicalDamage > 0 || finalVoidDamage > 0) {
                float chanceToPoison = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_POISON, weapon, "chance_to_poison", source);
                if (chanceToPoison > 0.0f && (isCrit || chanceToPoison >= 1.0f || attacker.getRandom().nextFloat() < chanceToPoison)) {
                    float poisonMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.POISON_MAGNITUDE_PERCENT, weapon, "poison_magnitude_percent", source);
                    float poisonBaseDamage = (float) ((physicalDamage + finalVoidDamage) / critMultiplier) * 0.2f;
                    if (isCrit) poisonBaseDamage *= 1.5f;
                    float finalPoisonDamage = poisonBaseDamage * (1.0f + poisonMagnitudePercent);
                    
                    if (finalPoisonDamage >= 0.5f) {
                        double poisonDuration = com.cranks.dungeons.ailment.AilmentType.POISON.getBaseDuration()
                                * (1.0 + poisonMagnitudePercent);
                        AilmentManager.applyAilment(target, com.cranks.dungeons.ailment.AilmentType.POISON, finalPoisonDamage, poisonDuration, attacker, null, null, 0, finalPoisonDamage);
                    }
                }
            }

            if (finalColdDamage > 0) {
                float freezeBuildupMultiplier = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FREEZE_BUILDUP_PERCENT, weapon, "freeze_buildup_percent", source);
                float freezeAilmentThreshold = Math.max(1.0f, target.getMaxHealth());
                
                float freezeBuildupAmount = ((float) (finalColdDamage / critMultiplier) * 2.0f / freezeAilmentThreshold) * freezeBuildupMultiplier;
                if (isCrit) freezeBuildupAmount *= 1.5f;
                
                if (freezeBuildupAmount > 0.01f) {
                    double freezeDuration = com.cranks.dungeons.ailment.AilmentType.FREEZE.getBaseDuration();
                    AilmentManager.addFreezeBuildup(target, freezeBuildupAmount, freezeDuration, attacker);
                }
            }

            if (physicalDamage > 0) {
                float chanceToBleed = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BLEED, weapon, "chance_to_bleed", source);
                if (chanceToBleed > 0.0f && (isCrit || chanceToBleed >= 1.0f || attacker.getRandom().nextFloat() < chanceToBleed)) {
                    float bleedMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.BLEED_MAGNITUDE_PERCENT, weapon, "bleed_magnitude_percent", source);
                    float bleedBaseDamage = (float) (physicalDamage / critMultiplier);
                    if (isCrit) bleedBaseDamage *= 1.5f;
                    float finalBleedDamage = bleedBaseDamage * (1.0f + bleedMagnitudePercent);
                    
                    if (finalBleedDamage >= 1.0f) {
                        double bleedDuration = com.cranks.dungeons.ailment.AilmentType.BLEED.getBaseDuration()
                                * (1.0 + bleedMagnitudePercent);
                        AilmentManager.applyAilment(target, com.cranks.dungeons.ailment.AilmentType.BLEED, finalBleedDamage, bleedDuration, attacker, null, null, 0, finalBleedDamage);
                    }
                }
            }

            if (finalLightningDamage > 0) {
                float increasedChance = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_SHOCK, weapon, "chance_to_shock", source);
                float baseChance = (float) (finalLightningDamage / critMultiplier / Math.max(1.0f, target.getMaxHealth())) * 0.25f;
                if (isCrit) baseChance *= 1.5f;
                float finalChance = baseChance * (1.0f + increasedChance);

                if (finalChance >= 1.0f || attacker.getRandom().nextFloat() < finalChance) {
                    float ailmentThreshold = Math.max(1.0f, target.getMaxHealth());
                    
                    float baseMagnitude = ((float) (finalLightningDamage / critMultiplier) / ailmentThreshold) * 5.0f;
                    if (isCrit) baseMagnitude *= 1.5f;
                    
                    float shockMagnitudePercent = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.SHOCK_MAGNITUDE_PERCENT, weapon, "shock_magnitude_percent", source);
                    float finalMagnitude = baseMagnitude * shockMagnitudePercent;
                    finalMagnitude = Math.min(finalMagnitude, 0.50f);
                    
                    if (finalMagnitude > 0.05f) {
                        double shockDuration = com.cranks.dungeons.ailment.AilmentType.SHOCK.getBaseDuration()
                                * shockMagnitudePercent;
                        AilmentManager.applyAilment(target, com.cranks.dungeons.ailment.AilmentType.SHOCK, (float) finalLightningDamage, shockDuration, attacker, null, null, 0, finalMagnitude);
                    }
                }
                
                float electrocuteAilmentThreshold = Math.max(1.0f, target.getMaxHealth());
                
                float electrocuteBuildupMultiplier = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.ELECTROCUTE_BUILDUP_PERCENT, weapon, "electrocute_buildup_percent", source);
                float electrocuteBuildupAmount = ((float) (finalLightningDamage / critMultiplier) * 4.0f / electrocuteAilmentThreshold) * electrocuteBuildupMultiplier;
                if (isCrit) electrocuteBuildupAmount *= 1.5f;
                
                if (electrocuteBuildupAmount > 0) {
                    double electrocuteDuration = com.cranks.dungeons.ailment.AilmentType.ELECTROCUTE.getBaseDuration();
                    AilmentManager.addElectrocuteBuildup(target, electrocuteBuildupAmount, electrocuteDuration, attacker);
                }
            }

            if (finalLightningDamage > 0 && hasShockBeforeDamage[0] && !AilmentManager.isShockChainInProgress()) {
                if (AilmentManager.acquireShockChainLock()) {
                    try {
                        AilmentManager.executeShockChain(target, finalLightningDamage, attacker);
                    } catch (Exception e) {
                        System.err.println("[CranksDungeons] Error in shock chain: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        AilmentManager.releaseShockChainLock();
                    }
                }
            }

            if (!AilmentManager.isPoisonDamageInProgress(target.getUuid()) && !AilmentManager.isBleedDamageInProgress(target.getUuid())) {
                com.cranks.dungeons.network.DamageNumberSender.broadcast(world, target, physicalDamage, finalFireDamage, finalColdDamage, finalLightningDamage, finalVoidDamage);
            }

        } finally {
            cranks$processingElementalDamage.set(false);
        }
    }

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
            boolean hasShockBeforeDamage = cranks$hadShock.get();

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



}
