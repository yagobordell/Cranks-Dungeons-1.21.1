/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.state.AttackActionTracker;
import punchy.client.state.SpearStateMachine;
import punchy.client.state.UseActionTracker;

@Mixin(value={class_1309.class})
public class LivingEntitySwingMixin {
    private static final long USE_SWING_GUARD_NANOS = 250000000L;

    @Inject(method={"method_23667(Lnet/minecraft/class_1268;Z)V"}, at={@At(value="HEAD")})
    private void punchy$onSwing(class_1268 hand, boolean fromServer, CallbackInfo ci) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return;
        }
        if (this != mc.field_1724) {
            return;
        }
        if (UseActionTracker.peekTrigger() != null) {
            return;
        }
        if (UseActionTracker.wasRecentUse(250000000L)) {
            return;
        }
        if (mc.field_1690 != null && mc.field_1690.field_1904.method_1434() && !mc.field_1690.field_1886.method_1434()) {
            return;
        }
        if (mc.field_1765 != null && mc.field_1765.method_17783() == class_239.class_240.field_1332 && LivingEntitySwingMixin.shouldIgnoreBlockSwing(mc.field_1724.method_5998(hand))) {
            return;
        }
        AttackActionTracker.recordAttack(hand);
    }

    private static boolean shouldIgnoreBlockSwing(class_1799 stack) {
        return !SpearStateMachine.isSpearStack(stack);
    }
}

