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
            // Get bonus attack damage from ARMOR (player attributes)
            double bonusDamage = attacker.getAttributeValue(ModAttributes.BONUS_ATTACK_DAMAGE);

            // Add bonus attack damage from WEAPON
            ItemStack weapon = attacker.getMainHandStack();
            ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

            bonusDamage += EquipmentStatApplier.getItemStatValue(weapon, "bonus_attack_damage");

            // Apply flat bonus first
            float damageWithBonus = amount + (float) bonusDamage;

            // Get percentage multipliers from items
            double percentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent");
            double percentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent");
            double totalPercent = percentFromWeapon + percentFromChestplate;

            // Apply percentage to total damage (base + bonus)
            float finalDamage = (float) (damageWithBonus * (1.0 + totalPercent));

            System.out.println("Base damage: " + amount);
            System.out.println("After bonus: " + damageWithBonus);
            System.out.println("Attack damage %: " + (totalPercent * 100) + "%");
            System.out.println("Final damage: " + finalDamage);

            return finalDamage;
        }

        return amount;
    }
}