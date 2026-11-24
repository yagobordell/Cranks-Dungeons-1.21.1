package com.cranks.dungeons.screen;

import com.cranks.dungeons.block.entity.ArtificialEnhancementAltarBlockEntity;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

public final class ArtificialEnhancementAltarScreenHandler extends LoadoutStationHandler {
    public static final int EQUIPMENT = PLAYER_SLOT_END, CONFIRM_REQUIRED = -3;
    private final Inventory station;
    private final PropertyDelegate revision;
    public ArtificialEnhancementAltarScreenHandler(int id, PlayerInventory inventory) { this(id, inventory, new SimpleInventory(1)); }
    public ArtificialEnhancementAltarScreenHandler(int id, PlayerInventory inventory, Inventory station) {
        super(ModScreenHandlers.ARTIFICIAL_ENHANCEMENT_ALTAR, id, inventory);
        checkSize(station, 1); this.station = station; station.onOpen(inventory.player);
        addSlot(new Slot(station, 0, 0, 0) {
            @Override public boolean canInsert(ItemStack stack) { return EquipmentType.getTypeForItem(stack).isPresent(); }
            @Override public int getMaxItemCount() { return 1; }
        });
        if (station instanceof ArtificialEnhancementAltarBlockEntity altar) altar.prepareOptions();
        revision = station instanceof ArtificialEnhancementAltarBlockEntity altar ? new PropertyDelegate() {
            @Override public int get(int index) { return index == 0 ? altar.revision() & 0xffff : altar.revision() >>> 16; }
            @Override public void set(int index, int value) {}
            @Override public int size() { return 2; }
        } : new ArrayPropertyDelegate(2);
        addProperties(revision);
    }
    public int revision() { return revision.get(0) & 0xffff | (revision.get(1) & 0xffff) << 16; }
    public ItemStack equipment() { return station.getStack(0); }
    @Override public boolean canUse(PlayerEntity player) { return station.canPlayerUse(player); }
    @Override public void onClosed(PlayerEntity player) { super.onClosed(player); station.onClose(player); }
    public int apply(PlayerEntity player, int expectedRevision, ItemStack expectedGear, int option, boolean confirmed) {
        if (!(station instanceof ArtificialEnhancementAltarBlockEntity altar) || player.currentScreenHandler != this
                || !player.isAlive() || player.isSpectator() || !canUse(player) || option < 0 || option > 2) return CraftMaterialsResultPayload.UNAVAILABLE;
        if (revision() != expectedRevision || !ItemStack.areEqual(equipment(), expectedGear)) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        if (!confirmed) return CONFIRM_REQUIRED;
        if (!altar.selectOption(option)) return CraftMaterialsResultPayload.UNAVAILABLE;
        sendContentUpdates(); return 1;
    }
    @Override public ItemStack quickMove(PlayerEntity player, int index) {
        if (index < 5 || index > EQUIPMENT) return ItemStack.EMPTY;
        var slot = getSlot(index); if (!slot.hasStack() || !slot.canTakeItems(player)) return ItemStack.EMPTY;
        var stack = slot.getStack(); var original = stack.copy();
        if (index == EQUIPMENT) { PlayerLoadout.of(player.getInventory()).insert(stack); if (stack.getCount() == original.getCount()) return ItemStack.EMPTY; }
        else if (EquipmentType.getTypeForItem(stack).isPresent()) { if (!insertItem(stack, EQUIPMENT, EQUIPMENT + 1, false)) return ItemStack.EMPTY; }
        else return super.quickMove(player, index);
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY); else slot.markDirty();
        slot.onTakeItem(player, stack); return original;
    }
}
