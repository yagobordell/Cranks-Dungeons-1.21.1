package com.cranks.dungeons.mixin;
import com.cranks.dungeons.economy.TradeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ScreenHandler.class,priority=900)
abstract class TradeInventoryLockMixin {
    @Inject(method="onSlotClick",at=@At("HEAD"),cancellable=true)
    private void lockInventory(int slot,int button,SlotActionType action,PlayerEntity player,CallbackInfo ci){if(player instanceof ServerPlayerEntity server&&TradeManager.active(server)){((ScreenHandler)(Object)this).syncState();ci.cancel();}}
    @Inject(method="onButtonClick",at=@At("HEAD"),cancellable=true)
    private void lockButtons(PlayerEntity player,int button,CallbackInfoReturnable<Boolean> cir){if(player instanceof ServerPlayerEntity server&&TradeManager.active(server))cir.setReturnValue(false);}
}
