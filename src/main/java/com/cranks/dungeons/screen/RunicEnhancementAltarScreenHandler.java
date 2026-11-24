package com.cranks.dungeons.screen;

import com.cranks.dungeons.block.entity.RunicEnhancementAltarBlockEntity;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.inventory.RunicAltarRules;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

/** Full loadout plus equipment and recoverable legacy tome/result slots. */
public final class RunicEnhancementAltarScreenHandler extends LoadoutStationHandler {
    public static final int EQUIPMENT = PLAYER_SLOT_END, STORED_TOME = EQUIPMENT + 1, OLD_RESULT = EQUIPMENT + 2;
    public static final int CONFIRM_REQUIRED = -3, NO_OUTCOME = -4;
    private final Inventory station;
    private final PropertyDelegate revision;

    public RunicEnhancementAltarScreenHandler(int id, PlayerInventory inventory) { this(id, inventory, new SimpleInventory(3)); }
    public RunicEnhancementAltarScreenHandler(int id, PlayerInventory inventory, Inventory station) {
        super(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, id, inventory);
        checkSize(station, 3);
        this.station = station;
        station.onOpen(inventory.player);
        for (int index = 0; index < 3; index++) {
            final int kind = index;
            addSlot(new Slot(station, index, 0, 0) {
                @Override public boolean canInsert(ItemStack stack) { return kind == 0 && EquipmentType.getTypeForItem(stack).isPresent(); }
                @Override public int getMaxItemCount() { return kind == 0 ? 1 : super.getMaxItemCount(); }
                @Override public boolean isEnabled() { return kind == 0 || !getStack().isEmpty(); }
            });
        }
        revision = station instanceof RunicEnhancementAltarBlockEntity altar ? new PropertyDelegate() {
            @Override public int get(int index) { return index == 0 ? altar.revision() & 0xffff : altar.revision() >>> 16; }
            @Override public void set(int index, int value) {}
            @Override public int size() { return 2; }
        } : new ArrayPropertyDelegate(2);
        addProperties(revision);
    }
    public int revision() { return revision.get(0) & 0xffff | (revision.get(1) & 0xffff) << 16; }
    public ItemStack equipment() { return station.getStack(0); }
    public ItemStack storedTome() { return station.getStack(1); }
    public ItemStack oldResult() { return station.getStack(2); }
    @Override public boolean canUse(PlayerEntity player) { return station.canPlayerUse(player); }
    @Override public void onClosed(PlayerEntity player) { super.onClosed(player); station.onClose(player); }

    public int apply(PlayerEntity player, int expectedRevision, ItemStack expectedGear, ItemStack expectedTome, boolean confirmed) {
        return apply(player,expectedRevision,expectedGear,expectedTome,confirmed,-1,0);
    }
    public int apply(PlayerEntity player,int expectedRevision,ItemStack expectedGear,ItemStack expectedTome,boolean confirmed,int target,int action) {
        if (!(station instanceof RunicEnhancementAltarBlockEntity altar) || !player.isAlive() || player.isSpectator()
                || player.currentScreenHandler != this || !canUse(player)) return CraftMaterialsResultPayload.UNAVAILABLE;
        if (revision() != expectedRevision || !ItemStack.areEqual(equipment(), expectedGear)) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        if(action==1||action==2){
            if(!com.cranks.dungeons.inventory.RuneCrafting.resolve(equipment(),action==2))return CraftMaterialsResultPayload.UNAVAILABLE;
            altar.markDirty();sendContentUpdates();return 1;
        }
        if(action!=0||!RunicAltarRules.isTome(expectedTome))return CraftMaterialsResultPayload.UNAVAILABLE;
        ItemStack tome = ItemStack.EMPTY;
        if (ItemStack.areItemsAndComponentsEqual(storedTome(), expectedTome)) tome = storedTome();
        else {
            var storage = new MaterialCrafting.Storage(player.getInventory());
            for (int i = 0; i < storage.size(); i++) {
                var candidate = player.getInventory().getStack(storage.slot(i));
                if (!candidate.isEmpty() && ItemStack.areItemsAndComponentsEqual(candidate, expectedTome)) { tome = candidate; break; }
            }
        }
        if (tome.isEmpty() || RunicAltarRules.reason(equipment(), tome, oldResult(),target) != null) return CraftMaterialsResultPayload.UNAVAILABLE;
        if (RunicAltarRules.destructive(equipment(), tome,target) && !confirmed) return CONFIRM_REQUIRED;
        if (!altar.tryEnhance(tome,target)) return NO_OUTCOME;
        player.getInventory().markDirty();
        sendContentUpdates();
        return 1;
    }
    @Override public ItemStack quickMove(PlayerEntity player, int index) {
        if (index < 5 || index > OLD_RESULT) return ItemStack.EMPTY;
        var slot = getSlot(index);
        if (!slot.hasStack() || !slot.canTakeItems(player)) return ItemStack.EMPTY;
        var stack = slot.getStack();
        var original = stack.copy();
        if (index >= EQUIPMENT) {
            PlayerLoadout.of(player.getInventory()).insert(stack);
            if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        } else if (EquipmentType.getTypeForItem(stack).isPresent()) {
            if (!insertItem(stack, EQUIPMENT, EQUIPMENT + 1, false)) return ItemStack.EMPTY;
        } else return super.quickMove(player, index);
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY); else slot.markDirty();
        slot.onTakeItem(player, stack);
        return original;
    }
}
