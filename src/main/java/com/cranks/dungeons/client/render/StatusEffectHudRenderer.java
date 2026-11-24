package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.StatusEffectOrientation;
import com.cranks.dungeons.effect.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class StatusEffectHudRenderer {
    private static final int TILE_SIZE = 24;
    private static final int TIMER_HEIGHT = 9;
    private static final int PAGE_BAR_HEIGHT = 14;
    private static final int PAGE_BAR_GAP = 3;
    private static final long PAGE_INTERVAL_MS = 5_000;
    private static int pageIndex;
    private static long pageChangedAt;
    private static UUID playerId;
    private static StatusEffectOrientation pageOrientation;

    private record EffectTile(StatusEffectInstance instance, float alpha, String duration) {
        RegistryEntry<StatusEffect> effect() { return instance.getEffectType(); }
        boolean ambient() { return instance.isAmbient(); }
        int stacks() { return effect().equals(ModEffects.POISON) ? instance.getAmplifier() + 1 : 1; }
    }

    public record EffectBounds(StatusEffectInstance effect, int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }
    }

    public record PreviewBounds(int width, int height, int anchorX, int anchorY) {}
    public record Region(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }
    }
    public record Navigation(Region bounds, Region previous, Region next, int page, int pageCount) {}
    public record Display(List<EffectBounds> effects, Navigation navigation) {}
    private record FittedLayout(StatusEffectLayout.Pages pages, float scale, int navigationWidth) {
        int width() { return Math.max(pages.width(), navigationWidth); }
        int height() { return pages.height() + (navigationWidth > 0 ? PAGE_BAR_GAP + PAGE_BAR_HEIGHT : 0); }
        int anchorX() { return pages.anchorX() + (width() - pages.width()) / 2; }
        int anchorY() { return pages.anchorY(); }
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter, MinecraftClient client) {
        if (client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen) return;
        renderEffects(context, client);
    }

    public static List<EffectBounds> renderEffects(DrawContext context, MinecraftClient client) {
        return renderDisplay(context, client, false).effects();
    }

    public static Display renderDisplay(DrawContext context, MinecraftClient client, boolean interactive) {
        if (client.player == null) return new Display(List.of(), null);
        var effects = client.player.getStatusEffects();
        if (effects.isEmpty()) {
            pageIndex = 0;
            pageChangedAt = Util.getMeasuringTimeMs();
            return new Display(List.of(), null);
        }

        var config = CranksDungeons.CONFIG;
        StatusEffectOrientation orientation = config.statusEffectsOrientation();
        float scale = config.statusEffectsScale();
        int maxPerLine = config.statusEffectsMaxPerLine();
        int rawSpacing = config.statusEffectsSpacing();
        int iconSize = (int) (TILE_SIZE * scale);
        boolean showDuration = config.statusEffectsShowDuration();

        int screenWidth = client.getWindow().getScaledWidth();

        int defaultX = (orientation == StatusEffectOrientation.LEFT_TO_RIGHT) ? 5 : (screenWidth - iconSize - 5);
        int defaultY = (orientation == StatusEffectOrientation.BOTTOM_TO_TOP) ? (client.getWindow().getScaledHeight() - 60) : 1;
        int startX = config.statusEffectsX() >= 0 ? config.statusEffectsX() : defaultX;
        int startY = config.statusEffectsY() >= 0 ? config.statusEffectsY() : defaultY;

        List<StatusEffectInstance> sortedEffects = new ArrayList<>(effects);
        sortedEffects.removeIf(effect -> !effect.shouldShowIcon());
        sortedEffects.sort(Collections.reverseOrder());
        if (sortedEffects.isEmpty()) return new Display(List.of(), null);

        List<EffectTile> tiles = new ArrayList<>(sortedEffects.size());
        for (StatusEffectInstance effectInstance : sortedEffects) {
            // Alpha animation when duration is low (<10 seconds / 200 ticks)
            float alpha = 1.0F;
            if (effectInstance.isDurationBelow(200)) {
                int duration = effectInstance.getDuration();
                int n = 10 - duration / 20;
                alpha = MathHelper.clamp((float) duration / 10.0F / 5.7F * 0.5F, 0.0F, 0.5F)
                        + MathHelper.cos((float) duration * (float) Math.PI / 5.0F) * MathHelper.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            String duration = showDuration && !isInfiniteOrPermanent(effectInstance)
                    ? formatDuration(effectInstance.getDuration()) : null;
            tiles.add(new EffectTile(effectInstance, alpha, duration));
        }
        int screenHeight = client.getWindow().getScaledHeight();
        int availableWidth = Math.max(1, screenWidth - 4);
        int availableHeight = Math.max(1, screenHeight - 4);
        int columnDirection = startX < screenWidth / 2 ? 1 : -1;
        var fitted = fit(tiles, orientation, maxPerLine, scale, rawSpacing, availableWidth, availableHeight, columnDirection);
        int pageCount = fitted.pages().pages().size();
        long now = Util.getMeasuringTimeMs();
        if (!client.player.getUuid().equals(playerId) || pageOrientation != orientation) {
            playerId = client.player.getUuid();
            pageOrientation = orientation;
            pageIndex = 0;
            pageChangedAt = now;
        }
        pageIndex = Math.clamp(pageIndex, 0, pageCount - 1);
        if (interactive || pageCount <= 1) pageChangedAt = now;
        else if (pageCount > 1 && now - pageChangedAt >= PAGE_INTERVAL_MS) {
            pageIndex = (pageIndex + 1) % pageCount;
            pageChangedAt = now;
        }
        int left = Math.clamp(startX - fitted.anchorX(), 2, Math.max(2, screenWidth - fitted.width() - 2));
        int top = Math.clamp(startY - fitted.anchorY(), 2, Math.max(2, screenHeight - fitted.height() - 2));
        return renderPage(context, client, tiles, fitted, pageIndex, left, top);
    }

    public static void changePage(int direction, int pageCount) {
        if (pageCount <= 1) return;
        pageIndex = Math.floorMod(pageIndex + direction, pageCount);
        pageChangedAt = Util.getMeasuringTimeMs();
    }

    public static boolean isInfiniteOrPermanent(StatusEffectInstance effectInstance) {
        if (effectInstance == null) return true;
        if (effectInstance.isInfinite()) return true;
        int duration = effectInstance.getDuration();
        return duration <= 0;
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static PreviewBounds previewBounds(StatusEffectOrientation orientation, int count, float scale,
                                              int rawSpacing, boolean showDuration) {
        var fitted = previewLayout(previewTiles(count, showDuration), orientation, count, scale, rawSpacing);
        return new PreviewBounds(fitted.width(), fitted.height(), fitted.anchorX(), fitted.anchorY());
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y,
                                     StatusEffectOrientation orientation, int maxPerLine, float scale,
                                     int rawSpacing, boolean showDuration) {
        var tiles = previewTiles(maxPerLine, showDuration);
        var fitted = previewLayout(tiles, orientation, maxPerLine, scale, rawSpacing);
        if (!fitted.pages().pages().isEmpty()) renderPage(context, client, tiles, fitted, 0, x, y);
    }

    private static FittedLayout previewLayout(List<EffectTile> tiles, StatusEffectOrientation orientation,
                                               int maxPerLine, float scale, int rawSpacing) {
        var window = MinecraftClient.getInstance().getWindow();
        int startX = CranksDungeons.CONFIG.statusEffectsX();
        int columnDirection = startX >= 0 && startX < window.getScaledWidth() / 2 ? 1 : -1;
        return fit(tiles, orientation, maxPerLine, scale, rawSpacing,
                Math.max(1, window.getScaledWidth() - 4), Math.max(1, window.getScaledHeight() - 4), columnDirection);
    }

    private static FittedLayout fit(List<EffectTile> tiles, StatusEffectOrientation orientation, int maxPerLine,
                                     float scale, int rawSpacing, int width, int height, int columnDirection) {
        var fitted = paginatedLayout(tiles, orientation, maxPerLine, scale, rawSpacing, width, height, columnDirection);
        while ((fitted.width() > width || fitted.height() > height) && scale > 0.5f) {
            scale = Math.max(0.5f, scale * 0.9f);
            fitted = paginatedLayout(tiles, orientation, maxPerLine, scale, rawSpacing, width, height, columnDirection);
        }
        return fitted;
    }

    private static List<EffectTile> previewTiles(int count, boolean showDuration) {
        List<RegistryEntry<StatusEffect>> samples = List.of(
                StatusEffects.SPEED, StatusEffects.STRENGTH, StatusEffects.REGENERATION, StatusEffects.RESISTANCE,
                StatusEffects.FIRE_RESISTANCE, ModEffects.BLEED, ModEffects.POISON, ModEffects.BURN,
                ModEffects.FREEZE, ModEffects.SHOCK);
        List<EffectTile> tiles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RegistryEntry<StatusEffect> effect = samples.get(i % samples.size());
            String duration = showDuration && i != 0 ? formatDuration((count - i) * 15 * 20) : null;
            tiles.add(new EffectTile(new StatusEffectInstance(effect, 1200, effect.equals(ModEffects.POISON) ? 2 : 0,
                    i == 0, false, true), 1, duration));
        }
        return tiles;
    }

    private static FittedLayout paginatedLayout(List<EffectTile> tiles, StatusEffectOrientation orientation,
                                                    int maxPerLine, float scale, int rawSpacing,
                                                    int maxWidth, int maxHeight, int columnDirection) {
        int size = Math.max(1, (int) (TILE_SIZE * scale));
        var heights = tiles.stream().map(tile -> size + (tile.duration() == null ? 0 : (int) (TIMER_HEIGHT * scale))).toList();
        var pages = StatusEffectLayout.paginate(heights, size, (int) (rawSpacing * scale), maxPerLine,
                orientation, maxWidth, Math.max(1, maxHeight - PAGE_BAR_HEIGHT - PAGE_BAR_GAP), columnDirection);
        int count = pages.pages().size();
        int navigationWidth = count > 1 ? 36 + (int) Math.ceil(MinecraftClient.getInstance().textRenderer.getWidth(count + "/" + count) * 0.65f) : 0;
        return new FittedLayout(pages, scale, navigationWidth);
    }

    private static Display renderPage(DrawContext context, MinecraftClient client, List<EffectTile> tiles,
                                       FittedLayout fitted, int pageIndex, int left, int top) {
        var page = fitted.pages().pages().get(pageIndex);
        var layout = page.layout();
        var bounds = renderTiles(context, client, tiles.subList(page.start(), page.end()), layout,
                left + fitted.anchorX() - layout.anchorX(), top + fitted.anchorY() - layout.anchorY(), fitted.scale());
        if (fitted.navigationWidth() == 0) return new Display(bounds, null);
        int x = left + (fitted.width() - fitted.navigationWidth()) / 2;
        int y = top + fitted.pages().height() + PAGE_BAR_GAP;
        var bar = new Region(x, y, fitted.navigationWidth(), PAGE_BAR_HEIGHT);
        var previous = new Region(x, y, PAGE_BAR_HEIGHT, PAGE_BAR_HEIGHT);
        var next = new Region(x + bar.width() - PAGE_BAR_HEIGHT, y, PAGE_BAR_HEIGHT, PAGE_BAR_HEIGHT);
        HudStyle.plate(context, x, y, bar.width(), bar.height(), false);
        HudStyle.drawLabel(context, client.textRenderer, "‹", previous.x() + 7, y + 7, 0.8f, 0xFFE6E6E6);
        HudStyle.drawLabel(context, client.textRenderer, "›", next.x() + 7, y + 7, 0.8f, 0xFFE6E6E6);
        int count = fitted.pages().pages().size();
        HudStyle.drawLabel(context, client.textRenderer, (pageIndex + 1) + "/" + count,
                x + bar.width() / 2f, y + 7, 0.65f, 0xFFE6E6E6);
        return new Display(bounds, new Navigation(bar, previous, next, pageIndex, count));
    }

    private static List<EffectBounds> renderTiles(DrawContext context, MinecraftClient client, List<EffectTile> tiles,
                                                 StatusEffectLayout.Layout layout, int left, int top, float scale) {
        var bounds = new ArrayList<EffectBounds>(tiles.size());
        var spriteManager = client.getStatusEffectSpriteManager();

        RenderSystem.enableBlend();
        try {
            for (int i = 0; i < tiles.size(); i++) {
                EffectTile tile = tiles.get(i);
                var cell = layout.cells().get(i);
                int x = left + cell.x(), y = top + cell.y();
                drawEffect(context, client.textRenderer, spriteManager.getSprite(tile.effect()),
                        x, y, cell.width(), scale, tile, cell.height() - cell.width());
                bounds.add(new EffectBounds(tile.instance(), x, y, cell.width(), cell.height()));
            }
        } finally {
            RenderSystem.disableBlend();
        }
        return bounds;
    }

    private static void drawEffect(DrawContext context, TextRenderer font, Sprite sprite, int x, int y, int size,
                                   float scale, EffectTile tile, int timerHeight) {
        String duration = tile.duration();
        HudStyle.plate(context, x, y, size, size + timerHeight, tile.ambient());
        int spriteSize = Math.round(size * 0.75f);
        int inset = (size - spriteSize) / 2;
        context.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.clamp(tile.alpha(), 0, 1));
        try {
            context.drawSprite(x + inset, y + inset, 0, spriteSize, spriteSize, sprite);
        } finally {
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        if (tile.stacks() > 1) {
            String text = Integer.toString(tile.stacks());
            float textScale = 0.65f * scale;
            float centerX = x + size - 3 * scale - font.getWidth(text) * textScale / 2;
            HudStyle.drawLabel(context, font, text, centerX, y + size - 5 * scale, textScale, 0xFFF2F2F2);
        }

        if (duration != null) {
            HudStyle.divider(context, x + 3, x + size - 3, y + size - 1);
            float textScale = Math.min(0.72f * scale, (size - 6f) / font.getWidth(duration));
            HudStyle.drawLabel(context, font, duration, x + size / 2f,
                    y + size + timerHeight / 2f - scale, textScale, 0xFFE6E6E6);
        }
    }
}
