package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.item.ArtificialTomeItem;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.item.ElementalTomeItem;
import com.cranks.dungeons.item.HealthBonusTomeItem;
import com.cranks.dungeons.item.RunicTomeItem;
import com.cranks.dungeons.item.SwiftnessTomeItem;
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
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
        return Text.literal("Runic Altar");
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

    public boolean tryEnhance() {
        ItemStack equipment = getStack(EQUIPMENT_SLOT);
        ItemStack tome = getStack(TOME_SLOT);

        if (equipment.isEmpty() || tome.isEmpty())
            return false;

        if (!getStack(RESULT_SLOT).isEmpty())
            return false;

        // Handle Artificial Tomes
        if (tome.getItem() instanceof ArtificialTomeItem) {
            return tryArtificialEnhance(equipment, tome);
        }

        // Handle Abstract Tomes (Specific stats)
        if (tome.getItem() instanceof AbstractTomeItem abstractTome) {
            return trySpecificTomeEnhance(equipment, tome, abstractTome);
        }

        // Handle Regular Runic Tomes (Currency)
        if (tome.getItem() instanceof RunicTomeItem runicTome) {
            return tryRunicEnhance(equipment, tome, runicTome);
        }

        return false;
    }

    private boolean trySpecificTomeEnhance(ItemStack equipment, ItemStack tome, AbstractTomeItem abstractTome) {
        EquipmentType equipType = EquipmentType.getTypeForItem(equipment).orElse(null);
        if (equipType == null)
            return false;
            
        String statId = abstractTome.getStatIdForEquipment(equipType);
        if (statId == null)
            return false;

        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null)
            return false;

        // Specialized tomes upgrade Magic to Rare
        if (CustomRarity.getRarity(equipment) != CustomRarity.MAGIC)
            return false;

        ItemStack result = equipment.copy();
        ItemStatManager.setRarity(result, CustomRarity.RARE);

        // Add guaranteed stat (decent tier T1-T3)
        int tier = RandomUtil.nextInt(3) + 1;
        if (abstractTome instanceof ElementalTomeItem et && et.getTier() == ElementalTomeItem.Tier.NORMAL) {
            tier = 4;
        } else if (abstractTome instanceof HealthBonusTomeItem || abstractTome instanceof SwiftnessTomeItem) {
            tier = 4;
        }
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
        // Artificial tomes can only be used on a rare item
        if (CustomRarity.getRarity(equipment) != CustomRarity.RARE)
            return false;

        if (ItemStatManager.hasArtificialStat(equipment))
            return false;
        if (ItemStatManager.hasCrypticStat(equipment))
            return false;

        ItemStack result = equipment.copy();

        // Randomly Prefix or Suffix
        CustomStat.AffixType type = RandomUtil.nextBoolean() ? CustomStat.AffixType.PREFIX
                : CustomStat.AffixType.SUFFIX;

        // If it selects one that already has the max limit of, it replaces one of them
        // randomly
        if (!ItemStatManager.canAddStat(result, type)) {
            ItemStatManager.removeRandomAffixOfType(result, type);
        }

        boolean success = ItemStatManager.addCrypticStat(result, type);

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

    private boolean tryRunicEnhance(ItemStack equipment, ItemStack tome, RunicTomeItem runicTome) {
        CustomRarity rarity = CustomRarity.getRarity(equipment);
        ItemStack result = equipment.copy();
        boolean success = false;
        int minLevel = runicTome.getMinStatLevel();

        switch (runicTome.getTomeType()) {
            case TRANSMUTATION, GREATER_TRANSMUTATION, PERFECT_TRANSMUTATION:
                if (rarity == CustomRarity.NORMAL) {
                    ItemStatManager.setRarity(result, CustomRarity.MAGIC);
                    success = addRandomAffix(result, minLevel);
                }
                break;
            case AUGMENTATION, GREATER_AUGMENTATION, PERFECT_AUGMENTATION:
                if (rarity == CustomRarity.MAGIC) {
                    success = addRandomAffix(result, minLevel);
                }
                break;
            case REGAL, GREATER_REGAL, PERFECT_REGAL:
                if (rarity == CustomRarity.MAGIC) {
                    ItemStatManager.setRarity(result, CustomRarity.RARE);
                    success = addRandomAffix(result, minLevel);
                }
                break;
            case ALCHEMY:
                if (rarity == CustomRarity.NORMAL || rarity == CustomRarity.MAGIC) {
                    ItemStatManager.clearStats(result);
                    ItemStatManager.setRarity(result, CustomRarity.RARE);
                    // Add 4 mods: 2 prefix, 2 suffix
                    boolean s1 = addAffixOfType(result, CustomStat.AffixType.PREFIX, minLevel);
                    boolean s2 = addAffixOfType(result, CustomStat.AffixType.PREFIX, minLevel);
                    boolean s3 = addAffixOfType(result, CustomStat.AffixType.SUFFIX, minLevel);
                    boolean s4 = addAffixOfType(result, CustomStat.AffixType.SUFFIX, minLevel);
                    success = s1 || s2 || s3 || s4;
                }
                break;
            case EXALTED, GREATER_EXALTED, PERFECT_EXALTED:
                if (rarity == CustomRarity.RARE) {
                    success = addRandomAffix(result, minLevel);
                }
                break;
        }

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

    private boolean addRandomAffix(ItemStack stack, int minLevel) {
        CustomStat.AffixType type = RandomUtil.nextBoolean() ? CustomStat.AffixType.PREFIX : CustomStat.AffixType.SUFFIX;
        boolean success = ItemStatManager.addRandomStat(stack, type, minLevel, 100);
        
        // If first choice failed (maybe slot full), try the other type
        if (!success) {
            type = (type == CustomStat.AffixType.PREFIX) ? CustomStat.AffixType.SUFFIX : CustomStat.AffixType.PREFIX;
            success = ItemStatManager.addRandomStat(stack, type, minLevel, 100);
        }
        return success;
    }

    private boolean addAffixOfType(ItemStack stack, CustomStat.AffixType type, int minLevel) {
        for (int i = 0; i < 50; i++) { // Try up to 50 times to find a unique stat
            if (ItemStatManager.addRandomStat(stack, type, minLevel, 100)) return true;
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
            this.world.playSound(null, this.pos, ModSounds.RUNIC_ALTAR_CLICK, SoundCategory.BLOCKS, 0.6F, 1.0F);
        }
    }
}