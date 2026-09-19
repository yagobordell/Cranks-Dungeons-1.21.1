package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.HorseMountHudLayout;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.LivingEntity;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HorseMountHealthHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private static Identifier VEHICLE_CONTAINER_HEART_TEXTURE;
    @Shadow @Final private static Identifier VEHICLE_FULL_HEART_TEXTURE;
    @Shadow @Final private static Identifier VEHICLE_HALF_HEART_TEXTURE;

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void cranks$renderScaledHorseHealth(DrawContext context, CallbackInfo ci) {
        if (client.player == null || !(client.player.getVehicle() instanceof LivingEntity horse)
                || MountKind.of(horse) == MountKind.UNSUPPORTED) return;

        int hearts = HorseMountHudLayout.visibleHeartCount(horse);
        int healthHalfUnits = HorseMountHudLayout.mountHealthHalfUnits(horse.getHealth());
        int y = context.getScaledWindowHeight() - 39;
        int right = context.getScaledWindowWidth() / 2 + 91;
        int healthOffset = 0;

        RenderSystem.enableBlend();
        while (hearts > 0) {
            int heartsInRow = Math.min(hearts, HorseMountHudLayout.HEARTS_PER_ROW);
            hearts -= heartsInRow;
            for (int column = 0; column < heartsInRow; column++) {
                int x = right - column * 8 - 9;
                context.drawGuiTexture(VEHICLE_CONTAINER_HEART_TEXTURE, x, y, 9, 9);

                int halfUnit = column * 2 + 1 + healthOffset;
                if (halfUnit < healthHalfUnits) {
                    context.drawGuiTexture(VEHICLE_FULL_HEART_TEXTURE, x, y, 9, 9);
                } else if (halfUnit == healthHalfUnits) {
                    context.drawGuiTexture(VEHICLE_HALF_HEART_TEXTURE, x, y, 9, 9);
                }
            }
            y -= 10;
            healthOffset += 20;
        }
        RenderSystem.disableBlend();
        ci.cancel();
    }
}
