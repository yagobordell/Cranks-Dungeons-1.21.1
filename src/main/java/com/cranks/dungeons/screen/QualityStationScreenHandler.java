package com.cranks.dungeons.screen;

import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import java.util.HashMap;
import java.util.Map;

public class QualityStationScreenHandler extends ScreenHandler {
    private final Map<Integer, Integer> sourceSlots = new HashMap<>();
    private static final int PLAYER_INV_START = 2;
    private static final int PLAYER_INV_END = 38;
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    public final QualityStationBlockEntity blockEntity;

    public QualityStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(2));
    }

    public QualityStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.QUALITY_STATION_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        if (inventory instanceof QualityStationBlockEntity) {
            this.blockEntity = (QualityStationBlockEntity) inventory;
        } else {
            this.blockEntity = null;
        }
        
        inventory.onOpen(playerInventory.player);

        // Quality Station Inventory
        this.addSlot(new Slot(inventory, QualityStationBlockEntity.EQUIPMENT_SLOT, 56, 26) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return com.cranks.dungeons.equipment.EquipmentType.getTypeForItem(stack).isPresent();
            }
        });
        this.addSlot(new Slot(inventory, QualityStationBlockEntity.MATERIAL_SLOT, 104, 26) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof com.cranks.dungeons.item.QualityMaterialItem;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addProperties(propertyDelegate);
    }

    public int getScaledProgress() {
        int processTick = propertyDelegate.get(0);
        int maxProcessTime = 600; // 30 seconds
        int progressArrowSize = 24; 
        
        return maxProcessTime != 0 && processTick != 0 ? processTick * progressArrowSize / maxProcessTime : 0;
    }

    public boolean isProcessing() {
        return propertyDelegate.get(1) > 0;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            
            if (invSlot < this.inventory.size()) {
                // Moving from station to player
                if (!this.tryReturnToSourceSlot(invSlot, originalStack)) {
                    if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                // Moving from player to station
                if (!this.tryInsertAndTrack(originalStack, QualityStationBlockEntity.EQUIPMENT_SLOT, invSlot) && 
                    !this.tryInsertAndTrack(originalStack, QualityStationBlockEntity.MATERIAL_SLOT, invSlot)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    private boolean tryInsertAndTrack(ItemStack stack, int altarSlot, int sourceSlot) {
        if (!this.insertItem(stack, altarSlot, altarSlot + 1, false)) {
            return false;
        }
        sourceSlots.put(altarSlot, sourceSlot);
        return true;
    }

    private boolean tryReturnToSourceSlot(int altarSlot, ItemStack stack) {
        Integer sourceSlot = sourceSlots.get(altarSlot);
        if (sourceSlot == null) return false;
        if (sourceSlot < PLAYER_INV_START || sourceSlot >= PLAYER_INV_END) return false;

        Slot target = this.slots.get(sourceSlot);
        if (target.hasStack()) {
            return false;
        }

        if (!target.canInsert(stack)) {
            return false;
        }

        target.setStack(stack.copy());
        stack.setCount(0);
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
        sourceSlots.clear();
    }

    private void returnToPlayerInventory(PlayerEntity player, ItemStack stack, int altarSlot) {
        Integer sourceSlot = sourceSlots.get(altarSlot);
        if (sourceSlot != null && sourceSlot >= PLAYER_INV_START && sourceSlot < PLAYER_INV_END) {
            Slot target = this.slots.get(sourceSlot);
            if (!target.hasStack()) {
                target.setStack(stack.copy());
                stack.setCount(0);
                return;
            }
        }

        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
