package com.cranks.dungeons.equipment;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;

public enum ArmorBaseType {
    STR,
    DEX,
    INT,
    STR_DEX,
    STR_INT,
    DEX_INT,
    ALL,
    ANY;

    public static ArmorBaseType getFromItem(ItemStack stack) {
        boolean hasArmor = false;
        boolean hasEvasion = false;
        boolean hasES = false;

        ItemStack defaultStack = new ItemStack(stack.getItem());
        var modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, defaultStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, net.minecraft.component.type.AttributeModifiersComponent.DEFAULT));
        if (modifiers != null) {
            for (var entry : modifiers.modifiers()) {
                var attr = entry.attribute();
                if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attr, EntityAttributes.GENERIC_ARMOR) || com.cranks.dungeons.util.CombatUtility.attributesMatch(attr, ModAttributes.BONUS_ARMOR)) hasArmor = true;
                if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attr, ModAttributes.EVASION_RATING)) hasEvasion = true;
                if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attr, ModAttributes.ENERGY_SHIELD)) hasES = true;
            }
        }

        if (hasArmor && hasEvasion && hasES) return ALL;
        if (hasArmor && hasEvasion) return STR_DEX;
        if (hasArmor && hasES) return STR_INT;
        if (hasEvasion && hasES) return DEX_INT;
        if (hasArmor) return STR;
        if (hasEvasion) return DEX;
        if (hasES) return INT;

        // If it lacks armor/evasion/ES modifiers, it might be a weapon or tool.
        // Let's check EquipmentType to assign proper stat pools for weapons.
        var equipTypeOpt = EquipmentType.getTypeForItem(stack);
        if (equipTypeOpt.isPresent()) {
            EquipmentType type = equipTypeOpt.get();
            return switch (type) {
                case MACE, AXE, SHIELD, PICKAXE, SHOVEL, HOE -> STR;
                case BOW -> DEX;
                case SWORD, SPEAR, CROSSBOW -> STR_DEX;
                case TRIDENT -> STR_INT;
                // If you add wands or staves in the future, you could add INT or DEX_INT here.
                default -> ANY;
            };
        }

        return ANY;
    }

    public boolean canRoll(ArmorBaseType statBase) {
        if (this == ANY || statBase == ANY) return true;
        if (this == ALL || statBase == ALL) return true;

        boolean itemStr = this == STR || this == STR_DEX || this == STR_INT;
        boolean itemDex = this == DEX || this == STR_DEX || this == DEX_INT;
        boolean itemInt = this == INT || this == STR_INT || this == DEX_INT;

        boolean statStr = statBase == STR || statBase == STR_DEX || statBase == STR_INT;
        boolean statDex = statBase == DEX || statBase == STR_DEX || statBase == DEX_INT;
        boolean statInt = statBase == INT || statBase == STR_INT || statBase == DEX_INT;

        if (statStr && !itemStr) return false;
        if (statDex && !itemDex) return false;
        if (statInt && !itemInt) return false;

        return true;
    }
}
