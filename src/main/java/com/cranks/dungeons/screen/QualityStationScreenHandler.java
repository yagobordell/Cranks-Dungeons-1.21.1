package com.cranks.dungeons.screen;

import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.Property;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

import java.util.UUID;

import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.COMPLETED_PROPERTY;
import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.EQUIPMENT_SLOT;
import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.JOB_PROPERTY;
import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.MATERIAL_SLOT;
import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.PROGRESS_PROPERTY;
import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.PROPERTY_COUNT;
import static com.cranks.dungeons.block.entity.QualityStationBlockEntity.REMAINING_PROPERTY;

public final class QualityStationScreenHandler extends LoadoutStationHandler {
    public static final int EQUIPMENT = PLAYER_SLOT_END;
    public static final int MATERIAL = EQUIPMENT + 1;

    private final Inventory station;
    private final PropertyDelegate properties;
    private final UUID viewer;
    private final Property cancelPermission;

    public QualityStationScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(2), new ArrayPropertyDelegate(PROPERTY_COUNT));
    }

    public QualityStationScreenHandler(int syncId, PlayerInventory inventory, Inventory station, PropertyDelegate properties) {
        super(ModScreenHandlers.QUALITY_STATION_SCREEN_HANDLER, syncId, inventory);
        checkSize(station, 2);
        checkDataCount(properties, PROPERTY_COUNT);
        this.station = station;
        this.properties = properties;
        viewer = inventory.player.getUuid();
        addSlot(inputSlot(EQUIPMENT_SLOT));
        addSlot(inputSlot(MATERIAL_SLOT));
        addProperties(properties);
        cancelPermission = addProperty(new Property() {
            private int synced;

            @Override
            public int get() {
                if (station instanceof QualityStationBlockEntity bench) return bench.canCancel(viewer) ? 1 : 0;
                return synced;
            }

            @Override
            public void set(int value) {
                synced = value;
            }
        });
    }

    private Slot inputSlot(int index) {
        return new Slot(station, index, 0, 0) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return !processing() && QualityStationBlockEntity.accepts(index, stack, equipment(), material());
            }

            @Override
            public boolean canTakeItems(PlayerEntity player) {
                return !processing();
            }

            @Override
            public int getMaxItemCount() {
                return index == EQUIPMENT_SLOT ? 1 : super.getMaxItemCount();
            }
        };
    }

    public ItemStack equipment() { return station.getStack(EQUIPMENT_SLOT); }
    public ItemStack material() { return station.getStack(MATERIAL_SLOT); }
    public int progress() { return properties.get(PROGRESS_PROPERTY); }
    public int remaining() { return properties.get(REMAINING_PROPERTY); }
    public boolean processing() { return remaining() > 0; }
    public boolean completed() { return properties.get(COMPLETED_PROPERTY) != 0; }
    public int jobId() { return properties.get(JOB_PROPERTY); }
    public boolean canCancel() { return cancelPermission.get() != 0; }

    public boolean updateJob(int amount, int expectedJob, ItemStack gear, ItemStack material) {
        if (!(station instanceof QualityStationBlockEntity bench)) return false;
        boolean changed = amount == 0 ? bench.cancel(viewer, expectedJob) : bench.start(viewer, amount, gear, material);
        if (changed) sendContentUpdates();
        return changed;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return station.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        if (index < 5 || index > MATERIAL) return ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (!slot.hasStack() || !slot.canTakeItems(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index >= EQUIPMENT) {
            if (!PlayerLoadout.of(player.getInventory()).insert(stack)) return ItemStack.EMPTY;
        } else if (!insertItem(stack, EQUIPMENT, MATERIAL + 1, false)) {
            return super.quickMove(player, index);
        }
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        slot.onTakeItem(player, stack);
        return original;
    }
}
