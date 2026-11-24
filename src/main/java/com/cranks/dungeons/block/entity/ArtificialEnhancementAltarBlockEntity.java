package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.inventory.ArtificialRunes;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.util.RandomUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Persistent single-slot altar. Choice IDs, tiers and exact values live on the item. */
public class ArtificialEnhancementAltarBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    public static final int EQUIPMENT_SLOT = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int revision;
    public ArtificialEnhancementAltarBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ARTIFICIAL_ALTAR_BE, pos, state); }
    public int revision() { return revision; }
    @Override public void markDirty() { revision++; super.markDirty(); }
    @Override public Text getDisplayName() { return Text.translatable("block.cranks-dungeons.artificial_enhancement_altar"); }
    @Override public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        prepareOptions(); return new ArtificialEnhancementAltarScreenHandler(id, inventory, this);
    }
    @Override public int size() { return inventory.size(); }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { var result = Inventories.splitStack(inventory, slot, amount); if (!result.isEmpty()) markDirty(); return result; }
    @Override public ItemStack removeStack(int slot) { var result = Inventories.removeStack(inventory, slot); if (!result.isEmpty()) markDirty(); return result; }
    @Override public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCount(stack)) stack.setCount(getMaxCount(stack));
        if (world == null || !world.isClient()) prepareOptions();
        markDirty();
    }
    @Override public boolean canPlayerUse(PlayerEntity player) { return world != null && world.getBlockEntity(pos) == this && player.canInteractWithBlockAt(pos, 4.0); }
    @Override public void clear() { inventory.clear(); markDirty(); }
    @Override public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) { super.writeNbt(nbt, lookup); Inventories.writeNbt(nbt, inventory, lookup); nbt.putInt("ArtificialRevision", revision); }
    @Override public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) { super.readNbt(nbt, lookup); Inventories.readNbt(nbt, inventory, lookup); revision = nbt.getInt("ArtificialRevision"); }
    @Override public int[] getAvailableSlots(Direction side) { return new int[]{0}; }
    @Override public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) { return slot == 0 && ArtificialRunes.reason(stack) == null; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction side) { return slot == 0; }

    public void prepareOptions() {
        var gear = getStack(0);
        if (world != null && world.isClient() || ArtificialRunes.reason(gear) != null) return;
        if (ArtificialRunes.hasSavedChoices(gear)) {
            // Migrate old choices that saved IDs and tiers but not numeric values, once.
            var ids = ItemStatManager.getStoredCrypticStatIds(gear);
            var tiers = ItemStatManager.getStoredCrypticTiers(gear);
            var values = ItemStatManager.getStoredCrypticValues(gear);
            if (ids == null || tiers == null || values == null) return;
            boolean changed = false;
            for (int i = 0; i < 3; i++) {
                var stat = StatRegistry.getStat(ids[i]);
                int capped=com.cranks.dungeons.equipment.RuneTiers.clamp(gear,tiers[i]);
                if ((values[i] == null || capped!=tiers[i]) && ArtificialRunes.compatible(gear, stat) && tiers[i] >= 1 && tiers[i] <= 10) {
                    tiers[i]=capped;
                    values[i] = stat.rollValues(tiers[i]); changed = true;
                }
            }
            if (changed) { ItemStatManager.storeCrypticOptions(gear, ids, tiers, values); markDirty(); }
            return;
        }
        var candidates = new ArrayList<>(StatRegistry.getAllStats().stream().filter(stat -> ArtificialRunes.compatible(gear, stat)).toList());
        String[] ids = new String[3]; int[] tiers = new int[3]; double[][] values = new double[3][];
        for(int i=0;i<3;i++){
            var roll=StatRegistry.rollFamilyAndTier(candidates,com.cranks.dungeons.equipment.RuneTiers.artificial(gear));
            if(roll==null)break;
            ids[i]=roll.stat.getId();tiers[i]=roll.tier;values[i]=roll.stat.rollValues(roll.tier);
            String group=roll.stat.getGroupId();candidates.removeIf(stat->stat.getGroupId().equals(group));
        }
        ItemStatManager.storeCrypticOptions(gear, ids, tiers, values);
        markDirty();
    }
    public boolean areOptionsGenerated() { return !ArtificialRunes.options(getStack(0)).isEmpty(); }
    public String getStatOption(int index) { return ArtificialRunes.options(getStack(0)).stream().filter(o -> o.index() == index).map(o -> o.stat().getId()).findFirst().orElse(null); }
    public int getTierOption(int index) { return ArtificialRunes.options(getStack(0)).stream().filter(o -> o.index() == index).mapToInt(ArtificialRunes.Option::tier).findFirst().orElse(0); }
    public boolean selectOption(int index) {
        if (world != null && world.isClient()) return false;
        var option = ArtificialRunes.options(getStack(0)).stream().filter(o -> o.index() == index).findFirst().orElse(null);
        if (option == null || !ItemStatManager.revealCrypticStat(getStack(0), option.stat().getId(), option.tier(), option.values())) return false;
        ItemStatManager.removeCrypticOptions(getStack(0)); markDirty(); return true;
    }
}
