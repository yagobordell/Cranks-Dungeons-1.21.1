package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public final class MountStandaloneCargoSlot extends Slot {
    private final LivingEntity mount;
    public MountStandaloneCargoSlot(Inventory inventory, int index, int x, int y, LivingEntity mount) {
        super(inventory, index, x, y);
        this.mount = mount;
    }
    private boolean available() {
        return ((MountDataProvider) mount).cranksdungeons$hasMountChest()
                && getIndex() < MountManager.cargoCapacity(mount);
    }
    @Override public boolean isEnabled() { return available(); }
    @Override public boolean canInsert(ItemStack stack) { return available(); }
    @Override public boolean canTakeItems(PlayerEntity player) { return available(); }
}
