package com.cranks.dungeons.client.compendium;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared visual language for the Compendium.
 *
 * <p>The interactive pieces deliberately use vanilla GUI sprites. Besides
 * keeping the screen familiar, this means a resource pack can restyle the
 * buttons, tabs, and scrollbars without the mod having to know about the
 * pack's art direction.</p>
 */
public final class CompendiumStyle {

    public static final Identifier VANILLA_BUTTON = Identifier.ofVanilla("widget/button");
    public static final Identifier VANILLA_BUTTON_HIGHLIGHTED = Identifier.ofVanilla("widget/button_highlighted");
    public static final Identifier VANILLA_BUTTON_DISABLED = Identifier.ofVanilla("widget/button_disabled");
    public static final Identifier VANILLA_CROSS_BUTTON = Identifier.ofVanilla("widget/cross_button");
    public static final Identifier VANILLA_CROSS_BUTTON_HIGHLIGHTED = Identifier.ofVanilla("widget/cross_button_highlighted");
    public static final Identifier VANILLA_POPUP_BACKGROUND = Identifier.ofVanilla("popup/background");
    public static final Identifier VANILLA_ADVANCEMENTS_WINDOW =
            Identifier.ofVanilla("textures/gui/advancements/window.png");
    public static final Identifier VANILLA_CREATIVE_TAB_TOP_UNSELECTED =
            Identifier.ofVanilla("textures/gui/sprites/container/creative_inventory/tab_top_unselected_2.png");
    public static final Identifier VANILLA_CREATIVE_TAB_TOP_SELECTED =
            Identifier.ofVanilla("textures/gui/sprites/container/creative_inventory/tab_top_selected_2.png");
    public static final Identifier VANILLA_SCROLLER = Identifier.ofVanilla("widget/scroller");
    public static final Identifier VANILLA_SCROLLER_BACKGROUND = Identifier.ofVanilla("widget/scroller_background");
    // Vanilla GUI colors, kept in one place so every Compendium tab agrees.
    public static final int SCREEN_DIM = 0x66000000;
    public static final int VANILLA_WINDOW_BACKGROUND = 0xFFC6C6C6;
    public static final int HEADER_BACKGROUND = 0xFF646464;
    public static final int PANEL_BACKGROUND = 0xFF858585;
    public static final int PANEL_INNER = 0xFF707070;
    public static final int PANEL_BORDER = 0xFF202020;
    public static final int PANEL_HIGHLIGHT = 0xFFB0B0B0;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_DARK = 0xFF202020;
    public static final int TEXT_MUTED = 0xFFAAAAAA;
    public static final int TEXT_DISABLED = 0xFF555555;
    public static final int TEXT_GOLD = 0xFFFFAA00;
    public static final int TEXT_GOLD_LIGHT = 0xFFFFFF55;
    public static final int TEXT_WARNING = 0xFFFF5555;
    public static final int TEXT_SUCCESS = 0xFF55FF55;

    // Vanilla formatting colors adapted for the tree's state indicators.
    public static final int NODE_LOCKED_INNER = 0xFF202020;
    public static final int NODE_LOCKED_OUTER = 0xFF555555;
    public static final int NODE_HOVER_INNER = 0xFF304444;
    public static final int NODE_HOVER_OUTER = 0xFF55FFFF;
    public static final int NODE_DRAFT_INNER = 0xFF44351C;
    public static final int NODE_DRAFT_OUTER = 0xFFAA5500;
    public static final int NODE_UNLOCKED_INNER = 0xFF443C1C;
    public static final int NODE_UNLOCKED_OUTER = TEXT_GOLD;
    public static final int NODE_AVAILABLE_INNER = 0xFF244024;
    public static final int NODE_AVAILABLE_OUTER = 0xFF00AA00;

    private static final int ADVANCEMENTS_WINDOW_TEXTURE_WIDTH = 256;
    private static final int ADVANCEMENTS_WINDOW_TEXTURE_HEIGHT = 256;
    private static final int ADVANCEMENTS_WINDOW_WIDTH = 252;
    private static final int ADVANCEMENTS_WINDOW_HEIGHT = 140;
    private static final int ADVANCEMENTS_WINDOW_HEADER_HEIGHT = 18;
    private static final int ADVANCEMENTS_WINDOW_FOOTER_HEIGHT = 9;
    private static final int ADVANCEMENTS_WINDOW_SIDE_WIDTH = 9;
    private static final int CREATIVE_TAB_TEXTURE_WIDTH = 26;
    private static final int CREATIVE_TAB_TEXTURE_HEIGHT = 32;
    private static final int CREATIVE_TAB_SIDE_WIDTH = 5;

    private CompendiumStyle() {}

    private static final String TREE_NODE_TEXTURE_ROOT = "passive/nodes/";
    private static final Map<String, Identifier> TREE_NODE_TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, Identifier> TREE_NODE_SELECTION_CACHE = new HashMap<>();
    private static final Map<String, Identifier> TREE_CONNECTION_TEXTURE_CACHE = new HashMap<>();

    public static Identifier treeNodeTexture(String shape, String state) {
        String safeShape = switch (shape == null ? "square" : shape.toLowerCase()) {
            case "circle", "diamond", "hexagon" -> shape.toLowerCase();
            default -> "square";
        };
        String key = safeShape + "_" + state;
        return TREE_NODE_TEXTURE_CACHE.computeIfAbsent(key,
                ignored -> Identifier.of("cranks-dungeons", TREE_NODE_TEXTURE_ROOT + key));
    }

    public static Identifier treeNodeSelectionTexture(String shape) {
        String safeShape = switch (shape == null ? "square" : shape.toLowerCase()) {
            case "circle", "diamond", "hexagon" -> shape.toLowerCase();
            default -> "square";
        };
        return TREE_NODE_SELECTION_CACHE.computeIfAbsent(safeShape,
                ignored -> Identifier.of("cranks-dungeons", TREE_NODE_TEXTURE_ROOT + safeShape + "_selection"));
    }

    public static Identifier treeConnectionTexture(String state) {
        return TREE_CONNECTION_TEXTURE_CACHE.computeIfAbsent(state,
                ignored -> Identifier.of("cranks-dungeons", TREE_NODE_TEXTURE_ROOT + "connection_" + state));
    }

    public static void drawTreeNodeTexture(DrawContext context, Identifier texture,
                                           double centerX, double centerY, double size) {
        int drawSize = snapTreePixelSize(size);
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) centerX, (float) centerY, 0.0f);
        context.drawGuiTexture(texture, -drawSize / 2, -drawSize / 2, drawSize, drawSize);
        matrices.pop();
    }

    /**
     * Keep pixel-art sprites on whole GUI pixels. Even dimensions also keep
     * the center of a 32x32 sprite symmetrical around a node's center.
     */
    private static int snapTreePixelSize(double size) {
        int pixelSize = Math.max(1, Math.min(64, (int) Math.round(size)));
        if (pixelSize >= 4 && (pixelSize & 1) != 0) {
            pixelSize++;
        }
        return pixelSize;
    }

    public static void drawTreeConnectionTexture(DrawContext context, Identifier texture,
                                                 float x1, float y1, float x2, float y2,
                                                 double zoom, float startInset, float endInset) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.0f) return;

        float ux = dx / length;
        float uy = dy / length;
        float trimmedLength = length - startInset - endInset;
        if (trimmedLength <= 0.0f) return;
        x1 += ux * startInset;
        y1 += uy * startInset;
        x2 -= ux * endInset;
        y2 -= uy * endInset;

        float pixelScale = (float) Math.sqrt(Math.max(0.01, zoom));
        int localLength = Math.max(1, (int) Math.ceil(trimmedLength / pixelScale));
        int tileCount = Math.max(1, (localLength + 7) / 8);
        boolean farZoom = zoom < 0.6;
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x1, y1, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.atan2(dy, dx)));
        // Keep the one-pixel center of the line readable at far zoom. The
        // horizontal scale still follows the line's screen-space length, so
        // this does not change its endpoints or its normal-zoom appearance.
        matrices.scale(farZoom ? pixelScale * localLength : pixelScale,
                farZoom ? 1.0f : pixelScale, 1.0f);
        if (farZoom) {
            // Nodes are at most a few screen pixels tall here. Use one source
            // row so the connection never becomes visually thicker than its
            // node while retaining one quad per connection. The hovered sprite
            // intentionally has a transparent center row, so use its visible
            // highlight row for the far-zoom representation.
            boolean highlight = texture.getPath().endsWith("connection_hovered");
            int sourceRow = highlight ? 0 : 1;
            context.drawGuiTexture(texture, 8, 3, 0, sourceRow,
                    0, 0, 1, 1);
        } else {
            for (int tile = 0; tile < tileCount; tile++) {
                int tileX = tile * 8;
                int sourceWidth = Math.min(8, localLength - tileX);
                if (sourceWidth <= 0) break;
                // Keep each tile at the source's 8:3 aspect ratio. The final
                // tile uses a clipped source region, so the line cannot protrude
                // beyond the node after zooming.
                context.drawGuiTexture(texture, 8, 3, 0, 0,
                        tileX, -1, sourceWidth, 3);
            }
        }
        matrices.pop();
    }

    /** Draws a restrained vanilla-style container with a one-pixel frame. */
    public static void drawPanel(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        context.fill(x, y, x + width, y + height, PANEL_BORDER);
        if (width > 2 && height > 2) {
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BACKGROUND);
        }
        if (width > 4 && height > 4) {
            context.fill(x + 2, y + 2, x + width - 2, y + 3, PANEL_HIGHLIGHT);
        }
    }

    /** Draws one continuous vanilla Advancements-style window. */
    public static void drawAdvancementWindow(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;

        int side = Math.min(ADVANCEMENTS_WINDOW_SIDE_WIDTH, width / 2);
        int header = Math.min(ADVANCEMENTS_WINDOW_HEADER_HEIGHT, height);
        int footer = Math.min(ADVANCEMENTS_WINDOW_FOOTER_HEIGHT, Math.max(0, height - header));
        int bodyHeight = height - header - footer;
        int bodyWidth = width - side * 2;

        // Vanilla inventory and menu screens use this light gray surface
        // beneath their texture frames. It keeps the window opaque without
        // adding a custom texture or a tiled decorative background.
        if (bodyWidth > 0 && bodyHeight > 0) {
            context.fill(x + side, y + header,
                    x + side + bodyWidth, y + header + bodyHeight,
                    VANILLA_WINDOW_BACKGROUND);
        }

        // Keep the title strip, side fades, and bottom strip from the actual
        // vanilla window. Its center stays transparent, just like the raw
        // Advancements frame when no category background is supplied.
        drawWindowSlice(context, x + side, y, bodyWidth, header,
                side, 0, ADVANCEMENTS_WINDOW_WIDTH - side * 2, header);
        drawWindowSlice(context, x + side, y + height - footer, bodyWidth, footer,
                side, ADVANCEMENTS_WINDOW_HEIGHT - footer,
                ADVANCEMENTS_WINDOW_WIDTH - side * 2, footer);
        drawWindowSlice(context, x, y + header, side, bodyHeight,
                0, header, side, ADVANCEMENTS_WINDOW_HEIGHT - header - footer);
        drawWindowSlice(context, x + width - side, y + header, side, bodyHeight,
                ADVANCEMENTS_WINDOW_WIDTH - side, header,
                side, ADVANCEMENTS_WINDOW_HEIGHT - header - footer);

        // Preserve the four native corners, which carry the crisp white and
        // black pixels that make the Advancements window read correctly.
        drawWindowSlice(context, x, y, side, header, 0, 0, side, header);
        drawWindowSlice(context, x + width - side, y,
                side, header, ADVANCEMENTS_WINDOW_WIDTH - side, 0, side, header);
        drawWindowSlice(context, x, y + height - footer,
                side, footer, 0, ADVANCEMENTS_WINDOW_HEIGHT - footer, side, footer);
        drawWindowSlice(context, x + width - side, y + height - footer,
                side, footer, ADVANCEMENTS_WINDOW_WIDTH - side, ADVANCEMENTS_WINDOW_HEIGHT - footer,
                side, footer);
    }

    private static void drawWindowSlice(DrawContext context, int x, int y, int width, int height,
                                        int u, int v, int regionWidth, int regionHeight) {
        if (width <= 0 || height <= 0) return;
        context.drawTexture(VANILLA_ADVANCEMENTS_WINDOW, x, y, width, height, u, v,
                regionWidth, regionHeight,
                ADVANCEMENTS_WINDOW_TEXTURE_WIDTH, ADVANCEMENTS_WINDOW_TEXTURE_HEIGHT);
    }

    /** Draws an inset area used for scrollable lists and pages. */
    public static void drawInsetPanel(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        context.fill(x, y, x + width, y + height, PANEL_BORDER);
        if (width > 2 && height > 2) {
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_INNER);
        }
        if (width > 3 && height > 3) {
            context.fill(x + 1, y + 1, x + width - 1, y + 2, PANEL_BORDER);
            context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, PANEL_HIGHLIGHT);
        }
    }

    public static void drawVanillaButton(DrawContext context, int x, int y, int width, int height,
                                          boolean hovered, boolean active) {
        Identifier texture = !active ? VANILLA_BUTTON_DISABLED
                : hovered ? VANILLA_BUTTON_HIGHLIGHTED : VANILLA_BUTTON;
        context.drawGuiTexture(texture, x, y, width, height);
    }

    /** Draws a non-interactive menu surface from the vanilla widget palette. */
    public static void drawVanillaMenuPanel(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        context.drawGuiTexture(VANILLA_POPUP_BACKGROUND, x, y, width, height);
    }

    /** Draws a creative-inventory-style tab with a non-distorting center stretch. */
    public static void drawCreativeTab(DrawContext context, int x, int y, int width, int height,
                                       boolean hovered, boolean selected) {
        Identifier texture = selected
                ? VANILLA_CREATIVE_TAB_TOP_SELECTED
                : VANILLA_CREATIVE_TAB_TOP_UNSELECTED;
        int side = Math.min(CREATIVE_TAB_SIDE_WIDTH, width / 2);
        int centerWidth = width - side * 2;
        int sourceCenterWidth = CREATIVE_TAB_TEXTURE_WIDTH - side * 2;

        drawCreativeTabSlice(context, texture, x, y, side, height,
                0, 0, side, CREATIVE_TAB_TEXTURE_HEIGHT);
        drawCreativeTabSlice(context, texture, x + side, y, centerWidth, height,
                side, 0, sourceCenterWidth, CREATIVE_TAB_TEXTURE_HEIGHT);
        drawCreativeTabSlice(context, texture, x + width - side, y, side, height,
                CREATIVE_TAB_TEXTURE_WIDTH - side, 0, side, CREATIVE_TAB_TEXTURE_HEIGHT);
    }

    private static void drawCreativeTabSlice(DrawContext context, Identifier texture,
                                             int x, int y, int width, int height,
                                             int u, int v, int regionWidth, int regionHeight) {
        if (width <= 0 || height <= 0) return;
        context.drawTexture(texture, x, y, width, height, u, v,
                regionWidth, regionHeight,
                CREATIVE_TAB_TEXTURE_WIDTH, CREATIVE_TAB_TEXTURE_HEIGHT);
    }

    public static void drawVanillaCrossButton(DrawContext context, int x, int y, int size,
                                              boolean hovered, boolean active) {
        Identifier texture = !active ? VANILLA_CROSS_BUTTON
                : hovered ? VANILLA_CROSS_BUTTON_HIGHLIGHTED : VANILLA_CROSS_BUTTON;
        context.drawGuiTexture(texture, x, y, size, size);
    }

    public static void drawVanillaScrollbar(DrawContext context, int x, int y, int width, int height,
                                            int thumbY, int thumbHeight) {
        context.drawGuiTexture(VANILLA_SCROLLER_BACKGROUND, x, y, width, height);
        context.drawGuiTexture(VANILLA_SCROLLER, x, thumbY, width, thumbHeight);
    }
}
