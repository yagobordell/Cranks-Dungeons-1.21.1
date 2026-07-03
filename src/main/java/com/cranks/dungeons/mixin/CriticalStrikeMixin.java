package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.util.CriticalHitState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.cranks.dungeons.util.CombatUtility;

@Mixin(value = LivingEntity.class, priority = 10000)
public abstract class CriticalStrikeMixin {

    @ModifyVariable(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)V",
                    shift = At.Shift.BEFORE
            ),
            ordinal = 0,
            argsOnly = true
    )
    private float applyTotalCalculatedDamage(float amount, ServerWorld world, DamageSource source) {
        // Reset crit state
        CriticalHitState.reset();

        if (source.getAttacker() instanceof LivingEntity attacker) {
            LivingEntity target = (LivingEntity) (Object) this;
            ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);
            ItemStack chest = attacker.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack helmet = attacker.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack leggings = attacker.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = attacker.getEquippedStack(EquipmentSlot.FEET);

            // Crit Chance with enchantments
            double baseCrit = attacker.getAttributeValue(ModAttributes.CRIT_CHANCE);
            double localCrit = EquipmentStatApplier.getItemStatValue(weapon, "local_crit_chance");
            double globalCrit = attacker.getAttributeValue(ModAttributes.GLOBAL_CRIT_CHANCE_PERCENT);
            double critChance = (baseCrit * (1.0 + localCrit)) * (1.0 + globalCrit);

            if (AilmentManager.isAfflicted(target)) {
                critChance += attacker.getAttributeValue(ModAttributes.CRIT_CHANCE_AGAINST_AFFLICTED);
            }

            if (attacker.getAttributeValue(ModAttributes.KEYSTONE_RESOLUTE_TECHNIQUE) > 0) {
                critChance = 0;
            }

            float finalAmount = amount;

            if (critChance > 0 && attacker.getRandom().nextFloat() < critChance) {
                double critMult = CombatUtility.getAttributeValue(attacker, ModAttributes.CRIT_MULTIPLIER, weapon, "crit_multiplier", source);
                
                // Elemental Overload check
                if (attacker.getAttributeValue(ModAttributes.KEYSTONE_ELEMENTAL_OVERLOAD) > 0) {
                    critMult = 1.0;
                    attacker.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                            com.cranks.dungeons.effect.ModEffects.ELEMENTAL_OVERLOAD,
                            160, // 8 seconds (20 ticks * 8)
                            0, false, true, true
                    ));
                }

                // --- Armor Crit Mitigation ---
                float extraDamageMult = (float) (critMult - 1.0);
                double mitigationMultiplier = target.getAttributeValue(ModAttributes.ARMOR_CRIT_MITIGATION);
                if (mitigationMultiplier > 0) {
                    double armor = target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ARMOR);
                    double mitigation = Math.min(1.0, armor * mitigationMultiplier);
                    extraDamageMult *= (float) (1.0 - mitigation);
                }
                
                finalAmount = amount * (1.0f + extraDamageMult);
                CriticalHitState.setCriticalHit(true);
                spawnCriticalEffects(world, target, attacker);
            }


            return finalAmount;
        }
        return amount;
    }


    @Unique
    private void spawnCriticalEffects(ServerWorld world, LivingEntity target, LivingEntity attacker) {
        // High density enchanted hit particles (Purple/White)
        world.spawnParticles(
                net.minecraft.particle.ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY() + target.getHeight() * 0.75, target.getZ(),
                15, 0.2, 0.2, 0.2, 0.2
        );

        // Standard Crit particles (White/Brownish)
        world.spawnParticles(
                net.minecraft.particle.ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(),
                10, 0.3, 0.3, 0.3, 0.15
        );

        // Glow particles for the "Gold" shimmer effect
        world.spawnParticles(
                net.minecraft.particle.ParticleTypes.GLOW,
                target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(),
                12, 0.4, 0.4, 0.4, 0.05
        );

        // Impact sound
        world.playSound(
                null,
                target.getBlockPos(),
                ModSounds.CRITICAL_HIT,
                attacker instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE,
                1.0f,
                1.0f
        );
    }
}