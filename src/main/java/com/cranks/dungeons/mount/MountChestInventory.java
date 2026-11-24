package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** A virtual one-slot inventory backed by the mount's attached-chest state. */
public final class MountChestInventory extends SimpleInventory {
    private final LivingEntity mount;

    public MountChestInventory(LivingEntity mount) {
        super(1);
        this.mount = mount;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 && ((MountDataProvider) mount).cranksdungeons$hasMountChest()
                ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return slot == 0 && ((MountDataProvider) mount).cranksdungeons$hasMountChest()
                ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot != 0) return;
        boolean equipped = ((MountDataProvider) mount).cranksdungeons$hasMountChest();
        if (stack.isEmpty()) {
            if (equipped && MountManager.cargoIsEmpty(mount) && !mount.getEntityWorld().isClient()) {
                MountManager.removeChestWithoutPlayer(mount);
            }
        } else if (stack.isOf(Items.CHEST) && !equipped && !mount.isBaby()) {
            ((MountDataProvider) mount).cranksdungeons$setMountChest(true);
            MountManager.refreshCargoCapacity(mount);
            if (mount instanceof net.minecraft.entity.passive.AbstractHorseEntity horse) {
                AbstractHorseInventoryAccess.of(horse).cranksdungeons$resizeHorseInventory();
            }
        }
        markDirty();
    }

    @Override public boolean isEmpty() { return !((MountDataProvider) mount).cranksdungeons$hasMountChest(); }
    @Override public boolean canPlayerUse(PlayerEntity player) {
        return mount.isAlive() && player.canInteractWithEntity(mount, 4.0);
    }
    @Override public void clear() { setStack(0, ItemStack.EMPTY); }
}
