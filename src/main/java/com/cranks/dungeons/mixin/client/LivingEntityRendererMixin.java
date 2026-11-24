package com.cranks.dungeons.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @org.spongepowered.asm.mixin.Unique
    private final java.util.Map<T,Float> cranks$frozenAnimation=new java.util.WeakHashMap<>();

    @Inject(method="getAnimationProgress",at=@At("RETURN"),cancellable=true)
    private void freezeAnimationClock(T entity,float tickDelta,CallbackInfoReturnable<Float> cir){
        if(com.cranks.dungeons.util.ClientAilmentHelper.isStunned(entity))
            cir.setReturnValue(cranks$frozenAnimation.computeIfAbsent(entity,key->cir.getReturnValue()));
        else cranks$frozenAnimation.remove(entity);
    }

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void hideNametag(T entity, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity localPlayer = MinecraftClient.getInstance().player;
        if (localPlayer != null && entity == localPlayer.getVehicle()) {
            cir.setReturnValue(false);
            return;
        }
        if ((entity instanceof PlayerEntity || entity.hasCustomName()) && com.cranks.dungeons.CranksDungeons.CONFIG.showPlayerInfoAboveHead()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderAilmentModel(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        com.cranks.dungeons.client.render.AilmentBodyRenderer.renderModelOverlay(entity,this.model,tickDelta,matrices,vertexConsumers,light);
    }

    /** Render mount panniers while the renderer still has the entity transform pushed. */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderMountPanniers(T entity, float yaw, float tickDelta, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Normal renderers use the registered feature; keep one fallback for
        // renderers supplied without Fabric's feature-registration callback.
        for (var feature:features) {
            if(feature instanceof com.cranks.dungeons.client.render.MountChestFeatureRenderer)return;
        }
        com.cranks.dungeons.client.render.MountChestFeatureRenderer.renderPanniers(
                this.model, matrices, vertexConsumers, light, entity);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderAilmentBody(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        com.cranks.dungeons.client.render.AilmentBodyRenderer.render(entity,tickDelta,matrices,vertexConsumers,light);
    }

    @Shadow
    protected M model;
    @Shadow @org.spongepowered.asm.mixin.Final
    protected java.util.List<net.minecraft.client.render.entity.feature.FeatureRenderer<T,M>> features;
}
