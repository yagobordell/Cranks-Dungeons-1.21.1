package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Inline node filtering shared by the two trees. */
final class TreeNodeSearch {
    static final int MATCH_TINT = 0xFFFFDB86;
    private final Supplier<Map<String, PassiveNode>> nodes;
    private final boolean primaryAttributeChoices;
    private CompendiumScreen parent;
    private TextFieldWidget field;
    private BooleanSupplier canFocus;
    private TreeNodeSearchIndex index;
    private Set<String> matches = Set.of();
    private String query = "";
    private int indexedSize;
    private int x, y, width;

    TreeNodeSearch(Supplier<Map<String, PassiveNode>> nodes, boolean primaryAttributeChoices) {
        this.nodes = nodes;
        this.primaryAttributeChoices = primaryAttributeChoices;
    }

    static int widthFor(int contentWidth) {
        return Math.clamp(contentWidth - 186, 74, 148);
    }

    void init(CompendiumScreen parent, int contentX, int contentY, int contentWidth, BooleanSupplier canFocus) {
        this.parent = parent;
        this.canFocus = canFocus;
        this.width = widthFor(contentWidth);
        this.x = contentX + contentWidth - 111 - width;
        this.y = contentY + 5;
        this.index = null;
        refreshMatches();

        field = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                x + 20, y + 6, width - 25, 12, Text.literal("Find node")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (!TreeNodeSearch.this.contains(mouseX, mouseY)) return false;
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && canFocus.getAsBoolean()) {
                    super.mouseClicked(mouseX, mouseY, button);
                }
                return canFocus.getAsBoolean();
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                CompendiumStyle.drawRailPlaque(context, TreeNodeSearch.this.x, TreeNodeSearch.this.y,
                        TreeNodeSearch.this.width, 20, false,
                        isFocused() || TreeNodeSearch.this.contains(mouseX, mouseY));
                CompendiumStyle.drawJournalIcon(context, 13, TreeNodeSearch.this.x + 3, TreeNodeSearch.this.y + 2);
                super.renderWidget(context, mouseX, mouseY, delta);
            }
        };
        field.setMaxLength(100);
        field.setDrawsBackground(false);
        field.setPlaceholder(Text.literal("Find node…"));
        field.setTooltip(Tooltip.of(Text.literal("Search node names, stats, descriptions, and IDs (Ctrl+F)")));
        field.setText(query);
        field.setChangedListener(value -> {
            query = value;
            refreshMatches();
        });
        parent.addTabWidget(field);
    }

    boolean contains(double mouseX, double mouseY) {
        return field != null && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 20;
    }

    boolean keyPressed(int key) {
        if (field == null) return false;
        if (key == GLFW.GLFW_KEY_F && Screen.hasControlDown()) {
            if (!canFocus.getAsBoolean()) return false;
            parent.setFocused(field);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && isFocused()) {
            blur();
            return true;
        }
        return false;
    }

    void blur() {
        if (isFocused()) parent.setFocused(null);
    }

    boolean isFocused() {
        return field != null && parent.getFocused() == field;
    }

    void detach() {
        blur();
        field = null;
    }

    Set<String> matches() {
        if (index != null && indexedSize != nodes.get().size()) {
            index = null;
            refreshMatches();
        }
        return matches;
    }

    private void refreshMatches() {
        if (query.isBlank()) {
            matches = Set.of();
            return;
        }
        if (index == null) {
            var entries = nodes.get();
            indexedSize = entries.size();
            index = new TreeNodeSearchIndex(entries.values(), primaryAttributeChoices);
        }
        matches = index.find(query);
    }
}
