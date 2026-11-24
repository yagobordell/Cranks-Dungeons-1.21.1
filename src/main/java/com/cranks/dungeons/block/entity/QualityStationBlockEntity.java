package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.item.DeepslateSigilItem;
import com.cranks.dungeons.item.QualityMaterialItem;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import com.cranks.dungeons.util.RandomUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class QualityStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public static final int EQUIPMENT_SLOT = 0;
    public static final int MATERIAL_SLOT = 1;
    public static final int QUALITY_CAP = 20;
    public static final int BONUS_CHANCE = 33;
    public static final int PROGRESS_PROPERTY = 0;
    public static final int REMAINING_PROPERTY = 1;
    public static final int COMPLETED_PROPERTY = 2;
    public static final int JOB_PROPERTY = 3;
    public static final int PROPERTY_COUNT = 4;

    private int processTick = 0;
    public static final int PROCESS_TIME = 600; // 30 seconds for quality
    public static final int SIGIL_PROCESS_TIME = 1200; // 60 seconds for sigils (double quality time)
    private int remainingUpgrades;
    private int jobId;
    @Nullable private UUID startedBy;
    private ItemStack processingEquipment = ItemStack.EMPTY;
    private ItemStack processingMaterial = ItemStack.EMPTY;
    private ItemStack completedEquipment = ItemStack.EMPTY;

    protected final PropertyDelegate propertyDelegate;

    public QualityStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUALITY_STATION_BE, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case PROGRESS_PROPERTY -> processTick;
                    case REMAINING_PROPERTY -> remainingUpgrades;
                    case COMPLETED_PROPERTY -> hasCompletedEquipment() ? 1 : 0;
                    case JOB_PROPERTY -> jobId;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {}

            @Override
            public int size() {
                return PROPERTY_COUNT;
            }
        };
    }

    @Override
    public Text getDisplayName() {
        return getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new QualityStationScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
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
        ItemStack removed = Inventories.removeStack(inventory, slot);
        if (!removed.isEmpty()) markDirty();
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        stack.capCount(slot == EQUIPMENT_SLOT ? 1 : getMaxCount(stack));
        if (slot == EQUIPMENT_SLOT && !stack.isEmpty() && !hasCompletedEquipment()) completedEquipment = ItemStack.EMPTY;
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return world != null && world.getBlockEntity(pos) == this
                && player.getBlockPos().isWithinDistance(this.pos, 8.0);
    }

    @Override
    public void clear() {
        inventory.clear();
        endJob();
        completedEquipment = ItemStack.EMPTY;
        markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("processTick", processTick);
        nbt.putInt("remainingUpgrades", remainingUpgrades);
        nbt.putInt("jobId", jobId);
        nbt.putBoolean("completed", hasCompletedEquipment());
        if (remainingUpgrades > 0) {
            if (startedBy != null) nbt.putUuid("startedBy", startedBy);
            nbt.put("processEquipment", processingEquipment.encode(registryLookup));
            nbt.put("processMaterial", processingMaterial.encode(registryLookup));
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        var equipment = getStack(EQUIPMENT_SLOT);
        var material = getStack(MATERIAL_SLOT);
        int cycles = nbt.contains("remainingUpgrades") ? nbt.getInt("remainingUpgrades") : nbt.getBoolean("isProcessing") ? 1 : 0;
        jobId = Math.clamp(nbt.getInt("jobId"), 0, 30_000);
        completedEquipment = nbt.getBoolean("completed") ? equipment.copy() : ItemStack.EMPTY;
        processingEquipment = ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("processEquipment"));
        processingMaterial = ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("processMaterial"));
        remainingUpgrades = sameInputs(equipment, material) ? Math.clamp(cycles, 0, upgradeLimit(equipment, material)) : 0;
        startedBy = remainingUpgrades > 0 && nbt.containsUuid("startedBy") ? nbt.getUuid("startedBy") : null;
        processTick = remainingUpgrades > 0 ? Math.clamp(nbt.getInt("processTick"), 0, ticksPerUpgrade(material) - 1) : 0;
        if (remainingUpgrades > 0 && jobId == 0) jobId = 1;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{EQUIPMENT_SLOT, MATERIAL_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return (slot != EQUIPMENT_SLOT || getStack(slot).isEmpty()) && isValid(slot, stack);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return remainingUpgrades == 0 && accepts(slot, stack, getStack(EQUIPMENT_SLOT), getStack(MATERIAL_SLOT));
    }

    public static boolean accepts(int slot, ItemStack stack, ItemStack equipment, ItemStack material) {
        if (slot == EQUIPMENT_SLOT) {
            if (!material.isEmpty()) return upgradeLimit(stack, material) > 0;
            return upgradeMaterial(stack) != null && getQuality(stack) < QUALITY_CAP
                    || EnchantmentSlotHelper.canAddSlot(stack);
        }
        if (slot != MATERIAL_SLOT) return false;
        if (equipment.isEmpty()) return stack.getItem() instanceof QualityMaterialItem || isSigil(stack);
        return upgradeLimit(equipment, stack) > 0;
    }

    public static boolean isSigil(ItemStack material) {
        return material.getItem() instanceof DeepslateSigilItem;
    }

    public static int ticksPerUpgrade(ItemStack material) {
        return isSigil(material) ? SIGIL_PROCESS_TIME : PROCESS_TIME;
    }

    public static int upgradeLimit(ItemStack equipment, ItemStack material) {
        if (equipment.isEmpty() || material.isEmpty()) return 0;
        int room;
        if (isSigil(material)) {
            room = EnchantmentSlotHelper.getMaxEnchantmentSlots(equipment) - EnchantmentSlotHelper.getEnchantmentSlots(equipment);
        } else if (material.getItem() instanceof QualityMaterialItem quality
                && quality.getMaterialType() == upgradeMaterial(equipment)) {
            room = QUALITY_CAP - getQuality(equipment);
        } else {
            return 0;
        }
        return Math.clamp(room, 0, material.getCount());
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == EQUIPMENT_SLOT && remainingUpgrades == 0 && hasCompletedEquipment();
    }

    private boolean hasCompletedEquipment() {
        return !completedEquipment.isEmpty() && ItemStack.areEqual(getStack(EQUIPMENT_SLOT), completedEquipment);
    }

    public boolean start(UUID player, int amount, ItemStack expectedGear, ItemStack expectedMaterial) {
        var equipment = getStack(EQUIPMENT_SLOT);
        var material = getStack(MATERIAL_SLOT);
        if (remainingUpgrades != 0 || equipment.getCount() != 1 || amount < 1 || amount > upgradeLimit(equipment, material)
                || !ItemStack.areEqual(equipment, expectedGear) || !ItemStack.areEqual(material, expectedMaterial)) return false;
        remainingUpgrades = amount;
        startedBy = player;
        jobId = jobId % 30_000 + 1;
        processTick = 0;
        completedEquipment = ItemStack.EMPTY;
        rememberInputs(equipment, material);
        markDirty();
        return true;
    }

    public boolean canCancel(UUID player) {
        // Jobs saved before ownership was recorded can still be cancelled.
        return remainingUpgrades > 0 && (startedBy == null || startedBy.equals(player));
    }

    public boolean cancel(UUID player, int expectedJob) {
        if (jobId != expectedJob || !canCancel(player)) return false;
        endJob();
        markDirty();
        return true;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) return;
        ItemStack equipment = getStack(EQUIPMENT_SLOT);
        ItemStack material = getStack(MATERIAL_SLOT);
        if (!completedEquipment.isEmpty() && !hasCompletedEquipment()) {
            completedEquipment = ItemStack.EMPTY;
            markDirty();
        }
        if (remainingUpgrades == 0) return;
        if (!sameInputs(equipment, material) || upgradeLimit(equipment, material) == 0) {
            endJob();
            markDirty();
            return;
        }

        boolean sigil = isSigil(material);
        processTick++;
        if (processTick % 40 == 1) {
            world.playSound(null, pos, ModSounds.QUALITY_STATION_2, SoundCategory.BLOCKS, 0.4f, 1.0f);
            world.playSound(null, pos, sigil ? ModSounds.QUALITY_STATION_1 : ModSounds.QUALITY_STATION_3,
                    SoundCategory.BLOCKS, 0.4f, 1.0f);
        }
        if (processTick < ticksPerUpgrade(material)) {
            if (processTick % 20 == 0) markDirty();
            return;
        }
        if (sigil) {
            EnchantmentSlotHelper.setEnchantmentSlots(equipment, EnchantmentSlotHelper.getEnchantmentSlots(equipment) + 1);
        } else {
            int increase = RandomUtil.nextInt(100) < BONUS_CHANCE ? 2 : 1;
            setQuality(equipment, Math.min(QUALITY_CAP, getQuality(equipment) + increase));
            EquipmentStatApplier.updateItemAttributes(equipment);
        }
        material.decrement(1);
        remainingUpgrades--;
        if (remainingUpgrades == 0 || upgradeLimit(equipment, material) == 0) {
            completedEquipment = equipment.copy();
            endJob();
        } else {
            processTick = 0;
            rememberInputs(equipment, material);
        }
        markDirty();
    }

    private void rememberInputs(ItemStack equipment, ItemStack material) {
        processingEquipment = equipment.copy();
        processingMaterial = material.copyWithCount(1);
    }

    private void endJob() {
        remainingUpgrades = 0;
        startedBy = null;
        processTick = 0;
        processingEquipment = ItemStack.EMPTY;
        processingMaterial = ItemStack.EMPTY;
        stopSounds();
    }

    private boolean sameInputs(ItemStack equipment, ItemStack material) {
        return !processingEquipment.isEmpty() && !processingMaterial.isEmpty()
                && ItemStack.areItemsAndComponentsEqual(equipment, processingEquipment)
                && ItemStack.areItemsAndComponentsEqual(material, processingMaterial);
    }

    public void stopSounds() {
        if (this.world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity p : serverWorld.getPlayers()) {
                if (p.squaredDistanceTo(pos.toCenterPos()) < 144.0) {
                    p.networkHandler.sendPacket(new StopSoundS2CPacket(
                        Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "quality_station_1"),
                        SoundCategory.BLOCKS
                    ));
                    p.networkHandler.sendPacket(new StopSoundS2CPacket(
                        Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "quality_station_2"),
                        SoundCategory.BLOCKS
                    ));
                    p.networkHandler.sendPacket(new StopSoundS2CPacket(
                        Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "quality_station_3"),
                        SoundCategory.BLOCKS
                    ));
                }
            }
        }
    }

    @Override
    public void markRemoved() {
        stopSounds();
        super.markRemoved();
    }

    @Nullable
    public static QualityMaterialItem.Type upgradeMaterial(ItemStack equipment) {
        var type = EquipmentType.getTypeForItem(equipment).orElse(null);
        if (type == null) return null;
        return switch (type) {
            case SWORD, SPEAR, MACE, TRIDENT, BOW, CROSSBOW -> QualityMaterialItem.Type.WEAPON;
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS, SHIELD, ELYTRA -> QualityMaterialItem.Type.ARMOR;
            default -> null;
        };
    }

    public static int getQuality(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (nbt.contains("Quality")) {
            return nbt.getInt("Quality");
        }
        return 0;
    }

    private void setQuality(ItemStack stack, int quality) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putInt("Quality", quality);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}
