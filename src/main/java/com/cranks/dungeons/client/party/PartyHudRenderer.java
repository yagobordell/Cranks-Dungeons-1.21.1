package com.cranks.dungeons.client.party;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.ShieldNumberSprites;
import com.cranks.dungeons.client.render.HudStyle;
import com.cranks.dungeons.client.render.StatBarHudRenderer;
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
    private static final int CONTENT_X = 24;
    private static final int HEALTH_Y = 16;
    private static final int SHIELD_Y = 26;

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen) return;
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
        
        var layout = PartyHudLayout.fit(screenWidth, screenHeight, config.partyHudX(), config.partyHudY(),
                config.partyHudMaxRows(), config.partyHudMaxColumns(), members.size(), config.partyHudSize());
        int x = layout.x(), y = layout.y(), maxRows = layout.rows();
        float scale = layout.scale();

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

        if (CranksDungeons.CONFIG.useStatBars() || !member.isAlive()) {
            var matrices = context.getMatrices();
            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(scale, scale, 1);
            drawMemberCard(context, client, displayName, isLeader, member.isAlive());
            renderPlayerHead(context, client, member, 5, 10, 1);
            if (member.isAlive() && maxHealth > 0) {
                var partyPlayer = client.world == null ? null : client.world.getPlayerByUuid(member.uuid());
                drawMemberMeters(context, client, partyPlayer, health, maxHealth, absorption, shield, maxShield);
            }
            matrices.pop();
            return;
        }

        int healthY = y + (int) (12 * scale);
        int healthHearts = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        int healthRows = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, (int) Math.ceil(healthHearts / 10.0));
        int healthSpacing = (int) (Math.max(10 - (healthRows - 2), 3) * scale);
        int healthTop = healthY - (healthRows - 1) * healthSpacing;
        int shieldY = healthTop - (int) (10 * scale);
        int shieldHearts = (int) Math.ceil(maxShield / 2.0f);
        int shieldRows = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, (int) Math.ceil(shieldHearts / 10.0));
        int shieldSpacing = (int) (Math.max(10 - (shieldRows - 2), 3) * scale);
        int top = maxShield > 0 ? shieldY - (shieldRows - 1) * shieldSpacing : healthTop;
        int faceY = (top + healthY + (int) (9 * scale)) / 2 - (int) (8 * scale);

        context.drawTextWithShadow(client.textRenderer, fitName(client, displayName, 92),
                x + (int) (2 * scale), Math.min(top, faceY) - (int) (10 * scale), isLeader ? 0xFFE9CD8E : 0xFFF3F3F3);
        renderPlayerHead(context, client, member, x + (int) (2 * scale), faceY, scale);
        int heartsX = x + (int) (CONTENT_X * scale);
        renderShieldBar(context, heartsX, shieldY, shield, maxShield, member.uuid(), scale);
        renderHealthBar(context, heartsX, healthY, health, maxHealth, absorption, member.uuid(), scale);
    }

    private void renderPlayerHead(DrawContext context, MinecraftClient client, PartyMemberData member, int x, int y, float scale) {
        boolean isSelf = member.uuid().equals(client.player.getUuid());
        boolean isLeader = PartyClientData.isLeader(member.uuid());

        int size = (int) (16 * scale);

        HudStyle.plate(context, x - 2, y - 2, size + 4, size + 4, !isLeader);

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

    private static void drawMemberMeters(DrawContext context, MinecraftClient client, net.minecraft.entity.LivingEntity entity,
                                         float health, float maxHealth, float absorption, float shield, float maxShield) {
        StatBarHudRenderer.drawHealthMeter(context, CONTENT_X, HEALTH_Y, StatBarHudRenderer.BAR_WIDTH,
                StatBarHudRenderer.BAR_HEIGHT, entity, health, maxHealth, absorption, health);
        StatBarHudRenderer.drawHealthLabel(context, client, CONTENT_X, HEALTH_Y, health, maxHealth, absorption);
        if (maxShield > 0) {
            StatBarHudRenderer.renderShieldBarDirect(context, client, CONTENT_X, SHIELD_Y, shield, maxShield, shield);
        }
    }

    private static void drawMemberCard(DrawContext context, MinecraftClient client, String name,
                                      boolean leader, boolean alive) {
        HudStyle.plate(context, 0, 0, BASE_MEMBER_WIDTH, BASE_MEMBER_HEIGHT, !alive);
        context.drawText(client.textRenderer, fitName(client, name, leader ? 70 : 80),
                CONTENT_X, 5, alive ? 0xFFF2F2F2 : 0xFF9A9A9A, false);
        if (leader) {
            int gold = 0xFFDDC18B;
            context.fill(102, 6, 103, 11, gold);
            context.fill(101, 7, 104, 10, gold);
        }
        if (!alive) {
            context.drawText(client.textRenderer, "Dead", CONTENT_X, 18, 0xFFD38E8E, false);
        }
    }

    private static String fitName(MinecraftClient client, String name, int width) {
        var font = client.textRenderer;
        return font.getWidth(name) <= width ? name : font.trimToWidth(name, width - font.getWidth("…")) + "…";
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y,
                                     int maxRows, int maxColumns, float scale) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(scale, scale, 1);
        for (int i = 0; i < maxRows * maxColumns; i++) {
            int memberX = i / maxRows * (BASE_MEMBER_WIDTH + 5);
            int memberY = i % maxRows * (BASE_MEMBER_HEIGHT + 2);
            matrices.push();
            matrices.translate(memberX, memberY, 0);
            drawMemberCard(context, client, "Player " + (i + 1), i == 0, true);
            HudStyle.plate(context, 3, 8, 20, 20, i != 0);
            context.fill(5, 10, 21, 26, 0xFF333333);
            drawMemberMeters(context, client, null, 64, 80, 0, 45, 60);
            matrices.pop();
        }
        matrices.pop();
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new PartyHudRenderer());
    }
}
