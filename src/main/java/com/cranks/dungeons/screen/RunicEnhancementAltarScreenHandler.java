package com.cranks.dungeons.screen;

import com.cranks.dungeons.block.entity.RunicEnhancementAltarBlockEntity;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.item.ArtificialTomeItem;
import com.cranks.dungeons.item.ElementalTomeItem;
import com.cranks.dungeons.item.RunicTomeItem;
import com.cranks.dungeons.item.SwiftnessTomeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import com.cranks.dungeons.item.EnergyTomeItem;
import com.cranks.dungeons.item.EvasionTomeItem;

import java.util.HashMap;
import java.util.Map;

public class RunicEnhancementAltarScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final RunicEnhancementAltarBlockEntity blockEntity;

    private final Map<Integer, Integer> sourceSlots = new HashMap<>();

    private static final int PLAYER_INV_START = 3;
    private static final int PLAYER_INV_END = 39;

    // Client constructor
    public RunicEnhancementAltarScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3));
    }

    // Server constructor
    public RunicEnhancementAltarScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, syncId);
        this.inventory = inventory;
        this.blockEntity = inventory instanceof RunicEnhancementAltarBlockEntity ? (RunicEnhancementAltarBlockEntity) inventory : null;

        checkSize(inventory, 3);
        inventory.onOpen(playerInventory.player);

        // Equipment slot
        this.addSlot(new Slot(inventory, RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT, 44, 9) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return EquipmentType.getTypeForItem(stack).isPresent();
            }
        });

        // Tome slot
        this.addSlot(new Slot(inventory, RunicEnhancementAltarBlockEntity.TOME_SLOT, 44, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof RunicTomeItem
                        || stack.getItem() instanceof ElementalTomeItem
                        || stack.getItem() instanceof ArtificialTomeItem
                        || stack.getItem() instanceof SwiftnessTomeItem
                        || stack.getItem() instanceof EnergyTomeItem
                        || stack.getItem() instanceof EvasionTomeItem;
            }
        });

        // Result slot
        this.addSlot(new Slot(inventory, RunicEnhancementAltarBlockEntity.RESULT_SLOT, 116, 31) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Player inventory
        int inventoryX = 8;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, inventoryX + j * 18, 84 + i * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, inventoryX + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slot);

        if (clickedSlot.hasStack()) {
            ItemStack clickedStack = clickedSlot.getStack();
            newStack = clickedStack.copy();

            if (slot == RunicEnhancementAltarBlockEntity.RESULT_SLOT) {
                // Result is the enhanced equipment, return to equipment's original slot
                if (!tryReturnToSourceSlot(RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT, clickedStack)) {
                    if (!this.insertItem(clickedStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                sourceSlots.remove(RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT);
                sourceSlots.remove(RunicEnhancementAltarBlockEntity.TOME_SLOT);
                clickedSlot.onQuickTransfer(clickedStack, newStack);
            } else if (slot == RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT
                    || slot == RunicEnhancementAltarBlockEntity.TOME_SLOT) {
                if (!tryReturnToSourceSlot(slot, clickedStack)) {
                    if (!this.insertItem(clickedStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                sourceSlots.remove(slot);
            } else if (slot >= PLAYER_INV_START) {
                if (clickedStack.getItem() instanceof RunicTomeItem
                        || clickedStack.getItem() instanceof ElementalTomeItem
                        || clickedStack.getItem() instanceof ArtificialTomeItem
                        || clickedStack.getItem() instanceof SwiftnessTomeItem
                        || clickedStack.getItem() instanceof EnergyTomeItem
                        || clickedStack.getItem() instanceof EvasionTomeItem) {
                    if (!tryInsertAndTrack(clickedStack, RunicEnhancementAltarBlockEntity.TOME_SLOT, slot)) {
                        return ItemStack.EMPTY;
                    }
                } else if (EquipmentType.getTypeForItem(clickedStack).isPresent()) {
                    if (!tryInsertAndTrack(clickedStack, RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT, slot)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slot < 30) {
                    if (!this.insertItem(clickedStack, 30, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(clickedStack, PLAYER_INV_START, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (clickedStack.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }

            if (clickedStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            clickedSlot.onTakeItem(player, clickedStack);
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

        // Return equipment slot item
        ItemStack equipment = inventory.getStack(RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT);
        if (!equipment.isEmpty()) {
            inventory.setStack(RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT, ItemStack.EMPTY);
            returnToPlayerInventory(player, equipment, RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT);
        }

        // Return tome slot item
        ItemStack tome = inventory.getStack(RunicEnhancementAltarBlockEntity.TOME_SLOT);
        if (!tome.isEmpty()) {
            inventory.setStack(RunicEnhancementAltarBlockEntity.TOME_SLOT, ItemStack.EMPTY);
            returnToPlayerInventory(player, tome, RunicEnhancementAltarBlockEntity.TOME_SLOT);
        }

        // Return result slot item
        ItemStack result = inventory.getStack(RunicEnhancementAltarBlockEntity.RESULT_SLOT);
        if (!result.isEmpty()) {
            inventory.setStack(RunicEnhancementAltarBlockEntity.RESULT_SLOT, ItemStack.EMPTY);
            returnToPlayerInventory(player, result, RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT);
        }

        this.inventory.onClose(player);
        sourceSlots.clear();
    }

    private void returnToPlayerInventory(PlayerEntity player, ItemStack stack, int altarSlot) {
        // Try original slot first
        Integer sourceSlot = sourceSlots.get(altarSlot);
        if (sourceSlot != null && sourceSlot >= PLAYER_INV_START && sourceSlot < PLAYER_INV_END) {
            Slot target = this.slots.get(sourceSlot);
            if (!target.hasStack()) {
                target.setStack(stack.copy());
                stack.setCount(0);
                return;
            }
        }

        // Fall back to any available slot
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    public boolean tryEnhance() {
        if (blockEntity != null) {
            return blockEntity.tryEnhance();
        }
        return false;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 0) {
            return tryEnhance();
        }
        return false;
    }
}