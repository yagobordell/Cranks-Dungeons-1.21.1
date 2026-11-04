package com.cranks.dungeons.mount;

import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;

public final class MountLlamaCarpetSlot extends Slot {
    private final LlamaEntity llama;

    public MountLlamaCarpetSlot(LlamaEntity llama, int x, int y) {
        super(new MountLlamaCarpetInventory(llama), 0, x, y);
        this.llama = llama;
    }

    @Override public boolean canInsert(ItemStack stack) {
        return stack.isIn(ItemTags.WOOL_CARPETS) && getStack().isEmpty();
    }
    @Override public int getMaxItemCount() { return 1; }
    @Override public boolean canTakeItems(PlayerEntity player) {
        return MountManager.canManage(player, llama) && !getStack().isEmpty();
    }
}
