package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class LoadoutContainerSyncMixin {
    @Unique private final DefaultedList<ItemStack> cranks$lastContainerLoadout =
            DefaultedList.ofSize(PlayerLoadout.END - PlayerLoadout.EXTRA_HOTBAR, ItemStack.EMPTY);

    @Inject(method = "tick", at = @At("TAIL"))
    private void syncLoadoutWhileUsingOtherContainers(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        boolean externalInventory = player.currentScreenHandler != player.playerScreenHandler;
        for (int i = 0; i < cranks$lastContainerLoadout.size(); i++) {
            int slot = PlayerLoadout.EXTRA_HOTBAR + i;
            ItemStack current = player.getInventory().getStack(slot);
            if (ItemStack.areEqual(current, cranks$lastContainerLoadout.get(i))) continue;
            ItemStack snapshot = current.copy();
            cranks$lastContainerLoadout.set(i, snapshot);
            if (externalInventory) {
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, slot, snapshot));
            }
        }
    }
}
