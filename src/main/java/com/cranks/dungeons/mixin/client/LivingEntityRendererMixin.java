package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.client.render.FrozenRenderState;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    private static final Identifier FROST_OVERLAY = Identifier.of(CranksDungeons.MOD_ID,
            "textures/misc/frost_overlay.png");

    @Shadow
    public abstract M getModel();

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void updateFrozenState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntityRenderState instanceof FrozenRenderState frozenState) {
            frozenState.cranks$setFrozen(AilmentManager.hasAilment(livingEntity, AilmentType.FREEZE));
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderFrostOverlay(S livingEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue queue,
            CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (livingEntityRenderState instanceof FrozenRenderState frozenState && frozenState.cranks$isFrozen()) {
            int overlay = LivingEntityRenderer.getOverlay(livingEntityRenderState, 0.0f);
            int frostColor = 0x05169C9C;

            queue.submitModel(
                    getModel(),
                    livingEntityRenderState,
                    matrixStack,
                    RenderLayers.entityTranslucent(FROST_OVERLAY, false), // false = no outline, keeps depth culling
                    livingEntityRenderState.light,
                    overlay,
                    frostColor,
                    null);
        }
    }
}
