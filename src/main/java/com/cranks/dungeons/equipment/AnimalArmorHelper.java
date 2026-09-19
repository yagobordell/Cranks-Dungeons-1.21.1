package com.cranks.dungeons.equipment;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Identifies the vanilla animal armor supported by the fixed base-stat system. */
public final class AnimalArmorHelper {
    private AnimalArmorHelper() {
    }

    public static boolean isSupported(ItemStack stack) {
        return stack != null && !stack.isEmpty() && isSupported(stack.getItem());
    }

    public static boolean isSupported(Item item) {
        return item == Items.LEATHER_HORSE_ARMOR ||
                item == Items.IRON_HORSE_ARMOR ||
                item == Items.GOLDEN_HORSE_ARMOR ||
                item == Items.DIAMOND_HORSE_ARMOR ||
                item == Items.WOLF_ARMOR;
    }
}
