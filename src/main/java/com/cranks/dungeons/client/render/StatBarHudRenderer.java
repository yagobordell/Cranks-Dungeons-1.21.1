package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class StatBarHudRenderer implements HudRenderCallback {

    public static final Identifier EMPTY_BAR = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/empty");
    public static final Identifier EMPTY_SHIELD_BAR = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/empty_energy_shield");
    public static final Identifier HEALTH_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health");
    public static final Identifier HEALTH_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_trail");
    public static final Identifier HEALTH_BURN_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_burn");
    public static final Identifier HEALTH_BURN_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_trail_burn");
    public static final Identifier HEALTH_POISON_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_poison");
    public static final Identifier HEALTH_POISON_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_trail_poison");
    public static final Identifier HEALTH_BLEED_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_bleed");
    public static final Identifier HEALTH_BLEED_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/health_trail_bleed");
    public static final Identifier SHIELD_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/energy_shield");
    public static final Identifier SHIELD_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/energy_shield_trail");
    public static final Identifier ABSORPTION_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/absorption");
    public static final Identifier ABSORPTION_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/absorption_trail");
    public static final Identifier HUNGER_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/hunger");
    public static final Identifier HUNGER_HIGH_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/hunger_high");
    public static final Identifier HUNGER_LOW_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/hunger_low");
    public static final Identifier OXYGEN_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/oxygen");
    public static final Identifier MANA_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/mana");
    public static final Identifier MANA_TRAIL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/mana_trail");
    public static final Identifier EMPTY_EXPERIENCE_BAR = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/empty_experience");
    public static final Identifier EXPERIENCE_FILL = Identifier.of(CranksDungeons.MOD_ID, "hud/stat_bar/experience");

    // Default dimensions for the bar (scaled to match a vanilla heart row: 81x9)
    public static final int BAR_WIDTH = 81;
    public static final int BAR_HEIGHT = 9;
    public static final int EXPERIENCE_BAR_WIDTH = 182;
    public static final int EXPERIENCE_BAR_HEIGHT = 5;
    public static final int SHIELD_HEIGHT = 6;
    public static final int FILL_OFFSET_X = 0;
    public static final int FILL_OFFSET_Y = 0;
    public static final int MAX_FILL_WIDTH = 81;
    public static final int FILL_HEIGHT = 9;
    // The vanilla font is 9px tall; scale bar labels to approximately 8px.
    private static final float BAR_TEXT_SCALE = 0.9f;

    private float displayedHealth = -1;
    private long lastHealthChangeTime = 0;
    
    private float displayedShield = -1;
    private long lastShieldChangeTime = 0;

    private float displayedAbsorption = -1;
    private long lastAbsorptionChangeTime = 0;
    
    private float displayedMana = -1;
    private long lastManaChangeTime = 0;
    
    private long lastRenderTime = 0;

    private static final long TRAIL_DELAY_MS = 600; // Time before trail starts shrinking
    private static final float TRAIL_SHRINK_SPEED = 15.0f; // Shrink amount per second

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!CranksDungeons.CONFIG.useStatBars())
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || !client.player.isAlive() || com.cranks.dungeons.CranksDungeonsClient.isCinematicRespawnActive())
            return;
        if (client.player.isCreative() || client.player.isSpectator())
            return;

        PlayerEntity player = client.player;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        ensurePositionReference(screenWidth, screenHeight);

        var config = CranksDungeons.CONFIG;
        int referenceWidth = config.statBarLayoutReferenceWidth();
        int referenceHeight = config.statBarLayoutReferenceHeight();
        int defaultHealthX = screenWidth / 2 - 91;
        int defaultHealthY = screenHeight - 39;
        int referenceHealthX = referenceWidth / 2 - 91;
        int referenceHealthY = referenceHeight - 39;
        int healthX = config.healthBarX() >= 0
                ? resolveConfiguredX(config.healthBarX(), screenWidth, BAR_WIDTH, referenceHealthX, defaultHealthX)
                : defaultHealthX;
        int healthY = config.healthBarY() >= 0
                ? resolveConfiguredY(config.healthBarY(), screenHeight, BAR_HEIGHT, referenceHealthY, defaultHealthY)
                : defaultHealthY;

        int defaultShieldX = healthX;
        int defaultShieldY = healthY - 7;
        int referenceShieldX = config.healthBarX() >= 0 ? config.healthBarX() : referenceHealthX;
        int referenceShieldY = (config.healthBarY() >= 0 ? config.healthBarY() : referenceHealthY) - 7;
        int shieldX = config.shieldBarX() >= 0
                ? resolveConfiguredX(config.shieldBarX(), screenWidth, BAR_WIDTH, referenceShieldX, defaultShieldX)
                : defaultShieldX;
        int shieldY = config.shieldBarY() >= 0
                ? resolveConfiguredY(config.shieldBarY(), screenHeight, SHIELD_HEIGHT, referenceShieldY, defaultShieldY)
                : defaultShieldY;

        int defaultHungerX = screenWidth / 2 + 10;
        int defaultHungerY = screenHeight - 39;
        int referenceHungerX = referenceWidth / 2 + 10;
        int referenceHungerY = referenceHeight - 39;
        int hungerX = config.hungerBarX() >= 0
                ? resolveConfiguredX(config.hungerBarX(), screenWidth, BAR_WIDTH, referenceHungerX, defaultHungerX)
                : defaultHungerX;
        int hungerY = config.hungerBarY() >= 0
                ? resolveConfiguredY(config.hungerBarY(), screenHeight, BAR_HEIGHT, referenceHungerY, defaultHungerY)
                : defaultHungerY;

        int defaultManaX = hungerX;
        int defaultManaY = hungerY - 10;
        int referenceManaX = config.hungerBarX() >= 0 ? config.hungerBarX() : referenceHungerX;
        int referenceManaY = (config.hungerBarY() >= 0 ? config.hungerBarY() : referenceHungerY) - 10;
        int manaX = config.manaBarX() >= 0
                ? resolveConfiguredX(config.manaBarX(), screenWidth, BAR_WIDTH, referenceManaX, defaultManaX)
                : defaultManaX;
        int manaY = config.manaBarY() >= 0
                ? resolveConfiguredY(config.manaBarY(), screenHeight, BAR_HEIGHT, referenceManaY, defaultManaY)
                : defaultManaY;

        int defaultExperienceX = screenWidth / 2 - EXPERIENCE_BAR_WIDTH / 2;
        int defaultExperienceY = screenHeight - 29;
        int referenceExperienceX = referenceWidth / 2 - EXPERIENCE_BAR_WIDTH / 2;
        int referenceExperienceY = referenceHeight - 29;
        int experienceX = config.experienceBarX() >= 0
                ? resolveConfiguredX(config.experienceBarX(), screenWidth, EXPERIENCE_BAR_WIDTH, referenceExperienceX, defaultExperienceX)
                : defaultExperienceX;
        int experienceY = config.experienceBarY() >= 0
                ? resolveConfiguredY(config.experienceBarY(), screenHeight, EXPERIENCE_BAR_HEIGHT, referenceExperienceY, defaultExperienceY)
                : defaultExperienceY;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();
        float shield = 0;
        float maxShield = 0;

        if (player instanceof EnergyShieldProvider provider) {
            shield = provider.cranksdungeons$getCurrentEnergyShield();
            maxShield = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
        }

        float mana = 0;
        float maxMana = 0;
        if (player instanceof com.cranks.dungeons.ManaProvider provider) {
            mana = provider.cranksdungeons$getCurrentMana();
            maxMana = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.MAX_MANA);
        }

        updateTrails(health, shield, absorption, mana);

        if (maxShield > 0) {
            renderShieldBarDirect(context, client, shieldX, shieldY, shield, maxShield, displayedShield);
        }
        renderHealthBarDirect(context, client, player, healthX, healthY, health, maxHealth, absorption, displayedHealth);
        if (maxMana > 0) {
            renderManaBarDirect(context, client, manaX, manaY, mana, maxMana, displayedMana);
        }
        renderHungerBarDirect(context, client, hungerX, hungerY, player.getHungerManager().getFoodLevel());

        int nextLevelExperience = Math.max(1, player.getNextLevelExperience());
        int currentExperience = MathHelper.clamp(
                MathHelper.floor(player.experienceProgress * nextLevelExperience),
                0,
                nextLevelExperience
        );
        renderExperienceBarDirect(context, client, experienceX, experienceY,
                currentExperience, nextLevelExperience, player.experienceProgress);
    }

    public static void renderHealthBarDirect(DrawContext context, MinecraftClient client, PlayerEntity player, int x, int y, float health, float maxHealth, float absorption, float displayedHealth) {
        // Draw Background Frame
        context.drawGuiTexture(EMPTY_BAR, x, y, BAR_WIDTH, BAR_HEIGHT);

        float hpPercent = MathHelper.clamp(health / maxHealth, 0, 1);
        int hpWidth = (int) (hpPercent * MAX_FILL_WIDTH);
        
        Identifier healthFillTexture = getHealthFillTexture(player);
        Identifier healthTrailTexture = getHealthTrailTexture(player);

        // Draw Health Trail
        if (displayedHealth > health) {
            int trailWidth = (int) (MathHelper.clamp(displayedHealth / maxHealth, 0, 1) * MAX_FILL_WIDTH);
            if (trailWidth > hpWidth) {
                context.drawGuiTexture(healthTrailTexture, MAX_FILL_WIDTH, FILL_HEIGHT, hpWidth, 0,
                        x + FILL_OFFSET_X + hpWidth, y + FILL_OFFSET_Y, trailWidth - hpWidth, FILL_HEIGHT);
            }
        }

        // Draw HP Fill (Red / Ailment color)
        if (hpWidth > 0) {
            context.drawGuiTexture(healthFillTexture, MAX_FILL_WIDTH, FILL_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, hpWidth, FILL_HEIGHT);
        }

        float absPercent = MathHelper.clamp(absorption / maxHealth, 0, 1);
        int absWidth = (int) (absPercent * MAX_FILL_WIDTH);
        
        // Draw Absorption Overlay (Yellow)
        if (absWidth > 0) {
            context.drawGuiTexture(ABSORPTION_FILL, MAX_FILL_WIDTH, FILL_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, absWidth, FILL_HEIGHT);
        }

        // --- Render Oxygen Overlay ---
        if (player != null) {
            int air = player.getAir();
            int maxAir = player.getMaxAir();
            if (air < maxAir || player.isSubmergedIn(net.minecraft.registry.tag.FluidTags.WATER)) {
                float airPercent = MathHelper.clamp((float) air / maxAir, 0f, 1f);
                int airWidth = (int) (airPercent * MAX_FILL_WIDTH);
                if (airWidth > 0) {
                    com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F);
                    context.drawGuiTexture(OXYGEN_FILL, MAX_FILL_WIDTH, FILL_HEIGHT, 0, 0,
                            x + FILL_OFFSET_X, y + FILL_OFFSET_Y, airWidth, FILL_HEIGHT);
                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }

        // Health Text
        TextRenderer textRenderer = client.textRenderer;
        String hText = (int) health + " / " + (int) maxHealth;
        int hColor = 0xFFFFFFFF;
        if (absorption > 0) {
            hText += " (+" + (int) absorption + ")";
            hColor = 0xFFFFAA00;
        }

        drawCenteredBarText(context, textRenderer, hText, x, y, BAR_WIDTH, BAR_HEIGHT, hColor);
    }

    public static void renderShieldBarDirect(DrawContext context, MinecraftClient client, int x, int y, float shield, float maxShield, float displayedShield) {
        context.drawGuiTexture(EMPTY_SHIELD_BAR, x, y, BAR_WIDTH, SHIELD_HEIGHT);

        float shieldPercent = MathHelper.clamp(shield / maxShield, 0, 1);
        int shieldWidth = (int) (shieldPercent * MAX_FILL_WIDTH);
        
        if (displayedShield > shield) {
            int trailWidth = (int) (MathHelper.clamp(displayedShield / maxShield, 0, 1) * MAX_FILL_WIDTH);
            if (trailWidth > shieldWidth) {
                context.drawGuiTexture(SHIELD_TRAIL, MAX_FILL_WIDTH, SHIELD_HEIGHT, shieldWidth, 0,
                        x + FILL_OFFSET_X + shieldWidth, y + FILL_OFFSET_Y, trailWidth - shieldWidth, SHIELD_HEIGHT);
            }
        }

        if (shieldWidth > 0) {
            context.drawGuiTexture(SHIELD_FILL, MAX_FILL_WIDTH, SHIELD_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, shieldWidth, SHIELD_HEIGHT);
        }

        String sText = (int) shield + " / " + (int) maxShield;
        TextRenderer tr = client.textRenderer;
        drawCenteredBarText(context, tr, sText, x, y, BAR_WIDTH, SHIELD_HEIGHT, 0xFFFFFFFF);
    }

    public static void renderManaBarDirect(DrawContext context, MinecraftClient client, int x, int y, float mana, float maxMana, float displayedMana) {
        int MANA_HEIGHT = 9;
        context.drawGuiTexture(EMPTY_BAR, x, y, BAR_WIDTH, MANA_HEIGHT);

        float manaPercent = MathHelper.clamp(mana / maxMana, 0, 1);
        int manaWidth = (int) (manaPercent * MAX_FILL_WIDTH);
        
        if (displayedMana > mana) {
            int trailWidth = (int) (MathHelper.clamp(displayedMana / maxMana, 0, 1) * MAX_FILL_WIDTH);
            if (trailWidth > manaWidth) {
                context.drawGuiTexture(MANA_TRAIL, MAX_FILL_WIDTH, MANA_HEIGHT, manaWidth, 0,
                        x + FILL_OFFSET_X + manaWidth, y + FILL_OFFSET_Y, trailWidth - manaWidth, MANA_HEIGHT);
            }
        }

        if (manaWidth > 0) {
            context.drawGuiTexture(MANA_FILL, MAX_FILL_WIDTH, MANA_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, manaWidth, MANA_HEIGHT);
        }

        String mText = (int) mana + " / " + (int) maxMana;
        TextRenderer tr = client.textRenderer;
        drawCenteredBarText(context, tr, mText, x, y, BAR_WIDTH, MANA_HEIGHT, 0xFFFFFFFF);
    }

    public static void renderHungerBarDirect(DrawContext context, MinecraftClient client, int x, int y, int hunger) {
        context.drawGuiTexture(EMPTY_BAR, x, y, BAR_WIDTH, BAR_HEIGHT);

        float hungerPercent = MathHelper.clamp(hunger / 20f, 0, 1);
        int hungerWidth = (int) (hungerPercent * MAX_FILL_WIDTH);
        if (hungerWidth > 0) {
            Identifier hungerTexture = HUNGER_FILL;
            if (hunger >= 16) {
                hungerTexture = HUNGER_HIGH_FILL;
            } else if (hunger <= 6) {
                hungerTexture = HUNGER_LOW_FILL;
            }
            context.drawGuiTexture(hungerTexture, MAX_FILL_WIDTH, FILL_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, hungerWidth, FILL_HEIGHT);
        }

        TextRenderer textRenderer = client.textRenderer;
        String text = hunger + " / 20";
        drawCenteredBarText(context, textRenderer, text, x, y, BAR_WIDTH, BAR_HEIGHT, 0xFFFFFFFF);
    }

    public static void renderExperienceBarDirect(DrawContext context, MinecraftClient client,
                                                  int x, int y, int currentExperience,
                                                  int nextLevelExperience, float progress) {
        context.drawGuiTexture(EMPTY_EXPERIENCE_BAR, x, y,
                EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT);

        float experienceProgress = MathHelper.clamp(progress, 0.0f, 1.0f);
        int experienceWidth = (int) (experienceProgress * EXPERIENCE_BAR_WIDTH);
        if (experienceWidth > 0) {
            context.drawGuiTexture(EXPERIENCE_FILL, EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT,
                    0, 0, x, y, experienceWidth, EXPERIENCE_BAR_HEIGHT);
        }

        String text = MathHelper.clamp(currentExperience, 0, Math.max(1, nextLevelExperience))
                + "/" + Math.max(1, nextLevelExperience);
        drawCenteredBarText(context, client.textRenderer, text, x, y,
                EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT, 0xFFFFFFFF);
    }

    private static void drawCenteredBarText(DrawContext context, TextRenderer textRenderer, String text,
                                            int x, int y, int width, int height, int color) {
        int textWidth = textRenderer.getWidth(text);

        context.getMatrices().push();
        context.getMatrices().translate(x + width / 2.0f, y + height / 2.0f, 0.0f);
        context.getMatrices().scale(BAR_TEXT_SCALE, BAR_TEXT_SCALE, 1.0f);
        context.drawTextWithShadow(textRenderer, text, -(textWidth / 2), -(textRenderer.fontHeight / 2), color);
        context.getMatrices().pop();
    }

    /** Initialize the coordinate baseline for configs created before responsive positions. */
    public static void ensurePositionReference(int screenWidth, int screenHeight) {
        var config = CranksDungeons.CONFIG;
        if (config.statBarLayoutReferenceWidth() > 0 && config.statBarLayoutReferenceHeight() > 0) {
            return;
        }

        config.statBarLayoutReferenceWidth(screenWidth);
        config.statBarLayoutReferenceHeight(screenHeight);
        config.save();
    }

    public static int resolveConfiguredX(int x, int screenWidth, int elementWidth,
                                         int referenceAnchorX, int currentAnchorX) {
        return resolveConfiguredPosition(x, screenWidth, elementWidth, referenceAnchorX, currentAnchorX);
    }

    public static int resolveConfiguredY(int y, int screenHeight, int elementHeight,
                                         int referenceAnchorY, int currentAnchorY) {
        return resolveConfiguredPosition(y, screenHeight, elementHeight, referenceAnchorY, currentAnchorY);
    }

    private static int resolveConfiguredPosition(int position, int screenExtent, int elementExtent,
                                                 int referenceAnchor, int currentAnchor) {
        int maxPosition = Math.max(0, screenExtent - elementExtent);
        // Keep the user's logical offset from the vanilla HUD anchor. The GUI
        // matrix scales this offset together with the bar, just like vanilla.
        int resolved = currentAnchor + position - referenceAnchor;
        return MathHelper.clamp(resolved, 0, maxPosition);
    }

    private void updateTrails(float health, float shield, float absorption, float mana) {
        long currentTime = net.minecraft.util.Util.getMeasuringTimeMs();
        
        if (lastRenderTime == 0) lastRenderTime = currentTime;
        float deltaSeconds = (currentTime - lastRenderTime) / 1000.0f;
        lastRenderTime = currentTime;
        
        // Cap delta to avoid huge jumps on lag spikes
        if (deltaSeconds > 0.5f) deltaSeconds = 0.5f;

        // Health trail
        if (displayedHealth < 0 || health >= displayedHealth) {
            displayedHealth = health;
            lastHealthChangeTime = currentTime;
        } else if (health < displayedHealth) {
            if (currentTime - lastHealthChangeTime > TRAIL_DELAY_MS) {
                displayedHealth -= TRAIL_SHRINK_SPEED * deltaSeconds;
                if (displayedHealth < health) displayedHealth = health;
            }
        }

        // Shield trail
        if (displayedShield < 0 || shield >= displayedShield) {
            displayedShield = shield;
            lastShieldChangeTime = currentTime;
        } else if (shield < displayedShield) {
            if (currentTime - lastShieldChangeTime > TRAIL_DELAY_MS) {
                displayedShield -= TRAIL_SHRINK_SPEED * deltaSeconds;
                if (displayedShield < shield) displayedShield = shield;
            }
        }

        // Absorption trail
        if (displayedAbsorption < 0 || absorption >= displayedAbsorption) {
            displayedAbsorption = absorption;
            lastAbsorptionChangeTime = currentTime;
        } else if (absorption < displayedAbsorption) {
            if (currentTime - lastAbsorptionChangeTime > TRAIL_DELAY_MS) {
                displayedAbsorption -= TRAIL_SHRINK_SPEED * deltaSeconds;
                if (displayedAbsorption < absorption) displayedAbsorption = absorption;
            }
        }

        // Mana trail
        if (displayedMana < 0 || mana >= displayedMana) {
            displayedMana = mana;
            lastManaChangeTime = currentTime;
        } else if (mana < displayedMana) {
            if (currentTime - lastManaChangeTime > TRAIL_DELAY_MS) {
                displayedMana -= TRAIL_SHRINK_SPEED * deltaSeconds;
                if (displayedMana < mana) displayedMana = mana;
            }
        }
    }

    public static Identifier getHealthFillTexture(net.minecraft.entity.LivingEntity entity) {
        if (entity != null) {
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.BLEED) || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BLEED)) {
                return HEALTH_BLEED_FILL;
            }
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.POISON) || entity.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON) || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.POISON)) {
                return HEALTH_POISON_FILL;
            }
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.BURN) || entity.isOnFire() || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BURN)) {
                return HEALTH_BURN_FILL;
            }
        }
        return HEALTH_FILL;
    }

    public static Identifier getHealthTrailTexture(net.minecraft.entity.LivingEntity entity) {
        if (entity != null) {
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.BLEED) || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BLEED)) {
                return HEALTH_BLEED_TRAIL;
            }
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.POISON) || entity.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON) || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.POISON)) {
                return HEALTH_POISON_TRAIL;
            }
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.BURN) || entity.isOnFire() || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BURN)) {
                return HEALTH_BURN_TRAIL;
            }
        }
        return HEALTH_TRAIL;
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new StatBarHudRenderer());
    }
}

