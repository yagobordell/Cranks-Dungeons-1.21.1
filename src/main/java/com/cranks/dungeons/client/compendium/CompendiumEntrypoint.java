package com.cranks.dungeons.client.compendium;

/**
 * Fabric entrypoint interface for cross-mod Compendium tab registration.
 *
 * <p>Other mods that want to add tabs to the Cranks Dungeons Compendium sidebar
 * should implement this interface and declare it in their {@code fabric.mod.json}:</p>
 *
 * <pre>{@code
 * "entrypoints": {
 *     "cranks-compendium": ["com.yourmod.YourCompendiumTabs"]
 * }
 * }</pre>
 *
 * <p>The {@link #registerCompendiumTabs()} method will be called during client
 * initialization. Use {@link CompendiumTabRegistry#registerTab} inside it.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * public class MyModCompendiumTabs implements CompendiumEntrypoint {
 *     @Override
 *     public void registerCompendiumTabs() {
 *         CompendiumTabRegistry.registerTab(
 *             Identifier.of("mymod", "my_tab"),
 *             Text.literal("My Tab"),
 *             Identifier.of("mymod", "compendium/my_icon"),
 *             MyTabRenderer::new,
 *             200 // priority (built-in tabs use 0-100)
 *         );
 *     }
 * }
 * }</pre>
 */
public interface CompendiumEntrypoint {

    /**
     * Called during client initialization to register Compendium tabs.
     * Use {@link CompendiumTabRegistry#registerTab} to add your tabs.
     */
    void registerCompendiumTabs();
}
