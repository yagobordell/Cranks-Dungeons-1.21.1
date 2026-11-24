package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** Retains native equipment callbacks while making all mount gear owner-controlled. */
public final class MountEquipmentSlot extends Slot {
    private final Slot delegate;
    private final LivingEntity mount;
    private final PlayerEntity viewer;

    public MountEquipmentSlot(Slot delegate, LivingEntity mount, PlayerEntity viewer) {
        super(delegate.inventory, delegate.getIndex(), delegate.x, delegate.y);
        this.delegate = delegate;
        this.mount = mount;
        this.viewer = viewer;
    }

    @Override public boolean canInsert(ItemStack stack) {
        return MountManager.canManage(viewer, mount) && delegate.canInsert(stack);
    }
    @Override public boolean canTakeItems(PlayerEntity player) {
        return MountManager.canManage(player, mount) && delegate.canTakeItems(player);
    }
    @Override public boolean isEnabled() { return delegate.isEnabled(); }
    @Override public ItemStack getStack() { return delegate.getStack(); }
    @Override public void setStack(ItemStack stack) { delegate.setStack(stack); }
    @Override public void setStack(ItemStack stack, ItemStack previous) { delegate.setStack(stack, previous); }
    @Override public void setStackNoCallbacks(ItemStack stack) { delegate.setStackNoCallbacks(stack); }
    @Override public ItemStack takeStack(int amount) { return delegate.takeStack(amount); }
    @Override public void onTakeItem(PlayerEntity player, ItemStack stack) { delegate.onTakeItem(player, stack); }
    @Override public void markDirty() { delegate.markDirty(); }
    @Override public int getMaxItemCount() { return delegate.getMaxItemCount(); }
    @Override public int getMaxItemCount(ItemStack stack) { return delegate.getMaxItemCount(stack); }
}
