package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class, priority = 99)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void customStatusEffectOverlay(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CranksDungeons.CONFIG.customStatusEffectOverlay()) {
            ci.cancel();
            com.cranks.dungeons.client.render.StatusEffectHudRenderer.render(context, tickCounter, client);
        }
    }

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

    // ── Heart scaling: 1 heart = 5 HP ──────────────────────────────────────
    // renderHealthBar parameter order (int ordinals in brackets):
    //   DrawContext, PlayerEntity,
    //   int x [0], int y [1], int lines [2], int regeneratingHeartIndex [3],
    //   float maxHealth [f0],
    //   int lastHealth [4], int health [5], int absorption [6],
    //   boolean blinking

    // Scale maxHealth (the only float param → float ordinal 0)
    @ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float scaleMaxHealth(float maxHealth) {
        if (CranksDungeons.CONFIG.useStatBars()) return maxHealth;
        return maxHealth / 2.5f;
    }

    // Recompute lines (vertical spacing, int ordinal 2) based on scaled health
    @ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int scaleLines(int lines) {
        if (CranksDungeons.CONFIG.useStatBars()) return lines;
        if (client.player == null) return lines;
        float scaledMaxHealth = client.player.getMaxHealth() / 2.5f;
        float scaledAbsorption = client.player.getAbsorptionAmount() / 2.5f;
        int maxHearts = MathHelper.ceil(scaledMaxHealth / 2.0f);
        int absHearts = MathHelper.ceil(scaledAbsorption / 2.0f);
        int scaledRows = Math.max((int) Math.ceil((maxHearts + absHearts) / 10.0), 1);
        return Math.max(10 - (scaledRows - 2), 3);
    }
    // Scale lastHealth (int ordinal 4)
    @ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private int scaleLastHealth(int lastHealth) {
        if (CranksDungeons.CONFIG.useStatBars()) return lastHealth;
        return MathHelper.ceil(lastHealth / 2.5f);
    }

    // Scale health (int ordinal 5)
    @ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), argsOnly = true, ordinal = 5)
    private int scaleHealth(int health) {
        if (CranksDungeons.CONFIG.useStatBars()) return health;
        return MathHelper.ceil(health / 2.5f);
    }

    // Scale absorption (int ordinal 6)
    @ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), argsOnly = true, ordinal = 6)
    private int scaleAbsorption(int absorption) {
        if (CranksDungeons.CONFIG.useStatBars()) return absorption;
        return MathHelper.ceil(absorption / 2.5f);
    }
}
