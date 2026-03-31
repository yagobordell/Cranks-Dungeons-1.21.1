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

    private static final int MEMBER_WIDTH = 110;
    private static final int MEMBER_HEIGHT = 36;
    private static final int COLUMN_SIZE = 5;


    
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
        if (y < 0) y = screenHeight / 2 - 100;
        
        int columnCount = (members.size() + COLUMN_SIZE - 1) / COLUMN_SIZE;
        columnCount = Math.min(columnCount, 2);
        
        for (int i = 0; i < members.size(); i++) {
            PartyMemberData member = members.get(i);
            int col = i / COLUMN_SIZE;
            int row = i % COLUMN_SIZE;
            
            int memberX = x + col * (MEMBER_WIDTH + 5);
            int memberY = y + row * (MEMBER_HEIGHT + 2);
            
            renderMember(context, client, member, memberX, memberY);
        }
    }
    
    private void renderMember(DrawContext context, MinecraftClient client, PartyMemberData member, int x, int y) {
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

        // Draw name above icon
        context.drawTextWithShadow(client.textRenderer, displayName, x + 2, y - 10, nameColor);

        // Icon on left side (16x16)
        renderPlayerHead(context, client, member, x + 2, y + 2);

        if (member.isAlive()) {
            // Shield on top, Health below - both to the right of icon (offset by 20 for 16px icon + spacing)
            renderShieldBar(context, x + 22, y, member);
            renderHealthBar(context, x + 22, y + 11, member);
        } else {
            context.drawTextWithShadow(client.textRenderer, "☠ Dead", x + 22, y + 5, 0xAA0000);
        }
    }
    
    private void renderPlayerHead(DrawContext context, MinecraftClient client, PartyMemberData member, int x, int y) {
        boolean isSelf = member.uuid().equals(client.player.getUuid());
        boolean isLeader = PartyClientData.isLeader(member.uuid());
        
        // Try to get skin textures and draw using PlayerSkinDrawer
        var playerListEntry = client.player.networkHandler.getPlayerListEntry(member.uuid());
        if (playerListEntry != null) {
            try {
                Object skinTextures = playerListEntry.getSkinTextures();
                if (skinTextures != null) {
                    // Use PlayerSkinDrawer to draw the head via reflection (16x16 size)
                    java.lang.reflect.Method drawMethod = net.minecraft.client.gui.PlayerSkinDrawer.class.getMethod("draw",
                        DrawContext.class, skinTextures.getClass(), int.class, int.class, int.class);
                    drawMethod.invoke(null, context, skinTextures, x, y, 16);

                    return;
                }
            } catch (Exception e) {
                // Failed to draw skin, use fallback
            }
        }
        
        // Fallback: draw colored square with initial (16x16 size)
        context.fill(x - 1, y - 1, x + 17, y + 17, 0xFF000000);

        String initial = member.name().substring(0, 1).toUpperCase();
        int textWidth = client.textRenderer.getWidth(initial);
        int textX = x + 8 - textWidth / 2;
        int textY = y + 4;

        int bgColor;
        if (isLeader) {
            bgColor = 0xFF8B4513;
        } else if (isSelf) {
            bgColor = 0xFF2E5AAC;
        } else {
            bgColor = 0xFF555555;
        }

        context.fill(x, y, x + 16, y + 16, bgColor);
        context.drawTextWithShadow(client.textRenderer, initial, textX, textY, 0xFFFFFFFF);
    }
    
    private void renderHealthBar(DrawContext context, int x, int y, PartyMemberData member) {
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

        // Draw only the hearts that exist in this row (no empty containers beyond max)
        for (int i = 0; i < heartsInThisRow; i++) {
            int heartX = x + i * 8;

            // Draw container (use blinking texture when flashing)
            Identifier containerTexture = isFlashing ? HEART_CONTAINER_BLINKING : HEART_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, y, 9, 9);

            // Draw filled or half heart
            if (i < fullHeartsInRow) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_FULL, heartX, y, 9, 9);
            } else if (i == fullHeartsInRow && hasHalfInRow) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_HALF, heartX, y, 9, 9);
            }
        }

        // Draw row indicator (e.g., "2x") at the end if not first row
        if (currentRow > 1) {
            String rowIndicator = (currentRow - 1) + "x";
            int indicatorX = x + heartsInThisRow * 8 + 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                rowIndicator, indicatorX, y, 0xFFAAAAAA);
        }
    }
    
    private void renderShieldBar(DrawContext context, int x, int y, PartyMemberData member) {
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

        // Draw only the hearts that exist in this row (no empty containers beyond max)
        for (int i = 0; i < heartsInThisRow; i++) {
            int heartX = x + i * 8;

            // Draw container (use blinking texture when flashing)
            Identifier containerTexture = isFlashing ? HEART_CONTAINER_BLINKING : SHIELD_CONTAINER;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, y, 9, 9);

            // Draw filled or half heart (use blinking textures when flashing)
            if (i < fullHeartsInRow) {
                Identifier shieldTexture = isFlashing ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, shieldTexture, heartX, y, 9, 9);
            } else if (i == fullHeartsInRow && hasHalfInRow) {
                Identifier shieldTexture = isFlashing ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, shieldTexture, heartX, y, 9, 9);
            }
        }

        // Draw row indicator (e.g., "2x") at the end if not first row
        if (currentRow > 1) {
            String rowIndicator = (currentRow - 1) + "x";
            int indicatorX = x + heartsInThisRow * 8 + 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                rowIndicator, indicatorX, y, 0xFFAAAAAA);
        }
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new PartyHudRenderer());
    }
}
