/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 *  net.minecraft.class_636
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package punchy.mixin.client;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.state.UseActionTracker;
import punchy.client.state.UseItemStateMachine;

@Mixin(value={class_636.class})
public class MultiPlayerGameModeMixin {
    @Inject(method={"method_2919(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"}, at={@At(value="HEAD")})
    private void punchy$onUseItemHead(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        class_1799 stack = player == null ? class_1799.field_8037 : player.method_5998(hand);
        UseItemStateMachine.capturePendingThrowableSnapshot(hand, stack);
    }

    @Inject(method={"method_2896(Lnet/minecraft/class_746;Lnet/minecraft/class_1268;Lnet/minecraft/class_3965;)Lnet/minecraft/class_1269;"}, at={@At(value="HEAD")})
    private void punchy$onUseItemOnHead(class_746 player, class_1268 hand, class_3965 hit, CallbackInfoReturnable<class_1269> cir) {
        class_1799 stack = player == null || hand == null ? class_1799.field_8037 : player.method_5998(hand);
        UseItemStateMachine.capturePendingBoneMealUseOnSnapshot(hand, stack);
        UseActionTracker.recordFallbackUse(hand, UseActionTracker.TriggerType.USE_ITEM_ON, hit);
    }

    @Inject(method={"method_2896(Lnet/minecraft/class_746;Lnet/minecraft/class_1268;Lnet/minecraft/class_3965;)Lnet/minecraft/class_1269;"}, at={@At(value="RETURN")})
    private void punchy$onUseItemOn(class_746 player, class_1268 hand, class_3965 hit, CallbackInfoReturnable<class_1269> cir) {
        class_1269 result = (class_1269)cir.getReturnValue();
        boolean tracked = this.shouldTrackUse(result);
        if (tracked) {
            UseActionTracker.recordUse(hand, UseActionTracker.TriggerType.USE_ITEM_ON, hit);
        } else if (UseItemStateMachine.hasPendingBoneMealUseOnSnapshot(hand) || this.isBoneMealUse((class_1657)player, hand)) {
            UseActionTracker.recordUse(hand, UseActionTracker.TriggerType.USE_ITEM_ON, hit);
        } else {
            UseActionTracker.discardFallbackForHand(hand);
        }
    }

    @Inject(method={"method_2919(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"}, at={@At(value="RETURN")})
    private void punchy$onUseItem(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        class_1269 result = (class_1269)cir.getReturnValue();
        boolean tracked = this.shouldTrackUse(result);
        if (tracked) {
            UseActionTracker.recordUse(hand, UseActionTracker.TriggerType.USE_ITEM);
        } else {
            UseActionTracker.discardFallbackForHand(hand);
        }
    }

    @Inject(method={"method_2905(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"}, at={@At(value="RETURN")})
    private void punchy$onInteract(class_1657 player, class_1297 entity, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        class_1269 result = (class_1269)cir.getReturnValue();
        boolean tracked = this.shouldTrackUse(result);
        if (tracked) {
            UseActionTracker.recordUse(hand, UseActionTracker.TriggerType.INTERACT);
        }
    }

    @Inject(method={"method_2917(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"}, at={@At(value="RETURN")})
    private void punchy$onInteractAt(class_1657 player, class_1297 entity, class_3966 hit, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        class_1269 result = (class_1269)cir.getReturnValue();
        boolean tracked = this.shouldTrackUse(result);
        if (tracked) {
            UseActionTracker.recordUse(hand, UseActionTracker.TriggerType.INTERACT_AT);
        }
    }

    private boolean shouldTrackUse(class_1269 result) {
        return result != null && result.method_23665();
    }

    private boolean isBoneMealUse(class_1657 player, class_1268 hand) {
        if (player == null || hand == null) {
            return false;
        }
        class_1799 stack = player.method_5998(hand);
        return stack != null && stack.method_31574(class_1802.field_8324);
    }
}

