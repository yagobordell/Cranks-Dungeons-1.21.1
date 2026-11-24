package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.render.HudStyle;
import com.cranks.dungeons.client.render.HudStyle.Ink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
abstract class BossBarHudMixin {
    @Inject(method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;)V",
            at = @At("HEAD"), cancellable = true)
    private void cranks$drawBossMeter(DrawContext context, int x, int y, BossBar bar, CallbackInfo ci) {
        if (!CranksDungeons.CONFIG.useStatBars()) return;
        Ink ink = switch (bar.getColor()) {
            case PINK -> Ink.BOSS_PINK;
            case BLUE -> Ink.MANA;
            case RED -> Ink.HEALTH;
            case GREEN -> Ink.XP;
            case YELLOW -> Ink.FOOD_HIGH;
            case PURPLE -> Ink.BOSS_PURPLE;
            case WHITE -> Ink.SHIELD;
        };
        HudStyle.meter(context, x, y, 182, 5, ink, bar.getPercent());
        int sections = switch (bar.getStyle()) {
            case PROGRESS -> 1;
            case NOTCHED_6 -> 6;
            case NOTCHED_10 -> 10;
            case NOTCHED_12 -> 12;
            case NOTCHED_20 -> 20;
        };
        for (int section = 1; section < sections; section++) {
            int divider = x + 2 + 178 * section / sections;
            context.fill(divider, y + 2, divider + 1, y + 3, 0xFF101010);
        }
        ci.cancel();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
    private int cranks$drawBossName(DrawContext context, TextRenderer renderer, Text name, int x, int y,
                                    int color, Operation<Integer> original) {
        if (CranksDungeons.CONFIG.useStatBars()) {
            HudStyle.plate(context, x - 4, y - 1, renderer.getWidth(name) + 8, 10, false);
        }
        return original.call(context, renderer, name, x, y, color);
    }
}
