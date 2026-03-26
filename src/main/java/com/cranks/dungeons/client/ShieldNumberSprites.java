package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.util.Identifier;

/**
 * Registry for shield row counter number sprites.
 * Each number is a separate texture file: 1x.png, 2x.png, etc.
 * Place these files in: assets/cranks-dungeons/textures/gui/sprites/hud/font/
 * (GUI sprites must be in the gui/sprites directory!)
 */
public class ShieldNumberSprites {
    
    public static final int MAX_ROWS = 10;
    
    /**
     * Gets the texture identifier for a specific row count (e.g., "3x")
     * GUI sprites automatically look in: textures/gui/sprites/
     */
    public static Identifier getSpriteId(int row) {
        if (row < 1) row = 1;
        if (row > MAX_ROWS) row = MAX_ROWS;
        return Identifier.of(CranksDungeons.MOD_ID, "hud/font/" + row + "x");
    }
    
    /**
     * Checks if a sprite exists for the given row count
     */
    public static boolean hasSprite(int row) {
        return row >= 1 && row <= MAX_ROWS;
    }
    
    /**
     * Called during client initialization to verify sprite availability.
     * The textures are automatically loaded by Minecraft if they exist in the correct location:
     * assets/cranks-dungeons/textures/gui/sprites/hud/font/1x.png, 2x.png, etc.
     */
    public static void register() {
        // Textures must be in: textures/gui/sprites/hud/font/
    }
}
