package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
                || path.contains("armor") || path.startsWith("hud/air") || path.contains("/air_")
                || path.contains("bubble") || path.contains("icons.png") || path.contains("tiny_numbers");
    }

    @Unique
    private static boolean isMultiplierBadge(String text) {
        if (text == null || text.isEmpty()) return false;
        if (!CranksDungeons.CONFIG.useStatBars()) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;

        String clean = Formatting.strip(text).replace("\u00a0", " ").trim().toLowerCase();
        if (clean.isEmpty()) return false;

        if (clean.length() <= 6) {
            if (clean.contains("x") || clean.contains("×") || clean.contains("#")) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static String getOrderedTextString(OrderedText orderedText) {
        if (orderedText == null) return "";
        StringBuilder sb = new StringBuilder();
        orderedText.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
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

    // ── Text suppression via DrawContext ─────────────────────────────────────

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void suppressTextString(TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(text)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I", at = @At("HEAD"), cancellable = true)
    private void suppressTextStringShadow(TextRenderer textRenderer, String text, int x, int y, int color, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(text)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void suppressTextComponent(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (text != null && isMultiplierBadge(text.getString())) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", at = @At("HEAD"), cancellable = true)
    private void suppressTextComponentShadow(TextRenderer textRenderer, Text text, int x, int y, int color, CallbackInfoReturnable<Integer> cir) {
        if (text != null && isMultiplierBadge(text.getString())) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void suppressOrderedText(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(getOrderedTextString(text))) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I", at = @At("HEAD"), cancellable = true)
    private void suppressOrderedTextShadow(TextRenderer textRenderer, OrderedText text, int x, int y, int color, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(getOrderedTextString(text))) {
            cir.setReturnValue(0);
        }
    }
}
