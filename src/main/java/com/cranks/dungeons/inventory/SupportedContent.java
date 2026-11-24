package com.cranks.dungeons.inventory;

import com.cranks.dungeons.alchemy.AlchemyPotions;
import com.cranks.dungeons.util.EnchantmentFilterUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;

/** One policy for item creation, component edits, NBT/packet loads, loot, trades, and inventory. */
public final class SupportedContent {
    private static boolean ready;
    private static final ThreadLocal<Boolean> NORMALIZING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> CREATIVE = ThreadLocal.withInitial(() -> 0);
    private SupportedContent() {}
    public static void enable() { ready = true; }
    public static boolean beginNormalization() {
        if (!ready || NORMALIZING.get() || CREATIVE.get() > 0) return false;
        NORMALIZING.set(true);
        return true;
    }
    public static void endNormalization() { NORMALIZING.remove(); }
    public static void beginCreativeEntries() { CREATIVE.set(CREATIVE.get() + 1); }
    public static void endCreativeEntries() {
        int depth = CREATIVE.get() - 1;
        if (depth == 0) CREATIVE.remove(); else CREATIVE.set(depth);
    }
    public static ItemStack normalize(ItemStack stack) {
        ItemStack result = AlchemyPotions.canonicalize(stack);
        EnchantmentFilterUtil.filterStack(result);
        return result;
    }
    /** Creative generation must filter before exposing entries, without changing hashed entries in-place. */
    public static boolean isSupported(ItemStack stack) {
        if (stack.isEmpty()) return true;
        for (var type : java.util.List.of(DataComponentTypes.ENCHANTMENTS, DataComponentTypes.STORED_ENCHANTMENTS)) {
            var enchantments = stack.getOrDefault(type, ItemEnchantmentsComponent.DEFAULT);
            if (enchantments.getEnchantments().stream().anyMatch(entry -> !EnchantmentFilterUtil.isApproved(entry))) return false;
        }
        return AlchemyPotions.canonicalize(stack) == stack;
    }
}
