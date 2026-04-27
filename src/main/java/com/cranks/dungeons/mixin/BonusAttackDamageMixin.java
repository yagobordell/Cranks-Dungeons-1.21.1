package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
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

import com.cranks.dungeons.util.CombatUtility;

@Mixin(LivingEntity.class)
public class BonusAttackDamageMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyBonusAttackDamage(float amount, ServerWorld world, DamageSource source) {
        if (source.getAttacker() instanceof LivingEntity attacker) {
            ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);
            ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

            // Get bonus attack damage from attributes
            double bonusDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.BONUS_ATTACK_DAMAGE, weapon,
                    "bonus_attack_damage", source);

            // Add bonus attack damage from weapon/chestplate enchantments
            if (weapon != null) bonusDamage += getEnchantmentValue(weapon, "minecraft:attack_damage", 4.0);
            if (chestplate != null) bonusDamage += getEnchantmentValue(chestplate, "minecraft:attack_damage", 4.0);

            // Apply flat bonus first
            float damageWithBonus = amount + (float) bonusDamage;

            // Get percentage multipliers from items and attributes
            double percentFromWeapon = weapon != null ? EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent") : 0;
            double percentFromChestplate = chestplate != null ? EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent") : 0;
            double attributePercent = attacker.getAttributeValue(ModAttributes.ATTACK_DAMAGE_PERCENT);
            double weaponSpecificPercent = CombatUtility.getWeaponSpecificDamageBonus(attacker, weapon);

            double afflictedBonus = 0;
            if (AilmentManager.isAfflicted((LivingEntity) (Object) this)) {
                afflictedBonus = attacker.getAttributeValue(ModAttributes.DAMAGE_AGAINST_AFFLICTED_PERCENT);
            }

            double typeBonus = 0;
            if (CombatUtility.isMelee(weapon, source)) {
                typeBonus = attacker.getAttributeValue(ModAttributes.MELEE_DAMAGE_PERCENT);
            } else if (CombatUtility.isRanged(weapon, source)) {
                typeBonus = attacker.getAttributeValue(ModAttributes.RANGED_DAMAGE_PERCENT);
            }

            double totalPercent = percentFromWeapon + percentFromChestplate + attributePercent + weaponSpecificPercent
                    + afflictedBonus + typeBonus;

            if (CombatUtility.isTwoHanding(attacker, weapon)) {
                totalPercent += attacker.getAttributeValue(ModAttributes.TWO_HANDING_DAMAGE_PERCENT);
            } else if (CombatUtility.isDualWielding(attacker, weapon)) {
                totalPercent += attacker.getAttributeValue(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT);
            } else if (CombatUtility.isOneHanding(attacker, weapon)) {
                totalPercent += attacker.getAttributeValue(ModAttributes.ONE_HANDED_DAMAGE_PERCENT);
            }

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