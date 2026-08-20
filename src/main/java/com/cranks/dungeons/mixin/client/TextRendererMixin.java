package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TextRenderer.class, priority = 1)
public abstract class TextRendererMixin {

    @Unique
    private static boolean isMultiplierBadge(String text) {
        if (text == null || text.isEmpty()) return false;
        if (!CranksDungeons.CONFIG.useStatBars()) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return false;

        String clean = Formatting.strip(text).replace("\u00a0", " ").trim().toLowerCase();
        if (clean.isEmpty()) return false;

        // Multiplier badge: short text containing 'x', '×', or '#' with a digit
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

    // ── drawInternal: the bottom of the draw() call chain ───────────────────

    @Inject(method = "drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I", at = @At("HEAD"), cancellable = true)
    private void suppressDrawInternalString(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int underlineColor, int light, boolean mirror, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(text)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), cancellable = true)
    private void suppressDrawInternalOrderedText(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(getOrderedTextString(text))) {
            cir.setReturnValue(0);
        }
    }

    // ── All public draw() overloads ─────────────────────────────────────────

    @Inject(method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), cancellable = true)
    private void suppressDrawString10(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(text)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I", at = @At("HEAD"), cancellable = true)
    private void suppressDrawString11(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int underlineColor, int light, boolean mirror, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(text)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), cancellable = true)
    private void suppressDrawOrderedText(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Integer> cir) {
        if (isMultiplierBadge(getOrderedTextString(text))) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), cancellable = true)
    private void suppressDrawText(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Integer> cir) {
        if (text != null && isMultiplierBadge(text.getString())) {
            cir.setReturnValue(0);
        }
    }

    // ── drawWithOutline: separate rendering path for outlined text ──────────

    @Inject(method = "drawWithOutline(Lnet/minecraft/text/OrderedText;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void suppressDrawWithOutline(OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (isMultiplierBadge(getOrderedTextString(text))) {
            ci.cancel();
        }
    }
}
