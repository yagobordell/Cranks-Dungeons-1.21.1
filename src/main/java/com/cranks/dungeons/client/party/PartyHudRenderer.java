package com.cranks.dungeons.client.party;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.ModConfig;
import com.cranks.dungeons.party.PartyMemberData;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
        // [Icon (16x16)] [Shield] Nx
        //                 [Health] Nx

        int iconSize = (int) (16 * scale);
        int textY = y - (int) (10 * scale);
        
        // Draw name above icon
        context.drawTextWithShadow(client.textRenderer, displayName, x + (int) (2 * scale), textY, nameColor);

        // Icon on left side
        renderPlayerHead(context, client, member, x + (int) (2 * scale), y + (int) (2 * scale), scale);

        if (member.isAlive()) {
            // Shield on top, Health below - both to the right of icon
            int barsX = x + (int) (22 * scale);
            renderShieldBar(context, barsX, y, member, scale);
            renderHealthBar(context, barsX, y + (int) (11 * scale), member, scale);
        } else {
            context.drawTextWithShadow(client.textRenderer, "☠ Dead", x + (int) (22 * scale), y + (int) (5 * scale), 0xAA0000);
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
    
    private void renderHealthBar(DrawContext context, int x, int y, PartyMemberData member, float scale) {
        float maxHealth = member.maxHealth();
        float health = member.health();
        
        if (maxHealth <= 0) return;
        
        int maxHearts = (int) Math.ceil(maxHealth / 2.0f);
        
        // Calculate which row contains current health (1-indexed)
        int currentHeart = Math.max(1, (int) Math.ceil(health / 2.0f));
        int currentRow = (currentHeart - 1) / 10 + 1;
        int totalRows = (maxHearts - 1) / 10 + 1;
        
        // Hearts in complete rows above current
        int heartsInFullRows = (currentRow - 1) * 10;
        
        // How many hearts to show in this row (up to 10, but not more than max)
        int heartsInThisRow = Math.min(10, maxHearts - heartsInFullRows);
        
        // Calculate filled hearts in this row
        int totalFullHearts = (int) (health / 2.0f);
        int fullHeartsInRow = Math.max(0, totalFullHearts - heartsInFullRows);
        
        // Check for half heart in this row
        boolean hasHalfInRow = false;
        if (health % 2.0f >= 1.0f) {
            int halfHeartPosition = totalFullHearts + 1;
            hasHalfInRow = halfHeartPosition > heartsInFullRows && halfHeartPosition <= heartsInFullRows + heartsInThisRow;
        }
        
        // Check for damage or heal flash
        boolean isFlashing = PartyClientData.isDamageFlashing(member.uuid()) || PartyClientData.isHealFlashing(member.uuid());

        int heartSize = (int) (9 * scale);
        int spacing = (int) (8 * scale);

        // Draw only the hearts that exist in this row (no empty containers beyond max)
        for (int i = 0; i < heartsInThisRow; i++) {
            int heartX = x + i * spacing;

            // Draw container (use blinking texture when flashing)
            Identifier containerTexture = isFlashing ? HEART_CONTAINER_BLINKING : HEART_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, y, heartSize, heartSize);

            // Draw filled or half heart
            if (i < fullHeartsInRow) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_FULL, heartX, y, heartSize, heartSize);
            } else if (i == fullHeartsInRow && hasHalfInRow) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_HALF, heartX, y, heartSize, heartSize);
            }
        }

        // Draw row indicator (e.g., "2x") at the end if not first row
        if (currentRow > 1) {
            String rowIndicator = (currentRow - 1) + "x";
            int indicatorX = x + heartsInThisRow * spacing + 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                rowIndicator, indicatorX, y, 0xFFAAAAAA);
        }
    }
    
    private void renderShieldBar(DrawContext context, int x, int y, PartyMemberData member, float scale) {
        float maxShield = member.maxEnergyShield();
        float shield = member.energyShield();
        
        if (maxShield <= 0) return;
        
        int maxHearts = (int) Math.ceil(maxShield / 2.0f);
        
        // Calculate which row contains current shield (1-indexed)
        int currentHeart = Math.max(1, (int) Math.ceil(shield / 2.0f));
        int currentRow = (currentHeart - 1) / 10 + 1;
        int totalRows = (maxHearts - 1) / 10 + 1;
        
        // Hearts in complete rows above current
        int heartsInFullRows = (currentRow - 1) * 10;
        
        // How many hearts to show in this row (up to 10, but not more than max)
        int heartsInThisRow = Math.min(10, maxHearts - heartsInFullRows);
        
        // Calculate filled hearts in this row
        int totalFullHearts = (int) (shield / 2.0f);
        int fullHeartsInRow = Math.max(0, totalFullHearts - heartsInFullRows);
        
        // Check for half heart in this row
        boolean hasHalfInRow = false;
        if (shield % 2.0f >= 1.0f) {
            int halfHeartPosition = totalFullHearts + 1;
            hasHalfInRow = halfHeartPosition > heartsInFullRows && halfHeartPosition <= heartsInFullRows + heartsInThisRow;
        }

        // Check for damage, heal, or shield flash
        boolean isFlashing = PartyClientData.isDamageFlashing(member.uuid()) || PartyClientData.isHealFlashing(member.uuid()) || PartyClientData.isShieldFlashing(member.uuid());

        int heartSize = (int) (9 * scale);
        int spacing = (int) (8 * scale);

        // Draw only the hearts that exist in this row (no empty containers beyond max)
        for (int i = 0; i < heartsInThisRow; i++) {
            int heartX = x + i * spacing;

            // Draw container (use blinking texture when flashing)
            Identifier containerTexture = isFlashing ? HEART_CONTAINER_BLINKING : SHIELD_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, y, heartSize, heartSize);

            // Draw filled or half heart (use blinking textures when flashing)
            if (i < fullHeartsInRow) {
                Identifier shieldTexture = isFlashing ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, shieldTexture, heartX, y, heartSize, heartSize);
            } else if (i == fullHeartsInRow && hasHalfInRow) {
                Identifier shieldTexture = isFlashing ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, shieldTexture, heartX, y, heartSize, heartSize);
            }
        }

        // Draw row indicator (e.g., "2x") at the end if not first row
        if (currentRow > 1) {
            String rowIndicator = (currentRow - 1) + "x";
            int indicatorX = x + heartsInThisRow * spacing + 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                rowIndicator, indicatorX, y, 0xFFAAAAAA);
        }
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new PartyHudRenderer());
    }
}
