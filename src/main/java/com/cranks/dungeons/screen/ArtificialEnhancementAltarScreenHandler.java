package com.cranks.dungeons.screen;

import com.cranks.dungeons.block.entity.ArtificialEnhancementAltarBlockEntity;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
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

public class ArtificialEnhancementAltarScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ArtificialEnhancementAltarBlockEntity blockEntity;
    private final PropertyDelegate propertyDelegate;

    private String[] clientStatIds = new String[]{"", "", ""};
    private int[] clientTiers = new int[]{0, 0, 0};
    private boolean clientOptionsReceived = false;

    private final Map<Integer, Integer> sourceSlots = new HashMap<>();

    private static final int PLAYER_INV_START = 1;
    private static final int PLAYER_INV_END = 37;

    // Client constructor
    public ArtificialEnhancementAltarScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(4));
    }

    // Server constructor
    public ArtificialEnhancementAltarScreenHandler(int syncId, PlayerInventory playerInventory,
                                                   ArtificialEnhancementAltarBlockEntity blockEntity,
                                                   PropertyDelegate propertyDelegate) {
        this(syncId, playerInventory, (Inventory) blockEntity, propertyDelegate);
    }

    private ArtificialEnhancementAltarScreenHandler(int syncId, PlayerInventory playerInventory,
                                                    Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.ARTIFICIAL_ENHANCEMENT_ALTAR, syncId);
        this.inventory = inventory;
        this.blockEntity = inventory instanceof ArtificialEnhancementAltarBlockEntity be ? be : null;
        this.propertyDelegate = propertyDelegate;

        checkSize(inventory, 1);
        inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);

        // Equipment slot - closer to top
        this.addSlot(new Slot(inventory, ArtificialEnhancementAltarBlockEntity.EQUIPMENT_SLOT, 80, 10) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return EquipmentType.getTypeForItem(stack).isPresent();
            }
        });

// Player inventory
        int inventoryX = 8;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, inventoryX + j * 18, 98 + i * 18));
            }
        }

// Player hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, inventoryX + i * 18, 156));
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

            if (slot == ArtificialEnhancementAltarBlockEntity.EQUIPMENT_SLOT) {
                if (!tryReturnToSourceSlot(slot, clickedStack)) {
                    if (!this.insertItem(clickedStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                sourceSlots.remove(slot);
            } else if (slot >= PLAYER_INV_START) {
                if (EquipmentType.getTypeForItem(clickedStack).isPresent()) {
                    if (!tryInsertAndTrack(clickedStack, ArtificialEnhancementAltarBlockEntity.EQUIPMENT_SLOT, slot)) {
                        return ItemStack.EMPTY;
                    }
                } else {
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

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id <= 2 && blockEntity != null) {
            return blockEntity.selectOption(id);
        }
        return false;
    }

    // === Client-side sync methods ===

    public void setClientStatOptions(String[] statIds, int[] tiers) {
        this.clientStatIds = statIds;
        this.clientTiers = tiers;
        this.clientOptionsReceived = !statIds[0].isEmpty() || !statIds[1].isEmpty() || !statIds[2].isEmpty();
    }

    public boolean hasClientOptions() {
        return clientOptionsReceived;
    }

    public void clearClientOptions() {
        clientStatIds = new String[]{"", "", ""};
        clientTiers = new int[]{0, 0, 0};
        clientOptionsReceived = false;
    }

    public String getClientStatId(int index) {
        if (index < 0 || index >= 3) return "";
        return clientStatIds[index];
    }

    public int getClientTier(int index) {
        if (index < 0 || index >= 3) return 0;
        return clientTiers[index];
    }

    public boolean areOptionsGenerated() {
        return propertyDelegate.get(0) == 1;
    }

    public int getTierOption(int index) {
        if (index < 0 || index >= 3) return 0;
        return propertyDelegate.get(index + 1);
    }
}