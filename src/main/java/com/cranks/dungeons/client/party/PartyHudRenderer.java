package com.cranks.dungeons.client.party;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.ShieldNumberSprites;
import com.cranks.dungeons.party.PartyMemberData;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.List;

public class PartyHudRenderer implements HudRenderCallback {

    private static final Identifier HEART_FULL = Identifier.of("minecraft", "hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.of("minecraft", "hud/heart/half");
    private static final Identifier HEART_CONTAINER = Identifier.of("minecraft", "hud/heart/container");
    private static final Identifier HEART_CONTAINER_BLINKING = Identifier.of("minecraft", "hud/heart/container_blinking");
    private static final Identifier HEART_ABSORPTION_FULL = Identifier.of("minecraft", "hud/heart/absorbing_full");
    private static final Identifier HEART_ABSORPTION_HALF = Identifier.of("minecraft", "hud/heart/absorbing_half");
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");
    private static final Identifier SHIELD_CONTAINER = Identifier.of("minecraft", "hud/heart/container");

    private static final boolean OVERFLOWING_BARS_LOADED = FabricLoader.getInstance().isModLoaded("overflowingbars");

    public static final int BASE_MEMBER_WIDTH = 110;
    public static final int BASE_MEMBER_HEIGHT = 36;

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (client.options.hudHidden || !client.player.isAlive() || com.cranks.dungeons.CranksDungeonsClient.isCinematicRespawnActive()) return;

        com.cranks.dungeons.config.ModConfig config = CranksDungeons.CONFIG;
        if (!config.partyHudEnabled()) return;

        if (!PartyClientData.isInParty()) return;

        List<PartyMemberData> members = PartyClientData.getMembers();
        
        // Filter out self if config says so
        if (!config.partyHudShowSelf()) {
            members.removeIf(m -> m.uuid().equals(client.player.getUuid()));
        }
        
        if (members.isEmpty()) return;
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        int x = config.partyHudX();
        int y = config.partyHudY();
        
        if (x < 0) x = 5;
        
        float scale = config.partyHudSize();
        int maxRows = config.partyHudMaxRows();
        int maxColumns = config.partyHudMaxColumns();
        
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

        float health, maxHealth, absorption, shield, maxShield;
        if (isSelf) {
            health = client.player.getHealth();
            maxHealth = client.player.getMaxHealth();
            absorption = client.player.getAbsorptionAmount();
            if (client.player instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
                shield = provider.cranksdungeons$getCurrentEnergyShield();
            } else {
                shield = member.energyShield();
            }
            maxShield = (float) client.player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
        } else {
            health = member.health();
            maxHealth = member.maxHealth();
            absorption = member.absorption();
            shield = member.energyShield();
            maxShield = member.maxEnergyShield();
        }

        boolean useStatBars = CranksDungeons.CONFIG.useStatBars();
        int healthBaseY = y + (int) (12 * scale);
        
        int highestBarY;
        int shieldBaseY;
        
        if (useStatBars) {
            shieldBaseY = healthBaseY;
            highestBarY = (maxShield > 0) ? healthBaseY - (int) (7 * scale) : healthBaseY;
        } else {
            int totalHealthHearts = (int) Math.ceil((maxHealth + absorption) / 2.0f);
            int healthRows = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, (int) Math.ceil(totalHealthHearts / 10.0));
            int healthVanillaSpacing = Math.max(10 - (healthRows - 2), 3);
            int healthRowHeight = (int) (healthVanillaSpacing * scale);
            int healthTotalHeight = (healthRows - 1) * healthRowHeight;

            shieldBaseY = healthBaseY - healthTotalHeight - (int) (10 * scale);

            int maxShieldHearts = (int) Math.ceil(maxShield / 2.0f);
            int shieldRows = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, (int) Math.ceil(maxShieldHearts / 10.0));
            int shieldVanillaSpacing = Math.max(10 - (shieldRows - 2), 3);
            int shieldRowHeight = (int) (shieldVanillaSpacing * scale);
            int shieldTotalHeight = (shieldRows > 0 && maxShield > 0) ? (shieldRows - 1) * shieldRowHeight : 0;
            
            highestBarY = (maxShield > 0) ? (shieldBaseY - shieldTotalHeight) : (healthBaseY - healthTotalHeight);
        }

        int iconY, nameY;
        if (member.isAlive()) {
            int lowestBarY = healthBaseY + (int) (9 * scale);
            int barsMidpointY = (highestBarY + lowestBarY) / 2;
            iconY = barsMidpointY - (int) (8 * scale);

            int absoluteTopY = Math.min(highestBarY, iconY);
            nameY = absoluteTopY - (int) (10 * scale);
            
            context.drawTextWithShadow(client.textRenderer, displayName, x + (int) (2 * scale), nameY, nameColor);
            renderPlayerHead(context, client, member, x + (int) (2 * scale), iconY, scale);

            int barsX = x + (int) (24 * scale);
            if (useStatBars) {
                renderStatBars(context, client, barsX, healthBaseY, health, maxHealth, absorption, shield, maxShield, member.uuid(), scale);
            } else {
                renderShieldBar(context, barsX, shieldBaseY, shield, maxShield, member.uuid(), scale);
                renderHealthBar(context, barsX, healthBaseY, health, maxHealth, absorption, member.uuid(), scale);
            }
        } else {
            iconY = y + (int) (10 * scale);
            nameY = iconY - (int) (10 * scale);
            
            context.drawTextWithShadow(client.textRenderer, displayName, x + (int) (2 * scale), nameY, nameColor);
            renderPlayerHead(context, client, member, x + (int) (2 * scale), iconY, scale);
            context.drawTextWithShadow(client.textRenderer, "☠ Dead", x + (int) (24 * scale), iconY + (int) (4 * scale), 0xAA0000);
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
                    Identifier texture = skinTextures.texture();
                    
                    if (texture != null) {
                        // Render face (base layer): UV (8, 8) to (16, 16) on 64x64 texture
                        context.drawTexture(
                            texture,
                            x, y,           // screen position
                            size, size,     // size on screen
                            8.0F, 8.0F,     // UV start (face region)
                            8, 8,           // UV region size
                            64, 64          // texture size
                        );
                        
                        // Render hat layer (overlay): UV (40, 8) to (48, 16) on 64x64 texture
                        context.drawTexture(
                            texture,
                            x, y,           // screen position (same as face)
                            size, size,     // size on screen
                            40.0F, 8.0F,    // UV start (hat region)
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

    private void renderHealthBar(DrawContext context, int x, int y, float health, float maxHealth, float absorption, java.util.UUID uuid, float scale) {
        if (maxHealth <= 0) return;

        // Apply a small epsilon to avoid lingering half-hearts when effectively full.
        float effectiveHealth = (health >= maxHealth - 0.1f) ? maxHealth : health + 0.001f;
        int totalHearts = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        boolean isFlashing = PartyClientData.isDamageFlashing(uuid) || PartyClientData.isHealFlashing(uuid);

        int heartSize = (int) (9 * scale);
        int spacing = (int) (8 * scale);

        if (OVERFLOWING_BARS_LOADED) {
            renderHealthBarStacked(context, x, y, totalHearts, effectiveHealth, maxHealth, absorption, isFlashing, heartSize, spacing, scale);
        } else {
            renderHealthBarAllRows(context, x, y, totalHearts, effectiveHealth, maxHealth, absorption, isFlashing, heartSize, spacing, scale);
        }
    }

    /**
     * Without Overflowing Bars: render all heart rows, just like vanilla does for
     * the player's own health bar.
     */
    private void renderHealthBarAllRows(DrawContext context, int x, int y,
                                        int totalHearts, float health, float maxHealth, float absorption,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int totalRows = (int) Math.ceil(totalHearts / 10.0);
        int vanillaSpacing = Math.max(10 - (totalRows - 2), 3);
        int scaledRowSpacing = (int) (vanillaSpacing * scale);

        for (int i = 0; i < totalHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * spacing;
            int heartY = y - row * scaledRowSpacing;

            // Container
            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : HEART_CONTAINER;
            context.drawGuiTexture(container, heartX, heartY, heartSize, heartSize);

            // Logic: Heart i represents range [i*2, i*2 + 2]
            // Half heart: health >= (i*2) + 0.01
            // Full heart: health >= (i*2) + 1.01
            float baseVisibility = i * 2.0f;

            if (health >= baseVisibility + 1.01f) {
                context.drawGuiTexture(HEART_FULL, heartX, heartY, heartSize, heartSize);
            } else if (health >= baseVisibility + 0.01f) {
                context.drawGuiTexture(HEART_HALF, heartX, heartY, heartSize, heartSize);
            } else if (baseVisibility >= maxHealth) {
                // Absorption zone
                float absorptionVisibility = baseVisibility - maxHealth;
                if (absorption >= absorptionVisibility + 1.01f) {
                    context.drawGuiTexture(HEART_ABSORPTION_FULL, heartX, heartY, heartSize, heartSize);
                } else if (absorption >= absorptionVisibility + 0.01f) {
                    context.drawGuiTexture(HEART_ABSORPTION_HALF, heartX, heartY, heartSize, heartSize);
                }
            }
        }
    }

    /**
     * With Overflowing Bars: stack hearts into a single row with an Nx indicator,
     * mirroring the Overflowing Bars mod behaviour for the player's own HUD.
     */
    private void renderHealthBarStacked(DrawContext context, int x, int y,
                                        int totalHearts, float health, float maxHealth, float absorption,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        // Determine which row the current 'highest' icon falls on (1-indexed)
        float totalEffective = health + absorption;
        int highestIconIndex = (int) Math.ceil(totalEffective / 2.0f) - 1;
        int currentRow = Math.max(1, (highestIconIndex / 10) + 1);

        int heartsInFullRowsBelow = (currentRow - 1) * 10;
        int heartsInThisRow = Math.min(10, totalHearts - heartsInFullRowsBelow);

        for (int i = 0; i < heartsInThisRow; i++) {
            int iconIndex = heartsInFullRowsBelow + i;
            int heartX = x + i * spacing;

            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : HEART_CONTAINER;
            context.drawGuiTexture(container, heartX, y, heartSize, heartSize);

            float baseVisibility = iconIndex * 2.0f;

            if (health >= baseVisibility + 1.01f) {
                context.drawGuiTexture(HEART_FULL, heartX, y, heartSize, heartSize);
            } else if (health >= baseVisibility + 0.01f) {
                context.drawGuiTexture(HEART_HALF, heartX, y, heartSize, heartSize);
            } else if (baseVisibility >= maxHealth) {
                // Absorption zone
                float absorptionVisibility = baseVisibility - maxHealth;
                if (absorption >= absorptionVisibility + 1.01f) {
                    context.drawGuiTexture(HEART_ABSORPTION_FULL, heartX, y, heartSize, heartSize);
                } else if (absorption >= absorptionVisibility + 0.01f) {
                    context.drawGuiTexture(HEART_ABSORPTION_HALF, heartX, y, heartSize, heartSize);
                }
            }
        }

        // Nx sprite indicator when on row 2+
        if (currentRow > 1) {
            int spriteX = x + heartsInThisRow * spacing;
            int spriteY = y - (int) (2 * scale);
            Identifier spriteId = ShieldNumberSprites.getSpriteId(currentRow);
            context.drawGuiTexture(spriteId, spriteX, spriteY, (int) (16 * scale), (int) (16 * scale));
        }
    }

    // ─── Shield Hearts ───────────────────────────────────────────────────

    private void renderShieldBar(DrawContext context, int x, int y, float shield, float maxShield, java.util.UUID uuid, float scale) {
        if (maxShield <= 0) return;

        // Apply a small epsilon to avoid lingering half-hearts when effectively full.
        float effectiveShield = (shield >= maxShield - 0.1f) ? maxShield : shield + 0.001f;

        int totalHearts = (int) Math.ceil(maxShield / 2.0f);
        boolean isFlashing = PartyClientData.isDamageFlashing(uuid)
                || PartyClientData.isHealFlashing(uuid)
                || PartyClientData.isShieldFlashing(uuid);

        int heartSize = (int) (9 * scale);
        int spacing = (int) (8 * scale);

        if (OVERFLOWING_BARS_LOADED) {
            renderShieldBarStacked(context, x, y, totalHearts, effectiveShield, isFlashing, heartSize, spacing, scale);
        } else {
            renderShieldBarAllRows(context, x, y, totalHearts, effectiveShield, isFlashing, heartSize, spacing, scale);
        }
    }

    private void renderShieldBarAllRows(DrawContext context, int x, int y,
                                        int totalHearts, float shield,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int totalRows = (int) Math.ceil(totalHearts / 10.0);
        int vanillaSpacing = Math.max(10 - (totalRows - 2), 3);
        int scaledRowSpacing = (int) (vanillaSpacing * scale);

        for (int i = 0; i < totalHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * spacing;
            int heartY = y - row * scaledRowSpacing;

            // Container
            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : SHIELD_CONTAINER;
            context.drawGuiTexture(container, heartX, heartY, heartSize, heartSize);

            float baseVisibility = i * 2.0f;

            if (shield >= baseVisibility + 1.01f) {
                Identifier tex = isFlashing ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                context.drawGuiTexture(tex, heartX, heartY, heartSize, heartSize);
            } else if (shield >= baseVisibility + 0.01f) {
                Identifier tex = isFlashing ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                context.drawGuiTexture(tex, heartX, heartY, heartSize, heartSize);
            }
        }
    }

    /**
     * With Overflowing Bars: stack shields into a single row with an Nx sprite,
     * mirroring the player's own energy shield HUD (EnergyShieldHudRenderer).
     */
    private void renderShieldBarStacked(DrawContext context, int x, int y,
                                        int totalHearts, float shield,
                                        boolean isFlashing, int heartSize, int spacing, float scale) {
        int highestIconIndex = (int) Math.ceil(shield / 2.0f) - 1;
        int currentRow = Math.max(1, (highestIconIndex / 10) + 1);

        int heartsInFullRowsBelow = (currentRow - 1) * 10;
        int heartsInThisRow = Math.min(10, totalHearts - heartsInFullRowsBelow);

        for (int i = 0; i < heartsInThisRow; i++) {
            int iconIndex = heartsInFullRowsBelow + i;
            int heartX = x + i * spacing;

            // Container
            Identifier container = isFlashing ? HEART_CONTAINER_BLINKING : SHIELD_CONTAINER;
            context.drawGuiTexture(container, heartX, y, heartSize, heartSize);

            float baseVisibility = iconIndex * 2.0f;

            if (shield >= baseVisibility + 1.01f) {
                Identifier tex = isFlashing ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                context.drawGuiTexture(tex, heartX, y, heartSize, heartSize);
            } else if (shield >= baseVisibility + 0.01f) {
                Identifier tex = isFlashing ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                context.drawGuiTexture(tex, heartX, y, heartSize, heartSize);
            }
        }

        // Nx sprite indicator when on row 2+ - matching health style with texture font
        if (currentRow > 1 && totalHearts > 10) {
            int spriteX = x + heartsInThisRow * spacing;
            int spriteY = y - (int) (2 * scale);
            Identifier spriteId = ShieldNumberSprites.getSpriteId(currentRow);
            context.drawGuiTexture(spriteId, spriteX, spriteY, (int) (16 * scale), (int) (16 * scale));
        }
    }

    private void renderStatBars(DrawContext context, MinecraftClient client, int x, int y, float health, float maxHealth, float absorption, float shield, float maxShield, java.util.UUID memberUuid, float scale) {
        if (maxHealth <= 0) return;

        int barWidth = (int) (com.cranks.dungeons.client.render.StatBarHudRenderer.BAR_WIDTH * scale);
        int barHeight = (int) (com.cranks.dungeons.client.render.StatBarHudRenderer.BAR_HEIGHT * scale);
        int shieldHeight = (int) (6 * scale); // New 6px height
        int maxFillWidth = (int) (com.cranks.dungeons.client.render.StatBarHudRenderer.MAX_FILL_WIDTH * scale);

        // --- Shield Bar (Above HP) ---
        if (maxShield > 0) {
            int shieldY = y - (int) (7 * scale); // 6px height + 1px gap
            
            // Draw Shield Background
            context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.EMPTY_SHIELD_BAR, x, shieldY, barWidth, shieldHeight);

            float shieldPercent = net.minecraft.util.math.MathHelper.clamp(shield / maxShield, 0, 1);
            int shieldWidth = (int) (shieldPercent * maxFillWidth);
            if (shieldWidth > 0) {
                context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.SHIELD_FILL, 
                        com.cranks.dungeons.client.render.StatBarHudRenderer.MAX_FILL_WIDTH, 
                        6, 0, 0,
                        x, shieldY, shieldWidth, shieldHeight);
            }

            // Shield Text (Smaller)
            String sText = (int) shield + " / " + (int) maxShield;
            net.minecraft.client.font.TextRenderer tr = client.textRenderer;
            int sTextWidth = (int) (tr.getWidth(sText) * 0.75f);
            
            context.getMatrices().push();
            context.getMatrices().translate(x + (barWidth / 2f), shieldY + (shieldHeight / 2f), 0.0f);
            context.getMatrices().scale(0.75f * scale, 0.75f * scale, 1.0f);
            context.drawTextWithShadow(tr, sText, -(tr.getWidth(sText) / 2), -(tr.fontHeight / 2), 0xFF55FFFF);
            context.getMatrices().pop();
        }

        // --- Health Bar ---
        // Draw Background Frame
        context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.EMPTY_BAR, x, y, barWidth, barHeight);

        // Draw HP Fill (Red / Ailment color)
        float hpPercent = net.minecraft.util.math.MathHelper.clamp(health / maxHealth, 0, 1);
        int hpWidth = (int) (hpPercent * maxFillWidth);
        if (hpWidth > 0) {
            net.minecraft.entity.player.PlayerEntity partyPlayer = (client.world != null && memberUuid != null) ? client.world.getPlayerByUuid(memberUuid) : null;
            Identifier healthTexture = com.cranks.dungeons.client.render.StatBarHudRenderer.getHealthFillTexture(partyPlayer);
            context.drawGuiTexture(healthTexture, 
                    com.cranks.dungeons.client.render.StatBarHudRenderer.MAX_FILL_WIDTH, 
                    com.cranks.dungeons.client.render.StatBarHudRenderer.FILL_HEIGHT, 0, 0,
                    x, y, hpWidth, barHeight);
        }

        // Draw Absorption Overlay (Yellow)
        if (absorption > 0) {
            float absPercent = net.minecraft.util.math.MathHelper.clamp(absorption / maxHealth, 0, 1);
            int absWidth = (int) (absPercent * maxFillWidth);
            if (absWidth > 0) {
                context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.ABSORPTION_FILL, 
                        com.cranks.dungeons.client.render.StatBarHudRenderer.MAX_FILL_WIDTH, 
                        com.cranks.dungeons.client.render.StatBarHudRenderer.FILL_HEIGHT, 0, 0,
                        x, y, absWidth, barHeight);
            }
        }

        // Health Text
        net.minecraft.client.font.TextRenderer textRenderer = client.textRenderer;
        String hText = (int) health + " / " + (int) maxHealth;
        int hColor = 0xFFFFFFFF;
        if (absorption > 0) {
            hText += " (+" + (int) absorption + ")";
            hColor = 0xFFFFAA00;
        }

        int hTextWidth = (int) (textRenderer.getWidth(hText) * 0.85f);
        context.getMatrices().push();
        context.getMatrices().translate(x + (barWidth / 2f), y + (barHeight / 2f), 0.0f);
        context.getMatrices().scale(0.85f * scale, 0.85f * scale, 1.0f);
        context.drawTextWithShadow(textRenderer, hText, -(textRenderer.getWidth(hText) / 2), -(textRenderer.fontHeight / 2), hColor);
        context.getMatrices().pop();
    }

    
    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y, int maxRows, int maxColumns, float scale) {
        int totalMembers = maxRows * maxColumns;
        String[] mockNames = { "Player 1", "Player 2", "Player 3", "Player 4", "Player 5", "Player 6", "Player 7", "Player 8", "Player 9", "Player 10" };
        
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        for (int i = 0; i < totalMembers; i++) {
            int col = i / maxRows;
            int row = i % maxRows;
            
            int memberX = col * (BASE_MEMBER_WIDTH + 5);
            int memberY = row * (BASE_MEMBER_HEIGHT + 2);
            
            String name = i < mockNames.length ? mockNames[i] : ("Member " + (i + 1));
            boolean isLeader = (i == 0);
            
            int nameColor = isLeader ? 0xFFFFD700 : 0xFFFFFFFF;
            int healthBaseY = memberY + 12;
            int highestBarY = healthBaseY - 7;
            int iconY = healthBaseY - 4;
            int nameY = highestBarY - 10;
            
            context.drawTextWithShadow(client.textRenderer, name + (isLeader ? " ★" : ""), memberX + 2, nameY, nameColor);
            
            int headSize = 16;
            context.fill(memberX + 2, iconY, memberX + 2 + headSize, iconY + headSize, 0xFF333333);
            context.drawBorder(memberX + 2, iconY, headSize, headSize, 0xFF666666);
            
            int barsX = memberX + 24;
            int barWidth = com.cranks.dungeons.client.render.StatBarHudRenderer.BAR_WIDTH;
            int barHeight = com.cranks.dungeons.client.render.StatBarHudRenderer.BAR_HEIGHT;
            int shieldHeight = 6;
            int maxFillWidth = com.cranks.dungeons.client.render.StatBarHudRenderer.MAX_FILL_WIDTH;
            int shieldY = healthBaseY - 7;
            
            context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.EMPTY_SHIELD_BAR, barsX, shieldY, barWidth, shieldHeight);
            context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.SHIELD_FILL, maxFillWidth, 6, 0, 0, barsX, shieldY, maxFillWidth, shieldHeight);
            
            context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.EMPTY_BAR, barsX, healthBaseY, barWidth, barHeight);
            context.drawGuiTexture(com.cranks.dungeons.client.render.StatBarHudRenderer.HEALTH_FILL, maxFillWidth, com.cranks.dungeons.client.render.StatBarHudRenderer.FILL_HEIGHT, 0, 0, barsX, healthBaseY, maxFillWidth, barHeight);
        }

        context.getMatrices().pop();
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new PartyHudRenderer());
    }
}
