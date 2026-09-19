package com.cranks.dungeons.mount;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** Prevents rounded-up final columns from granting slots beyond the exact genetic capacity. */
public final class MountHorseCargoSlot extends Slot {
    private final AbstractHorseEntity mount;
    private final int cargoIndex;

    public MountHorseCargoSlot(Inventory inventory, int inventoryIndex, int x, int y,
                               AbstractHorseEntity mount) {
        super(inventory, inventoryIndex, x, y);
        this.mount = mount;
        this.cargoIndex = inventoryIndex - 1;
    }

    private boolean available() {
        return ((MountDataProvider) mount).cranksdungeons$hasMountChest()
                && cargoIndex >= 0 && cargoIndex < MountManager.cargoCapacity(mount);
    }

    public int cranksdungeons$getCargoIndex() {
        return cargoIndex;
    }

    @Override public boolean isEnabled() { return available(); }
    @Override public boolean canInsert(ItemStack stack) { return available(); }
    @Override public boolean canTakeItems(PlayerEntity player) { return available(); }
}
