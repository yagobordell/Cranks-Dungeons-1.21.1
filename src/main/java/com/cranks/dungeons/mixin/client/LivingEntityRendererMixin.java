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

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void freezeRenderState(T entity, S state, float tickDelta, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        boolean isStunned = entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.FREEZE) || entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.ELECTROCUTE);
        if (isStunned) {
            state.age = entity.age;
            state.bodyYaw = entity.getBodyYaw();
            state.pitch = entity.getPitch();

            long currentTime = net.minecraft.util.Util.getMeasuringTimeMs();
            for (java.lang.reflect.Field field : com.cranks.dungeons.util.AnimationStateCacher.getAnimFields(state.getClass())) {
                try {
                    net.minecraft.entity.AnimationState animState = (net.minecraft.entity.AnimationState) field.get(state);
                    if (animState != null) {
                        java.lang.reflect.Field[] fields = animState.getClass().getDeclaredFields();
                        java.lang.reflect.Field longField1 = null;
                        java.lang.reflect.Field longField2 = null;
                        
                        for (java.lang.reflect.Field f : fields) {
                            if (f.getType() == long.class) {
                                if (longField1 == null) longField1 = f;
                                else if (longField2 == null) longField2 = f;
                            }
                        }
                        
                        if (longField1 != null && longField2 != null) {
                            longField1.setAccessible(true);
                            longField2.setAccessible(true);
                            long val1 = longField1.getLong(animState);
                            long val2 = longField2.getLong(animState);
                            
                            if (val1 > val2) {
                                longField1.setLong(animState, currentTime);
                            } else {
                                longField2.setLong(animState, currentTime);
                            }
                        }
                    }
                } catch (Exception e) {}
            }
        }
        
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
