package com.cranks.dungeons.client.party;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.ShieldNumberSprites;
import com.cranks.dungeons.config.ModConfig;
import com.cranks.dungeons.party.PartyMemberData;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.List;

public class PartyHudRenderer implements HudRenderCallback {

    private static final Identifier HEART_FULL = Identifier.of("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.of("hud/heart/half");
    private static final Identifier HEART_CONTAINER = Identifier.of("hud/heart/container");
    private static final Identifier HEART_CONTAINER_BLINKING = Identifier.of("hud/heart/container_blinking");
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");
    private static final Identifier SHIELD_CONTAINER = Identifier.of("hud/heart/container");

    private static final boolean OVERFLOWING_BARS_LOADED = FabricLoader.getInstance().isModLoaded("overflowingbars");

    private static final int BASE_MEMBER_WIDTH = 110;
    private static final int BASE_MEMBER_HEIGHT = 36;

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (client.options.hudHidden) return;

        ModConfig config = ModConfig.get();
        if (!config.partyHudEnabled) return;

        if (!PartyClientData.isInParty()) return;

        List<PartyMemberData> members = PartyClientData.getMembers();
        
        // Filter out self if config says so
        if (!config.partyHudShowSelf) {
            members.removeIf(m -> m.uuid().equals(client.player.getUuid()));
        }
        
        if (members.isEmpty()) return;
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        int x = config.partyHudX;
        int y = config.partyHudY;
        
        if (x < 0) x = 5;
        
        float scale = config.partyHudSize;
        int maxRows = config.partyHudMaxRows;
        int maxColumns = config.partyHudMaxColumns;
        
        int columnCount = (members.size() + maxRows - 1) / maxRows;
        columnCount = Math.min(columnCount, maxColumns);
        
        for (int i = 0; i < members.size(); i++) {
            PartyMemberData member = members.get(i);
            int col = i / maxRows;
            int row = i % maxRows;
            
            int memberX = x + (int) (col * (BASE_MEMBER_WIDTH + 5) * scale);
            int memberY = y + (int) (row * (BASE_MEMBER_HEIGHT + 2) * scale);
            
            renderMember(context, client, member, memberX, memberY, scale);
        }
    }
    
    private void renderMember(DrawContext context, MinecraftClient client, PartyMemberData member, int x, int y, float scale) {
        boolean isLeader = PartyClientData.isLeader(member.uuid());
        boolean isSelf = member.uuid().equals(client.player.getUuid());
        
        String displayName = isSelf ? "You" : member.name();
        if (displayName.length() > 12) {
            displayName = displayName.substring(0, 11) + "...";
        }
        
        int nameColor = isLeader ? 0xFFFFD700 : 0xFFFFFFFF;
        if (!member.isAlive()) {
            nameColor = 0xFF888888;
        }
        
        // Layout:
        // [Name]
        // [Icon (16x16)] [Shield]
        //                 [Health]

        int maxHealthHearts = (int) Math.ceil(member.maxHealth() / 2.0f);
        int healthRows = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, (int) Math.ceil(maxHealthHearts / 10.0));
        int healthVanillaSpacing = Math.max(10 - (healthRows - 2), 3);
        int healthRowHeight = (int) (healthVanillaSpacing * scale);

        // Height of the health bar from its base (y) to its top row.
        int healthTotalHeight = (healthRows - 1) * healthRowHeight;

        // The base Y for the health bar. We'll place it slightly below the starting 'y' 
        // to leave room for everything else.
        int healthBaseY = y + (int) (12 * scale);
        
        // The shield should be above the top-most health row.
        // Vanilla standard gap between bars is 10 pixels.
        int shieldBaseY = healthBaseY - healthTotalHeight - (int) (10 * scale);

        // Name should be above the highest bar (either name bar or shield bar).
        // Max shield rows calculation for name placement
        int maxShieldHearts = (int) Math.ceil(member.maxEnergyShield() / 2.0f);
        int shieldRows = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, (int) Math.ceil(maxShieldHearts / 10.0));
        int shieldVanillaSpacing = Math.max(10 - (shieldRows - 2), 3);
        int shieldRowHeight = (int) (shieldVanillaSpacing * scale);
        int shieldTotalHeight = (shieldRows > 0 && member.maxEnergyShield() > 0) ? (shieldRows - 1) * shieldRowHeight : 0;
        
        int highestBarY = (member.maxEnergyShield() > 0) ? (shieldBaseY - shieldTotalHeight) : (healthBaseY - healthTotalHeight);
        int lowestBarY = healthBaseY + (int) (9 * scale);
        int barsMidpointY = (highestBarY + lowestBarY) / 2;
        int iconY = barsMidpointY - (int) (8 * scale); // Center 16x16 icon on midpoint

        int nameY = highestBarY - (int) (10 * scale);

        // Draw name above everything
        context.drawTextWithShadow(client.textRenderer, displayName, x + (int) (2 * scale), nameY, nameColor);

        // Icon on left side - dynamically centered vertically relative to bars
        renderPlayerHead(context, client, member, x + (int) (2 * scale), iconY, scale);

        if (member.isAlive()) {
            int barsX = x + (int) (24 * scale); // Shift slightly right to give icon more breathing room
            renderShieldBar(context, barsX, shieldBaseY, member, scale);
            renderHealthBar(context, barsX, healthBaseY, member, scale);
        } else {
            // Center the "Dead" text too if needed, but keeping fixed for now as per layout
            context.drawTextWithShadow(client.textRenderer, "☠ Dead", x + (int) (24 * scale), y + (int) (5 * scale), 0xAA0000);
        }
    }
    
    private void renderPlayerHead(DrawContext context, MinecraftClient client, PartyMemberData member, int x, int y, float scale) {
        boolean isSelf = member.uuid().equals(client.player.getUuid());
        boolean isLeader = PartyClientData.isLeader(member.uuid());

        int size = (int) (16 * scale);

        // Try to render player skin using the proper approach from Wynntils
        boolean skinRendered = false;
        try {
            var playerListEntry = client.player.networkHandler.getPlayerListEntry(member.uuid());
            if (playerListEntry != null) {
                var skinTextures = playerListEntry.getSkinTextures();
                if (skinTextures != null) {
                    // Get the texture path from the body texture
                    Identifier texture = skinTextures.body().texturePath();
                    
                    if (texture != null) {
                        // Render face (base layer): UV (8, 8) to (16, 16) on 64x64 texture
                        context.drawTexture(
                            RenderPipelines.GUI_TEXTURED,
                            texture,
                            x, y,           // screen position
                            8, 8,           // UV start (face region)
                            size, size,     // size on screen
                            8, 8,           // UV region size
                            64, 64          // texture size
                        );
                        
                        // Render hat layer (overlay): UV (40, 8) to (48, 16) on 64x64 texture
                        context.drawTexture(
                            RenderPipelines.GUI_TEXTURED,
                            texture,
                            x, y,           // screen position (same as face)
                            40, 8,          // UV start (hat region)
                            size, size,     // size on screen
                            8, 8,           // UV region size
                            64, 64          // texture size
                        );
                        
                        skinRendered = true;
                    }
                }
            }
        } catch (Exception e) {
            // Skin rendering failed, use fallback
            CranksDungeons.LOGGER.debug("Failed to render skin for {}: {}", member.name(), e.getMessage());
        }

        if (skinRendered) {
            return;
        }

        // Fallback: draw colored square with initial
        context.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF000000);

        String initial = member.name().substring(0, 1).toUpperCase();
        int textWidth = client.textRenderer.getWidth(initial);
        int textX = x + size / 2 - textWidth / 2;
        int textY = y + size / 2 - 4;

        int bgColor;
        if (isLeader) {
            bgColor = 0xFF8B4513;
        } else if (isSelf) {
            bgColor = 0xFF2E5AAC;
        } else {
            bgColor = 0xFF555555;
        }

        context.fill(x, y, x + size, y + size, bgColor);
        context.drawTextWithShadow(client.textRenderer, initial, textX, textY, 0xFFFFFFFF);
    }
    
    // ─── Health Hearts ───────────────────────────────────────────────────

    private void renderHealthBar(DrawContext context, int x, int y, PartyMemberData member, float scale) {
        float maxHealth = member.maxHealth();
        float health = member.health();
        
        if (maxHealth <= 0) return;

        int maxHearts = (int) Math.ceil(maxHealth / 2.0f);
        int totalFullHearts = (int) (health / 2.0f);
        boolean hasHalf = (health % 2.0f) >= 1.0f;
        boolean isFlashing = PartyClientData.isDamageFlashing(member.uuid()) || PartyClientData.isHealFlashing(member.uuid());

        int heartSize = (int) (9 * scale);
        int spacing = (int) (8 * scale);

        if (OVERFLOWING_BARS_LOADED) {
            renderHealthBarStacked(context, x, y, maxHearts, totalFullHearts, hasHalf, isFlashing, heartSize, spacing, scale);
        } else {
            renderHealthBarAllRows(context, x, y, maxHearts, totalFullHearts, hasHalf, isFlashing, heartSize, spacing, scale);
        }
    }

    /**
     * Without Overflowing Bars: render all heart rows, just like vanilla does for
     * the player's own health bar.
     */
    private void renderHealthBarAllRows(DrawContext context, int x, int y,
                                        int maxHearts, int fullHearts, boolean hasHalf,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int totalRows = (int) Math.ceil(maxHearts / 10.0);
        int vanillaSpacing = Math.max(10 - (totalRows - 2), 3);
        int scaledRowSpacing = (int) (vanillaSpacing * scale);

        for (int i = 0; i < maxHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * spacing;
            int heartY = y - row * scaledRowSpacing;

            // Container
            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : HEART_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, container, heartX, heartY, heartSize, heartSize);

            // Fill
            if (i < fullHearts) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_FULL, heartX, heartY, heartSize, heartSize);
            } else if (i == fullHearts && hasHalf) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_HALF, heartX, heartY, heartSize, heartSize);
            }
        }
    }

    /**
     * With Overflowing Bars: stack hearts into a single row with an Nx indicator,
     * mirroring the Overflowing Bars mod behaviour for the player's own HUD.
     */
    private void renderHealthBarStacked(DrawContext context, int x, int y,
                                        int maxHearts, int fullHearts, boolean hasHalf,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int totalRows = (int) Math.ceil(maxHearts / 10.0);

        // Determine which row the current health falls on (1-indexed)
        int currentRow;
        if (fullHearts <= 0 && !hasHalf) {
            currentRow = 1; // empty, show row 1
        } else {
            int topFilledIndex = hasHalf ? fullHearts : Math.max(0, fullHearts - 1);
            currentRow = (topFilledIndex / 10) + 1;
        }

        int heartsInFullRowsBelow = (currentRow - 1) * 10;
        int heartsInThisRow = Math.min(10, maxHearts - heartsInFullRowsBelow);
        int filledInRow = Math.max(0, fullHearts - heartsInFullRowsBelow);
        boolean halfInRow = hasHalf && (fullHearts >= heartsInFullRowsBelow) && (fullHearts < heartsInFullRowsBelow + heartsInThisRow);

        for (int i = 0; i < heartsInThisRow; i++) {
            int heartX = x + i * spacing;

            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : HEART_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, container, heartX, y, heartSize, heartSize);

            if (i < filledInRow) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_FULL, heartX, y, heartSize, heartSize);
            } else if (i == filledInRow && halfInRow) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_HALF, heartX, y, heartSize, heartSize);
            }
        }

        // Nx sprite indicator when on row 2+ - using the custom texture font
        if (currentRow > 1) {
            int spriteX = x + heartsInThisRow * spacing;
            int spriteY = y - (int) (2 * scale); // Adjusted down by 2 pixels for better alignment
            Identifier spriteId = ShieldNumberSprites.getSpriteId(currentRow);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, spriteId, spriteX, spriteY, (int) (16 * scale), (int) (16 * scale));
        }
    }

    // ─── Shield Hearts ───────────────────────────────────────────────────

    private void renderShieldBar(DrawContext context, int x, int y, PartyMemberData member, float scale) {
        float maxShield = member.maxEnergyShield();
        float shield = member.energyShield();
        
        if (maxShield <= 0) return;

        int maxHearts = (int) Math.ceil(maxShield / 2.0f);
        int totalFullHearts = (int) (shield / 2.0f);
        boolean hasHalf = (shield % 2.0f) >= 1.0f;
        boolean isFlashing = PartyClientData.isDamageFlashing(member.uuid())
                || PartyClientData.isHealFlashing(member.uuid())
                || PartyClientData.isShieldFlashing(member.uuid());

        int heartSize = (int) (9 * scale);
        int spacing = (int) (8 * scale);

        if (OVERFLOWING_BARS_LOADED) {
            renderShieldBarStacked(context, x, y, maxHearts, totalFullHearts, hasHalf, isFlashing, heartSize, spacing, scale);
        } else {
            renderShieldBarAllRows(context, x, y, maxHearts, totalFullHearts, hasHalf, isFlashing, heartSize, spacing, scale);
        }
    }

    /**
     * Without Overflowing Bars: render all shield rows normally.
     */
    private void renderShieldBarAllRows(DrawContext context, int x, int y,
                                        int maxHearts, int fullHearts, boolean hasHalf,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int totalRows = (int) Math.ceil(maxHearts / 10.0);
        int vanillaSpacing = Math.max(10 - (totalRows - 2), 3);
        int scaledRowSpacing = (int) (vanillaSpacing * scale);

        for (int i = 0; i < maxHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * spacing;
            int heartY = y - row * scaledRowSpacing;

            // Container
            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : SHIELD_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, container, heartX, heartY, heartSize, heartSize);

            // Fill
            if (i < fullHearts) {
                Identifier tex = isFlashing ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, heartX, heartY, heartSize, heartSize);
            } else if (i == fullHearts && hasHalf) {
                Identifier tex = isFlashing ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, heartX, heartY, heartSize, heartSize);
            }
        }
    }

    /**
     * With Overflowing Bars: stack shields into a single row with an Nx sprite,
     * mirroring the player's own energy shield HUD (EnergyShieldHudRenderer).
     */
    private void renderShieldBarStacked(DrawContext context, int x, int y,
                                        int maxHearts, int fullHearts, boolean hasHalf,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int totalRows = (int) Math.ceil(maxHearts / 10.0);

        int currentRow;
        if (fullHearts <= 0 && !hasHalf) {
            currentRow = 1;
        } else {
            int topFilledIndex = hasHalf ? fullHearts : Math.max(0, fullHearts - 1);
            currentRow = (topFilledIndex / 10) + 1;
        }

        int heartsInFullRowsBelow = (currentRow - 1) * 10;
        int heartsInThisRow = Math.min(10, maxHearts - heartsInFullRowsBelow);
        int filledInRow = Math.max(0, fullHearts - heartsInFullRowsBelow);
        boolean halfInRow = hasHalf && (fullHearts >= heartsInFullRowsBelow) && (fullHearts < heartsInFullRowsBelow + heartsInThisRow);

        for (int i = 0; i < heartsInThisRow; i++) {
            int heartX = x + i * spacing;

            // Container
            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : SHIELD_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, container, heartX, y, heartSize, heartSize);

            if (i < filledInRow) {
                Identifier tex = isFlashing ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, heartX, y, heartSize, heartSize);
            } else if (i == filledInRow && halfInRow) {
                Identifier tex = isFlashing ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, heartX, y, heartSize, heartSize);
            }
        }

        // Nx sprite indicator when on row 2+ - matching health style with texture font
        if (currentRow > 1 && maxHearts > 10) {
            int spriteX = x + heartsInThisRow * spacing;
            int spriteY = y - (int) (2 * scale); // Adjusted down by 2 pixels for better alignment
            Identifier spriteId = ShieldNumberSprites.getSpriteId(currentRow);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, spriteId, spriteX, spriteY, (int) (16 * scale), (int) (16 * scale));
        }
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new PartyHudRenderer());
    }
}
