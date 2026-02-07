package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
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

@Mixin(LivingEntity.class)
public class BonusAttackDamageMixin {

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float applyBonusAttackDamage(float amount, ServerWorld world, DamageSource source) {
        if (source.getAttacker() instanceof PlayerEntity attacker) {
            ItemStack weapon = attacker.getMainHandStack();
            ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

            // Get bonus attack damage from attributes + stats
            double bonusDamage = attacker.getAttributeValue(ModAttributes.BONUS_ATTACK_DAMAGE);
            bonusDamage += EquipmentStatApplier.getItemStatValue(weapon, "bonus_attack_damage");

            // Add bonus attack damage from weapon/chestplate enchantments
            bonusDamage += getEnchantmentValue(weapon, "minecraft:attack_damage", 6.0);
            bonusDamage += getEnchantmentValue(chestplate, "minecraft:attack_damage", 6.0);

            // Apply flat bonus first
            float damageWithBonus = amount + (float) bonusDamage;

            // Get percentage multipliers from items
            double percentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent");
            double percentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent");
            double totalPercent = percentFromWeapon + percentFromChestplate;

            // Apply percentage to total damage (base + bonus)
            return (float) (damageWithBonus * (1.0 + totalPercent));
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
}