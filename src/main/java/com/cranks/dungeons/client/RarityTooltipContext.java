package com.cranks.dungeons.client;

import com.cranks.dungeons.item.CustomRarity;

/**
 * Thread-local context that stores the rarity of the item whose tooltip is
 * currently being rendered. Set by HoveredItemTooltipMixin before the tooltip
 * background draws, read by TooltipBackgroundMixin to pick the right texture.
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
