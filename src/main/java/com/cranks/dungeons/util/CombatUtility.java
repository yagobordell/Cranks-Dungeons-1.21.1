package com.cranks.dungeons.util;

import com.cranks.dungeons.util.TridentStackProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;

public class CombatUtility {

    /**
     * Determines the active weapon stack for a damage event.
     * If the damage was caused by a thrown trident, returns the trident's item stack.
     * Otherwise, returns the attacker's main hand stack.
     */
    public static ItemStack getCombatWeapon(DamageSource source, PlayerEntity attacker) {
        if (source.getSource() instanceof TridentEntity trident) {
            return ((TridentStackProvider) trident).cranks$getTridentStack();
        }
        return attacker.getMainHandStack();
    }

    /**
     * Checks if the damage source is a thrown trident.
     */
    public static boolean isTridentThrow(DamageSource source) {
        return source.getSource() instanceof TridentEntity;
    }

    /**
     * Returns the attribute value for a player, accounting for trident throws and potential doubling.
     * To prevent doubling (e.g. if the trident returned to hand via Loyalty), we subtract
     * the stat value of the player's CURRENTLY held item before adding the stats of the
     * weapon being analyzed.
     */
    public static double getAttributeValue(PlayerEntity attacker, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, ItemStack weapon, String statId, DamageSource source) {
        double currentTotal = attacker.getAttributeValue(attribute);
        
        // 1. Get the "Armor + Base" part by subtracting whatever is currently in hand
        ItemStack held = attacker.getMainHandStack();
        double heldStat = com.cranks.dungeons.equipment.EquipmentStatApplier.getItemStatValue(held, statId);
        double armorAndBase = currentTotal - heldStat;

        // 2. Add the projectile weapon's stat
        double weaponStat = com.cranks.dungeons.equipment.EquipmentStatApplier.getItemStatValue(weapon, statId);
        double finalValue = armorAndBase + weaponStat;
        
        // 3. Apply the global trident throw nerf (8/12 = 0.66x) to the WHOLE value
        if (isTridentThrow(source)) {
            finalValue *= 0.6667;
        }

        return finalValue;
    }
}
