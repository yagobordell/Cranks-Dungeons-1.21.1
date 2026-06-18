package com.cranks.dungeons.equipment;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import com.cranks.dungeons.registry.ModAttributes;

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

        var modifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (var entry : modifiers.modifiers()) {
                var attr = entry.attribute();
                if (attr.equals(EntityAttributes.ARMOR) || attr.equals(ModAttributes.BONUS_ARMOR)) hasArmor = true;
                if (attr.equals(ModAttributes.EVASION_RATING)) hasEvasion = true;
                if (attr.equals(ModAttributes.ENERGY_SHIELD)) hasES = true;
            }
        }

        if (hasArmor && hasEvasion && hasES) return ALL;
        if (hasArmor && hasEvasion) return STR_DEX;
        if (hasArmor && hasES) return STR_INT;
        if (hasEvasion && hasES) return DEX_INT;
        if (hasArmor) return STR;
        if (hasEvasion) return DEX;
        if (hasES) return INT;

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

        if (itemStr && statStr) return true;
        if (itemDex && statDex) return true;
        if (itemInt && statInt) return true;

        return false;
    }
}
