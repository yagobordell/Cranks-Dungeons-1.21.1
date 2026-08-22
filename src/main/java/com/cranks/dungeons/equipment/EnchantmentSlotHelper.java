package com.cranks.dungeons.equipment;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public final class EnchantmentSlotHelper {
    public static final String NBT_KEY = "EnchantmentSlots";

    private EnchantmentSlotHelper() {}

    /**
     * Returns the current number of enchantment slots on the item.
     * Defaults to 0 if not set.
     */
    public static int getEnchantmentSlots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        return nbt.getInt(NBT_KEY);
    }

    /**
     * Sets the number of enchantment slots on the item.
     */
    public static void setEnchantmentSlots(ItemStack stack, int slots) {
        if (stack == null || stack.isEmpty()) return;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putInt(NBT_KEY, Math.max(0, slots));
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    /**
     * Gets the maximum number of enchantment slots allowed for this item.
     * - Helmets, boots, shields, 1H weapons (sword, mace): 1
     * - Chestplates, leggings, elytra, 2H weapons (spear, bow, crossbow), tools (pickaxe, axe, shovel, hoe): 2
     * - Trident (unique): 3
     */
    public static int getMaxEnchantmentSlots(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;

        // Specific unique overrides
        if (stack.isOf(Items.TRIDENT)) {
            return 3;
        }

        Optional<EquipmentType> typeOpt = EquipmentType.getTypeForItem(stack);
        if (typeOpt.isEmpty()) return 0;

        EquipmentType type = typeOpt.get();
        return switch (type) {
            case HELMET, BOOTS, SHIELD, SWORD, MACE -> 1;
            case CHESTPLATE, LEGGINGS, ELYTRA, SPEAR, BOW, CROSSBOW, TRIDENT, AXE, PICKAXE, SHOVEL, HOE -> 2;
        };
    }

    /**
     * Whether this item can receive another enchantment slot.
     */
    public static boolean canAddSlot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getEnchantmentSlots(stack) < getMaxEnchantmentSlots(stack);
    }

    /**
     * Whether this item can receive enchantment slots from a deepslate sigil.
     */
    public static boolean isValidForSigil(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getMaxEnchantmentSlots(stack) > 0;
    }
}
