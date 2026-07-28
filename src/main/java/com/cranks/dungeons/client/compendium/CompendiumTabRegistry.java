package com.cranks.dungeons.client.compendium;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Central registry for Compendium sidebar tabs.
 *
 * <p>Built-in tabs are registered during client initialization.
 * Other mods can register tabs either by:</p>
 * <ol>
 *   <li>Calling {@link #registerTab} directly during their client init, OR</li>
 *   <li>Declaring a {@code "cranks-compendium"} entrypoint in their {@code fabric.mod.json}
 *       that implements {@link CompendiumEntrypoint}.</li>
 * </ol>
 *
 * <p>Entrypoint discovery is triggered automatically by Cranks Dungeons during
 * client initialization via {@link #discoverEntrypoints()}.</p>
 */
public final class CompendiumTabRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("CompendiumTabRegistry");
    private static final List<CompendiumTab> tabs = new ArrayList<>();
    private static boolean entrypointsDiscovered = false;

    /** Well-known tab IDs for built-in tabs. */
    public static final Identifier GUIDE_TAB_ID = Identifier.of("cranks-dungeons", "compendium_guide");
    public static final Identifier PASSIVE_TREE_TAB_ID = Identifier.of("cranks-dungeons", "passive_tree");
    public static final Identifier WORLD_TREE_TAB_ID = Identifier.of("cranks-dungeons", "world_tree");

    private CompendiumTabRegistry() {}

    /**
     * Register a tab with full control over all parameters.
     *
     * @param tab the tab to register
     */
    public static void registerTab(CompendiumTab tab) {
        // Prevent duplicate registrations
        for (CompendiumTab existing : tabs) {
            if (existing.id().equals(tab.id())) {
                LOGGER.warn("Compendium tab '{}' is already registered, skipping duplicate.", tab.id());
                return;
            }
        }
        tabs.add(tab);
        LOGGER.info("Registered compendium tab: {} (priority {})", tab.id(), tab.priority());
    }

    /**
     * Convenience method to register a tab without creating a CompendiumTab manually.
     *
     * @param id          unique identifier
     * @param tooltip     hover tooltip text
     * @param iconTexture icon sprite identifier (24x24)
     * @param factory     supplier for the tab renderer
     * @param priority    sort order (lower = higher in sidebar)
     */
    public static void registerTab(Identifier id, Text tooltip, Identifier iconTexture,
                                   Supplier<CompendiumTabRenderer> factory, int priority) {
        registerTab(new CompendiumTab(id, tooltip, iconTexture, factory, priority));
    }

    /**
     * Returns all registered tabs sorted by priority (ascending).
     */
    public static List<CompendiumTab> getTabs() {
        if (!entrypointsDiscovered) {
            discoverEntrypoints();
        }
        List<CompendiumTab> sorted = new ArrayList<>(tabs);
        sorted.sort(Comparator.comparingInt(CompendiumTab::priority));
        return sorted;
    }

    /**
     * Finds a registered tab by its ID.
     * @return the tab, or null if not found
     */
    public static CompendiumTab getTab(Identifier id) {
        for (CompendiumTab tab : tabs) {
            if (tab.id().equals(id)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Discovers and invokes all mods that declare a {@code "cranks-compendium"} entrypoint.
     * Called automatically on first access to {@link #getTabs()}.
     */
    public static void discoverEntrypoints() {
        if (entrypointsDiscovered) return;
        entrypointsDiscovered = true;

        List<CompendiumEntrypoint> entrypoints = FabricLoader.getInstance()
                .getEntrypoints("cranks-compendium", CompendiumEntrypoint.class);

        for (CompendiumEntrypoint entrypoint : entrypoints) {
            try {
                entrypoint.registerCompendiumTabs();
            } catch (Exception e) {
                LOGGER.error("Failed to invoke compendium entrypoint: {}", entrypoint.getClass().getName(), e);
            }
        }

        LOGGER.info("Discovered {} compendium entrypoint(s), total tabs registered: {}",
                entrypoints.size(), tabs.size());
    }
}
