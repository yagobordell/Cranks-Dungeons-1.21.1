package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.item.ArtificialTomeItem;
import com.cranks.dungeons.item.RunicTomeItem;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class RunicEnhancementAltarBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public static final int EQUIPMENT_SLOT = 0;
    public static final int TOME_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    public RunicEnhancementAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNIC_ALTAR_BE, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Runic Enhancement Altar");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RunicEnhancementAltarScreenHandler(syncId, playerInventory, this);
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
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCount(stack)) {
            stack.setCount(getMaxCount(stack));
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
        return new int[]{EQUIPMENT_SLOT, TOME_SLOT, RESULT_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == RESULT_SLOT) return false;
        if (slot == EQUIPMENT_SLOT) {
            return EquipmentType.getTypeForItem(stack).isPresent();
        }
        if (slot == TOME_SLOT) {
            return stack.getItem() instanceof RunicTomeItem
                    || stack.getItem() instanceof ArtificialTomeItem
                    || stack.getItem() instanceof AbstractTomeItem;
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == RESULT_SLOT;
    }

    public boolean tryEnhance() {
        ItemStack equipment = getStack(EQUIPMENT_SLOT);
        ItemStack tome = getStack(TOME_SLOT);

        if (equipment.isEmpty() || tome.isEmpty()) {
            return false;
        }

        if (EquipmentType.getTypeForItem(equipment).isEmpty()) {
            return false;
        }

        if (!ItemStatManager.canAddStat(equipment)) {
            return false;
        }

        // Handle Artificial Tomes
        if (tome.getItem() instanceof ArtificialTomeItem) {
            return tryArtificialEnhance(equipment, tome);
        }

        // Handle Abstract Tomes (Elemental, Swiftness, Energy, Evasion, Health, Regen, Range)
        if (tome.getItem() instanceof AbstractTomeItem abstractTome) {
            return trySpecificTomeEnhance(equipment, tome, abstractTome::canApplyTo, abstractTome::getStatIdForEquipment);
        }

        // Handle Regular Runic Tomes
        if (tome.getItem() instanceof RunicTomeItem runicTome) {
            return tryRunicEnhance(equipment, tome, runicTome);
        }

        return false;
    }

    private boolean trySpecificTomeEnhance(ItemStack equipment, ItemStack tome,
                                           java.util.function.Predicate<ItemStack> canApply,
                                           java.util.function.Function<EquipmentType, String> getStatId) {
        EquipmentType equipType = EquipmentType.getTypeForItem(equipment).get();

        if (!canApply.test(equipment)) return false;

        String statId = getStatId.apply(equipType);
        if (statId == null) return false;

        int currentStatCount = ItemStatManager.getStats(equipment).size();
        int tier = AbstractTomeItem.getTierForStatCount(currentStatCount);

        ItemStack result = equipment.copy();
        boolean success = ItemStatManager.addSpecificStat(result, statId, tier);

        if (success) {
            setStack(RESULT_SLOT, result);
            equipment.decrement(1);
            tome.decrement(1);
            markDirty();
            playEnhanceSound();
            return true;
        }

        return false;
    }

    private boolean tryArtificialEnhance(ItemStack equipment, ItemStack tome) {
        if (ItemStatManager.hasArtificialStat(equipment)) {
            return false;
        }

        if (ItemStatManager.hasCrypticStat(equipment)) {
            return false;
        }

        ItemStack result = equipment.copy();

        boolean success = ItemStatManager.addCrypticStat(result);

        if (success) {
            setStack(RESULT_SLOT, result);
            equipment.decrement(1);
            tome.decrement(1);
            markDirty();
            playEnhanceSound();   // ← here
            return true;
        }

        return false;
    }


    private boolean tryRunicEnhance(ItemStack equipment, ItemStack tome, RunicTomeItem runicTome) {
        ItemStack result = equipment.copy();

        boolean success = false;
        for (int attempts = 0; attempts < 10 && !success; attempts++) {
            success = ItemStatManager.addRandomStat(result, runicTome.getTier());
        }

        if (success) {
            setStack(RESULT_SLOT, result);
            equipment.decrement(1);
            tome.decrement(1);
            markDirty();
            playEnhanceSound();   // ← here
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

    private void playEnhanceSound() {
        if (this.world != null && !this.world.isClient()) {
            this.world.playSound(
                    null,                    // all nearby players
                    this.pos,                // altar position
                    ModSounds.RUNIC_ALTAR_CLICK,
                    SoundCategory.BLOCKS,
                    0.6F,                    // volume
                    1.0F                     // pitch
            );
        }
    }
}