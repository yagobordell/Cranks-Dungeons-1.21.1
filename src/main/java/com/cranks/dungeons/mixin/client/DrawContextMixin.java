package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawContext.class, priority = 1)
public abstract class DrawContextMixin {

    @Unique
    private static boolean shouldSuppressTexture(Identifier texture) {
        if (texture == null) return false;
        if (!CranksDungeons.CONFIG.useStatBars()) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;

        // Suppress all textures from Overflowing Bars (hearts, custom icons, and tiny_numbers font sprite sheet)
        if ("overflowingbars".equals(texture.getNamespace())) {
            return true;
        }

        String path = texture.getPath();
        // Whitelist essential HUD elements that must NEVER be suppressed
        if (path.contains("hud/stat_bar")) return false; // Our custom stat bars
        if (path.contains("crosshair")) return false;
        if (path.contains("hotbar")) return false;
        if (path.contains("experience_bar")) return false;
        if (path.contains("jump_bar")) return false;
        if (path.contains("boss_bar")) return false;

        // Suppress any heart (including containers), food, armor, air textures, or custom tiny numbers font textures
        return path.contains("heart") || path.contains("food") || path.contains("hunger")
                || path.contains("hud/armor") || path.startsWith("hud/air") || path.contains("/air_")
                || path.contains("bubble") || path.contains("icons.png") || path.contains("tiny_numbers");
    }

    // ── 4 overloads of drawGuiTexture ───────────────────────────────────────

    @Inject(method = "drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressGuiTexture4(Identifier texture, int x, int y, int width, int height, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawGuiTexture(Lnet/minecraft/util/Identifier;IIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressGuiTexture5(Identifier texture, int x, int y, int z, int width, int height, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressGuiTexture8(Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressGuiTexture9(Identifier texture, int i, int j, int k, int l, int x, int y, int z, int width, int height, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    // ── 4 overloads of drawTexture ──────────────────────────────────────────

    @Inject(method = "drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressTexture6(Identifier texture, int x, int y, int u, int v, int width, int height, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressTexture8(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressTexture9(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", at = @At("HEAD"), cancellable = true)
    private void suppressTexture10(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, CallbackInfo ci) {
        if (shouldSuppressTexture(texture)) {
            ci.cancel();
        }
    }
}
