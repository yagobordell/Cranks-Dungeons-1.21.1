package com.cranks.dungeons.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

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
    private void renderElectrocuted(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        java.util.List<com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry> ailments =
                com.cranks.dungeons.client.render.AilmentIconRenderer.getAilments(entity.getId());
        boolean isElectrocuted = ailments.stream().anyMatch(e -> e.texture().getPath().equals("textures/mob_effect/electrocute.png"));

        if (isElectrocuted && this.model != null) {
            float f = entity.age + tickDelta;
            VertexConsumer consumer = vertexConsumers.getBuffer(
                    net.minecraft.client.render.RenderLayer.getEnergySwirl(
                            Identifier.of("minecraft", "textures/entity/creeper/creeper_armor.png"),
                            f * 0.01F % 1.0F,
                            f * 0.0001F % 1.0F
                    )
            );
            this.model.render(matrices, consumer, 15728880, OverlayTexture.DEFAULT_UV);
        }
    }

    /** Render mount panniers while the renderer still has the entity transform pushed. */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderMountPanniers(T entity, float yaw, float tickDelta, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        com.cranks.dungeons.client.render.MountChestFeatureRenderer.renderPanniers(
                matrices, vertexConsumers, light, entity);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderFrozen(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        java.util.List<com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry> ailments =
                com.cranks.dungeons.client.render.AilmentIconRenderer.getAilments(entity.getId());
        boolean isFrozen = ailments.stream().anyMatch(e -> e.texture().getPath().equals("textures/mob_effect/freeze.png"));

        if (isFrozen) {
            matrices.push();
            float width = entity.getWidth() + 1.2f;
            float height = entity.getHeight() + 0.5f;
            matrices.scale(width, height, width);
            matrices.translate(-0.5f, 0.0f, -0.5f);

            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    net.minecraft.block.Blocks.ICE.getDefaultState(),
                    matrices,
                    vertexConsumers,
                    light,
                    OverlayTexture.DEFAULT_UV
            );
            matrices.pop();
        }
    }

    @Shadow
    protected M model;
}
