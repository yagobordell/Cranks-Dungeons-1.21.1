package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.ModKeybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared Compendium navigation and tab viewport. */
public class CompendiumScreen extends Screen {

    private static final int MARGIN = 8;
    private static final int TOP = 34;
    private static final int HEADER_Y = 8;
    private static final int HEADER_HEIGHT = 21;
    private static final int CLOSE_SIZE = 17;
    private static final int RAIL_STEP = 37;

    private int contentX, contentY, contentW, contentH;
    private int railWidth;
    private int firstRailTab;

    private Identifier activeTabId;
    private CompendiumTabRenderer activeRenderer;
    public boolean openWikiReference(com.cranks.dungeons.client.guide.GuideTarget target){
        if(activeRenderer instanceof CompendiumGuideTabRenderer guide){guide.openReference(target);return true;}return false;
    }

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

        railWidth = width >= 390 ? 76 : 35;
        contentX = MARGIN + railWidth + 8;
        contentY = TOP;
        contentW = Math.max(1, width - contentX - MARGIN);
        contentH = Math.max(1, height - contentY - MARGIN);

        if (tabList == null) {
            tabList = CompendiumTabRegistry.getTabs();
            for (CompendiumTab tab : tabList) {
                renderers.computeIfAbsent(tab.id(), id -> tab.createRenderer());
            }
        }

        if (!renderers.containsKey(activeTabId) && !tabList.isEmpty()) {
            activeTabId = tabList.get(0).id();
        }

        activeRenderer = renderers.get(activeTabId);
        if (activeRenderer != null) {
            activeRenderer.init(this, contentX, contentY, contentW, contentH);
        }

        int visible = visibleRailTabs();
        firstRailTab = Math.max(0, Math.min(firstRailTab, tabList.size() - visible));
        for (int index = firstRailTab; index < Math.min(tabList.size(), firstRailTab + visible); index++) {
            RailButton button = new RailButton(tabList.get(index), TOP + (index - firstRailTab) * RAIL_STEP);
            addDrawableChild(button);
        }

        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Close"), button -> close())
                .crossButton().dimensions(closeX(), HEADER_Y + 2, CLOSE_SIZE, CLOSE_SIZE).build())
                .setTooltip(Tooltip.of(Text.literal("Close")));
    }

    private int closeX() {
        return width - MARGIN - CLOSE_SIZE - 3;
    }

    private int visibleRailTabs() {
        return Math.max(1, (height - TOP - MARGIN + 3) / RAIL_STEP);
    }

    private final class RailButton extends ClickableWidget {
        private final CompendiumTab tab;

        private RailButton(CompendiumTab tab, int y) {
            super(MARGIN, y, railWidth, RAIL_STEP - 3, tab.tooltip());
            this.tab = tab;
            setTooltip(Tooltip.of(tab.tooltip()));
        }

        @Override
        public void onClick(double x, double y) {
            switchToTab(tab.id());
        }

        @Override
        public boolean keyPressed(int key, int scanCode, int modifiers) {
            if (active && visible && KeyCodes.isToggle(key)) {
                playDownSound(MinecraftClient.getInstance().getSoundManager());
                switchToTab(tab.id());
                return true;
            }
            return false;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean selected = tab.id().equals(activeTabId);
            CompendiumStyle.drawNavigationPlaque(context, getX(), getY(), getWidth(), getHeight(),
                    selected, isHovered() || isFocused());
            int center = getX() + getWidth() / 2;
            CompendiumStyle.drawTabIcon(context, tab, center - 8, getY() + (railWidth > 35 ? 2 : 9));
            if (railWidth > 35) {
                String label = tab.id().equals(CompendiumTabRegistry.GUIDE_TAB_ID) ? "Compendium" : tab.tooltip().getString();
                context.drawCenteredTextWithShadow(textRenderer, textRenderer.trimToWidth(label, getWidth() - 10),
                        center, getY() + 20, selected ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_MUTED);
            }
        }
    }

    /** Add a widget to the screen on behalf of a tab renderer. */
    public <T extends ClickableWidget> T addTabWidget(T widget) {
        return this.addDrawableChild(widget);
    }

    /** Register input and narration for a widget drawn by the tab's overlay. */
    <T extends ClickableWidget> T addOverlayWidget(T widget) {
        return this.addSelectableChild(widget);
    }

    void addTreeButtons(int x, int y, int width, CompendiumButtonWidget.Builder... buttons) {
        int step = Math.min(105, (width - 16) / buttons.length);
        int left = x + (width - (step * buttons.length - 5)) / 2;
        for (var button : buttons) {
            addTabWidget(button.dimensions(left, y, step - 5, 20).build());
            left += step;
        }
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
        super.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0x55000000);

        CompendiumStyle.drawPanel(context, MARGIN, HEADER_Y, width - MARGIN * 2, HEADER_HEIGHT);
        context.fill(closeX() - 2, HEADER_Y + 4, closeX() - 1, HEADER_Y + HEADER_HEIGHT - 4,
                CompendiumStyle.DIVIDER);
        CompendiumStyle.drawTabIcon(context, CompendiumTabRegistry.getTab(CompendiumTabRegistry.GUIDE_TAB_ID), MARGIN + 5, 10);
        context.drawTextWithShadow(textRenderer, Text.literal("Compendium"), MARGIN + 26, 14, CompendiumStyle.TEXT);
        if (activeRenderer != null) {
            activeRenderer.render(context, mouseX, mouseY, delta);
        }
        super.render(context, mouseX, mouseY, delta);
        if (activeRenderer != null) {
            activeRenderer.renderOverlay(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
        if (tabList != null && tabList.size() > visibleRailTabs()
                && mouseX >= MARGIN && mouseX < MARGIN + railWidth && mouseY >= TOP && verticalAmount != 0) {
            int next = Math.clamp(firstRailTab - (int) Math.signum(verticalAmount), 0,
                    tabList.size() - visibleRailTabs());
            if (next != firstRailTab) {
                firstRailTab = next;
                refreshTab();
            }
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
        if (activeRenderer != null && activeRenderer.overlayKeyPressed(keyCode)) return true;

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
