package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.CriticalHitState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
            ItemStack weapon = attacker.getMainHandStack();
            ItemStack chest = attacker.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack helmet = attacker.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack leggings = attacker.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = attacker.getEquippedStack(EquipmentSlot.FEET);

            // Crit Chance with enchantments from all armor pieces
            double critChance = attacker.getAttributeValue(ModAttributes.CRIT_CHANCE);
            critChance += EquipmentStatApplier.getItemStatValue(weapon, "crit_chance");
            critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(chest, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.075);

            float finalAmount = amount;
            boolean isCrit = false;

            if (critChance > 0 && attacker.getRandom().nextFloat() < critChance) {
                finalAmount = amount * 1.5f;
                isCrit = true;
                CriticalHitState.setCriticalHit(true);
                spawnCriticalEffects(world, target);
            }

            double lifeStealStat = EquipmentStatApplier.getItemStatValue(weapon, "life_steal")
                    + EquipmentStatApplier.getItemStatValue(chest, "life_steal");

            if (lifeStealStat > 0) {
                float healAmount = (float) (finalAmount * lifeStealStat);
                attacker.heal(healAmount);
            }

            if (isCrit) {
                System.out.println(String.format("[Dungeons] Base: %.1f | Crit: %b | Physical: %.1f",
                        amount, isCrit, finalAmount));
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
        world.spawnParticles(
                net.minecraft.particle.ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY() + target.getHeight() * 0.75, target.getZ(),
                15, 0.2, 0.2, 0.2, 0.15
        );

        for (int i = 0; i < 12; i++) {
            double angle = i * (Math.PI * 2 / 12);
            double vx = Math.cos(angle) * 0.2;
            double vz = Math.sin(angle) * 0.2;
            world.spawnParticles(
                    net.minecraft.particle.ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    0, vx, 0.1, vz, 0.5
            );
        }

        world.playSound(null, target.getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.2f);
    }
}