package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LivingEntity.class, priority = 1000)
public abstract class KnockbackProcessorMixin {

    @ModifyVariable(
            method = "takeKnockback",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private double calculateFinalKnockback(double strength) {
        LivingEntity target = (LivingEntity) (Object) this;
        LivingEntity attacker = target.getAttacker();

        double finalStrength = strength;

        // 1. Apply Attacker's Bonus first (Multiplicative is recommended)
        if (attacker == null) attacker = target.getLastAttacker(); // Fallback for some mixin execution orders

        if (attacker instanceof PlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            ItemStack chestplate = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);

            double knockbackBonus = player.getAttributeValue(ModAttributes.KNOCKBACK);
            knockbackBonus += EquipmentStatApplier.getItemStatValue(weapon, "knockback");

            // Add enchantment bonuses
            knockbackBonus += getEnchantmentValue(weapon, "minecraft:bonus_knockback", 0.35);
            knockbackBonus += getEnchantmentValue(chestplate, "minecraft:bonus_knockback", 0.35);

            // If base strength is 0 (standing attack), give it a base strength from the bonus
            if (finalStrength <= 0.01 && knockbackBonus > 0) {
                // Increased base floor to 0.35 * bonus to ensure visibility for Spears
                finalStrength = Math.max(0.2, knockbackBonus * 0.4); 
            } else {
                // Otherwise amplify existing knockback (sprinting/crit)
                finalStrength *= (1.0 + knockbackBonus);
            }
        }

        // 2. Apply Target's Resistance to the NEW total
        double resistance = target.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE);

        // Clamp resistance between 0 and 1 so it doesn't pull the player inward
        double resistanceMultiplier = Math.max(0.0, 1.0 - resistance);

        finalStrength *= resistanceMultiplier;

        return finalStrength;
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