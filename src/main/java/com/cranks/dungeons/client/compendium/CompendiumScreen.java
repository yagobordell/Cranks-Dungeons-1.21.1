package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.ModKeybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Main Compendium screen with a vanilla-style header and shared content area. */
public class CompendiumScreen extends Screen {

    private static final int HEADER_HEIGHT = 32;
    private static final int HEADER_X = 4;
    private static final int HEADER_Y = 4;
    private static final int TAB_START_X = 10;
    private static final int COMPACT_TAB_START_X = 8;
    private static final int TAB_HEIGHT = 32;
    // Keep the tabs attached to the header while giving the frame a little
    // more breathing room above the content seam.
    private static final int TAB_Y = HEADER_Y + HEADER_HEIGHT - TAB_HEIGHT + 3;
    private static final int TAB_GAP = 3;

    /** Content bounds passed to the active tab renderer. */
    private int contentX, contentY, contentW, contentH;
    private int windowX, windowY, windowW, windowH;

    private Identifier activeTabId;
    private CompendiumTabRenderer activeRenderer;
    private final Map<Identifier, CompendiumTabRenderer> renderers = new LinkedHashMap<>();
    private List<CompendiumTab> tabList;
    private final List<ClickableWidget> backgroundWidgets = new ArrayList<>();

    /**
     * Opens the Compendium with a specific tab pre-selected.
     * @param initialTabId the tab to show first, or null for the default tab
     */
    public CompendiumScreen(@Nullable Identifier initialTabId) {
        super(Text.literal("Compendium"));
        this.activeTabId = initialTabId;
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.backgroundWidgets.clear();
        super.init();

        // The renderer receives the inside of one Advancements-style window;
        // the title strip and border stay owned by this screen.
        windowX = 8;
        windowY = HEADER_HEIGHT + 4;
        windowW = Math.max(1, this.width - 16);
        windowH = Math.max(1, this.height - windowY - 8);
        contentX = windowX + 9;
        contentY = windowY + 24;
        contentW = Math.max(1, windowW - 18);
        contentH = Math.max(1, windowH - 33);

        if (tabList == null) {
            tabList = CompendiumTabRegistry.getTabs();
            for (CompendiumTab tab : tabList) {
                renderers.computeIfAbsent(tab.id(), id -> tab.createRenderer());
            }
        }

        if (activeTabId == null && !tabList.isEmpty()) {
            activeTabId = tabList.get(0).id();
        }

        activeRenderer = renderers.get(activeTabId);
        if (activeRenderer != null) {
            activeRenderer.init(this, contentX, contentY, contentW, contentH);
        }

        this.addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .crossButton().dimensions(this.width - 28, 8, 20, 20).build());
    }

    /** Add a widget to the screen on behalf of a tab renderer. */
    public <T extends ClickableWidget> T addTabWidget(T widget) {
        return this.addDrawableChild(widget);
    }

    /**
     * Adds a widget that belongs behind the active renderer's content. These
     * widgets are rendered by the renderer at the appropriate layer instead
     * of by Screen's normal foreground widget pass.
     */
    public <T extends ClickableWidget> T addBackgroundTabWidget(T widget) {
        this.backgroundWidgets.add(widget);
        return widget;
    }

    public void renderBackgroundTabWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ClickableWidget widget : this.backgroundWidgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }

    public void switchToTab(Identifier tabId) {
        if (tabId.equals(activeTabId)) return;

        if (activeRenderer != null) {
            activeRenderer.onDeactivated();
        }

        activeTabId = tabId;
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));

        this.init();

        if (activeRenderer != null) {
            activeRenderer.onActivated();
        }
    }

    public void refreshTab() {
        this.init();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // The background is rendered explicitly at the start of render().
        // Keeping this hook empty prevents super.render() from blurring the
        // Compendium UI a second time before it draws the widgets.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Blur and darken the world first, then keep every Compendium element
        // crisp. Calling super.render() at the end is safe because the
        // override above prevents a second background pass.
        super.renderBackground(context, mouseX, mouseY, delta);

        // Draw the tabs before the panel so the panel's top frame covers their
        // lower edge. This makes them read as tabs emerging from behind it.
        renderHeader(context, mouseX, mouseY);
        CompendiumStyle.drawAdvancementWindow(context, windowX, windowY, windowW, windowH);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text windowTitle = activeTabId == null ? Text.literal("Compendium") : getActiveTabTitle();
        context.drawText(textRenderer, windowTitle, windowX + 8, windowY + 6,
                CompendiumStyle.TEXT_DARK, false);

        if (activeRenderer != null) {
            activeRenderer.render(context, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private Text getActiveTabTitle() {
        if (tabList != null) {
            for (CompendiumTab tab : tabList) {
                if (tab.id().equals(activeTabId)) return tab.tooltip();
            }
        }
        return Text.literal("Compendium");
    }

    private int getTabStartX() {
        return this.width < 520 ? COMPACT_TAB_START_X : TAB_START_X;
    }

    private int getTabWidth(CompendiumTab tab, TextRenderer textRenderer) {
        int minimumWidth = this.width < 520 ? 44 : 58;
        int idealWidth = textRenderer.getWidth(tab.tooltip()) + 20;
        int available = Math.max(minimumWidth, this.width - getTabStartX() - 34);
        int maxWidth = tabList == null || tabList.isEmpty()
                ? 130
                : Math.max(minimumWidth, (available - TAB_GAP * (tabList.size() - 1)) / tabList.size());
        // Creative inventory tabs are 26x32 natively. Their center is a flat
        // pixel surface, so the Minecraft-style tab can safely widen for a
        // text label without changing its border height or proportions.
        return Math.max(minimumWidth, Math.min(130, Math.min(idealWidth, maxWidth)));
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (tabList == null) return;
        int tabX = getTabStartX();
        for (int tabIndex = 0; tabIndex < tabList.size(); tabIndex++) {
            CompendiumTab tab = tabList.get(tabIndex);
            int tabWidth = getTabWidth(tab, textRenderer);
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabWidth
                    && mouseY >= TAB_Y && mouseY < TAB_Y + TAB_HEIGHT;
            boolean selected = tab.id().equals(activeTabId);
            CompendiumStyle.drawCreativeTab(context, tabX, TAB_Y, tabWidth, TAB_HEIGHT,
                    hovered, selected);
            String label = textRenderer.trimToWidth(tab.tooltip().getString(), Math.max(10, tabWidth - 12));
            context.drawCenteredTextWithShadow(textRenderer, label, tabX + tabWidth / 2,
                    TAB_Y + 12, CompendiumStyle.TEXT);
            tabX += tabWidth + TAB_GAP;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tabList != null && mouseY >= TAB_Y && mouseY < TAB_Y + TAB_HEIGHT) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int tabX = getTabStartX();
            for (CompendiumTab tab : tabList) {
                int tabWidth = getTabWidth(tab, textRenderer);
                if (mouseX >= tabX && mouseX < tabX + tabWidth) {
                    switchToTab(tab.id());
                    return true;
                }
                tabX += tabWidth + TAB_GAP;
            }
        }

        // Background tabs are not regular Screen children because they must
        // render behind the overview panel, so route their clicks explicitly.
        for (int i = this.backgroundWidgets.size() - 1; i >= 0; i--) {
            if (this.backgroundWidgets.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (activeRenderer != null && mouseX >= contentX && mouseX < contentX + contentW
                && mouseY >= contentY && mouseY < contentY + contentH) {
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
        if (activeRenderer != null && mouseX >= contentX && mouseX < contentX + contentW
                && mouseY >= contentY && mouseY < contentY + contentH) {
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
        // Let focused text inputs consume their own keystrokes before checking
        // global Compendium shortcuts such as inventory and tab keybinds.
        if (this.getFocused() instanceof TextFieldWidget) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (ModKeybinds.OPEN_PASSIVE_TREE.matchesKey(keyCode, scanCode)) {
            if (CompendiumTabRegistry.PASSIVE_TREE_TAB_ID.equals(activeTabId)) {
                this.close();
            } else {
                switchToTab(CompendiumTabRegistry.PASSIVE_TREE_TAB_ID);
            }
            return true;
        }

        if (ModKeybinds.OPEN_WORLD_TREE.matchesKey(keyCode, scanCode)) {
            if (CompendiumTabRegistry.WORLD_TREE_TAB_ID.equals(activeTabId)) {
                this.close();
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
                if (client.interactionManager != null
                        && client.interactionManager.getCurrentGameMode() == net.minecraft.world.GameMode.CREATIVE) {
                    client.setScreen(new net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen(
                            client.player, client.player.networkHandler.getEnabledFeatures(),
                            client.options.getOperatorItemsTab().getValue()));
                } else {
                    client.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(client.player));
                }
            }
            return true;
        }

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
