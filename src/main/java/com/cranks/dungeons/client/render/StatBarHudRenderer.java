package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.client.render.HudStyle.Ink;
import com.cranks.dungeons.effect.ModEffects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class StatBarHudRenderer implements HudRenderCallback {

    // Default dimensions for the bar (scaled to match a vanilla heart row: 81x9)
    public static final int BAR_WIDTH = 81;
    public static final int BAR_HEIGHT = 9;
    public static final int EXPERIENCE_BAR_WIDTH = 182;
    public static final int EXPERIENCE_BAR_HEIGHT = 5;
    public static final int SHIELD_HEIGHT = 6;

    private float displayedHealth = -1;
    private long lastHealthChangeTime = 0;
    
    private float displayedShield = -1;
    private long lastShieldChangeTime = 0;

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
        if (client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen) return;
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
        int defaultHealthY = screenHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        int referenceHealthX = referenceWidth / 2 - 91;
        int referenceHealthY = referenceHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
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
        int defaultHungerY = screenHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        int referenceHungerX = referenceWidth / 2 + 10;
        int referenceHungerY = referenceHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
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
        int defaultExperienceY = screenHeight - 29 - HotbarHudRenderer.DEFAULT_LIFT;
        int referenceExperienceX = referenceWidth / 2 - EXPERIENCE_BAR_WIDTH / 2;
        int referenceExperienceY = referenceHeight - 29 - HotbarHudRenderer.DEFAULT_LIFT;
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

        updateTrails(health, shield, mana);

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

    public static void renderHealthBarDirect(DrawContext context, MinecraftClient client, PlayerEntity player,
                                             int x, int y, float health, float maxHealth,
                                             float absorption, float displayedHealth) {
        drawHealthMeter(context, x, y, BAR_WIDTH, BAR_HEIGHT, player, health, maxHealth, absorption, displayedHealth);
        if (player != null && (player.getAir() < player.getMaxAir()
                || player.isSubmergedIn(net.minecraft.registry.tag.FluidTags.WATER))) {
            HudStyle.overlayMeter(context, x, y, BAR_WIDTH, BAR_HEIGHT, Ink.AIR,
                    ratio(player.getAir(), player.getMaxAir()));
        }
        drawHealthLabel(context, client, x, y, health, maxHealth, absorption);
    }

    public static void drawHealthMeter(DrawContext context, int x, int y, int width, int height, LivingEntity entity,
                                       float health, float maxHealth, float absorption, float displayedHealth) {
        HudStyle.meter(context, x, y, width, height, healthInk(entity),
                ratio(health, maxHealth), ratio(displayedHealth, maxHealth));
        if (absorption > 0) {
            HudStyle.overlayMeter(context, x, y, width, height, Ink.ABSORPTION, ratio(absorption, maxHealth));
        }
    }

    public static void drawHealthLabel(DrawContext context, MinecraftClient client, int x, int y,
                                       float health, float maxHealth, float absorption) {
        String label = (int) health + " / " + (int) maxHealth;
        if (absorption > 0) label += " (+" + (int) absorption + ")";
        HudStyle.meterLabel(context, client.textRenderer, label, x, y, BAR_WIDTH, BAR_HEIGHT,
                absorption > 0 ? 0xFFFFDB9A : 0xFFFFFFFF);
    }

    public static void renderShieldBarDirect(DrawContext context, MinecraftClient client, int x, int y,
                                             float shield, float maxShield, float displayedShield) {
        HudStyle.meter(context, x, y, BAR_WIDTH, SHIELD_HEIGHT, Ink.SHIELD,
                ratio(shield, maxShield), ratio(displayedShield, maxShield));
        HudStyle.meterLabel(context, client.textRenderer, (int) shield + " / " + (int) maxShield,
                x, y, BAR_WIDTH, SHIELD_HEIGHT, 0xFFFFFFFF);
    }

    public static void renderManaBarDirect(DrawContext context, MinecraftClient client, int x, int y,
                                           float mana, float maxMana, float displayedMana) {
        HudStyle.meter(context, x, y, BAR_WIDTH, BAR_HEIGHT, Ink.MANA, ratio(mana, maxMana), ratio(displayedMana, maxMana));
        HudStyle.meterLabel(context, client.textRenderer, (int) mana + " / " + (int) maxMana,
                x, y, BAR_WIDTH, BAR_HEIGHT, 0xFFFFFFFF);
    }

    public static void renderHungerBarDirect(DrawContext context, MinecraftClient client, int x, int y, int hunger) {
        Ink ink = hunger >= 16 ? Ink.FOOD_HIGH : hunger <= 6 ? Ink.FOOD_LOW : Ink.FOOD;
        HudStyle.meter(context, x, y, BAR_WIDTH, BAR_HEIGHT, ink, hunger / 20f);
        HudStyle.meterLabel(context, client.textRenderer, hunger + " / 20", x, y, BAR_WIDTH, BAR_HEIGHT, 0xFFFFFFFF);
    }

    public static void renderExperienceBarDirect(DrawContext context, MinecraftClient client, int x, int y,
                                                  int currentExperience, int nextLevelExperience, float progress) {
        HudStyle.meter(context, x, y, EXPERIENCE_BAR_WIDTH, EXPERIENCE_BAR_HEIGHT, Ink.XP, progress);
        String text = client.player!=null && com.cranks.dungeons.component.ModComponents.PROGRESSION.get(client.player).required()==0 ? "Max level" : MathHelper.clamp(currentExperience, 0, Math.max(1, nextLevelExperience))
                + "/" + Math.max(1, nextLevelExperience);
        HudStyle.badge(context, client.textRenderer, text, x + EXPERIENCE_BAR_WIDTH / 2f,
                y + EXPERIENCE_BAR_HEIGHT / 2f, 0.65f, 0xFFE0E7D6);
    }

    public static float ratio(float value, float maximum) {
        return maximum > 0 ? value / maximum : 0;
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

    private void updateTrails(float health, float shield, float mana) {
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

    private static Ink healthInk(LivingEntity entity) {
        if (entity == null) return Ink.HEALTH;
        if (entity.hasStatusEffect(ModEffects.BLEED) || AilmentManager.hasAilment(entity, AilmentType.BLEED)) {
            return Ink.BLEED;
        }
        if (entity.hasStatusEffect(ModEffects.POISON) || entity.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON)
                || AilmentManager.hasAilment(entity, AilmentType.POISON)) {
            return Ink.POISON;
        }
        if (entity.hasStatusEffect(ModEffects.BURN) || entity.isOnFire() || AilmentManager.hasAilment(entity, AilmentType.BURN)) {
            return Ink.BURN;
        }
        return Ink.HEALTH;
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new StatBarHudRenderer());
    }
}

