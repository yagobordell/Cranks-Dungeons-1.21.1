package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.StatusEffectOrientation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StatusEffectHudRenderer {

    private static final Identifier EFFECT_BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/effect_background");
    private static final Identifier EFFECT_BACKGROUND_AMBIENT_TEXTURE = Identifier.ofVanilla("hud/effect_background_ambient");

    public static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, MinecraftClient client) {
        if (client.player == null) return;
        if (client.options.hudHidden || !client.player.isAlive()) return;

        var config = CranksDungeons.CONFIG;
        if (!config.customStatusEffectOverlay()) return;

        PlayerEntity player = client.player;
        Collection<StatusEffectInstance> effects = player.getStatusEffects();
        if (effects.isEmpty()) return;

        StatusEffectOrientation orientation = config.statusEffectsOrientation();
        float scale = config.statusEffectsScale();
        int maxPerLine = config.statusEffectsMaxPerLine();
        int rawSpacing = config.statusEffectsSpacing();
        int spacing = (int) (rawSpacing * scale);
        int baseIconSize = (int) (24 * scale);
        int innerSpriteSize = (int) (18 * scale);
        int spriteOffset = (int) (3 * scale);
        boolean showDuration = config.statusEffectsShowDuration();
        boolean showAmplifier = config.statusEffectsShowAmplifier();
        int durationExtra = showDuration ? (int) (9 * scale) : 0;

        int screenWidth = client.getWindow().getScaledWidth();

        int defaultX = (orientation == StatusEffectOrientation.LEFT_TO_RIGHT) ? 5 : (screenWidth - baseIconSize - 5);
        int defaultY = (orientation == StatusEffectOrientation.BOTTOM_TO_TOP) ? (client.getWindow().getScaledHeight() - 60) : 1;
        int startX = config.statusEffectsX() >= 0 ? config.statusEffectsX() : defaultX;
        int startY = config.statusEffectsY() >= 0 ? config.statusEffectsY() : defaultY;

        List<StatusEffectInstance> sortedEffects = new ArrayList<>(effects);
        Collections.sort(sortedEffects, Collections.reverseOrder());

        RenderSystem.enableBlend();
        StatusEffectSpriteManager spriteManager = client.getStatusEffectSpriteManager();
        TextRenderer textRenderer = client.textRenderer;

        for (int i = 0; i < sortedEffects.size(); i++) {
            StatusEffectInstance effectInstance = sortedEffects.get(i);
            if (!effectInstance.shouldShowIcon()) continue;

            RegistryEntry<StatusEffect> effectType = effectInstance.getEffectType();
            Sprite sprite = spriteManager.getSprite(effectType);

            int lineIndex = i % maxPerLine;
            int crossIndex = i / maxPerLine;

            int iconX;
            int iconY;

            switch (orientation) {
                case LEFT_TO_RIGHT -> {
                    iconX = startX + (lineIndex * spacing);
                    iconY = startY + (crossIndex * (baseIconSize + durationExtra + 2));
                }
                case RIGHT_TO_LEFT -> {
                    iconX = startX - (lineIndex * spacing);
                    iconY = startY + (crossIndex * (baseIconSize + durationExtra + 2));
                }
                case TOP_TO_BOTTOM -> {
                    iconX = startX + (crossIndex * spacing);
                    iconY = startY + (lineIndex * (spacing + durationExtra));
                }
                case BOTTOM_TO_TOP -> {
                    iconX = startX + (crossIndex * spacing);
                    iconY = startY - (lineIndex * (spacing + durationExtra));
                }
                default -> {
                    iconX = startX;
                    iconY = startY;
                }
            }

            // Alpha animation when duration is low (<10 seconds / 200 ticks)
            float alpha = 1.0F;
            if (effectInstance.isDurationBelow(200)) {
                int duration = effectInstance.getDuration();
                int n = 10 - duration / 20;
                alpha = MathHelper.clamp((float) duration / 10.0F / 5.7F * 0.5F, 0.0F, 0.5F)
                        + MathHelper.cos((float) duration * (float) Math.PI / 5.0F) * MathHelper.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            // Draw Background
            Identifier bgTexture = effectInstance.isAmbient() ? EFFECT_BACKGROUND_AMBIENT_TEXTURE : EFFECT_BACKGROUND_TEXTURE;
            context.drawGuiTexture(bgTexture, iconX, iconY, baseIconSize, baseIconSize);

            // Draw Effect Icon Sprite
            context.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            context.drawSprite(iconX + spriteOffset, iconY + spriteOffset, 0, innerSpriteSize, innerSpriteSize, sprite);
            context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw Amplifier (Level)
            if (showAmplifier && effectInstance.getAmplifier() > 0) {
                String ampText = formatAmplifier(effectInstance.getAmplifier());
                context.getMatrices().push();
                float textScale = 0.65f * scale;
                context.getMatrices().translate(iconX + baseIconSize - 2, iconY + baseIconSize - (7 * scale), 0.0f);
                context.getMatrices().scale(textScale, textScale, 1.0f);
                int ampWidth = textRenderer.getWidth(ampText);
                context.drawTextWithShadow(textRenderer, ampText, -ampWidth, 0, 0xFFFFFFFF);
                context.getMatrices().pop();
            }

            // Draw Duration Text (Omit if infinite/permanent)
            if (showDuration && !isInfiniteOrPermanent(effectInstance)) {
                String durText = formatDuration(effectInstance.getDuration());
                context.getMatrices().push();
                float durScale = 0.75f * scale;
                context.getMatrices().translate(iconX + (baseIconSize / 2f), iconY + baseIconSize + 1, 0.0f);
                context.getMatrices().scale(durScale, durScale, 1.0f);
                int durWidth = textRenderer.getWidth(durText);
                context.drawTextWithShadow(textRenderer, durText, -durWidth / 2, 0, 0xFFFFFFFF);
                context.getMatrices().pop();
            }
        }

        RenderSystem.disableBlend();
    }

    public static boolean isInfiniteOrPermanent(StatusEffectInstance effectInstance) {
        if (effectInstance == null) return true;
        if (effectInstance.isInfinite()) return true;
        int duration = effectInstance.getDuration();
        // Any duration 30 minutes or more (36000 ticks) or non-positive is considered infinite/permanent
        return duration <= 0 || duration >= 36000;
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y, StatusEffectOrientation orientation, int maxPerLine, float scale, int spacingUnscaled, boolean showDuration, boolean showAmplifier) {
        int baseIconSize = 24;
        int innerSpriteSize = 18;
        int spriteOffset = 3;
        int durationExtra = showDuration ? 9 : 0;

        RenderSystem.enableBlend();
        StatusEffectSpriteManager spriteManager = client.getStatusEffectSpriteManager();
        TextRenderer textRenderer = client.textRenderer;

        List<RegistryEntry<StatusEffect>> sampleEffects = new ArrayList<>();
        sampleEffects.add(net.minecraft.entity.effect.StatusEffects.SPEED);
        sampleEffects.add(net.minecraft.entity.effect.StatusEffects.STRENGTH);
        sampleEffects.add(net.minecraft.entity.effect.StatusEffects.REGENERATION);
        sampleEffects.add(net.minecraft.entity.effect.StatusEffects.RESISTANCE);
        sampleEffects.add(net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE);
        sampleEffects.add(com.cranks.dungeons.effect.ModEffects.BLEED);
        sampleEffects.add(com.cranks.dungeons.effect.ModEffects.POISON);
        sampleEffects.add(com.cranks.dungeons.effect.ModEffects.BURN);
        sampleEffects.add(com.cranks.dungeons.effect.ModEffects.FREEZE);
        sampleEffects.add(com.cranks.dungeons.effect.ModEffects.SHOCK);

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        int spacing = spacingUnscaled;

        for (int i = 0; i < maxPerLine; i++) {
            RegistryEntry<StatusEffect> effectType = sampleEffects.get(i % sampleEffects.size());
            int lineIndex = i % maxPerLine;
            int crossIndex = i / maxPerLine;

            int iconX;
            int iconY;

            switch (orientation) {
                case LEFT_TO_RIGHT -> {
                    iconX = lineIndex * spacing;
                    iconY = crossIndex * (baseIconSize + durationExtra + 2);
                }
                case RIGHT_TO_LEFT -> {
                    iconX = (maxPerLine - 1 - lineIndex) * spacing;
                    iconY = crossIndex * (baseIconSize + durationExtra + 2);
                }
                case TOP_TO_BOTTOM -> {
                    iconX = crossIndex * spacing;
                    iconY = lineIndex * (spacing + durationExtra);
                }
                case BOTTOM_TO_TOP -> {
                    iconX = crossIndex * spacing;
                    iconY = (maxPerLine - 1 - lineIndex) * (spacing + durationExtra);
                }
                default -> {
                    iconX = 0;
                    iconY = 0;
                }
            }

            context.drawGuiTexture(EFFECT_BACKGROUND_TEXTURE, iconX, iconY, baseIconSize, baseIconSize);

            if (spriteManager != null && effectType != null) {
                Sprite sprite = spriteManager.getSprite(effectType);
                if (sprite != null) {
                    context.drawSprite(iconX + spriteOffset, iconY + spriteOffset, 0, innerSpriteSize, innerSpriteSize, sprite);
                }
            }

            if (showAmplifier && (i % 3 != 0)) {
                String ampText = String.valueOf((i % 3) + 1);
                context.getMatrices().push();
                context.getMatrices().translate(iconX + baseIconSize - 2, iconY + baseIconSize - 7, 0.0f);
                context.getMatrices().scale(0.65f, 0.65f, 1.0f);
                int ampWidth = textRenderer.getWidth(ampText);
                context.drawTextWithShadow(textRenderer, ampText, -ampWidth, 0, 0xFFFFFFFF);
                context.getMatrices().pop();
            }

            // Duration timer preview: omit for infinite sample (e.g. index 0)
            if (showDuration && i != 0) {
                String durText = String.format("%d:%02d", (maxPerLine - i) / 2, ((maxPerLine - i) * 15) % 60);
                context.getMatrices().push();
                float durScale = 0.75f;
                context.getMatrices().translate(iconX + (baseIconSize / 2f), iconY + baseIconSize + 1, 0.0f);
                context.getMatrices().scale(durScale, durScale, 1.0f);
                int durWidth = textRenderer.getWidth(durText);
                context.drawTextWithShadow(textRenderer, durText, -durWidth / 2, 0, 0xFFFFFFFF);
                context.getMatrices().pop();
            }
        }

        context.getMatrices().pop();
        RenderSystem.disableBlend();
    }

    public static String formatAmplifier(int amplifier) {
        if (amplifier <= 0) return "";
        return String.valueOf(amplifier + 1);
    }
}
