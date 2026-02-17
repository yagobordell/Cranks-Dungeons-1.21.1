package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.network.StatOptionsSyncPacket;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ArtificialEnhancementAltarBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public static final int EQUIPMENT_SLOT = 0;
    public static final int RESULT_SLOT = 1;

    private String[] statOptions = new String[3];
    private int[] tierOptions = new int[3];
    private boolean optionsGenerated = false;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> optionsGenerated ? 1 : 0;
                case 1 -> tierOptions[0];
                case 2 -> tierOptions[1];
                case 3 -> tierOptions[2];
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> optionsGenerated = value == 1;
                case 1 -> tierOptions[0] = value;
                case 2 -> tierOptions[1] = value;
                case 3 -> tierOptions[2] = value;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public ArtificialEnhancementAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARTIFICIAL_ALTAR_BE, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Artificial Enhancement Altar");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        ArtificialEnhancementAltarScreenHandler handler = new ArtificialEnhancementAltarScreenHandler(syncId, playerInventory, this, propertyDelegate);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            syncOptionsToPlayer(serverPlayer, syncId);
        }

        return handler;
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) {
            if (slot == EQUIPMENT_SLOT) {
                clearOptions();
                syncClearToOpenPlayers();
            }
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(inventory, slot);
        if (slot == EQUIPMENT_SLOT) {
            clearOptions();
            syncClearToOpenPlayers();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCount(stack)) {
            stack.setCount(getMaxCount(stack));
        }
        if (slot == EQUIPMENT_SLOT) {
            clearOptions();
            if (!stack.isEmpty() && ItemStatManager.hasCrypticStat(stack)) {
                generateOrLoadOptions(stack);
                syncToOpenPlayers();
            } else {
                syncClearToOpenPlayers();
            }
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player.getBlockPos().isWithinDistance(this.pos, 8.0);
    }

    @Override
    public void clear() {
        inventory.clear();
        clearOptions();
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{EQUIPMENT_SLOT, RESULT_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == RESULT_SLOT) return false;
        if (slot == EQUIPMENT_SLOT) {
            return EquipmentType.getTypeForItem(stack).isPresent() && ItemStatManager.hasCrypticStat(stack);
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == RESULT_SLOT;
    }

    /**
     * Load stored options from the item, or generate new ones if first time.
     */
    private void generateOrLoadOptions(ItemStack equipment) {
        // Try to load previously rolled options from the item
        String[] storedStatIds = ItemStatManager.getStoredCrypticStatIds(equipment);
        int[] storedTiers = ItemStatManager.getStoredCrypticTiers(equipment);

        if (storedStatIds != null && storedTiers != null) {
            // Use the stored options - no re-rolling
            for (int i = 0; i < 3; i++) {
                statOptions[i] = storedStatIds[i];
                tierOptions[i] = storedTiers[i];
            }
            optionsGenerated = true;
            markDirty();
            return;
        }

        // First time - generate new options and store them on the item
        Optional<EquipmentType> equipTypeOpt = EquipmentType.getTypeForItem(equipment);
        if (equipTypeOpt.isEmpty()) return;

        EquipmentType equipType = equipTypeOpt.get();
        List<CustomStat> availableStats = StatRegistry.getStatsByEquipmentType(equipType);

        List<ItemStatManager.ItemStat> existingStats = ItemStatManager.getStats(equipment);
        availableStats = availableStats.stream()
                .filter(stat -> {
                    for (var existing : existingStats) {
                        if (existing.statId.equals(stat.getId())) return false;
                        CustomStat existingStat = StatRegistry.getStat(existing.statId);
                        if (existingStat != null && existingStat.getAttribute().equals(stat.getAttribute())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if (availableStats.size() < 3) {
            clearOptions();
            return;
        }

        Random random = new Random();
        Collections.shuffle(availableStats, random);

        for (int i = 0; i < 3; i++) {
            statOptions[i] = availableStats.get(i).getId();
            tierOptions[i] = random.nextInt(5) + 1;
        }

        optionsGenerated = true;

        // Store the rolled options on the item so they persist
        ItemStatManager.storeCrypticOptions(equipment, statOptions, tierOptions);

        markDirty();
    }

    private void clearOptions() {
        statOptions = new String[3];
        tierOptions = new int[3];
        optionsGenerated = false;
        markDirty();
    }

    public boolean areOptionsGenerated() {
        return optionsGenerated;
    }

    public String getStatOption(int index) {
        if (index < 0 || index >= 3) return null;
        return statOptions[index];
    }

    public int getTierOption(int index) {
        if (index < 0 || index >= 3) return 0;
        return tierOptions[index];
    }

    public boolean selectOption(int optionIndex) {
        if (!optionsGenerated) return false;
        if (optionIndex < 0 || optionIndex >= 3) return false;

        ItemStack equipment = getStack(EQUIPMENT_SLOT);
        if (equipment.isEmpty()) return false;
        if (!ItemStatManager.hasCrypticStat(equipment)) return false;

        String statId = statOptions[optionIndex];
        int tier = tierOptions[optionIndex];

        if (statId == null) return false;

        ItemStack result = equipment.copy();

        // Remove stored cryptic options before revealing
        ItemStatManager.removeCrypticOptions(result);

        boolean success = ItemStatManager.revealCrypticStat(result, statId, tier);

        if (success) {
            setStack(EQUIPMENT_SLOT, ItemStack.EMPTY);
            setStack(RESULT_SLOT, result);
            clearOptions();
            syncClearToOpenPlayers();
            markDirty();
            return true;
        }

        return false;
    }

    public void takeResult(PlayerEntity player) {
        ItemStack result = getStack(RESULT_SLOT);
        if (!result.isEmpty()) {
            player.getInventory().offerOrDrop(result);
            setStack(RESULT_SLOT, ItemStack.EMPTY);
            markDirty();
        }
    }

    public void syncOptionsToPlayer(ServerPlayerEntity player, int syncId) {
        if (optionsGenerated) {
            ServerPlayNetworking.send(player,
                    new StatOptionsSyncPacket(
                            syncId,
                            statOptions[0] != null ? statOptions[0] : "",
                            statOptions[1] != null ? statOptions[1] : "",
                            statOptions[2] != null ? statOptions[2] : "",
                            tierOptions[0], tierOptions[1], tierOptions[2]
                    ));
        } else {
            // Send empty/clear packet
            ServerPlayNetworking.send(player,
                    new StatOptionsSyncPacket(syncId, "", "", "", 0, 0, 0));
        }
    }

    private void syncToOpenPlayers() {
        if (world != null && !world.isClient()) {
            for (var player : world.getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    if (serverPlayer.currentScreenHandler instanceof ArtificialEnhancementAltarScreenHandler handler) {
                        syncOptionsToPlayer(serverPlayer, handler.syncId);
                    }
                }
            }
        }
    }

    private void syncClearToOpenPlayers() {
        if (world != null && !world.isClient()) {
            for (var player : world.getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    if (serverPlayer.currentScreenHandler instanceof ArtificialEnhancementAltarScreenHandler handler) {
                        ServerPlayNetworking.send(serverPlayer,
                                new StatOptionsSyncPacket(handler.syncId, "", "", "", 0, 0, 0));
                    }
                }
            }
        }
    }
}