package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

/** Equipment slot that exposes a generic mount's vanilla saddle state. */
public final class MountSaddleSlot extends Slot {
    private final LivingEntity mount;

    public MountSaddleSlot(LivingEntity mount, int x, int y) {
        super(new MountSaddleInventory(mount), 0, x, y);
        this.mount = mount;
    }

    @Override public boolean isEnabled() {
        return mount instanceof Saddleable saddleable
                && mount instanceof MountSaddleAccess
                && saddleable.canBeSaddled();
    }

    @Override public boolean canInsert(ItemStack stack) {
        return stack.isOf(Items.SADDLE) && getStack().isEmpty()
                && mount instanceof Saddleable saddleable
                && saddleable.canBeSaddled()
                && !((MountSaddleAccess) mount).cranksdungeons$isSaddled();
    }

    @Override public int getMaxItemCount() { return 1; }

    @Override public boolean canTakeItems(PlayerEntity player) {
        return MountManager.canManage(player, mount)
                && mount instanceof MountSaddleAccess
                && ((MountSaddleAccess) mount).cranksdungeons$isSaddled();
    }
}
