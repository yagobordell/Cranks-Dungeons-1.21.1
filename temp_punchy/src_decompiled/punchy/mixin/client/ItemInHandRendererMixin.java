/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1690
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4597$class_4598
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  net.minecraft.class_759
 *  net.minecraft.class_7833
 *  net.minecraft.class_7923
 *  net.minecraft.class_804
 *  net.minecraft.class_811
 *  org.joml.Matrix3f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1690;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_804;
import net.minecraft.class_811;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.render.HandRenderBobContext;
import punchy.client.render.PunchyArmRenderer;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.UseAnimationSuppressor;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.MovementStateMachine;
import punchy.config.PunchyConfig;
import punchy.mixin.client.accessor.ItemInHandRendererInvoker;

@Mixin(value={class_759.class})
public class ItemInHandRendererMixin {
    private static final ThreadLocal<class_804> PUNCHY_PREV_BASELINE = new ThreadLocal();
    private static final ThreadLocal<Boolean> PUNCHY_BASELINE_ACTIVE = ThreadLocal.withInitial(() -> false);
    private static class_3300 punchy$cachedResourceManager;
    private static final Map<class_2960, Boolean> PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE;

    @Inject(method={"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_4597$class_4598;Lnet/minecraft/class_746;I)V"}, at={@At(value="HEAD")})
    private void punchy$renderFirstPersonModel(float partialTicks, class_4587 poseStack, class_4597.class_4598 nodeCollector, class_746 player, int combinedLight, CallbackInfo ci) {
        HandRenderBobContext.begin();
        if (ItemInHandRendererMixin.punchy$shouldSuppressVanillaHandBob(player)) {
            ItemInHandRendererMixin.punchy$applyInverseVanillaBob(poseStack);
        }
        PunchyArmRenderer.renderFirstPerson((class_759)this, player, partialTicks, poseStack, (class_4597)nodeCollector, combinedLight);
    }

    @Inject(method={"method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_4597$class_4598;Lnet/minecraft/class_746;I)V"}, at={@At(value="RETURN")}, require=0)
    private void punchy$endHandRenderContext(float partialTicks, class_4587 poseStack, class_4597.class_4598 nodeCollector, class_746 player, int combinedLight, CallbackInfo ci) {
        HandRenderBobContext.end();
    }

    @Inject(method={"method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void punchy$cancelVanillaArms(class_742 player, float partialTick, float pitch, class_1268 hand, float swingProgress, class_1799 item, float equippedProgress, class_4587 poseStack, class_4597 nodeCollector, int combinedLight, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1690.method_31044().method_31034()) {
            if (mc.field_1724 != player) {
                return;
            }
            if (PunchyArmRenderer.shouldHideAboveInterface(mc)) {
                ci.cancel();
                return;
            }
            if (!PunchyConfig.isModEnabled()) {
                return;
            }
            if (HandEquipStateMachine.shouldAllowVanillaHandOut(hand)) {
                if (ItemInHandRendererMixin.punchy$isActiveUseHand(player, hand)) {
                    UseAnimationSuppressor.clear(hand);
                    UseAnimationSuppressor.clearMeshStart(hand);
                    UseAnimationSuppressor.force(false);
                }
                return;
            }
            if (PunchyConfig.isHandBlacklisted((class_1657)player, hand)) {
                if (PunchyAnimationManager.shouldKeepBlacklistedOppositeHandForDualHanded(mc, hand)) {
                    ci.cancel();
                    return;
                }
                if (ItemInHandRendererMixin.punchy$isActiveUseHand(player, hand)) {
                    UseAnimationSuppressor.clear(hand);
                    UseAnimationSuppressor.clearMeshStart(hand);
                    UseAnimationSuppressor.force(false);
                }
                return;
            }
            ci.cancel();
        }
    }

    private static boolean punchy$isActiveUseHand(class_742 player, class_1268 hand) {
        return player != null && hand != null && player.method_6115() && player.method_6058() == hand;
    }

    private static boolean punchy$shouldSuppressVanillaHandBob(class_746 player) {
        if (!PunchyConfig.isModEnabled()) {
            return false;
        }
        if (player != null && (PunchyConfig.isHandBlacklisted((class_1657)player, class_1268.field_5808) || PunchyConfig.isHandBlacklisted((class_1657)player, class_1268.field_5810))) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc != null && ItemInHandRendererMixin.punchy$isBoatVehicle((class_1309)mc.field_1724)) {
            return false;
        }
        if (PunchyConfig.enableCustomWalk()) {
            return true;
        }
        if (!PunchyConfig.enableSprintArmSwing()) {
            return false;
        }
        return MovementStateMachine.get().getSprintBlendAlpha() > 0.001f;
    }

    private static boolean punchy$isBoatVehicle(class_1309 entity) {
        return entity != null && entity.method_5854() instanceof class_1690;
    }

    private static void punchy$applyInverseVanillaBob(class_4587 poseStack) {
        if (poseStack == null || !HandRenderBobContext.hasBobSample()) {
            return;
        }
        float walk = HandRenderBobContext.getLastWalkDist();
        float bob = HandRenderBobContext.getLastBob();
        float phase = walk * (float)Math.PI;
        float sin = class_3532.method_15374((float)phase);
        float cos = class_3532.method_15362((float)phase);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(-Math.abs(class_3532.method_15362((float)(phase - 0.2f)) * bob) * 5.0f));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(-(sin * bob * 3.0f)));
        poseStack.method_46416(-(sin * bob * 0.5f), Math.abs(cos * bob), 0.0f);
    }

    @Redirect(method={"method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_759;method_3219(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;IFFLnet/minecraft/class_1306;)V"))
    private void punchy$renderCustomArm(class_759 instance, class_4587 poseStack, class_4597 nodeCollector, int combinedLight, float equipProgress, float swingProgress, class_1306 arm, class_742 player, float partialTicks, float pitch, class_1268 hand, float swingProgressArg, class_1799 stack, float equippedProgress, class_4587 originalPoseStack, class_4597 originalCollector, int originalLight) {
        if (!PunchyConfig.isModEnabled() || HandEquipStateMachine.shouldAllowVanillaHandOut(hand) || PunchyConfig.isHandBlacklisted((class_1657)player, hand)) {
            ((ItemInHandRendererInvoker)instance).punchy$renderPlayerArm(poseStack, nodeCollector, combinedLight, equipProgress, swingProgress, arm);
        }
    }

    @Inject(method={"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"}, at={@At(value="HEAD")})
    private void punchy$lockUseDisplayBaseline(class_1309 entity, class_1799 stack, class_811 displayContext, boolean leftHand, class_4587 poseStack, class_4597 buffer, int light, CallbackInfo ci) {
        if (ItemInHandRendererMixin.shouldSkipRenderItemHooksForInteractiveStuffCompat()) {
            ItemInHandRendererMixin.clearBaselineHookState();
            return;
        }
        if (entity == null || stack == null || stack.method_7960() || !entity.method_6115()) {
            return;
        }
        if (PunchyConfig.isItemBlacklisted(stack)) {
            return;
        }
        class_1799 useStack = entity.method_6030();
        if (useStack == null || useStack.method_7960() || !class_1799.method_31577((class_1799)stack, (class_1799)useStack)) {
            return;
        }
        class_804 baseline = PunchyArmRenderer.resolveBaselineUseTransform(stack, displayContext, entity);
        if (baseline == null) {
            return;
        }
        PUNCHY_PREV_BASELINE.set(PunchyArmRenderer.getBaselineUseTransform());
        PUNCHY_BASELINE_ACTIVE.set(true);
        PunchyArmRenderer.setBaselineUseTransform(baseline);
    }

    @Inject(method={"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"}, at={@At(value="RETURN")})
    private void punchy$restoreUseDisplayBaseline(class_1309 entity, class_1799 stack, class_811 displayContext, boolean leftHand, class_4587 poseStack, class_4597 buffer, int light, CallbackInfo ci) {
        if (ItemInHandRendererMixin.shouldSkipRenderItemHooksForInteractiveStuffCompat()) {
            ItemInHandRendererMixin.clearBaselineHookState();
            return;
        }
        if (!PUNCHY_BASELINE_ACTIVE.get().booleanValue()) {
            return;
        }
        PunchyArmRenderer.setBaselineUseTransform(PUNCHY_PREV_BASELINE.get());
        PUNCHY_PREV_BASELINE.remove();
        PUNCHY_BASELINE_ACTIVE.set(false);
    }

    @ModifyVariable(method={"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"}, at=@At(value="HEAD"), argsOnly=true)
    private class_811 punchy$forceVanillaDisplay(class_811 displayContext, class_1309 entity, class_1799 stack) {
        if (!PunchyArmRenderer.shouldForceVanillaDisplay()) {
            return displayContext;
        }
        if (!ItemInHandRendererMixin.shouldForceVanillaDisplay(displayContext, entity, stack)) {
            return displayContext;
        }
        if (displayContext == class_811.field_4320) {
            return class_811.field_4322;
        }
        if (displayContext == class_811.field_4323) {
            return class_811.field_4321;
        }
        return displayContext;
    }

    private static boolean shouldForceVanillaDisplay(class_811 displayContext, class_1309 entity, class_1799 stack) {
        class_1799 using;
        if (displayContext != class_811.field_4320 && displayContext != class_811.field_4323) {
            return false;
        }
        if (entity == null || stack == null || stack.method_7960()) {
            return false;
        }
        if (entity.method_6115() && (using = entity.method_6030()) != null && !using.method_7960() && class_1799.method_31577((class_1799)stack, (class_1799)using)) {
            return false;
        }
        if (stack.method_7909() instanceof class_1835) {
            return false;
        }
        if (PunchyConfig.isItemBlacklisted(stack)) {
            return false;
        }
        if (ItemInHandRendererMixin.isResourcePackItemDefinition(stack)) {
            return false;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        if (kind == VanillaFirstPersonItemLayer.ToolKind.INGREDIENT || kind == VanillaFirstPersonItemLayer.ToolKind.FOOD) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return false;
        }
        class_811 firstContext = displayContext == class_811.field_4320 ? class_811.field_4322 : class_811.field_4321;
        class_804 thirdTransform = PunchyArmRenderer.resolveBaselineUseTransform(stack, displayContext, entity);
        class_804 firstTransform = PunchyArmRenderer.resolveBaselineUseTransform(stack, firstContext, entity);
        if (thirdTransform == null || firstTransform == null) {
            return false;
        }
        Vector3f upThird = ItemInHandRendererMixin.computeUpVector(thirdTransform);
        Vector3f upFirst = ItemInHandRendererMixin.computeUpVector(firstTransform);
        if (upThird == null || upFirst == null) {
            return false;
        }
        float dot = upThird.dot((Vector3fc)upFirst);
        return dot < 0.6f;
    }

    private static Vector3f computeUpVector(class_804 transform) {
        if (transform == null || transform.field_4287 == null) {
            return null;
        }
        Vector3f rotation = transform.field_4287;
        Matrix3f matrix = new Matrix3f().rotateX((float)Math.toRadians(rotation.x)).rotateY((float)Math.toRadians(rotation.y)).rotateZ((float)Math.toRadians(rotation.z));
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        matrix.transform(up);
        if (up.lengthSquared() > 0.0f) {
            up.normalize();
        }
        return up;
    }

    private static boolean isResourcePackItemDefinition(class_1799 stack) {
        class_2960 itemId;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1478() == null || stack == null || stack.method_7960()) {
            return false;
        }
        class_3300 manager = mc.method_1478();
        if (manager != punchy$cachedResourceManager) {
            punchy$cachedResourceManager = manager;
            PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE.clear();
        }
        if ((itemId = class_7923.field_41178.method_10221((Object)stack.method_7909())) == null) {
            return false;
        }
        Boolean cached = PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE.get(itemId);
        if (cached != null) {
            return cached;
        }
        class_2960 defId = class_2960.method_60655((String)itemId.method_12836(), (String)("items/" + itemId.method_12832() + ".json"));
        List resources = manager.method_14489(defId);
        if (resources == null || resources.isEmpty()) {
            PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE.put(itemId, false);
            return false;
        }
        for (class_3298 resource : resources) {
            String packId;
            if (resource == null || (packId = resource.method_14480()) == null || "vanilla".equals(packId)) continue;
            PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE.put(itemId, true);
            return true;
        }
        PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE.put(itemId, false);
        return false;
    }

    private static boolean shouldSkipRenderItemHooksForInteractiveStuffCompat() {
        if (!PunchyConfig.isInteractiveStuffCompatEnabled()) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        return mc != null && mc.field_1690 != null && mc.field_1690.method_31044().method_31034();
    }

    private static void clearBaselineHookState() {
        PunchyArmRenderer.setBaselineUseTransform(PUNCHY_PREV_BASELINE.get());
        PUNCHY_PREV_BASELINE.remove();
        PUNCHY_BASELINE_ACTIVE.set(false);
    }

    static {
        PUNCHY_RESOURCE_PACK_ITEM_DEFINITION_CACHE = new HashMap<class_2960, Boolean>();
    }
}

