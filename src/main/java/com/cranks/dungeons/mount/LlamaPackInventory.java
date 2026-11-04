package com.cranks.dungeons.mount;

import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** A live view over each chested pack member's own vanilla cargo slots. */
public final class LlamaPackInventory extends SimpleInventory {
    private final LlamaEntity anchor;
    private final List<UUID> memberUuids;

    private LlamaPackInventory(LlamaEntity anchor, List<LlamaEntity> members, int capacity) {
        super(Math.max(0, capacity));
        this.anchor = anchor;
        memberUuids = members.stream().map(LlamaEntity::getUuid).toList();
    }

    private LlamaPackInventory(int capacity) {
        super(Math.max(0, capacity));
        anchor = null;
        memberUuids = List.of();
    }

    public static LlamaPackInventory forPack(LlamaEntity anchor, int screenCapacity) {
        return new LlamaPackInventory(anchor, LlamaPackManager.members(anchor), screenCapacity);
    }

    public static LlamaPackInventory clientView(int capacity) {
        return new LlamaPackInventory(capacity);
    }

    public List<UUID> memberUuids() {
        return memberUuids;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (anchor == null) return super.getStack(slot);
        SlotRef ref = slotRef(slot);
        return ref == null || ref.slot >= ref.inventory.size()
                ? ItemStack.EMPTY : ref.inventory.getStack(ref.slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (anchor == null) return super.removeStack(slot, amount);
        SlotRef ref = slotRef(slot);
        return ref == null || ref.slot >= ref.inventory.size()
                ? ItemStack.EMPTY : ref.inventory.removeStack(ref.slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (anchor == null) return super.removeStack(slot);
        SlotRef ref = slotRef(slot);
        return ref == null || ref.slot >= ref.inventory.size()
                ? ItemStack.EMPTY : ref.inventory.removeStack(ref.slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (anchor == null) {
            super.setStack(slot, stack);
            return;
        }
        SlotRef ref = slotRef(slot);
        if (ref != null && ref.slot < ref.inventory.size()) {
            ref.inventory.setStack(ref.slot, stack);
        }
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < size(); slot++) {
            if (!getStack(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public void markDirty() {
        if (anchor == null) {
            super.markDirty();
            return;
        }
        Set<SimpleInventory> inventories = new HashSet<>();
        for (SlotRef ref : activeCargoSlots()) inventories.add(ref.inventory);
        inventories.forEach(SimpleInventory::markDirty);
    }

    @Override
    public void clear() {
        for (int slot = 0; slot < size(); slot++) setStack(slot, ItemStack.EMPTY);
        markDirty();
    }

    private SlotRef slotRef(int slot) {
        if (slot < 0 || slot >= size()) return null;
        if (anchor == null) return null;
        List<SlotRef> active = activeCargoSlots();
        return slot < active.size() ? active.get(slot) : null;
    }

    private List<SlotRef> activeCargoSlots() {
        if (anchor == null) return List.of();
        return LlamaPackManager.members(anchor).stream()
                .filter(llama -> ((MountDataProvider) llama).cranksdungeons$hasMountChest())
                .flatMap(llama -> {
                    SimpleInventory inventory = AbstractHorseInventoryAccess.of(llama)
                            .cranksdungeons$getHorseItems();
                    int cargoCapacity = Math.max(0,
                            Math.min(inventory.size() - 2, llama.getStrength() * 3));
                    return java.util.stream.IntStream.range(0, cargoCapacity)
                            .mapToObj(index -> new SlotRef(inventory, index + 2));
                })
                .toList();
    }

    private record SlotRef(SimpleInventory inventory, int slot) {
    }
}
