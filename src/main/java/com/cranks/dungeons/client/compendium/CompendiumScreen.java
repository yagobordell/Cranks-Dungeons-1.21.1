package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.ModKeybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Compendium screen with a narrow vertical icon sidebar on the left
 * and a content area on the right. The active tab's renderer handles all
 * content within the content area bounds.
 *
 * <p>Inspired by the Ars Elixirum compendium UI — dark Minecraft-style
 * pixelated theme with icon strip navigation.</p>
 */
public class CompendiumScreen extends Screen {

    /** Sidebar dimensions */
    private static final int SIDEBAR_WIDTH = 32;
    private static final int ICON_SIZE = 24;
    private static final int ICON_PADDING = 4;
    private static final int ICON_TOP_MARGIN = 8;
    private static final int ICON_SLOT_HEIGHT = ICON_SIZE + ICON_PADDING;

    /** Colors */
    private static final int SIDEBAR_BG_COLOR = 0xFF141418;
    private static final int SIDEBAR_BORDER_COLOR = 0xFF2A2A32;
    private static final int TAB_SELECTED_BG = 0xFF24242C;
    private static final int TAB_HOVER_BG = 0xFF1C1C22;
    private static final int CONTENT_BG_COLOR = 0xFF18181E;

    /** Content area bounds (set during init) */
    private int contentX, contentY, contentW, contentH;

    /** Tab state */
    private Identifier activeTabId;
    private CompendiumTabRenderer activeRenderer;
    private final Map<Identifier, CompendiumTabRenderer> renderers = new LinkedHashMap<>();
    private List<CompendiumTab> tabList;

    /**
     * Opens the Compendium with a specific tab pre-selected.
     * @param initialTabId the tab to show first, or null for the default (first) tab
     */
    public CompendiumScreen(@Nullable Identifier initialTabId) {
        super(Text.literal("Compendium"));
        this.activeTabId = initialTabId;
    }

    @Override
    protected void init() {
        this.clearChildren();
        super.init();

        // Calculate content area bounds
        contentX = SIDEBAR_WIDTH;
        contentY = 0;
        contentW = this.width - SIDEBAR_WIDTH;
        contentH = this.height;

        // Load tabs on first init
        if (tabList == null) {
            tabList = CompendiumTabRegistry.getTabs();
            for (CompendiumTab tab : tabList) {
                renderers.computeIfAbsent(tab.id(), id -> tab.createRenderer());
            }
        }

        // Default to first tab if none selected
        if (activeTabId == null && !tabList.isEmpty()) {
            activeTabId = tabList.get(0).id();
        }

        // Activate the current tab renderer
        activeRenderer = renderers.get(activeTabId);
        if (activeRenderer != null) {
            activeRenderer.init(this, contentX, contentY, contentW, contentH);
        }

        // Global Close Button (Top Right of the screen)
        this.addDrawableChild(CompendiumButtonWidget.builder(Text.literal("✕"), b -> this.close())
                .dimensions(this.width - 25, 5, 20, 20).build());
    }

    /**
     * Add a widget to the screen on behalf of a tab renderer.
     * Tab renderers should use this instead of directly accessing Screen methods.
     */
    public <T extends ClickableWidget> T addTabWidget(T widget) {
        return this.addDrawableChild(widget);
    }

    /**
     * Switch to a different tab by ID.
     */
    public void switchToTab(Identifier tabId) {
        if (tabId.equals(activeTabId)) return;

        // Deactivate current
        if (activeRenderer != null) {
            activeRenderer.onDeactivated();
        }

        activeTabId = tabId;
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));

        // Re-init the screen to add/remove widgets for the new tab
        this.init();

        // Activate new
        if (activeRenderer != null) {
            activeRenderer.onActivated();
        }
    }

    /**
     * Force the screen to re-initialize widgets for the current tab.
     * Useful when a tab's internal state changes and it needs different buttons.
     */
    public void refreshTab() {
        this.init();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Prevent default 1.21 screen blur
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim the world instead of solid background
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        // Render sidebar
        renderSidebar(context, mouseX, mouseY);

        // Render active tab content
        if (activeRenderer != null) {
            activeRenderer.render(context, mouseX, mouseY, delta);
        }

        // Render widgets (buttons from tab renderers)
        super.render(context, mouseX, mouseY, delta);

        // Render sidebar tooltips AFTER everything else (on top)
        renderSidebarTooltips(context, mouseX, mouseY);
    }

    private int getSidebarStartY() {
        if (tabList == null) return 0;
        int totalHeight = tabList.size() * ICON_SLOT_HEIGHT;
        return (this.height - totalHeight) / 2;
    }

    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        // Sidebar background
        context.fill(0, 0, SIDEBAR_WIDTH, this.height, SIDEBAR_BG_COLOR);

        // Right border line
        context.fill(SIDEBAR_WIDTH - 1, 0, SIDEBAR_WIDTH, this.height, SIDEBAR_BORDER_COLOR);

        // Render each tab icon
        if (tabList == null) return;
        int startY = getSidebarStartY();
        for (int i = 0; i < tabList.size(); i++) {
            CompendiumTab tab = tabList.get(i);
            int iconX = (SIDEBAR_WIDTH - ICON_SIZE) / 2;
            int iconY = startY + i * ICON_SLOT_HEIGHT;

            boolean isActive = tab.id().equals(activeTabId);
            boolean isHovered = mouseX >= 0 && mouseX < SIDEBAR_WIDTH
                    && mouseY >= iconY && mouseY < iconY + ICON_SIZE;

            // Tab background highlight
            if (isActive) {
                context.fill(0, iconY - 2, SIDEBAR_WIDTH - 1, iconY + ICON_SIZE + 2, TAB_SELECTED_BG);
                // Active indicator: Vanilla Gold line on the left edge
                context.fill(0, iconY - 2, 2, iconY + ICON_SIZE + 2, 0xFFFFAA00);
            } else if (isHovered) {
                context.fill(0, iconY - 2, SIDEBAR_WIDTH - 1, iconY + ICON_SIZE + 2, TAB_HOVER_BG);
            }

            // Draw icon
            context.drawGuiTexture(tab.iconTexture(), iconX, iconY, ICON_SIZE, ICON_SIZE);
        }
    }

    private void renderSidebarTooltips(DrawContext context, int mouseX, int mouseY) {
        if (tabList == null) return;
        int startY = getSidebarStartY();
        for (int i = 0; i < tabList.size(); i++) {
            int iconY = startY + i * ICON_SLOT_HEIGHT;
            if (mouseX >= 0 && mouseX < SIDEBAR_WIDTH && mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, tabList.get(i).tooltip(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check sidebar clicks first
        if (mouseX >= 0 && mouseX < SIDEBAR_WIDTH && tabList != null) {
            int startY = getSidebarStartY();
            for (int i = 0; i < tabList.size(); i++) {
                CompendiumTab tab = tabList.get(i);
                int iconY = startY + i * ICON_SLOT_HEIGHT;

                if (mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                    switchToTab(tab.id());
                    return true;
                }
            }
        }

        // Let widgets handle clicks
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Delegate to active tab renderer (for content area)
        if (activeRenderer != null && mouseX >= contentX) {
            return activeRenderer.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (activeRenderer != null) {
            return activeRenderer.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (activeRenderer != null && mouseX >= contentX) {
            return activeRenderer.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (activeRenderer != null) {
            return activeRenderer.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle tab-switching keybinds
        if (ModKeybinds.OPEN_PASSIVE_TREE.matchesKey(keyCode, scanCode)) {
            if (CompendiumTabRegistry.PASSIVE_TREE_TAB_ID.equals(activeTabId)) {
                this.close(); // Already on passive tree → close
            } else {
                switchToTab(CompendiumTabRegistry.PASSIVE_TREE_TAB_ID);
            }
            return true;
        }

        if (ModKeybinds.OPEN_WORLD_TREE.matchesKey(keyCode, scanCode)) {
            if (CompendiumTabRegistry.WORLD_TREE_TAB_ID.equals(activeTabId)) {
                this.close(); // Already on world tree → close
            } else {
                switchToTab(CompendiumTabRegistry.WORLD_TREE_TAB_ID);
            }
            return true;
        }

        if (ModKeybinds.OPEN_COMPENDIUM.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }

        if (MinecraftClient.getInstance().options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                if (client.interactionManager != null && client.interactionManager.getCurrentGameMode() == net.minecraft.world.GameMode.CREATIVE) {
                    client.setScreen(new net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen(client.player, client.player.networkHandler.getEnabledFeatures(), client.options.getOperatorItemsTab().getValue()));
                } else {
                    client.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(client.player));
                }
            }
            return true;
        }

        // Delegate to active tab renderer
        if (activeRenderer != null && activeRenderer.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }
}
