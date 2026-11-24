package com.cranks.dungeons.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class InventoryStyle {
    public static final int SLOT_PITCH = 18;
    public static final int CELL_SIZE = SLOT_PITCH - 1;
    public static final int HUD_SLOT_SIZE = 20;

    public enum Panel { SOCKET, ARMOR, PORTRAIT, STORAGE, BAG_STORAGE, HOTBAR, STATS, JOURNAL }

    private static final int BLACK = 0xFF000000;
    private static final int RAIL = 0xFF808080;
    private static final int CELL = 0xFF1A1A1A;
    private static final Identifier ATLAS = Identifier.of("cranks-dungeons", "textures/gui/inventory_trim.png");
    private static final int ATLAS_WIDTH = 256;
    private static final int ATLAS_HEIGHT = 128;

    private static final Sprite BAG_ICON = new Sprite(24, 45, 16, 16);
    private static final Sprite SWAP_ICON = new Sprite(0, 46, 8, 7);
    private static final Sprite LOCK_ICON = new Sprite(11, 46, 7, 8);

    private enum Corner {
        PRONG(0, 6, 0, 3, 1, 3, 1, 1, 4, 1, 4, 0, 8, 0),
        LATCH(0, 7, 0, 5, -1, 5, -1, 2, -2, 2, -2, 0, 3, 0, 3, -1, 6, -1, 6, 0, 9, 0),
        BEVEL(0, 7, 0, 4, 1, 4, 1, 2, 2, 2, 2, 1, 5, 1, 5, 0, 8, 0),
        SHOULDER(0, 8, 0, 5, -2, 5, -2, 3, 0, 3, 0, 1, 3, 1, 3, 0, 9, 0),
        SPLICE(0, 7, 0, 4, -1, 4, -1, 1, 2, 1, 2, -1, 5, -1, 5, 0, 9, 0);

        private final int[] points;

        Corner(int... points) {
            this.points = points;
        }
    }

    private static final int[] CORNER_PRONG = {1, 3, -1, 3, -1, 1, -2, 1};
    private static final int[] LOWER_CORNER = {
            -9, 0, -6, 0, -6, -1, -3, -1, -3, 0, -1, 0, -1, -3, 0, -3, 0, -7
    };
    private static final int[] SIDE_CLASP = {
            0, -4, 0, -2, -1, -2, -1, 0, 0, 0, 0, 2, 1, 2, 1, 3, 0, 3, 0, 4
    };
    private static final int[] PORTRAIT_CROWN = {
            -13, 0, -10, 0, -10, 1, -7, 1, -7, 2, -4, 2, -4, 0, 4, 0,
            4, 2, 7, 2, 7, 1, 10, 1, 10, 0, 13, 0
    };
    private static final int[] PORTRAIT_FOOT = {
            -12, 0, -9, 0, -9, 1, -5, 1, -5, 3, -2, 3, -2, 2, 2, 2,
            2, 3, 5, 3, 5, 1, 9, 1, 9, 0, 12, 0
    };
    private static final int[] STORAGE_FOOT = {
            -24, 0, -17, 0, -17, 1, -12, 1, -12, 2, -7, 2, -7, 0, -3, 0, -3, -1,
            1, -1, 1, 0, 8, 0, 8, 2, 13, 2, 13, 1, 18, 1, 18, 0, 24, 0
    };
    private static final int[] BAG_FOOT = {
            -34, 0, -28, 0, -28, 1, -21, 1, -21, 2, -16, 2, -16, 1, -12, 1, -12, 0,
            -7, 0, -7, 1, -5, 1, -5, 3, -2, 3, -2, 4, 2, 4, 2, 3, 5, 3,
            5, 1, 7, 1, 7, 0, 13, 0, 13, 2, 19, 2, 19, 1, 25, 1, 25, 0, 32, 0
    };
    private static final int[] HOTBAR_LEFT = {
            0, 5, 0, 1, 1, 1, 1, -1, 4, -1, 4, -3, 8, -3, 8, -2, 11, -2, 11, 0, 15, 0
    };
    private static final int[] HOTBAR_RIGHT = {
            -15, 0, -11, 0, -11, -1, -8, -1, -8, -3, -5, -3, -5, -1, -2, -1, -2, 1, 0, 1, 0, 5
    };
    private static final int[] RULE_TOP = {-12, 0, -6, 0, -6, 1, -3, 1, -3, 0, 3, 0, 3, 1, 6, 1, 6, 0, 12, 0};
    private static final int[] RULE_BOTTOM = {-12, 4, -6, 4, -6, 3, -3, 3, -3, 4, 3, 4, 3, 3, 6, 3, 6, 4, 12, 4};
    private static final int[] RULE_LINK = {0, 0, 0, 4};

    private record Sprite(int u, int v, int width, int height) {
        void draw(DrawContext context, int x, int y) {
            drawAtlas(context, x, y, u, v, width, height);
        }
    }

    private InventoryStyle() {}

    private static void drawAtlas(DrawContext context, int x, int y, int u, int v, int width, int height) {
        context.drawTexture(ATLAS, x, y, u, v, width, height, ATLAS_WIDTH, ATLAS_HEIGHT);
    }

    public static void drawCell(DrawContext context, int x, int y, boolean enabled) {
        context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, enabled ? CELL : 0xFF131313);
    }

    public static void drawBagIcon(DrawContext context, int x, int y) {
        BAG_ICON.draw(context, x, y);
    }

    public static void drawLockIcon(DrawContext context, int x, int y) {
        LOCK_ICON.draw(context, x + (CELL_SIZE - LOCK_ICON.width) / 2,
                y + (CELL_SIZE - LOCK_ICON.height + 1) / 2);
    }

    public static void drawSwapIcon(DrawContext context, int x, int y, int width, int height) {
        // The sprite's one-pixel shadow extends to the right of the visible arrows.
        SWAP_ICON.draw(context, x + (width - SWAP_ICON.width + 1) / 2, y + (height - SWAP_ICON.height) / 2);
    }

    public static void drawSocket(DrawContext context, int x, int y, int width, int height, boolean hovered) {
        drawSocket(context, x, y, width, height, hovered, Corner.SHOULDER);
    }

    public static void drawBagSocket(DrawContext context, int x, int y, int size, boolean hovered, int index) {
        Corner corner = switch (index % 3) {
            case 0 -> Corner.PRONG;
            case 1 -> Corner.LATCH;
            default -> Corner.BEVEL;
        };
        drawSocket(context, x, y, size, size, hovered, corner);
    }

    private static void drawSocket(DrawContext context, int x, int y, int width, int height, boolean hovered,
                                   Corner corner) {
        drawFrame(context, x, y, width, height, hovered ? 0xFF282828 : CELL, Panel.SOCKET, corner);
    }

    public static void drawPanel(DrawContext context, int x, int y, int width, int height, boolean active, Panel panel) {
        int surface = panel == Panel.STATS || panel == Panel.JOURNAL ? 0xFF090909 : BLACK;
        Corner corner = switch (panel) {
            case BAG_STORAGE, STATS -> Corner.SPLICE;
            case STORAGE, JOURNAL -> Corner.BEVEL;
            case ARMOR -> Corner.LATCH;
            default -> Corner.PRONG;
        };
        drawFrame(context, x, y, width, height, surface, panel, corner);
        if (active && panel == Panel.ARMOR) {
            int middle = x + width / 2;
            context.fill(middle - 1, y + height + 1, middle + 2, y + height + 2, RAIL);
        }
    }

    private static void drawFrame(DrawContext context, int x, int y, int width, int height, int surface,
                                  Panel panel, Corner corner) {
        if (width < 4 || height < 4) return;
        int right = x + width - 1;
        int bottom = y + height - 1;
        int middle = x + width / 2;
        int[] foot = switch (panel) {
            case PORTRAIT -> PORTRAIT_FOOT;
            case BAG_STORAGE -> BAG_FOOT;
            case STORAGE -> STORAGE_FOOT;
            default -> null;
        };
        context.fill(x + 1, y + 1, right, bottom, surface);
        if (foot != null) fillOverhang(context, middle, bottom, foot, false, surface);
        if (panel == Panel.HOTBAR) {
            fillOverhang(context, x, y, HOTBAR_LEFT, true, surface);
            fillOverhang(context, right, y, HOTBAR_RIGHT, true, surface);
        }
        drawRails(context, x, y, right, bottom, middle, panel, corner, foot, BLACK, 1);
        drawRails(context, x, y, right, bottom, middle, panel, corner, foot, RAIL, 0);
    }

    private static void drawRails(DrawContext context, int x, int y, int right, int bottom, int middle,
                                  Panel panel, Corner corner, int[] foot, int color, int padding) {
        boolean hotbar = panel == Panel.HOTBAR;
        boolean upperCorner = right - x >= 13 && bottom - y >= 12;
        boolean lowerCorner = upperCorner && (panel == Panel.JOURNAL || panel == Panel.STATS || panel == Panel.PORTRAIT);
        int topStart = x;
        int topEnd = right;
        int leftStart = y;
        int rightStart = y;
        int rightEnd = bottom;
        int bottomEnd = right;
        if (hotbar) {
            path(context, x, y, HOTBAR_LEFT, color, padding);
            path(context, right, y, HOTBAR_RIGHT, color, padding);
            topStart = x + 15;
            topEnd = right - 15;
            leftStart = rightStart = y + 5;
        } else if (upperCorner) {
            path(context, x, y, corner.points, color, padding);
            if (corner == Corner.PRONG) path(context, x, y, CORNER_PRONG, color, padding);
            topStart = x + corner.points[corner.points.length - 2];
            leftStart = y + corner.points[1];
        }

        if (panel == Panel.PORTRAIT) {
            horizontalWithTrim(context, topStart, topEnd, y, middle, PORTRAIT_CROWN, color, padding);
        } else {
            rail(context, topStart, y, topEnd, y, color, padding);
        }
        if (lowerCorner) {
            path(context, right, bottom, LOWER_CORNER, color, padding);
            rightEnd = bottom - 7;
            bottomEnd = right - 9;
        }
        if (foot != null) {
            horizontalWithTrim(context, x, bottomEnd, bottom, middle, foot, color, padding);
        } else {
            rail(context, x, bottom, bottomEnd, bottom, color, padding);
        }
        rail(context, right, rightStart, right, rightEnd, color, padding);

        boolean sideClasp = panel == Panel.ARMOR || panel == Panel.STATS || panel == Panel.PORTRAIT
                || panel == Panel.JOURNAL && bottom - y >= 74;
        if (sideClasp) {
            int hinge = panel == Panel.ARMOR ? bottom - 4 : y + (bottom - y) * 3 / 4;
            rail(context, x, leftStart, x, hinge - 4, color, padding);
            path(context, x, hinge, SIDE_CLASP, color, padding);
            rail(context, x, hinge + 4, x, bottom, color, padding);
        } else {
            rail(context, x, leftStart, x, bottom, color, padding);
        }
    }

    public static void drawDivider(DrawContext context, int left, int right, int y) {
        int middle = (left + right) / 2;
        for (int padding = 1; padding >= 0; padding--) {
            int color = padding == 1 ? BLACK : RAIL;
            horizontalWithTrim(context, left, right, y, middle, RULE_TOP, color, padding);
            rail(context, left, y + 4, middle - 12, y + 4, color, padding);
            path(context, middle, y, RULE_BOTTOM, color, padding);
            rail(context, middle + 12, y + 4, right, y + 4, color, padding);
            path(context, middle, y, RULE_LINK, color, padding);
        }
    }

    private static void horizontalWithTrim(DrawContext context, int left, int right, int y,
                                           int middle, int[] trim, int color, int padding) {
        rail(context, left, y, middle + trim[0], y, color, padding);
        path(context, middle, y, trim, color, padding);
        rail(context, middle + trim[trim.length - 2], y, right, y, color, padding);
    }

    private static void fillOverhang(DrawContext context, int x, int y, int[] path, boolean above, int surface) {
        for (int i = 0; i + 3 < path.length; i += 2) {
            int depth = path[i + 1];
            if (depth == path[i + 3] && (above ? depth < 0 : depth > 0)) {
                context.fill(x + path[i], y + Math.min(0, depth), x + path[i + 2] + 1,
                        y + Math.max(0, depth) + 1, surface);
            }
        }
    }

    private static void path(DrawContext context, int x, int y, int[] points, int color, int padding) {
        for (int i = 0; i + 3 < points.length; i += 2) {
            rail(context, x + points[i], y + points[i + 1], x + points[i + 2], y + points[i + 3], color, padding);
        }
    }

    private static void rail(DrawContext context, int x1, int y1, int x2, int y2, int color, int padding) {
        context.fill(Math.min(x1, x2) - padding, Math.min(y1, y2) - padding, Math.max(x1, x2) + 1 + padding,
                Math.max(y1, y2) + 1 + padding, color);
    }

    public static void drawHudSlot(DrawContext context, int x, int y, int index) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            drawAtlas(context, x, y, (index % 9) * HUD_SLOT_SIZE, 68, HUD_SLOT_SIZE, HUD_SLOT_SIZE);
        } finally {
            RenderSystem.disableBlend();
        }
    }

    public static void drawHudNumber(DrawContext context, int x, int y, int index) {
        int digit = (index + 1) % 10;
        drawAtlas(context, x + 14, y + 12, digit * 3, 91, 3, 5);
    }

    public static void drawHudSelection(DrawContext context, int x, int y) {
        int left = x - 1, right = x + HUD_SLOT_SIZE;
        int top = y - 2, bottom = y + HUD_SLOT_SIZE + 1;
        drawSelectionCorner(context, left, top, 1, 1);
        drawSelectionCorner(context, right, top, -1, 1);
        drawSelectionCorner(context, left, bottom, 1, -1);
        drawSelectionCorner(context, right, bottom, -1, -1);
    }

    private static void drawSelectionCorner(DrawContext context, int x, int y, int dx, int dy) {
        int armX = x + 3 * dx, armY = y + dy;
        int l = Math.min(x, armX), r = Math.max(x, armX);
        int t = Math.min(y, armY), b = Math.max(y, armY);
        context.fill(l - 1, y - 1, r + 2, y + 2, BLACK);
        context.fill(x - 1, t - 1, x + 2, b + 2, BLACK);
        context.fill(l, y, r + 1, y + 1, 0xFFFFFFFF);
        context.fill(x, t, x + 1, b + 1, 0xFFFFFFFF);
    }
}
