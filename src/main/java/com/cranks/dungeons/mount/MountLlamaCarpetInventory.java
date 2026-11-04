package com.cranks.dungeons.mount;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

/** One-slot view of the carpet equipped in a llama's body slot. */
public final class MountLlamaCarpetInventory extends SimpleInventory {
    private final LlamaEntity llama;

    public MountLlamaCarpetInventory(LlamaEntity llama) {
        super(1);
        this.llama = llama;
    }

    @Override public ItemStack getStack(int slot) {
        return slot == 0 ? llama.getEquippedStack(EquipmentSlot.BODY) : ItemStack.EMPTY;
    }

    @Override public ItemStack removeStack(int slot, int amount) { return removeStack(slot); }

    @Override public ItemStack removeStack(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack carpet = getStack(0).copy();
        if (carpet.isEmpty()) return ItemStack.EMPTY;
        llama.equipStack(EquipmentSlot.BODY, ItemStack.EMPTY);
        markDirty();
        return carpet;
    }

    @Override public void setStack(int slot, ItemStack stack) {
        if (slot != 0 || (!stack.isEmpty() && !stack.isIn(ItemTags.WOOL_CARPETS))) return;
        llama.equipStack(EquipmentSlot.BODY, stack.copy());
        markDirty();
    }

    @Override public boolean isEmpty() { return getStack(0).isEmpty(); }
    @Override public boolean canPlayerUse(PlayerEntity player) {
        return llama.isAlive() && player.canInteractWithEntity(llama, 4.0);
    }
    @Override public void clear() { setStack(0, ItemStack.EMPTY); }
}
