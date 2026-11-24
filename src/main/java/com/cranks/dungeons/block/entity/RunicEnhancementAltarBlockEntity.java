package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.item.ArtificialTomeItem;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.item.RunicTomeItem;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.util.RandomUtil;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RunicEnhancementAltarBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    private int revision;
    public int revision() { return revision; }
    @Override public void markDirty() { revision++; super.markDirty(); }

    public static final int EQUIPMENT_SLOT = 0;
    public static final int TOME_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    public RunicEnhancementAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNIC_ALTAR_BE, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.cranks-dungeons.runic_enhancement_altar");
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
        var removed = Inventories.removeStack(inventory, slot);
        if (!removed.isEmpty()) markDirty();
        return removed;
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
        return world != null && world.getBlockEntity(pos) == this
                && player.canInteractWithBlockAt(pos, 4.0);
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("RunicRevision", revision);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        revision = nbt.getInt("RunicRevision");
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] { EQUIPMENT_SLOT, TOME_SLOT, RESULT_SLOT };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == RESULT_SLOT)
            return false;
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

    public static String unavailableReason(ItemStack equipment, ItemStack tome, ItemStack output) {
        return com.cranks.dungeons.inventory.RunicAltarRules.reason(equipment,tome,output);
    }
    public boolean tryEnhance() { return tryEnhance(getStack(TOME_SLOT)); }
    public boolean tryEnhance(ItemStack tome) { return tryEnhance(tome,-1); }
    public boolean tryEnhance(ItemStack tome,int target) {
        if(world==null||world.isClient())return false;
        var gear=getStack(EQUIPMENT_SLOT);
        if(com.cranks.dungeons.inventory.RunicAltarRules.reason(gear,tome,getStack(RESULT_SLOT),target)!=null)return false;
        var candidate=com.cranks.dungeons.inventory.RuneCrafting.roll(gear,tome,target);
        if(candidate.isEmpty())return false;
        if(com.cranks.dungeons.inventory.RuneCrafting.comparison(gear,tome,target)){
            com.cranks.dungeons.inventory.RuneCrafting.stage(gear,candidate);markDirty();
        }else setStack(EQUIPMENT_SLOT,candidate);
        tome.decrement(1);markDirty();
        world.playSound(null,pos,ModSounds.RUNIC_ALTAR_CLICK,SoundCategory.BLOCKS,.6F,1);
        return true;
    }
}
