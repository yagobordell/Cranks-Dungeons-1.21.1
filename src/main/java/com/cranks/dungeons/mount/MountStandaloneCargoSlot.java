package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import java.util.function.IntSupplier;

public final class MountStandaloneCargoSlot extends Slot {
    private final LivingEntity mount;
    private final IntSupplier capacitySupplier;
    public MountStandaloneCargoSlot(Inventory inventory, int index, int x, int y, LivingEntity mount) {
        this(inventory, index, x, y, mount, () -> MountManager.cargoCapacity(mount));
    }
    public MountStandaloneCargoSlot(Inventory inventory, int index, int x, int y,
                                    LivingEntity mount, IntSupplier capacitySupplier) {
        super(inventory, index, x, y);
        this.mount = mount;
        this.capacitySupplier = capacitySupplier;
    }
    private boolean available() {
        if (mount instanceof net.minecraft.entity.passive.LlamaEntity) {
            return getIndex() < capacitySupplier.getAsInt();
        }
        return ((MountDataProvider) mount).cranksdungeons$hasMountChest()
                && getIndex() < capacitySupplier.getAsInt();
    }
    @Override public boolean isEnabled() { return available(); }
    @Override public boolean canInsert(ItemStack stack) { return available(); }
    @Override public boolean canTakeItems(PlayerEntity player) { return available(); }
}
