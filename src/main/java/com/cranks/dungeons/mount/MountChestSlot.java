package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public final class MountChestSlot extends Slot {
    private final LivingEntity mount;

    public MountChestSlot(LivingEntity mount, int x, int y) {
        super(new MountChestInventory(mount), 0, x, y);
        this.mount = mount;
    }

    @Override public boolean canInsert(ItemStack stack) {
        return stack.isOf(Items.CHEST) && !((MountDataProvider) mount).cranksdungeons$hasMountChest();
    }
    @Override public int getMaxItemCount() { return 1; }
    @Override public boolean canTakeItems(PlayerEntity player) {
        return MountManager.canManage(player, mount) && MountManager.cargoIsEmpty(mount);
    }
}
