package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.InventoryStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Shared pixel-art journal surfaces and widget colors. */
public final class CompendiumStyle {

    public static final int TEXT = 0xFFF0F0F2;
    public static final int TEXT_MUTED = 0xFFAAAAAF;
    public static final int TEXT_DISABLED = 0xFF88888E;
    public static final int TEXT_GOLD = 0xFFE3CB90;
    public static final int TEXT_GOLD_LIGHT = 0xFFF0DCA8;
    public static final int TEXT_WARNING = 0xFFF4A1A1;
    public static final int TEXT_SUCCESS = 0xFFABD2AD;
    static final int DIVIDER = 0xFF696969;
    static final int RECESSED = 0xFF090909;
    private static final int FRAME = 0xFF929292;
    private static final int SILVER = 0xFFB9B9B9;
    private static final int PANEL = 0xFF191919;
    private static final int KEYLINE = 0xFF000000;

    private CompendiumStyle() {}

    private static final String TREE_NODE_TEXTURE_ROOT = "passive/nodes/";
    private static final Map<String, Identifier> TREE_NODE_TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, Identifier> TREE_NODE_SELECTION_CACHE = new HashMap<>();
    private static final Map<String, Identifier> TREE_CONNECTION_TEXTURE_CACHE = new HashMap<>();
    private static final String[] JOURNAL_ICON_NAMES = {
            "journal", "constellation", "sapling", "lantern", "banner", "blades", "shield", "anvil",
            "sword", "chestplate", "compass", "horseshoe", "scroll", "search", "close",
            "ranger_bow", "witch_staff", "death_knight_helm"
    };
    private static final Identifier[] JOURNAL_ICONS = java.util.Arrays.stream(JOURNAL_ICON_NAMES)
            .map(name -> Identifier.of("cranks-dungeons", "compendium/" + name))
            .toArray(Identifier[]::new);
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

    public static void drawPanel(DrawContext context, int x, int y, int width, int height) {
        InventoryStyle.drawPanel(context, x, y, width, height, false, InventoryStyle.Panel.JOURNAL);
    }

    private static void pixel(DrawContext context, int x, int y, int color) {
        context.fill(x, y, x + 1, y + 1, color);
    }

    private static void frame(DrawContext context, int x, int y, int width, int height, int surface, int frame) {
        if (width < 5 || height < 5) return;
        int right = x + width;
        int bottom = y + height;
        context.fill(x - 1, y - 1, right + 1, bottom + 1, KEYLINE);
        context.fill(x + 2, y + 2, right - 2, bottom - 2, surface);
        context.fill(x, y, right, y + 1, frame);
        context.fill(x, bottom - 1, right, bottom, frame);
        context.fill(x, y + 1, x + 1, bottom - 1, frame);
        context.fill(right - 1, y + 1, right, bottom - 1, frame);
    }

    public static void drawJournalIcon(DrawContext context, int index, int x, int y) {
        drawSprite(context, JOURNAL_ICONS[index], x, y, 16, 16);
    }

    private static void drawSprite(DrawContext context, Identifier sprite, int x, int y, int width, int height) {
        var matrices = context.getMatrices();
        matrices.push();
        try {
            matrices.translate(0, 0, 1);
            context.drawGuiTexture(sprite, x, y, width, height);
        } finally {
            matrices.pop();
        }
    }

    public static void drawTabIcon(DrawContext context, CompendiumTab tab, int x, int y) {
        if (tab != null && tab.id().equals(CompendiumTabRegistry.PASSIVE_TREE_TAB_ID)) {
            drawJournalIcon(context, 1, x, y);
        } else if (tab != null && tab.id().equals(CompendiumTabRegistry.WORLD_TREE_TAB_ID)) {
            drawJournalIcon(context, 2, x, y);
        } else if (tab == null || tab.id().equals(CompendiumTabRegistry.GUIDE_TAB_ID)) {
            drawJournalIcon(context, 0, x, y);
        } else {
            Identifier sprite = tab.iconTexture();
            Identifier resource = sprite.withPath(path -> "textures/gui/sprites/" + path + ".png");
            if (MinecraftClient.getInstance().getResourceManager().getResource(resource).isPresent()) {
                context.drawGuiTexture(sprite, x, y, 16, 16);
            } else {
                drawJournalIcon(context, 0, x, y);
            }
        }
    }

    public static void drawGuideFrame(DrawContext context, int x, int y, int width, int height, int bodyTop) {
        drawPanel(context, x, y, width, height);
        if (width >= 43 && bodyTop < y + height - 7) {
            InventoryStyle.drawDivider(context, x + 9, x + width - 10, bodyTop - 7);
        }
    }

    public static void drawRailPlaque(DrawContext context, int x, int y, int width, int height,
                                      boolean selected, boolean hovered) {
        int edge = selected ? SILVER : hovered ? FRAME : DIVIDER;
        int surface = selected ? 0xFF282828 : hovered ? 0xFF222222 : PANEL;
        frame(context, x, y, width, height, surface, edge);
        if (selected && width >= 10 && height >= 13) {
            context.fill(x + 1, y + 5, x + 2, y + height - 5, edge);
        }
    }

    public static void drawTextFieldFrame(DrawContext context, int x, int y, int width, int height, boolean focused) {
        frame(context, x, y, width, height, focused ? 0xFF282828 : PANEL, focused ? SILVER : DIVIDER);
    }

    static void drawNavigationPlaque(DrawContext context, int x, int y, int width, int height,
                                     boolean selected, boolean hovered) {
        InventoryStyle.drawSocket(context, x, y, width, height, selected || hovered);
        if (selected && height >= 18) context.fill(x + 2, y + 10, x + 3, y + height - 5, SILVER);
    }

    static void drawToolbarControl(DrawContext context, int x, int y, int width, int height, boolean highlighted) {
        if (highlighted) context.fill(x, y, x + width, y + height, 0xFF252525);
    }

    public static void drawChapterCard(DrawContext context, int x, int y, int width, int height,
                                       int icon, boolean hovered) {
        drawRailPlaque(context, x, y, width, height, false, hovered);
        frame(context, x + 5, y + 10, 22, 22, RECESSED, FRAME);
        drawJournalIcon(context, icon, x + 8, y + 13);
    }

    public static void drawTopicStrip(DrawContext context, int x, int y, int width, int height, boolean hovered) {
        drawRailPlaque(context, x, y, width, height, false, hovered);
        context.fill(x + 22, y + 4, x + 23, y + height - 4, DIVIDER);
        int right = x + width - 9;
        int middle = y + height / 2;
        pixel(context, right, middle - 2, SILVER);
        pixel(context, right + 1, middle - 1, SILVER);
        pixel(context, right + 2, middle, SILVER);
        pixel(context, right + 1, middle + 1, SILVER);
        pixel(context, right, middle + 2, SILVER);
    }

    public static void drawGuideScrollbar(DrawContext context, int x, int y, int height, int thumbY, int thumbHeight) {
        context.fill(x, y, x + 4, y + height, 0xFF242429);
        context.fill(x, thumbY, x + 4, thumbY + thumbHeight, FRAME);
        context.fill(x + 1, thumbY, x + 2, thumbY + thumbHeight, SILVER);
    }

    public static void drawInsetPanel(DrawContext context, int x, int y, int width, int height) {
        frame(context, x, y, width, height, PANEL, DIVIDER);
    }

    static void drawTreePoints(DrawContext context, TextRenderer font,
                               int centerX, int y, int points) {
        String label = "Unspent Points: " + points;
        int width = font.getWidth(label) + 16;
        drawRailPlaque(context, centerX - width / 2, y - 4, width, 17, false, false);
        context.drawCenteredTextWithShadow(font, label, centerX, y, points > 0 ? TEXT_GOLD : TEXT);
    }
}
