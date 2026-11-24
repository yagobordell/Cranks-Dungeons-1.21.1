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
        return !mount.isBaby() && stack.isOf(Items.CHEST) && !((MountDataProvider) mount).cranksdungeons$hasMountChest();
    }
    @Override public int getMaxItemCount() { return 1; }
    @Override public boolean canTakeItems(PlayerEntity player) {
        if (!MountManager.canManage(player, mount)) return false;
        if (!MountManager.cargoIsEmpty(mount)) {
            if (!player.getWorld().isClient()) player.sendMessage(
                    net.minecraft.text.Text.translatable("mount.inventory.cranks-dungeons.empty_attachment"), true);
            return false;
        }
        return true;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        if (!player.getEntityWorld().isClient()
                && ((MountDataProvider) mount).cranksdungeons$hasMountChest()) {
            MountManager.setChest(player, mount, false);
        }
        super.onTakeItem(player, stack);
    }
}
