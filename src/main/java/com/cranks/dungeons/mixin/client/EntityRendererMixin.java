package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void hideNametag(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        // Hide vanilla nametags if our custom UI is enabled
        if ((entity instanceof PlayerEntity || entity.hasCustomName()) && CranksDungeons.CONFIG.showPlayerInfoAboveHead()) {
            cir.setReturnValue(false);
        }
    }
}
