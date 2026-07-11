/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1087
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_746
 *  net.minecraft.class_804
 *  net.minecraft.class_809
 *  net.minecraft.class_811
 *  net.minecraft.class_918
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_1087;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_746;
import net.minecraft.class_804;
import net.minecraft.class_809;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.render.CreatureBucketAnimator;
import punchy.client.render.PunchyArmRenderer;
import punchy.mixin.client.accessor.ItemRendererInvoker;

@Mixin(value={class_918.class})
public class ItemRendererMixin {
    private static final ThreadLocal<class_804> PUNCHY_PREV_BASELINE = new ThreadLocal();
    private static final ThreadLocal<Boolean> PUNCHY_BASELINE_ACTIVE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<class_1799> PUNCHY_CURRENT_STACK = new ThreadLocal();
    private static final ThreadLocal<class_811> PUNCHY_DISPLAY_CONTEXT = new ThreadLocal();
    private static final ThreadLocal<Boolean> PUNCHY_LEFT_HAND = new ThreadLocal();

    @Inject(method={"method_23179(Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;IILnet/minecraft/class_1087;)V"}, at={@At(value="HEAD")})
    private void punchy$lockUseDisplayBaseline(class_1799 stack, class_811 displayContext, boolean leftHand, class_4587 poseStack, class_4597 buffer, int packedLight, int packedOverlay, class_1087 model, CallbackInfo ci) {
        class_1799 useStack;
        if (!PunchyArmRenderer.isCapturingItemRenderer()) {
            return;
        }
        PUNCHY_DISPLAY_CONTEXT.set(displayContext);
        PUNCHY_LEFT_HAND.set(leftHand);
        PUNCHY_CURRENT_STACK.set(stack);
        if (stack == null || stack.method_7960() || model == null) {
            return;
        }
        if (displayContext != class_811.field_4322 && displayContext != class_811.field_4321 && displayContext != class_811.field_4320 && displayContext != class_811.field_4323) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return;
        }
        class_746 player = mc.field_1724;
        if (player != null && player.method_6115() && (useStack = player.method_6030()) != null && !useStack.method_7960() && class_1799.method_31577((class_1799)stack, (class_1799)useStack)) {
            class_804 baseline = PunchyArmRenderer.getCachedIdleTransform(stack, displayContext);
            if (baseline == null) {
                baseline = PunchyArmRenderer.resolveBaselineUseTransform(stack, displayContext, (class_1309)player);
            }
            if (baseline == null) {
                return;
            }
            PUNCHY_PREV_BASELINE.set(PunchyArmRenderer.getBaselineUseTransform());
            PUNCHY_BASELINE_ACTIVE.set(true);
            PunchyArmRenderer.setBaselineUseTransform(baseline);
            PunchyArmRenderer.setLastRenderedItemTransform(baseline);
            return;
        }
        class_804 idle = model.method_4709().method_3503(displayContext);
        if (idle != null) {
            PunchyArmRenderer.cacheIdleUseTransform(stack, displayContext, idle);
            PunchyArmRenderer.setLastRenderedItemTransform(idle);
        }
    }

    @Inject(method={"method_23179(Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;IILnet/minecraft/class_1087;)V"}, at={@At(value="RETURN")})
    private void punchy$restoreUseDisplayBaseline(class_1799 stack, class_811 displayContext, boolean leftHand, class_4587 poseStack, class_4597 buffer, int packedLight, int packedOverlay, class_1087 model, CallbackInfo ci) {
        if (!PunchyArmRenderer.isCapturingItemRenderer()) {
            return;
        }
        if (!PUNCHY_BASELINE_ACTIVE.get().booleanValue()) {
            PUNCHY_CURRENT_STACK.remove();
            PUNCHY_DISPLAY_CONTEXT.remove();
            PUNCHY_LEFT_HAND.remove();
            return;
        }
        PunchyArmRenderer.setBaselineUseTransform(PUNCHY_PREV_BASELINE.get());
        PUNCHY_PREV_BASELINE.remove();
        PUNCHY_BASELINE_ACTIVE.set(false);
        PUNCHY_CURRENT_STACK.remove();
        PUNCHY_DISPLAY_CONTEXT.remove();
        PUNCHY_LEFT_HAND.remove();
    }

    @Redirect(method={"method_23179(Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;IILnet/minecraft/class_1087;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_809;method_3503(Lnet/minecraft/class_811;)Lnet/minecraft/class_804;"), require=0)
    private class_804 punchy$neutralizeUseTransform(class_809 transforms, class_811 context) {
        return transforms.method_3503(context);
    }

    @Redirect(method={"method_23179(Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;IILnet/minecraft/class_1087;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_918;method_23182(Lnet/minecraft/class_1087;Lnet/minecraft/class_1799;IILnet/minecraft/class_4587;Lnet/minecraft/class_4588;)V"))
    private void punchy$renderCreatureBucket(class_918 instance, class_1087 model, class_1799 stack, int light, int overlay, class_4587 poseStack, class_4588 consumer) {
        if (!PunchyArmRenderer.isCapturingItemRenderer()) {
            ((ItemRendererInvoker)instance).punchy$renderModelLists(model, stack, light, overlay, poseStack, consumer);
            return;
        }
        PunchyArmRenderer.setLastRenderedItemPose(new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761()));
        class_811 context = PUNCHY_DISPLAY_CONTEXT.get();
        if (!CreatureBucketAnimator.shouldAnimate(stack, context)) {
            ((ItemRendererInvoker)instance).punchy$renderModelLists(model, stack, light, overlay, poseStack, consumer);
            return;
        }
        Boolean leftHand = PUNCHY_LEFT_HAND.get();
        CreatureBucketAnimator.renderSplit((ItemRendererInvoker)instance, model, stack, light, overlay, poseStack, consumer, leftHand != null && leftHand != false);
    }
}

