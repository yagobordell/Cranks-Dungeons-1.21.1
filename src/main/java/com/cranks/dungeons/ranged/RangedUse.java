package com.cranks.dungeons.ranged;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/** Identifies the weapon with which a server-side draw or reload began. */
public interface RangedUse {
    boolean cranks$matchesRangedUse(ItemStack weapon, Hand hand, int selectedSlot);
}
