package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.HotbarHudRenderer;
import com.cranks.dungeons.client.screen.HudLayoutEditScreen;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract class HotbarLoadoutHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private PlayerEntity getCameraPlayer() { throw new AssertionError(); }
    @Shadow private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter ticker,
                                          PlayerEntity player, ItemStack stack, int seed) { throw new AssertionError(); }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderTenSlotHotbar(DrawContext context, RenderTickCounter ticker, CallbackInfo ci) {
        PlayerEntity player = getCameraPlayer();
        if (player == null) return;
        ci.cancel();
        if (client.currentScreen instanceof HudLayoutEditScreen) return;

        int x = HotbarHudRenderer.resolveX(context.getScaledWindowWidth());
        int y = HotbarHudRenderer.resolveY(context.getScaledWindowHeight());
        HotbarHudRenderer.drawBar(context, x, y, player.getInventory(), (index, stack, sx, sy) ->
                renderHotbarItem(context, sx, sy, ticker, player, stack, index + 1));

        boolean offhandRight = player.getMainArm() == Arm.LEFT;
        if (!player.getOffHandStack().isEmpty()) {
            int offhand = HotbarHudRenderer.drawSideSocket(context, x, y, offhandRight);
            renderHotbarItem(context, offhand + 3, y + 5, ticker, player, player.getOffHandStack(), 11);
        }
        if (client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            float ranged = com.cranks.dungeons.client.render.RangedHud.progress(client);
            float cooldown = ranged >= 0 ? ranged : player.getAttackCooldownProgress(0);
            if (ranged >= 0) {
                int left=HotbarHudRenderer.drawSideSocket(context,x,y,!offhandRight);
                com.cranks.dungeons.client.render.RangedHud.drawArrow(context,left+9,y+4,ranged,true);
            } else if (cooldown < 1) HotbarHudRenderer.drawAttackIndicator(context, x, y, !offhandRight, cooldown);
        }
    }

    @ModifyExpressionValue(method = {"renderExperienceBar", "renderExperienceLevel", "renderStatusBars",
            "renderMountHealth", "renderMountJumpBar", "renderHeldItemTooltip"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"))
    private int reserveRoomForRaisedHotbar(int screenHeight) {
        return screenHeight - HotbarHudRenderer.DEFAULT_LIFT;
    }
}
