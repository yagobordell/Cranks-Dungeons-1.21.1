package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
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

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            LivingEntity target = (LivingEntity) (Object) this;
            ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);
            ItemStack chest = attacker.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack helmet = attacker.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack leggings = attacker.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = attacker.getEquippedStack(EquipmentSlot.FEET);

            // Crit Chance with enchantments from all armor pieces
            double critChance = CombatUtility.getAttributeValue(attacker, ModAttributes.CRIT_CHANCE, weapon, "crit_chance", source);
            critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(chest, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.075);

            float finalAmount = amount;
            boolean isCrit = false;

            if (critChance > 0 && attacker.getRandom().nextFloat() < critChance) {
                double critMult = CombatUtility.getAttributeValue(attacker, ModAttributes.CRIT_MULTIPLIER, weapon, "crit_multiplier", source);
                
                // --- Armor Crit Mitigation ---
                float extraDamageMult = (float) (critMult - 1.0);
                if (target instanceof PlayerEntity defender) {
                    double mitigationMultiplier = defender.getAttributeValue(ModAttributes.ARMOR_CRIT_MITIGATION);
                    if (mitigationMultiplier > 0) {
                        double armor = defender.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ARMOR);
                        double mitigation = Math.min(1.0, armor * mitigationMultiplier);
                        extraDamageMult *= (float) (1.0 - mitigation);
                    }
                }
                
                finalAmount = amount * (1.0f + extraDamageMult);
                isCrit = true;
                CriticalHitState.setCriticalHit(true);
                spawnCriticalEffects(world, target);
            }

            double lifeStealStat = CombatUtility.getAttributeValue(attacker, ModAttributes.LIFE_STEAL, weapon, "life_steal", source);

            if (lifeStealStat > 0) {
                float healAmount = (float) (finalAmount * lifeStealStat);
                attacker.heal(healAmount);
            }

            return finalAmount;
        }
        return amount;
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
    private void spawnCriticalEffects(ServerWorld world, LivingEntity target) {
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
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }
}