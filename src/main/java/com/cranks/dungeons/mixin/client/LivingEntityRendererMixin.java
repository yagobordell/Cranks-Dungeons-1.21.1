package com.cranks.dungeons.mixin.client;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @org.spongepowered.asm.mixin.Unique
    private static final ThreadLocal<Boolean> cranks$isCurrentlyFrozen = ThreadLocal.withInitial(() -> false);

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private void captureStunState(T entity, S state, float tickDelta, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        cranks$isCurrentlyFrozen.set(com.cranks.dungeons.util.ClientAilmentHelper.isStunned(entity));
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyTickDelta(float original) {
        if (cranks$isCurrentlyFrozen.get()) {
            return 0.0f;
        }
        return original;
    }



    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void addStunState(T entity, S state, float tickDelta, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (state instanceof com.cranks.dungeons.client.render.StunRenderState stunState) {
            boolean isFrozen = false;
            boolean isElectrocuted = false;
            java.util.List<com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry> ailments = com.cranks.dungeons.client.render.AilmentIconRenderer.getAilments(entity.getId());
            for (com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry entry : ailments) {
                String path = entry.texture().getPath();
                if (path.equals("textures/mob_effect/freeze.png")) {
                    isFrozen = true;
                } else if (path.equals("textures/mob_effect/electrocute.png")) {
                    isElectrocuted = true;
                }
            }
            stunState.cranks$setFrozen(isFrozen);
            stunState.cranks$setElectrocuted(isElectrocuted);
            stunState.cranks$setWidth(entity.getWidth());
            stunState.cranks$setHeight(entity.getHeight());
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderElectrocuted(S state, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.command.OrderedRenderCommandQueue vertexConsumers, net.minecraft.client.render.state.CameraRenderState light, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (state instanceof com.cranks.dungeons.client.render.StunRenderState stunState) {
            if (stunState.cranks$isElectrocuted()) {
                if (this.model != null) {
                    float f = (float)state.age;
                    vertexConsumers.getBatchingQueue(1).submitModel(
                        this.model,
                        state,
                        matrices,
                        net.minecraft.client.render.RenderLayers.energySwirl(net.minecraft.util.Identifier.of("minecraft", "textures/entity/creeper/creeper_armor.png"), f * 0.01F % 1.0F, f * 0.0001F % 1.0F),
                        15728880,
                        net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                        -1,
                        null
                    );
                }
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderFrozen(S state, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.command.OrderedRenderCommandQueue vertexConsumers, net.minecraft.client.render.state.CameraRenderState light, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (state instanceof com.cranks.dungeons.client.render.StunRenderState stunState) {
            if (stunState.cranks$isFrozen()) {
                matrices.push();
                float width = stunState.cranks$getWidth() + 1.2f;
                float height = stunState.cranks$getHeight() + 0.5f;
                matrices.scale(width, height, width);
                matrices.translate(-0.5f, 0.0f, -0.5f);
                
                vertexConsumers.submitBlock(
                    matrices,
                    net.minecraft.block.Blocks.ICE.getDefaultState(),
                    state.light,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                    0 // 0 means no outline, -1 caused a solid white glowing outline through walls!
                );
                matrices.pop();
            }
        }
    }

    @Shadow
    protected M model;
}
