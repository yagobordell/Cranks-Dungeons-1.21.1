package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountChestSlot;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.mount.HorseScreenHandlerMountAccess;
import com.cranks.dungeons.horse.HorseBreedingManager;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HorseScreenHandler.class)
public abstract class HorseScreenHandlerMountChestMixin extends ScreenHandler
        implements HorseScreenHandlerMountAccess {
    @Shadow @Final private AbstractHorseEntity entity;
    @Shadow @Final private Inventory inventory;

    protected HorseScreenHandlerMountChestMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    @org.spongepowered.asm.mixin.Unique
    public AbstractHorseEntity cranks$getMountEntity() {
        return entity;
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void cranks$checkMountInventoryAccess(PlayerEntity player,
            CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof MountDataProvider
                && !MountManager.canAccessMount(player, entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cranks$addChestEquipmentSlot(int syncId,
                                               net.minecraft.entity.player.PlayerInventory playerInventory,
                                               Inventory inventory, AbstractHorseEntity entity,
                                               int columns, CallbackInfo ci) {
        if (entity instanceof MountDataProvider && MountKind.of(entity) != MountKind.UNSUPPORTED) {
            addSlot(new MountChestSlot(entity, 8, 54));
            if (playerInventory.player instanceof ServerPlayerEntity serverPlayer) {
                MountManager.syncToPlayer(serverPlayer, entity);
                if (entity instanceof net.minecraft.entity.passive.HorseEntity horse) {
                    HorseBreedingManager.syncToPlayer(serverPlayer, horse);
                }
            }
        }
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/screen/slot/Slot"))
    private Slot cranks$useCapacityAwareCargoSlots(Inventory slotInventory, int inventoryIndex,
                                                   int x, int y) {
        if (slotInventory == inventory && inventoryIndex >= 1
                && entity instanceof MountDataProvider
                && MountKind.of(entity) != MountKind.UNSUPPORTED) {
            return new com.cranks.dungeons.mount.MountHorseCargoSlot(
                    slotInventory, inventoryIndex, x, y, entity);
        }
        return new Slot(slotInventory, inventoryIndex, x, y);
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void cranks$quickMoveChest(PlayerEntity player, int index,
                                       CallbackInfoReturnable<ItemStack> cir) {
        if (!(entity instanceof MountDataProvider)
                || MountKind.of(entity) == MountKind.UNSUPPORTED
                || index < 0 || index >= slots.size()) return;
        int chestSlotIndex = slots.size() - 1;
        Slot source = slots.get(index);
        if (!source.hasStack()) return;
        ItemStack moving = source.getStack();
        int playerStart = Integer.MAX_VALUE;
        int playerEnd = -1;
        for (int slotIndex = 0; slotIndex < chestSlotIndex; slotIndex++) {
            if (slots.get(slotIndex).inventory == player.getInventory()) {
                playerStart = Math.min(playerStart, slotIndex);
                playerEnd = Math.max(playerEnd, slotIndex + 1);
            }
        }
        if (playerEnd < 0) return;

        if (index == chestSlotIndex) {
            ItemStack original = moving.copy();
            if (!insertItem(moving, playerStart, playerEnd, true)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            if (moving.isEmpty()) source.onTakeItem(player, original);
            if (moving.isEmpty()) source.setStack(ItemStack.EMPTY);
            else source.markDirty();
            cir.setReturnValue(original);
        } else if (index >= playerStart && index < playerEnd
                && moving.isOf(net.minecraft.item.Items.CHEST)
                && !slots.get(chestSlotIndex).hasStack()) {
            ItemStack original = moving.copy();
            if (!insertItem(moving, chestSlotIndex, chestSlotIndex + 1, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            if (moving.isEmpty()) source.setStack(ItemStack.EMPTY);
            else source.markDirty();
            cir.setReturnValue(original);
        }
    }
}
