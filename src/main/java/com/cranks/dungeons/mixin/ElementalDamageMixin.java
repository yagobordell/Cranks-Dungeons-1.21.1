package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.CombatUtility;
import com.cranks.dungeons.util.DamageHit;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.cranks.dungeons.util.DamageConversionHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


@Mixin(value = LivingEntity.class, priority = 1000)
public abstract class ElementalDamageMixin {

    @WrapMethod(method = "damage")
    private boolean cranks$resolveHit(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (!(target.getWorld() instanceof ServerWorld world)) return original.call(source, amount);
        if (amount <= 0 || target.isInvulnerableTo(source) || !target.isAlive()) return false;
        if (target instanceof PlayerEntity victim && source.getAttacker() instanceof PlayerEntity attacker && victim != attacker
                && com.cranks.dungeons.party.PartyManager.areInSameParty(victim.getUuid(), attacker.getUuid())) {
            return false;
        }

        try (DamageHit hit = DamageHit.begin(target)) {
            boolean accepted = original.call(source, amount);
            if (accepted && hit.critical() && hit.damageToLife() + hit.damageToShield() > 0
                    && !AilmentManager.isSecondaryDamage(target)
                    && source.getAttacker() instanceof LivingEntity attacker) {
                CombatUtility.recordRecentCrit(attacker, world.getTime());
                world.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT,target.getX(),target.getBodyY(0.5),target.getZ(),6,0.2,0.2,0.2,0.05);
                world.playSound(null,target.getBlockPos(),ModSounds.CRITICAL_HIT,
                        attacker instanceof PlayerEntity?SoundCategory.PLAYERS:SoundCategory.HOSTILE,0.7f,1.0f);
                if(attacker.getAttributeValue(ModAttributes.KEYSTONE_ELEMENTAL_OVERLOAD)>0)
                    CombatUtility.triggerElementalOverload(attacker);
            }
            if (source.getSource() instanceof com.cranks.dungeons.inventory.ArrowCoatingAccess arrow) {
                arrow.cranks$recordCoatingHit(target.getUuid(), accepted && !hit.customBlock()
                        && hit.damageToLife() + hit.damageToShield() > 0);
            }
            if (accepted && hit.breakdown() != null && !AilmentManager.isSecondaryDamage(target)
                    && !AilmentManager.isShockChainInProgress()
                    && !AilmentManager.isPoisonDamageInProgress(target.getUuid())
                    && !AilmentManager.isBleedDamageInProgress(target.getUuid())) {
                float lifeDamage = hit.damageToLife();
                float connected = lifeDamage + hit.damageToShield();
                var breakdown = hit.breakdown().scaledTo(connected);
                if (connected > 0) {
                    AilmentManager.accumulateBleedDamage(target, connected);
                    if (!com.cranks.dungeons.effect.PotionRules.isPotionDamage(source)
                            && source.getAttacker() instanceof LivingEntity attacker) {
                        handleLivingAttacker(world, source, breakdown, attacker, target, hit, lifeDamage / connected);
                    }
                }
                if (source.isOf(DamageTypes.DROWN) && lifeDamage > 0) {
                    DamageNumberSender.broadcastDrown(world, target, lifeDamage);
                } else if (lifeDamage + hit.damageToShield() > 0) {
                    var displayed = hit.breakdown().scaledTo(lifeDamage + hit.damageToShield());
                    DamageNumberSender.broadcast(world, target, displayed.get(DamageElement.PHYSICAL),
                            displayed.get(DamageElement.FIRE), displayed.get(DamageElement.COLD),
                            displayed.get(DamageElement.LIGHTNING), displayed.get(DamageElement.VOID),
                            source.getAttacker() == null ? null : source.getAttacker().getUuid(), hit.critical());
                }
            }
            return accepted;
        }
    }

    @Unique
    private void handleLivingAttacker(ServerWorld world, DamageSource source,
                                      DamageConversionHelper.DamageBreakdown breakdown,
                                      LivingEntity attacker, LivingEntity target, DamageHit hit, float lifeFraction) {
        ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);
        boolean isCrit = hit.critical();
        float physicalDamage = breakdown.get(DamageElement.PHYSICAL);
        float finalFireDamage = breakdown.get(DamageElement.FIRE);
        float finalColdDamage = breakdown.get(DamageElement.COLD);
        float finalLightningDamage = breakdown.get(DamageElement.LIGHTNING);
        float finalVoidDamage = breakdown.get(DamageElement.VOID);
        float connectedFraction = hit.breakdown().getTotal() > 0
                ? Math.min(1, breakdown.getTotal()/hit.breakdown().getTotal()) : 0;
        var offensive = hit.beforeMitigation() == null ? breakdown : hit.beforeMitigation().scaledTo(
                hit.beforeMitigation().getTotal()*connectedFraction);
        float rawFire=offensive.get(DamageElement.FIRE), rawLightning=offensive.get(DamageElement.LIGHTNING);
        float rawPoison=offensive.get(DamageElement.PHYSICAL)+offensive.get(DamageElement.VOID);

        // Life Leech
        double physicalLeech = CombatUtility.getAttributeValue(attacker, ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE, weapon, "physical_damage_leeched_as_life", source);
        double totalLeech = 0;
        if (physicalDamage > 0) totalLeech += physicalDamage * lifeFraction * physicalLeech;

        double lifeLeechMult = 1.0 + CombatUtility.getAttributeValue(attacker, ModAttributes.INCREASED_LIFE_LEECH_PERCENT, weapon, "increased_life_leech_percent", source);
        totalLeech *= lifeLeechMult;

        if (totalLeech > 0) {
            com.cranks.dungeons.util.LeechManager.addLeech(attacker, (float) totalLeech);
        }

        // Mana Leech
        double manaLeechPercent = CombatUtility.getAttributeValue(attacker, ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_MANA, weapon, "physical_damage_leeched_as_mana", source);
        double totalManaLeech = 0;
        if (physicalDamage > 0) totalManaLeech += physicalDamage * lifeFraction * manaLeechPercent;

        double manaLeechMult = 1.0 + CombatUtility.getAttributeValue(attacker, ModAttributes.INCREASED_MANA_LEECH_PERCENT, weapon, "increased_mana_leech_percent", source);
        totalManaLeech *= manaLeechMult;

        double recoveryRate = attacker.getAttributeValue(ModAttributes.MANA_RECOVERY_RATE);
        totalManaLeech *= (1.0 + recoveryRate);

        if (totalManaLeech > 0) com.cranks.dungeons.util.LeechManager.addManaLeech(attacker, (float)totalManaLeech);

        float globalAilmentChance = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.INCREASED_AILMENT_CHANCE_PERCENT, weapon, "increased_ailment_chance_percent", source);
        float critNonDamagingMagnitude = isCrit ? (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CRIT_NON_DAMAGING_AILMENT_MAGNITUDE_PERCENT, weapon, "crit_non_damaging_ailment_magnitude_percent", source) : 0.0f;

        // Fire / Burn (Ignite)
        if (finalFireDamage > 0) {
            float flammabilityMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FLAMMABILITY_MAGNITUDE_PERCENT, weapon, "flammability_magnitude_percent", source);
            float burnAilmentThreshold = CombatUtility.getEffectiveAilmentThreshold(target, AilmentType.BURN);
            float flammabilityAdded = (finalFireDamage / burnAilmentThreshold)
                    * com.cranks.dungeons.ailment.AilmentRules.FIRE_BUILDUP_PER_HEALTH_FRACTION;
            float finalFlammability = flammabilityAdded * (1.0f + flammabilityMagnitudePercent);

            if (finalFlammability > 0.01f) {
                float burnMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.BURN_MAGNITUDE_PERCENT, weapon, "burn_magnitude_percent", source);
                float burnBaseDamage = rawFire * 0.5f;
                float finalBurnDamage = burnBaseDamage * (1.0f + burnMagnitudePercent);

                if (finalBurnDamage >= 1.0f) {
                    double burnDuration = AilmentType.BURN.getBaseDuration();
                    AilmentManager.addFlammability(target, finalFlammability, burnDuration, attacker, finalBurnDamage);
                }
            }
        }

        // Poison
        if (physicalDamage > 0 || finalVoidDamage > 0) {
            float chanceToPoison = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_POISON, weapon, "chance_to_poison", source);
            if (chanceToPoison > 0.0f) {
                chanceToPoison *= (1.0f + globalAilmentChance);
                if (isCrit || chanceToPoison >= 1.0f || attacker.getRandom().nextFloat() < chanceToPoison) {
                    float poisonMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.POISON_MAGNITUDE_PERCENT, weapon, "poison_magnitude_percent", source);
                    float poisonBaseDamage = rawPoison * 0.2f;
                    float finalPoisonDamage = poisonBaseDamage * (1.0f + poisonMagnitudePercent);

                    if (finalPoisonDamage >= 0.5f) {
                        double poisonDuration = AilmentType.POISON.getBaseDuration();
                        AilmentManager.applyAilment(target, AilmentType.POISON, finalPoisonDamage, poisonDuration, attacker, null, null, 0, finalPoisonDamage);
                    }
                }
            }
        }

        // Cold / Freeze
        if (finalColdDamage > 0) {
            float freezeBuildupMultiplier = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FREEZE_BUILDUP_PERCENT, weapon, "freeze_buildup_percent", source);
            float freezeAilmentThreshold = CombatUtility.getEffectiveAilmentThreshold(target, AilmentType.FREEZE);
            float freezeBuildupAmount = (finalColdDamage * 2.0f / freezeAilmentThreshold) * freezeBuildupMultiplier;

            if (freezeBuildupAmount > 0.01f) {
                float freezeMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.FREEZE_MAGNITUDE_PERCENT, weapon, "freeze_magnitude_percent", source) + critNonDamagingMagnitude;
                float magnitudeMultiplier = 1.0f + freezeMagnitudePercent;
                double freezeDuration = AilmentType.FREEZE.getBaseDuration() * magnitudeMultiplier;
                AilmentManager.addFreezeBuildup(target, freezeBuildupAmount, freezeDuration, attacker, magnitudeMultiplier);
            }
        }

        // Bleed
        if (physicalDamage > 0) {
            float chanceToBleed = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_BLEED, weapon, "chance_to_bleed", source);
            if (chanceToBleed > 0.0f) {
                chanceToBleed *= (1.0f + globalAilmentChance);
                if (isCrit || chanceToBleed >= 1.0f || attacker.getRandom().nextFloat() < chanceToBleed) {
                    float bleedMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.BLEED_MAGNITUDE_PERCENT, weapon, "bleed_magnitude_percent", source);
                    if (physicalDamage >= 1.0f) {
                        float fraction=(float)com.cranks.dungeons.ailment.AilmentRules.bleedFraction(bleedMagnitudePercent);
                        AilmentManager.applyAilment(target, AilmentType.BLEED, 0, AilmentType.BLEED.getBaseDuration(), attacker, null, null, 0, fraction);
                    }
                }
            }
        }

        // Lightning / Shock & Electrocute
        if (finalLightningDamage > 0) {
            float shockAilmentThreshold = CombatUtility.getEffectiveAilmentThreshold(target, AilmentType.SHOCK);
            float increasedChance = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.CHANCE_TO_SHOCK, weapon, "chance_to_shock", source);
            float baseChance = (finalLightningDamage / shockAilmentThreshold) * 0.25f;
            float finalChance = baseChance * (1.0f + increasedChance + globalAilmentChance);

            if (isCrit || finalChance >= 1.0f || attacker.getRandom().nextFloat() < finalChance) {
                float baseMagnitude = (finalLightningDamage / shockAilmentThreshold) * 0.30f;
                float shockMagnitudeBonus = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.SHOCK_MAGNITUDE_PERCENT, weapon, "shock_magnitude_percent", source) + critNonDamagingMagnitude;
                float shockMagnitudePercent = 1.0f + shockMagnitudeBonus;
                float finalMagnitude = baseMagnitude * shockMagnitudePercent;
                float maxShockCap = Math.min(0.50f, 0.30f * (1.0f + shockMagnitudeBonus));
                finalMagnitude = Math.min(finalMagnitude, maxShockCap);

                if (finalMagnitude >= 0.05f) {
                    double shockDuration = AilmentType.SHOCK.getBaseDuration();
                    AilmentManager.applyAilment(target, AilmentType.SHOCK, finalLightningDamage, shockDuration, attacker, null, null, 0, finalMagnitude);
                }
            }

            float electrocuteAilmentThreshold = CombatUtility.getEffectiveAilmentThreshold(target, AilmentType.ELECTROCUTE);
            float electrocuteBuildupMultiplier = 1.0f + (float) CombatUtility.getAttributeValue(attacker, ModAttributes.ELECTROCUTE_BUILDUP_PERCENT, weapon, "electrocute_buildup_percent", source);
            float electrocuteBuildupAmount = (finalLightningDamage * 4.0f / electrocuteAilmentThreshold) * electrocuteBuildupMultiplier;

            if (electrocuteBuildupAmount > 0) {
                float electrocuteMagnitudePercent = (float) CombatUtility.getAttributeValue(attacker, ModAttributes.ELECTROCUTE_MAGNITUDE_PERCENT, weapon, "electrocute_magnitude_percent", source) + critNonDamagingMagnitude;
                float magnitudeMultiplier = 1.0f + electrocuteMagnitudePercent;
                double electrocuteDuration = AilmentType.ELECTROCUTE.getBaseDuration() * magnitudeMultiplier;
                AilmentManager.addElectrocuteBuildup(target, electrocuteBuildupAmount, electrocuteDuration, attacker);
            }
        }

        // Shock Chain
        if (finalLightningDamage > 0 && hit.hadShock() && !AilmentManager.isShockChainInProgress()) {
            if (AilmentManager.acquireShockChainLock()) {
                try {
                    float outgoing=rawLightning/(attacker instanceof PlayerEntity && target instanceof PlayerEntity?.25f:1);
                    AilmentManager.executeShockChain(target, outgoing, attacker);
                } catch (Exception e) {
                    System.err.println("[CranksDungeons] Error in shock chain: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    AilmentManager.releaseShockChainLock();
                }
            }
        }

    }
}
