package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import com.cranks.dungeons.ranged.RangedUse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class ReloadSpeedMixin implements RangedUse {

    @Unique private ItemStack cranks$chargeWeapon = ItemStack.EMPTY;
    @Unique private Hand cranks$chargeHand;
    @Unique private int cranks$chargeSlot = -1;

    @Inject(method = "setCurrentHand", at = @At("HEAD"))
    private void cranks$rememberWeapon(Hand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayerEntity player && !self.isUsingItem()) {
            cranks$chargeWeapon = self.getStackInHand(hand);
            cranks$chargeHand = hand;
            cranks$chargeSlot = player.getInventory().selectedSlot;
        }
    }

    @Override
    public boolean cranks$matchesRangedUse(ItemStack weapon, Hand hand, int selectedSlot) {
        return weapon == cranks$chargeWeapon && hand == cranks$chargeHand && selectedSlot == cranks$chargeSlot;
    }

    @Unique
    private double cranks$reloadFractionalAccumulator = 0.0;

    @Shadow
    private int itemUseTimeLeft;

    /** Shared virtual charge ticks keep gameplay, model pull and the HUD in sync for both weapons. */
    @Inject(method = "tickActiveItemStack", at = @At("HEAD"), cancellable = true)
    private void cranks$applyReloadSpeed(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack activeStack = self.getActiveItem();

        if (activeStack.isEmpty()) return;
        if (!(activeStack.getItem() instanceof BowItem) && !(activeStack.getItem() instanceof CrossbowItem)) return;
        if (self instanceof ServerPlayerEntity player && !cranks$matchesRangedUse(
                self.getStackInHand(self.getActiveHand()), self.getActiveHand(), player.getInventory().selectedSlot)) {
            self.stopUsingItem();
            ci.cancel();
            return;
        }
        if (activeStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(activeStack)) return;

        if (self.getItemUseTime() == 0) cranks$reloadFractionalAccumulator = 0;
        double speed = com.cranks.dungeons.ranged.RangedTiming.speed(self, activeStack);

        if (speed == 1.0) return;

        // speed = 1.30  -> 30% faster -> extraTicks = 0.3
        // speed = 0.70  -> 30% slower -> extraTicks = -0.3
        double extraTicksDouble = speed - 1.0;
        int extraTicks = (int) extraTicksDouble;
        double fractional = extraTicksDouble - extraTicks;

        // Handle fractional part deterministically to prevent client/server drift
        if (fractional != 0.0) {
            this.cranks$reloadFractionalAccumulator += fractional;
            if (this.cranks$reloadFractionalAccumulator >= 1.0) {
                extraTicks += 1;
                this.cranks$reloadFractionalAccumulator -= 1.0;
            } else if (this.cranks$reloadFractionalAccumulator <= -1.0) {
                extraTicks -= 1;
                this.cranks$reloadFractionalAccumulator += 1.0;
            }
        }

        if (extraTicks != 0) {
            this.itemUseTimeLeft = Math.max(0, this.itemUseTimeLeft - extraTicks);
        }
    }
}
