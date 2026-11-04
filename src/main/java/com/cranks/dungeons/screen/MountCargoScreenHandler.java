package com.cranks.dungeons.screen;

import com.cranks.dungeons.mount.MountChestSlot;
import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.mount.LlamaPackInventory;
import com.cranks.dungeons.mount.LlamaPackManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.Slot;

public final class MountCargoScreenHandler extends ScreenHandler {
    public record OpeningData(int entityId, int capacity, int packSize, int activeCapacity) {
        public OpeningData(int entityId) {
            this(entityId, 0, 0, 0);
        }

        public OpeningData(int entityId, int capacity) {
            this(entityId, capacity, 0, capacity);
        }

        public OpeningData(int entityId, int capacity, int packSize) {
            this(entityId, capacity, packSize, capacity);
        }

        public static final PacketCodec<RegistryByteBuf, OpeningData> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, OpeningData::entityId,
                PacketCodecs.VAR_INT, OpeningData::capacity,
                PacketCodecs.VAR_INT, OpeningData::packSize,
                PacketCodecs.VAR_INT, OpeningData::activeCapacity,
                OpeningData::new);
    }

    private final LivingEntity mount;
    private final PlayerInventory playerInventory;
    private final int capacity;
    private final int mountSlotCount;
    private final int cargoStartIndex;
    private final int chestSlotIndex;
    private final LlamaPackInventory llamaPackInventory;
    private final int packSize;
    private int syncedActiveCapacity;

    public MountCargoScreenHandler(int syncId, PlayerInventory playerInventory, OpeningData opening) {
        this(syncId, playerInventory, findMount(playerInventory.player, opening.entityId()),
                opening.capacity(), opening.packSize(), opening.activeCapacity());
    }

    public MountCargoScreenHandler(int syncId, PlayerInventory playerInventory, LivingEntity mount) {
        this(syncId, playerInventory, mount, MountManager.cargoScreenCapacity(mount),
                mount instanceof LlamaEntity llama ? LlamaPackManager.members(llama).size() : 0,
                MountManager.cargoCapacity(mount));
    }

    private MountCargoScreenHandler(int syncId, PlayerInventory playerInventory,
                                    LivingEntity mount, int sentCapacity,
                                    int sentPackSize, int sentActiveCapacity) {
        super(ModScreenHandlers.MOUNT_CARGO, syncId);
        this.mount = mount;
        this.playerInventory = playerInventory;
        boolean llama = mount instanceof LlamaEntity;
        boolean clientSide = playerInventory.player.getEntityWorld().isClient();
        this.capacity = mount == null || clientSide || llama
                ? Math.max(0, sentCapacity) : MountManager.cargoCapacity(mount);
        this.syncedActiveCapacity = Math.min(this.capacity, Math.max(0, sentActiveCapacity));

        Inventory cargo;
        if (mount instanceof LlamaEntity llamaMount) {
            llamaPackInventory = clientSide ? LlamaPackInventory.clientView(capacity)
                    : LlamaPackInventory.forPack(llamaMount, capacity);
            packSize = sentPackSize > 0 ? sentPackSize : llamaPackInventory.memberUuids().size();
            cargo = llamaPackInventory;
        } else {
            llamaPackInventory = null;
            packSize = 0;
            cargo = mount == null ? new SimpleInventory(Math.max(MountData.MAX_CARGO_SLOTS, sentCapacity))
                    : MountManager.data(mount).cargo;
        }

        // Keep the slot grid fixed for the life of the screen, while syncing
        // the currently usable llama pack slots as chests change.
        addProperty(new Property() {
            @Override
            public int get() {
                return playerInventory.player.getEntityWorld().isClient()
                        ? syncedActiveCapacity : serverActiveCapacity();
            }

            @Override
            public void set(int value) {
                syncedActiveCapacity = Math.max(0, Math.min(MountCargoScreenHandler.this.capacity, value));
            }
        });

        if (mount != null) {
            addSlot(new com.cranks.dungeons.mount.MountSaddleSlot(mount, 8, 18));
            if (mount instanceof LlamaEntity llamaMount) {
                addSlot(new com.cranks.dungeons.mount.MountLlamaCarpetSlot(llamaMount, 8, 36));
                chestSlotIndex = 2;
                cargoStartIndex = 3;
            } else {
                chestSlotIndex = 1;
                cargoStartIndex = 2;
            }
            addSlot(new MountChestSlot(mount, 8, 54));
        } else {
            addSlot(new Slot(new SimpleInventory(1), 0, 8, 18));
            addSlot(new Slot(new SimpleInventory(1), 0, 8, 54));
            chestSlotIndex = 1;
            cargoStartIndex = 2;
        }

        for (int slot = 0; slot < capacity; slot++) {
            int row = slot / 5;
            int column = slot % 5;
            if (mount != null) addSlot(new com.cranks.dungeons.mount.MountStandaloneCargoSlot(
                    cargo, slot, 80 + column * 18, 18 + row * 18, mount,
                    this::activeCapacity));
            else addSlot(new Slot(cargo, slot, 80 + column * 18, 18 + row * 18));
        }
        mountSlotCount = cargoStartIndex + capacity;

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
    public int activeCapacity() {
        if (!(mount instanceof LlamaEntity llama)) return capacity;
        if (playerInventory.player.getEntityWorld().isClient()) {
            return syncedActiveCapacity;
        }
        return serverActiveCapacity();
    }

    private int serverActiveCapacity() {
        return mount instanceof LlamaEntity llama
                ? Math.min(capacity, LlamaPackManager.cargoCapacity(llama)) : capacity;
    }
    public int cargoStartIndex() { return cargoStartIndex; }
    public int chestSlotIndex() { return chestSlotIndex; }
    public int packSize() { return packSize; }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (mount == null || !mount.isAlive() || !player.canInteractWithEntity(mount, 4.0)) return false;
        if (player.getEntityWorld().isClient()) return true;
        if (!MountManager.canAccessMount(player, mount)) return false;
        if (llamaPackInventory == null || !(mount instanceof LlamaEntity llama)) return true;
        return LlamaPackManager.samePack(llama, llamaPackInventory.memberUuids())
                && LlamaPackManager.canPlayerAccessPack(player, llama);
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
            } else if (mount instanceof LlamaEntity && moving.isIn(ItemTags.WOOL_CARPETS)
                    && !getSlot(1).hasStack() && insertItem(moving, 1, 2, false)) {
                // Carpet moved into the llama's body equipment slot.
            } else if (moving.isOf(net.minecraft.item.Items.CHEST)
                    && !getSlot(chestSlotIndex).hasStack()
                    && insertItem(moving, chestSlotIndex, chestSlotIndex + 1, false)) {
                // Chest moved into the equipment slot.
            } else if (!hasCargoAccess() || !insertItem(moving, cargoStartIndex, mountSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (index == chestSlotIndex && moving.isEmpty()) {
            slot.onTakeItem(player, original);
        }
        if (moving.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return original;
    }

    private boolean hasCargoAccess() {
        return activeCapacity() > 0
                && (llamaPackInventory != null || getSlot(chestSlotIndex).hasStack());
    }

    private static LivingEntity findMount(PlayerEntity player, int entityId) {
        Entity entity = player.getEntityWorld().getEntityById(entityId);
        return entity instanceof LivingEntity living ? living : null;
    }
}
