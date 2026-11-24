package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.PlayerLoadout;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerHotbarLoadoutMixin {
    @Shadow public ServerPlayerEntity player;

    @ModifyExpressionValue(method = "onUpdateSelectedSlot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;getHotbarSize()I"))
    private int allowTenthHotbarSelection(int vanillaSize, UpdateSelectedSlotC2SPacket packet) {
        return packet.getSelectedSlot() == PlayerLoadout.EXTRA_HOTBAR ? PlayerLoadout.EXTRA_HOTBAR + 1 : vanillaSize;
    }

    @ModifyConstant(method = "onCreativeInventoryAction", constant = @Constant(intValue = 45))
    private int allowTenthCreativeSlot(int offhand) {
        return PlayerLoadout.FIRST_HANDLER_SLOT;
    }

    @Inject(method = "onPickFromInventory", at = @At("HEAD"), cancellable = true)
    private void requireMainInventoryPick(PickFromInventoryC2SPacket packet, CallbackInfo ci) {
        if (packet.getSlot() < 0 || packet.getSlot() >= PlayerInventory.MAIN_SIZE) ci.cancel();
    }

    @Inject(method = "onCraftRequest", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/screen/ScreenHandler;canUse(Lnet/minecraft/entity/player/PlayerEntity;)Z"),
            cancellable = true)
    private void skipPersonalCrafting(CraftRequestC2SPacket packet, CallbackInfo ci) {
        if (player.currentScreenHandler == player.playerScreenHandler) ci.cancel();
    }
}
