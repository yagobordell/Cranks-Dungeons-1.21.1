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
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
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
        
        // Draw name with shadow for visibility
        context.drawTextWithShadow(client.textRenderer, displayName, x + 20, y + 2, nameColor);
        
        renderPlayerHead(context, client, member, x + 2, y + 2);
        
        if (member.isAlive()) {
            renderHealthBar(context, x + 20, y + 14, member);
            renderShieldBar(context, x + 20, y + 24, member);
        } else {
            context.drawTextWithShadow(client.textRenderer, "☠ Dead", x + 20, y + 16, 0xAA0000);
        }
    }
    
    private void renderPlayerHead(DrawContext context, MinecraftClient client, PartyMemberData member, int x, int y) {
        // Draw border
        context.fill(x - 1, y - 1, x + 17, y + 17, 0xFF000000);
        
        String initial = member.name().substring(0, 1).toUpperCase();
        int textWidth = client.textRenderer.getWidth(initial);
        int textX = x + 8 - textWidth / 2;
        int textY = y + 4;
        
        boolean isLeader = PartyClientData.isLeader(member.uuid());
        boolean isSelf = member.uuid().equals(client.player.getUuid());
        
        // Different background colors for different member types
        int bgColor;
        if (isLeader) {
            bgColor = 0xFF8B4513; // Brown-gold for leader
        } else if (isSelf) {
            bgColor = 0xFF2E5AAC; // Blue for self
        } else {
            bgColor = 0xFF555555; // Gray for others
        }
        
        context.fill(x, y, x + 16, y + 16, bgColor);
        
        // Draw initial
        context.drawTextWithShadow(client.textRenderer, initial, textX, textY, 0xFFFFFFFF);
        
        // If it's the local player, draw a small indicator
        if (isSelf) {
            context.fill(x + 12, y + 12, x + 15, y + 15, 0xFF00FF00);
        }
    }
    
    private void renderHealthBar(DrawContext context, int x, int y, PartyMemberData member) {
        float maxHealth = member.maxHealth();
        float health = member.health();
        
        if (maxHealth <= 0) return;
        
        int maxHearts = (int) Math.ceil(maxHealth / 2.0f);
        int fullHearts = (int) (health / 2.0f);
        boolean hasHalfHeart = (health % 2.0f) >= 1.0f && (health % 2.0f) < 2.0f;
        
        // Limit to 10 hearts displayed
        int heartsToShow = Math.min(maxHearts, 10);
        
        for (int i = 0; i < heartsToShow; i++) {
            int heartX = x + i * 9;
            
            // Always draw container
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, heartX, y, 9, 9);
            
            // Draw filled or half heart
            if (i < fullHearts) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_FULL, heartX, y, 9, 9);
            } else if (i == fullHearts && hasHalfHeart) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_HALF, heartX, y, 9, 9);
            }
        }
        
        // If max hearts > 10, show indicator
        if (maxHearts > 10) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, 
                "+" + (maxHearts - 10), x + 91, y, 0xFFFFFF);
        }
    }
    
    private void renderShieldBar(DrawContext context, int x, int y, PartyMemberData member) {
        float maxShield = member.maxEnergyShield();
        float shield = member.energyShield();
        
        if (maxShield <= 0) return;
        
        int maxHearts = (int) Math.ceil(maxShield / 2.0f);
        int fullHearts = (int) (shield / 2.0f);
        boolean hasHalfHeart = (shield % 2.0f) >= 1.0f && (shield % 2.0f) < 2.0f;
        
        // Limit to 10 hearts displayed
        int heartsToShow = Math.min(maxHearts, 10);
        
        for (int i = 0; i < heartsToShow; i++) {
            int heartX = x + i * 9;
            
            // Always draw container
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SHIELD_CONTAINER, heartX, y, 9, 9);
            
            // Draw filled or half heart
            if (i < fullHearts) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SHIELD_FULL, heartX, y, 9, 9);
            } else if (i == fullHearts && hasHalfHeart) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SHIELD_HALF, heartX, y, 9, 9);
            }
        }
        
        // If max hearts > 10, show indicator
        if (maxHearts > 10) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, 
                "+" + (maxHearts - 10), x + 91, y, 0xFFFFFF);
        }
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new PartyHudRenderer());
    }
}
