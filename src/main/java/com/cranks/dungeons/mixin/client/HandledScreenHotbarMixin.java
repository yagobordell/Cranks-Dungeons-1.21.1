package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.ModKeybinds;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
abstract class HandledScreenHotbarMixin {
    @Shadow protected Slot focusedSlot;
    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType action);

    @Inject(method = "handleHotbarKeyPressed", at = @At("HEAD"), cancellable = true)
    private void swapWithTenthSlot(int keyCode, int scanCode, CallbackInfoReturnable<Boolean> cir) {
        if (focusedSlot == null || !ModKeybinds.TENTH_HOTBAR.matchesKey(keyCode, scanCode)) return;
        if (!((Object) this instanceof CreativeInventoryScreen)) {
            onMouseClick(focusedSlot, focusedSlot.id, PlayerLoadout.EXTRA_HOTBAR, SlotActionType.SWAP);
        }
        cir.setReturnValue(true);
    }
}
