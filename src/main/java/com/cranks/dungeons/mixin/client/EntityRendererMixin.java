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
    private void hideNametag(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // Hide vanilla nametags if our custom UI is enabled
        if ((entity instanceof PlayerEntity || entity.hasCustomName()) && CranksDungeons.CONFIG.showPlayerInfoAboveHead()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(Entity entity, float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (entity instanceof net.minecraft.entity.LivingEntity living) {
            boolean isStunned = living.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.FREEZE) || living.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.ELECTROCUTE);
            com.cranks.dungeons.util.FreezeState.setRenderingFrozen(isStunned);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void afterRender(Entity entity, float yaw, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        com.cranks.dungeons.util.FreezeState.setRenderingFrozen(false);
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "render",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1 // the second float: tickDelta
    )
    private float modifyTickDelta(float tickDelta, Entity entity) {
        if (entity instanceof net.minecraft.entity.LivingEntity living) {
            boolean isStunned = living.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.FREEZE) || living.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.ELECTROCUTE);
            if (isStunned) {
                return 0.0f;
            }
        }
        return tickDelta;
    }
}
