package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class, priority = 100)
public abstract class InGameHudMixin {

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void cancelStatusBars(DrawContext context, CallbackInfo ci) {
        if (CranksDungeons.CONFIG.useStatBars()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void cancelHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (CranksDungeons.CONFIG.useStatBars()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAir()I"))
    private int redirectGetAir(PlayerEntity player) {
        if (CranksDungeons.CONFIG.useStatBars()) {
            return player.getMaxAir();
        }
        return player.getAir();
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean redirectIsSubmergedIn(PlayerEntity player, net.minecraft.registry.tag.TagKey<net.minecraft.fluid.Fluid> fluidTag) {
        if (CranksDungeons.CONFIG.useStatBars() && fluidTag == net.minecraft.registry.tag.FluidTags.WATER) {
            return false;
        }
        return player.isSubmergedIn(fluidTag);
    }
}
