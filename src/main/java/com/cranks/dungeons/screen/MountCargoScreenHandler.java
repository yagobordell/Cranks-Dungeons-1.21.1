package com.cranks.dungeons.screen;

import com.cranks.dungeons.mount.MountChestSlot;
import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class MountCargoScreenHandler extends ScreenHandler {
    public record OpeningData(int entityId, int capacity) {
        public OpeningData(int entityId) {
            this(entityId, 0);
        }

        public static final PacketCodec<RegistryByteBuf, OpeningData> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, OpeningData::entityId,
                PacketCodecs.VAR_INT, OpeningData::capacity,
                OpeningData::new);
    }

    private final LivingEntity mount;
    private final int capacity;
    private final int mountSlotCount;

    public MountCargoScreenHandler(int syncId, PlayerInventory playerInventory, OpeningData opening) {
        this(syncId, playerInventory, findMount(playerInventory.player, opening.entityId()),
                opening.capacity());
    }

    public MountCargoScreenHandler(int syncId, PlayerInventory playerInventory, LivingEntity mount) {
        this(syncId, playerInventory, mount, MountManager.cargoCapacity(mount));
    }

    private MountCargoScreenHandler(int syncId, PlayerInventory playerInventory,
                                    LivingEntity mount, int sentCapacity) {
        super(ModScreenHandlers.MOUNT_CARGO, syncId);
        this.mount = mount;
        this.capacity = mount == null ? Math.max(0, sentCapacity) : MountManager.cargoCapacity(mount);

        SimpleInventory cargo = mount == null ? new SimpleInventory(MountData.MAX_CARGO_SLOTS)
                : MountManager.data(mount).cargo;
        if (mount != null) {
            addSlot(new com.cranks.dungeons.mount.MountSaddleSlot(mount, 8, 18));
            addSlot(new MountChestSlot(mount, 8, 54));
        } else {
            addSlot(new Slot(new SimpleInventory(1), 0, 8, 18));
            addSlot(new Slot(new SimpleInventory(1), 0, 8, 54));
        }

        for (int slot = 0; slot < capacity; slot++) {
            int row = slot / 5;
            int column = slot % 5;
            if (mount != null) addSlot(new com.cranks.dungeons.mount.MountStandaloneCargoSlot(
                    cargo, slot, 80 + column * 18, 18 + row * 18, mount));
            else addSlot(new Slot(cargo, slot, 80 + column * 18, 18 + row * 18));
        }
        mountSlotCount = 2 + capacity;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    public LivingEntity mount() { return mount; }
    public int capacity() { return capacity; }

    @Override
    public boolean canUse(PlayerEntity player) {
        return mount != null && mount.isAlive() && player.canInteractWithEntity(mount, 4.0);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        ItemStack moving = slot.getStack();
        ItemStack original = moving.copy();
        if (index < mountSlotCount) {
            if (!insertItem(moving, mountSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (moving.isOf(net.minecraft.item.Items.SADDLE) && !getSlot(0).hasStack()
                    && insertItem(moving, 0, 1, false)) {
                // Saddle moved into the equipment slot.
            } else if (moving.isOf(net.minecraft.item.Items.CHEST) && !getSlot(1).hasStack()
                    && insertItem(moving, 1, 2, false)) {
                // Chest moved into the equipment slot.
            } else if (capacity <= 0 || !getSlot(1).hasStack()
                    || !insertItem(moving, 2, mountSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (moving.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return original;
    }

    private static LivingEntity findMount(PlayerEntity player, int entityId) {
        Entity entity = player.getEntityWorld().getEntityById(entityId);
        return entity instanceof LivingEntity living ? living : null;
    }
}
