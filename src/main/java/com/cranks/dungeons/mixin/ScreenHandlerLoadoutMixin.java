package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
abstract class ScreenHandlerLoadoutMixin {
    @ModifyConstant(method = "internalOnSlotClick", constant = @Constant(intValue = 40))
    private int allowTenthHotbarSwap(int offhand, int slot, int button, SlotActionType action, PlayerEntity player) {
        return button == PlayerLoadout.EXTRA_HOTBAR ? PlayerLoadout.EXTRA_HOTBAR : offhand;
    }

    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void hidePersonalCraftingSlots(int slot, int button, SlotActionType action, PlayerEntity player, CallbackInfo ci) {
        if (slot >= 0 && slot < 5
                && ((Object) this instanceof PlayerScreenHandler || (Object) this instanceof LoadoutStationHandler)) {
            ci.cancel();
        }
    }
}
