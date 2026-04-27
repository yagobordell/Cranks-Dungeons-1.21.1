package com.cranks.dungeons.mixin.client;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void hideNametag(T entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if ((entity instanceof PlayerEntity || entity.hasCustomName()) && com.cranks.dungeons.CranksDungeons.CONFIG.showPlayerInfoAboveHead()) {
            cir.setReturnValue(false);
        }
    }
}
