package com.cranks.dungeons.client.compendium;

import net.minecraft.client.gui.DrawContext;

/**
 * Interface for rendering a tab's content within the CompendiumScreen.
 * Each tab (Passive Tree, World Tree, Guide, etc.) implements this to
 * handle its own rendering and input within the content area bounds.
 *
 * <p>Implementations should use the content area bounds (contentX, contentY,
 * contentW, contentH) provided in {@link #init} for all positioning calculations
 * rather than the full screen dimensions.</p>
 */
public interface CompendiumTabRenderer {

    /**
     * Initialize the tab renderer with its content area bounds.
     * Called when the tab becomes active or when the screen is resized.
     * Implementations should add their widgets (buttons, etc.) through
     * the parent screen's {@link CompendiumScreen#addTabWidget} method.
     *
     * @param parent   the parent CompendiumScreen (for widget management)
     * @param contentX left edge of the content area (right of sidebar)
     * @param contentY top edge of the content area
     * @param contentW width of the content area
     * @param contentH height of the content area
     */
    void init(CompendiumScreen parent, int contentX, int contentY, int contentW, int contentH);

    /**
     * Render the tab's content. Called every frame when this tab is active.
     * All rendering should be constrained to the content area bounds.
     */
    void render(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     * Handle mouse click events within the content area.
     * @return true if the event was consumed
     */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    /**
     * Handle mouse drag events within the content area.
     * @return true if the event was consumed
     */
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    /**
     * Handle mouse scroll events within the content area.
     * @return true if the event was consumed
     */
    boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);

    /**
     * Handle mouse release events within the content area.
     * Used by designer mode for node drop handling.
     * @return true if the event was consumed
     */
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    /**
     * Handle key press events when this tab is active.
     * @return true if the event was consumed
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * Called when this tab becomes the active tab (user clicks its icon or uses keybind).
     * Use for any setup that should happen each time the tab is viewed.
     */
    default void onActivated() {}

    /**
     * Called when this tab is being deactivated (user switches to another tab).
     * Use for cleanup or state preservation.
     */
    default void onDeactivated() {}
}
