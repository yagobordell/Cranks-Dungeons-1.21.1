package com.cranks.dungeons.client;

import com.cranks.dungeons.item.CustomRarity;

/**
 * Render-thread context for the tooltip currently being drawn. The scoped
 * DrawContext wrappers restore the previous rarity after nested tooltip draws.
 */
public final class RarityTooltipContext {

    private RarityTooltipContext() {}

    /** The rarity active during the current tooltip render pass, or null. */
    private static CustomRarity current = null;

    public static void set(CustomRarity rarity) {
        current = rarity;
    }

    public static CustomRarity get() {
        return current;
    }

    public static void clear() {
        current = null;
    }
}
