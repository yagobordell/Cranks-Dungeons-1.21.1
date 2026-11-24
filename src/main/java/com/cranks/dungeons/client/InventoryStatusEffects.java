package com.cranks.dungeons.client;

import com.cranks.dungeons.effect.EffectDescriptions;
import com.cranks.dungeons.client.render.StatusEffectHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/** Inventory paging and tooltips over the same effect layout used during gameplay. */
public final class InventoryStatusEffects {
    private List<StatusEffectHudRenderer.EffectBounds> bounds = List.of();
    private StatusEffectHudRenderer.Navigation navigation;

    public void render(DrawContext context, MinecraftClient client) {
        context.getMatrices().push();
        try {
            context.getMatrices().translate(0, 0, 320);
            var display = StatusEffectHudRenderer.renderDisplay(context, client, true);
            bounds = display.effects();
            navigation = display.navigation();
        } finally {
            context.getMatrices().pop();
        }
    }

    public boolean contains(double mouseX, double mouseY) {
        return navigation != null && navigation.bounds().contains(mouseX, mouseY) || effectAt(mouseX, mouseY) != null;
    }

    public boolean click(double mouseX, double mouseY, int button) {
        if (navigation != null && button == 0) {
            if (navigation.previous().contains(mouseX, mouseY)) StatusEffectHudRenderer.changePage(-1, navigation.pageCount());
            else if (navigation.next().contains(mouseX, mouseY)) StatusEffectHudRenderer.changePage(1, navigation.pageCount());
        }
        return contains(mouseX, mouseY);
    }

    public boolean scroll(double mouseX, double mouseY, double vertical) {
        if (!contains(mouseX, mouseY)) return false;
        if (navigation != null) StatusEffectHudRenderer.changePage(-(int) Math.signum(vertical), navigation.pageCount());
        return true;
    }

    public void tooltip(DrawContext context, MinecraftClient client, int mouseX, int mouseY) {
        if (navigation != null && navigation.bounds().contains(mouseX, mouseY)) {
            Text title = Text.translatable("hud.cranks-dungeons.effects.page", navigation.page() + 1, navigation.pageCount());
            if (navigation.previous().contains(mouseX, mouseY)) title = Text.translatable("hud.cranks-dungeons.effects.previous");
            else if (navigation.next().contains(mouseX, mouseY)) title = Text.translatable("hud.cranks-dungeons.effects.next");
            var lines = new ArrayList<OrderedText>();
            lines.add(title.asOrderedText());
            lines.addAll(client.textRenderer.wrapLines(Text.translatable("hud.cranks-dungeons.effects.browse")
                    .formatted(Formatting.GRAY), Math.min(220, client.getWindow().getScaledWidth() - 24)));
            context.drawOrderedTooltip(client.textRenderer, lines, mouseX, mouseY);
            return;
        }
        var hovered = effectAt(mouseX, mouseY);
        if (hovered == null) return;
        List<OrderedText> lines = new ArrayList<>();
        float tickRate = client.world.getTickManager().getTickRate();
        var details=com.cranks.dungeons.client.render.AilmentIconRenderer.details(client.player.getId(),hovered.effect());
        for (var text : EffectDescriptions.activeTooltip(hovered.effect(), tickRate, details)) {
            var linked=com.cranks.dungeons.client.guide.GuideText.link(text,new com.cranks.dungeons.client.guide.GuideTarget(
                    com.cranks.dungeons.client.guide.GuideTarget.Kind.EFFECT,net.minecraft.registry.Registries.STATUS_EFFECT.getId(hovered.effect().getEffectType().value()).toString()));
            lines.addAll(client.textRenderer.wrapLines(linked, Math.min(220, client.getWindow().getScaledWidth() - 24)));
        }
        context.drawOrderedTooltip(client.textRenderer, lines, mouseX, mouseY);
    }

    private StatusEffectHudRenderer.EffectBounds effectAt(double mouseX, double mouseY) {
        for (var effect : bounds) if (effect.contains(mouseX, mouseY)) return effect;
        return null;
    }
}
