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
    private List<CargoSource> sources=List.of();
    private List<SlotRef> cachedSlots=List.of();

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
        return new LlamaPackInventory(anchor, LlamaPackManager.freshMembers(anchor), screenCapacity);
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
        var members=LlamaPackManager.members(anchor);
        // Never reinterpret an old handler's slot indices as a different pack.
        if(members.size()!=memberUuids.size())return List.of();
        for(int i=0;i<members.size();i++)if(!members.get(i).getUuid().equals(memberUuids.get(i)))return List.of();
        boolean changed=sources.size()!=members.size();
        for(int i=0;!changed&&i<members.size();i++){
            var llama=members.get(i);var source=sources.get(i);
            changed=source.llama!=llama||source.inventory!=AbstractHorseInventoryAccess.of(llama).cranksdungeons$getHorseItems()
                    ||source.capacity!=LlamaPackManager.memberCapacity(llama)
                    ||source.chest!=((MountDataProvider)llama).cranksdungeons$hasMountChest();
        }
        if(changed){
            var next=new java.util.ArrayList<CargoSource>();var slots=new java.util.ArrayList<SlotRef>();
            for(var llama:members){
                var inventory=AbstractHorseInventoryAccess.of(llama).cranksdungeons$getHorseItems();
                int count=LlamaPackManager.memberCapacity(llama);boolean chest=((MountDataProvider)llama).cranksdungeons$hasMountChest();
                next.add(new CargoSource(llama,inventory,count,chest));
                if(chest)for(int i=0;i<count;i++)slots.add(new SlotRef(inventory,i+LlamaPackManager.CARGO_START));
            }
            sources=List.copyOf(next);cachedSlots=List.copyOf(slots);
        }
        return cachedSlots;
    }

    /** Whether a displayed slot belongs to the selected llama's attachment. */
    public boolean belongsToAnchor(int slot) {
        var ref=slotRef(slot);
        return anchor!=null&&ref!=null&&ref.inventory==AbstractHorseInventoryAccess.of(anchor).cranksdungeons$getHorseItems();
    }

    private record CargoSource(LlamaEntity llama, SimpleInventory inventory, int capacity, boolean chest) {}

    private record SlotRef(SimpleInventory inventory, int slot) {
    }
}
