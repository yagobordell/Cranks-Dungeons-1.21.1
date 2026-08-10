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

    // Default dimensions for the bar (scaled to match a vanilla heart row: 81x9)
    public static final int BAR_WIDTH = 81;
    public static final int BAR_HEIGHT = 9;
    public static final int FILL_OFFSET_X = 0;
    public static final int FILL_OFFSET_Y = 0;
    public static final int MAX_FILL_WIDTH = 81;
    public static final int FILL_HEIGHT = 9;

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

        // Position health bar at vanilla spot
        int x = screenWidth / 2 - 91;
        int y = screenHeight - 39;

        float mana = 0;
        float maxMana = 0;
        if (player instanceof com.cranks.dungeons.ManaProvider provider) {
            mana = provider.cranksdungeons$getCurrentMana();
            maxMana = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.MAX_MANA);
        }

        renderHealthAndShield(context, client, player, x, y, mana);
        renderManaAndHunger(context, client, player, screenWidth / 2 + 10, y, mana, maxMana);
    }

    private void renderHealthAndShield(DrawContext context, MinecraftClient client, PlayerEntity player, int x, int y, float mana) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();
        float shield = 0;
        float maxShield = 0;

        if (player instanceof EnergyShieldProvider provider) {
            shield = provider.cranksdungeons$getCurrentEnergyShield();
            maxShield = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
        }

        updateTrails(health, shield, absorption, mana);

        // --- Render Shield Bar (Above HP) ---
        if (maxShield > 0) {
            int shieldY = y - 7; // 6px height + 1px gap
            int SHIELD_HEIGHT = 6;
            
            // Draw Shield Background
            context.drawGuiTexture(EMPTY_SHIELD_BAR, x, shieldY, BAR_WIDTH, SHIELD_HEIGHT);

            float shieldPercent = MathHelper.clamp(shield / maxShield, 0, 1);
            int shieldWidth = (int) (shieldPercent * MAX_FILL_WIDTH);
            
            // Draw Shield Trail
            if (displayedShield > shield) {
                int trailWidth = (int) (MathHelper.clamp(displayedShield / maxShield, 0, 1) * MAX_FILL_WIDTH);
                if (trailWidth > shieldWidth) {
                    context.drawGuiTexture(SHIELD_TRAIL, MAX_FILL_WIDTH, SHIELD_HEIGHT, shieldWidth, 0,
                            x + FILL_OFFSET_X + shieldWidth, shieldY + FILL_OFFSET_Y, trailWidth - shieldWidth, SHIELD_HEIGHT);
                }
            }

            // Draw Shield Fill
            if (shieldWidth > 0) {
                context.drawGuiTexture(SHIELD_FILL, MAX_FILL_WIDTH, SHIELD_HEIGHT, 0, 0,
                        x + FILL_OFFSET_X, shieldY + FILL_OFFSET_Y, shieldWidth, SHIELD_HEIGHT);
            }

            // Shield Text
            String sText = (int) shield + " / " + (int) maxShield;
            TextRenderer tr = client.textRenderer;
            int sTextWidth = tr.getWidth(sText);

            context.getMatrices().push();
            context.getMatrices().translate(x + (BAR_WIDTH / 2f), shieldY + (SHIELD_HEIGHT / 2f), 0.0f);
            context.getMatrices().scale(0.8f, 0.8f, 1.0f);
            context.drawTextWithShadow(tr, sText, -(sTextWidth / 2), -(tr.fontHeight / 2), 0xFFFFFFFF);
            context.getMatrices().pop();
        }

        // --- Render Health Bar ---
        // Draw Background Frame
        context.drawGuiTexture(EMPTY_BAR, x, y, BAR_WIDTH, BAR_HEIGHT);

        float hpPercent = MathHelper.clamp(health / maxHealth, 0, 1);
        int hpWidth = (int) (hpPercent * MAX_FILL_WIDTH);
        
        // Draw Health Trail
        if (displayedHealth > health) {
            int trailWidth = (int) (MathHelper.clamp(displayedHealth / maxHealth, 0, 1) * MAX_FILL_WIDTH);
            if (trailWidth > hpWidth) {
                context.drawGuiTexture(HEALTH_TRAIL, MAX_FILL_WIDTH, FILL_HEIGHT, hpWidth, 0,
                        x + FILL_OFFSET_X + hpWidth, y + FILL_OFFSET_Y, trailWidth - hpWidth, FILL_HEIGHT);
            }
        }

        // Draw HP Fill (Red)
        if (hpWidth > 0) {
            context.drawGuiTexture(HEALTH_FILL, MAX_FILL_WIDTH, FILL_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, hpWidth, FILL_HEIGHT);
        }

        float absPercent = MathHelper.clamp(absorption / maxHealth, 0, 1);
        int absWidth = (int) (absPercent * MAX_FILL_WIDTH);
        
        // Draw Absorption Trail
        if (displayedAbsorption > absorption) {
            int trailWidth = (int) (MathHelper.clamp(displayedAbsorption / maxHealth, 0, 1) * MAX_FILL_WIDTH);
            if (trailWidth > absWidth) {
                context.drawGuiTexture(ABSORPTION_TRAIL, MAX_FILL_WIDTH, FILL_HEIGHT, absWidth, 0,
                        x + FILL_OFFSET_X + absWidth, y + FILL_OFFSET_Y, trailWidth - absWidth, FILL_HEIGHT);
            }
        }

        // Draw Absorption Overlay (Yellow)
        if (absWidth > 0) {
            context.drawGuiTexture(ABSORPTION_FILL, MAX_FILL_WIDTH, FILL_HEIGHT, 0, 0,
                    x + FILL_OFFSET_X, y + FILL_OFFSET_Y, absWidth, FILL_HEIGHT);
        }

        // --- Render Oxygen Overlay ---
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

        // Health Text
        TextRenderer textRenderer = client.textRenderer;
        String hText = (int) health + " / " + (int) maxHealth;
        int hColor = 0xFFFFFFFF;
        if (absorption > 0) {
            hText += " (+" + (int) absorption + ")";
            hColor = 0xFFFFAA00;
        }

        int hTextWidth = textRenderer.getWidth(hText);
        context.drawTextWithShadow(textRenderer, hText, x + (BAR_WIDTH / 2) - (hTextWidth / 2),
                y + (BAR_HEIGHT / 2) - (textRenderer.fontHeight / 2), hColor);
    }

    private void renderManaAndHunger(DrawContext context, MinecraftClient client, PlayerEntity player, int x, int y, float mana, float maxMana) {
        int hunger = player.getHungerManager().getFoodLevel();

        // --- Render Mana Bar (Above Hunger) ---
        if (maxMana > 0) {
            int manaY = y - 10;
            int MANA_HEIGHT = 9;
            
            context.drawGuiTexture(EMPTY_BAR, x, manaY, BAR_WIDTH, MANA_HEIGHT);

            float manaPercent = MathHelper.clamp(mana / maxMana, 0, 1);
            int manaWidth = (int) (manaPercent * MAX_FILL_WIDTH);
            
            if (displayedMana > mana) {
                int trailWidth = (int) (MathHelper.clamp(displayedMana / maxMana, 0, 1) * MAX_FILL_WIDTH);
                if (trailWidth > manaWidth) {
                    context.drawGuiTexture(MANA_TRAIL, MAX_FILL_WIDTH, MANA_HEIGHT, manaWidth, 0,
                            x + FILL_OFFSET_X + manaWidth, manaY + FILL_OFFSET_Y, trailWidth - manaWidth, MANA_HEIGHT);
                }
            }

            if (manaWidth > 0) {
                context.drawGuiTexture(MANA_FILL, MAX_FILL_WIDTH, MANA_HEIGHT, 0, 0,
                        x + FILL_OFFSET_X, manaY + FILL_OFFSET_Y, manaWidth, MANA_HEIGHT);
            }

            String mText = (int) mana + " / " + (int) maxMana;
            TextRenderer tr = client.textRenderer;
            int mTextWidth = tr.getWidth(mText);

            context.drawTextWithShadow(tr, mText, x + (BAR_WIDTH / 2) - (mTextWidth / 2),
                    manaY + (MANA_HEIGHT / 2) - (tr.fontHeight / 2), 0xFFFFFFFF);
        }

        // Draw Background Frame
        context.drawGuiTexture(EMPTY_BAR, x, y, BAR_WIDTH, BAR_HEIGHT);

        // Draw Hunger Fill
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

        // Text Display
        TextRenderer textRenderer = client.textRenderer;
        String text = hunger + " / 20";
        int textWidth = textRenderer.getWidth(text);
        context.drawTextWithShadow(textRenderer, text, x + (BAR_WIDTH / 2) - (textWidth / 2),
                y + (BAR_HEIGHT / 2) - (textRenderer.fontHeight / 2), 0xFFFFFFFF);
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

    public static void register() {
        HudRenderCallback.EVENT.register(new StatBarHudRenderer());
    }
}

