package com.cranks.dungeons.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.screen.PlayerScreenHandler;

/** Low-resolution monochrome hints for empty inputs; actual item renders use their own textures. */
public final class StationSlotHints {
    public enum Kind { EQUIPMENT, WHETSTONE, SCRAP, SIGIL, MATERIAL, BOTTLE, MAP }
    private static final int GRAY = 0xff555555;
    // Native 16-pixel silhouettes and structural edges traced from the item sprites.
    // The whetstone source is a doubled 32-pixel sprite, sampled at its native pixel grid.
    private static final String[] WHETSTONE = {
            "................", "................", "..........##....", ".......###..#...",
            "....###......#..", "..##..........#.", ".#............#.", ".#............#.",
            ".#...........##.", "#.#.......###..#", "#..#...###.....#", ".#..###......##.",
            "..#.......###...", "...#...###......", "....###.........", "................"
    };
    private static final String[] SCRAP = {
            "................", ".........##.....", ".......##..#....", ".....##.....#...",
            "...##........#..", "..#...........#.", ".#..........##..", "..#.......##....",
            "...##...##..#...", "..#..###.....#..", ".#..........#...", "..#........#....",
            "...#.....##.....", "....#..##.......", ".....##.........", "................"
    };
    private static final String[] SIGIL = {
            "................", "................", "................", ".........##.....",
            ".......##..#....", ".....##.....#...", "...##........#..", ".##..........##.",
            ".##........##.#.", ".#.#.....##..##.", "..#.#..##..##...", "...#.##..##.....",
            "....##.##.......", ".....##.........", "................", "................"
    };
    private StationSlotHints() {}
    public static void draw(DrawContext context, Kind kind, int x, int y) {
        if (kind == Kind.EQUIPMENT) {
            context.drawSprite(x, y, 0, 16, 16, MinecraftClient.getInstance()
                    .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                    .apply(PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE));
            return;
        }
        switch (kind) {
            case WHETSTONE -> {
                mask(context, x, y, WHETSTONE);
            }
            case SCRAP -> {
                mask(context, x, y, SCRAP);
            }
            case SIGIL -> {
                mask(context, x, y, SIGIL);
            }
            case BOTTLE -> {
                path(context, x, y, 6,2, 9,2, 9,6, 12,9, 12,13, 11,14, 4,14, 3,13, 3,9, 6,6, 6,2);
                path(context, x, y, 5,1, 10,1);
            }
            case MAP -> {
                path(context, x, y, 2,4, 5,2, 10,4, 13,2, 13,12, 10,14, 5,12, 2,14, 2,4);
                path(context, x, y, 5,2, 5,12); path(context, x, y, 10,4, 10,14);
            }
            default -> {
                path(context, x, y, 2,5, 7,2, 13,5, 13,11, 8,14, 2,11, 2,5);
                path(context, x, y, 2,5, 8,8, 13,5); path(context, x, y, 8,8, 8,14);
            }
        }
    }
    private static void mask(DrawContext context, int x, int y, String[] rows) {
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < rows[row].length(); column++) {
                if (rows[row].charAt(column) == '#') dot(context, x + column, y + row);
            }
        }
    }
    private static void dot(DrawContext context, int x, int y) { context.fill(x, y, x + 1, y + 1, GRAY); }
    private static void path(DrawContext context, int x, int y, int... points) {
        for (int i = 2; i < points.length; i += 2) {
            int ax = points[i-2], ay = points[i-1], bx = points[i], by = points[i+1];
            int dx = Math.abs(bx-ax), sx = ax < bx ? 1 : -1, dy = -Math.abs(by-ay), sy = ay < by ? 1 : -1;
            int error = dx+dy;
            while (true) {
                dot(context,x+ax,y+ay);
                if (ax == bx && ay == by) break;
                int e = error*2;
                if (e >= dy) { error += dy; ax += sx; }
                if (e <= dx) { error += dx; ay += sy; }
            }
        }
    }
}
