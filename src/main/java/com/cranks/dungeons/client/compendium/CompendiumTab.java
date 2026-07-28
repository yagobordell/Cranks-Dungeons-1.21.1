package com.cranks.dungeons.client.compendium;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

/**
 * Represents a single tab entry in the Compendium sidebar.
 * Each tab has an ID, display tooltip, icon texture, a renderer factory,
 * and a priority for ordering in the sidebar.
 *
 * @param id          unique identifier for this tab (e.g. "cranks-dungeons:passive_tree")
 * @param tooltip     text shown when hovering the sidebar icon
 * @param iconTexture identifier for the 24x24 icon sprite texture
 * @param factory     supplier that creates a new renderer instance for this tab
 * @param priority    sort order in the sidebar (lower values appear higher)
 */
public record CompendiumTab(
        Identifier id,
        Text tooltip,
        Identifier iconTexture,
        Supplier<CompendiumTabRenderer> factory,
        int priority
) {

    /** Priority constants for built-in tabs. Other mods should use values > 100. */
    public static final int PRIORITY_GUIDE = 0;
    public static final int PRIORITY_PASSIVE_TREE = 10;
    public static final int PRIORITY_WORLD_TREE = 20;

    /**
     * Creates a new renderer instance for this tab.
     */
    public CompendiumTabRenderer createRenderer() {
        return factory.get();
    }
}
