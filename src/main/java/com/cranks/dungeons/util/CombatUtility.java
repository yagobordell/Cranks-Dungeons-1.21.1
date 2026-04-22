package com.cranks.dungeons.util;

import com.cranks.dungeons.util.WeaponProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.registry.ModAttributes;

public class CombatUtility {

    /**
     * Determines the active weapon stack for a damage event.
     * If the damage was caused by a thrown trident, returns the trident's item
     * stack.
     * Otherwise, returns the attacker's main hand stack.
     */
    public static ItemStack getCombatWeapon(DamageSource source, PlayerEntity attacker) {
        if (source.getSource() instanceof WeaponProvider provider) {
            ItemStack weapon = provider.cranks$getWeapon();
            if (weapon != null && !weapon.isEmpty()) {
                return weapon;
            }
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
     * Returns the attribute value for a player, accounting for trident throws and
     * potential doubling.
     * To prevent doubling (e.g. if the trident returned to hand via Loyalty), we
     * subtract
     * the stat value of the player's CURRENTLY held item before adding the stats of
     * the
     * weapon being analyzed.
     */
    public static double getAttributeValue(PlayerEntity attacker,
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
            ItemStack weapon, String statId, DamageSource source) {
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

    /**
     * Returns the weapon-specific damage percentage bonus for the player based on
     * the held weapon.
     */
    public static double getWeaponSpecificDamageBonus(PlayerEntity attacker, ItemStack weapon) {
        EquipmentType type = EquipmentType.getTypeForItem(weapon).orElse(null);
        if (type == null)
            return 0.0;

        return switch (type) {
            case SWORD -> attacker.getAttributeValue(ModAttributes.SWORD_DAMAGE_PERCENT);
            case AXE -> attacker.getAttributeValue(ModAttributes.AXE_DAMAGE_PERCENT);
            case MACE -> attacker.getAttributeValue(ModAttributes.MACE_DAMAGE_PERCENT);
            case SPEAR -> attacker.getAttributeValue(ModAttributes.SPEAR_DAMAGE_PERCENT);
            case BOW -> attacker.getAttributeValue(ModAttributes.BOW_DAMAGE_PERCENT);
            case CROSSBOW -> attacker.getAttributeValue(ModAttributes.CROSSBOW_DAMAGE_PERCENT);
            case TRIDENT -> attacker.getAttributeValue(ModAttributes.TRIDENT_DAMAGE_PERCENT);
            default -> 0.0;
        };
    }

    public static boolean isMelee(ItemStack weapon, DamageSource source) {
        if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE))
            return false;
        EquipmentType type = EquipmentType.getTypeForItem(weapon).orElse(null);
        if (type == null)
            return true; // Default to melee
        return switch (type) {
            case SWORD, SPEAR, MACE, AXE, PICKAXE, SHOVEL, HOE -> true;
            case BOW, CROSSBOW -> false;
            case TRIDENT -> !isTridentThrow(source);
            default -> true;
        };
    }

    public static boolean isRanged(ItemStack weapon, DamageSource source) {
        if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE))
            return true;
        EquipmentType type = EquipmentType.getTypeForItem(weapon).orElse(null);
        if (type == null)
            return false;
        return switch (type) {
            case BOW, CROSSBOW -> true;
            case TRIDENT -> isTridentThrow(source);
            default -> false;
        };
    }

    public static boolean isTwoHanding(PlayerEntity player, ItemStack mainWeapon) {
        ItemStack offhand = player.getOffHandStack();
        return offhand.isEmpty();
    }

    public static boolean isDualWielding(PlayerEntity player, ItemStack mainWeapon) {
        ItemStack offhand = player.getOffHandStack();
        if (offhand.isEmpty())
            return false;

        EquipmentType mainType = EquipmentType.getTypeForItem(mainWeapon).orElse(null);
        EquipmentType offType = EquipmentType.getTypeForItem(offhand).orElse(null);

        return mainType != null && mainType == offType;
    }

    public static boolean isOneHanding(PlayerEntity player, ItemStack mainWeapon) {
        ItemStack offhand = player.getOffHandStack();
        if (offhand.isEmpty())
            return false;

        EquipmentType mainType = EquipmentType.getTypeForItem(mainWeapon).orElse(null);
        EquipmentType offType = EquipmentType.getTypeForItem(offhand).orElse(null);

        // If offhand is not a weapon of the same type, it's one handing
        return mainType != offType;
    }
}
