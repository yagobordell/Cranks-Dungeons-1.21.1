package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** A virtual one-slot saddle inventory for mounts without HorseScreenHandler. */
public final class MountSaddleInventory extends SimpleInventory {
    private final LivingEntity mount;

    public MountSaddleInventory(LivingEntity mount) {
        super(1);
        this.mount = mount;
    }

    private boolean isSaddleable() {
        return mount instanceof Saddleable && mount instanceof MountSaddleAccess;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 && isSaddleable()
                && ((MountSaddleAccess) mount).cranksdungeons$isSaddled()
                ? new ItemStack(Items.SADDLE) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot != 0 || !isSaddleable()
                || !((MountSaddleAccess) mount).cranksdungeons$isSaddled()) {
            return ItemStack.EMPTY;
        }
        ((MountSaddleAccess) mount).cranksdungeons$setSaddled(false);
        markDirty();
        return new ItemStack(Items.SADDLE);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot != 0 || !isSaddleable()) return;
        MountSaddleAccess access = (MountSaddleAccess) mount;
        if (stack.isEmpty()) {
            if (access.cranksdungeons$isSaddled()) access.cranksdungeons$setSaddled(false);
        } else if (stack.isOf(Items.SADDLE) && !access.cranksdungeons$isSaddled()
                && ((Saddleable) mount).canBeSaddled()) {
            access.cranksdungeons$setSaddled(true);
        }
        markDirty();
    }

    @Override public boolean isEmpty() {
        return !isSaddleable() || !((MountSaddleAccess) mount).cranksdungeons$isSaddled();
    }

    @Override public boolean canPlayerUse(PlayerEntity player) {
        return mount.isAlive() && player.canInteractWithEntity(mount, 4.0);
    }

    @Override public void clear() {
        setStack(0, ItemStack.EMPTY);
    }
}
