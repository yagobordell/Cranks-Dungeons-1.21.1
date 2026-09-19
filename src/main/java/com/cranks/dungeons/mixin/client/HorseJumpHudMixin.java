package com.cranks.dungeons.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.Entity;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HorseJumpHudMixin {
    @Shadow @org.spongepowered.asm.mixin.Final
    private MinecraftClient client;

    @Shadow
    private void renderExperienceBar(DrawContext context, int x) {
        throw new AssertionError();
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"), cancellable = true)
    private void cranks$showExperienceInsteadOfHorseJumpBar(
            JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        // All mounts supported by the mount system should not use vanilla's
        // jump bar. The player's XP bar is rendered from renderMainHud below,
        // which also covers mounts that do not implement JumpingMount.
        if (!(mount instanceof Entity entity)
                || MountKind.of(entity) == MountKind.UNSUPPORTED) return;
        ci.cancel();
    }

    @Inject(method = "renderMainHud", at = @At("TAIL"))
    private void cranks$renderExperienceWhileMounted(
            DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client.player == null
                || !(client.player.getVehicle() instanceof Entity vehicle)
                || MountKind.of(vehicle) == MountKind.UNSUPPORTED) return;

        // Match vanilla's status-bar visibility rules without using
        // shouldRenderExperience(), whose vehicle check is exactly what hides
        // the XP bar in the first place.
        if (client.interactionManager == null) return;
        GameMode mode = client.interactionManager.getCurrentGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) return;

        int x = context.getScaledWindowWidth() / 2 - 91;
        renderExperienceBar(context, x);
    }
}
