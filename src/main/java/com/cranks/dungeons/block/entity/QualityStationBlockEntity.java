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

public class QualityStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public static final int EQUIPMENT_SLOT = 0;
    public static final int MATERIAL_SLOT = 1;

    private int processTick = 0;
    public static final int PROCESS_TIME = 600; // 30 seconds for quality
    public static final int SIGIL_PROCESS_TIME = 1200; // 60 seconds for sigils (double quality time)
    private boolean isProcessing = false;

    protected final PropertyDelegate propertyDelegate;

    public QualityStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUALITY_STATION_BE, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> QualityStationBlockEntity.this.processTick;
                    case 1 -> QualityStationBlockEntity.this.isProcessing ? 1 : 0;
                    case 2 -> QualityStationBlockEntity.this.isSigilMode() ? SIGIL_PROCESS_TIME : PROCESS_TIME;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> QualityStationBlockEntity.this.processTick = value;
                    case 1 -> QualityStationBlockEntity.this.isProcessing = value == 1;
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };
    }

    public boolean isSigilMode() {
        ItemStack material = getStack(MATERIAL_SLOT);
        return !material.isEmpty() && material.getItem() instanceof DeepslateSigilItem;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Quality Station");
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
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("processTick", processTick);
        nbt.putBoolean("isProcessing", isProcessing);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        processTick = nbt.getInt("processTick");
        isProcessing = nbt.getBoolean("isProcessing");
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{EQUIPMENT_SLOT, MATERIAL_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == EQUIPMENT_SLOT) {
            if (EquipmentType.getTypeForItem(stack).isEmpty()) {
                return false;
            }
            ItemStack material = getStack(MATERIAL_SLOT);
            if (!material.isEmpty()) {
                if (material.getItem() instanceof QualityMaterialItem) {
                    return isValidMaterialForEquipment(stack, material) && getQuality(stack) < 20;
                } else if (material.getItem() instanceof DeepslateSigilItem) {
                    return EnchantmentSlotHelper.isValidForSigil(stack) && EnchantmentSlotHelper.canAddSlot(stack);
                }
                return false;
            } else {
                return canIncreaseQuality(stack) || canAddEnchantmentSlot(stack);
            }
        }
        if (slot == MATERIAL_SLOT) {
            ItemStack equipment = getStack(EQUIPMENT_SLOT);
            if (stack.getItem() instanceof QualityMaterialItem) {
                if (!equipment.isEmpty()) {
                    return isValidMaterialForEquipment(equipment, stack) && getQuality(equipment) < 20;
                }
                return true;
            }
            if (stack.getItem() instanceof DeepslateSigilItem) {
                if (!equipment.isEmpty()) {
                    return EnchantmentSlotHelper.isValidForSigil(equipment) && EnchantmentSlotHelper.canAddSlot(equipment);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean canIncreaseQuality(ItemStack equipment) {
        if (equipment.isEmpty()) return false;
        if (getQuality(equipment) >= 20) return false;
        return EquipmentType.getTypeForItem(equipment).map(type -> {
            boolean isWeapon = type == EquipmentType.SWORD || type == EquipmentType.SPEAR || 
                               type == EquipmentType.MACE || type == EquipmentType.TRIDENT || 
                               type == EquipmentType.BOW || type == EquipmentType.CROSSBOW;
                               
            boolean isArmor = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE || 
                              type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS || 
                              type == EquipmentType.SHIELD || type == EquipmentType.ELYTRA;
            return isWeapon || isArmor;
        }).orElse(false);
    }

    public boolean canAddEnchantmentSlot(ItemStack equipment) {
        if (equipment.isEmpty()) return false;
        return EnchantmentSlotHelper.isValidForSigil(equipment) && EnchantmentSlotHelper.canAddSlot(equipment);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) return;

        ItemStack equipment = getStack(EQUIPMENT_SLOT);
        ItemStack material = getStack(MATERIAL_SLOT);

        boolean isSigil = !material.isEmpty() && material.getItem() instanceof DeepslateSigilItem;
        boolean isQuality = !material.isEmpty() && material.getItem() instanceof QualityMaterialItem;

        boolean canProcess = false;
        int currentProcessTime = PROCESS_TIME;

        if (!equipment.isEmpty() && !material.isEmpty()) {
            if (isSigil) {
                canProcess = EnchantmentSlotHelper.isValidForSigil(equipment) 
                          && EnchantmentSlotHelper.canAddSlot(equipment);
                currentProcessTime = SIGIL_PROCESS_TIME;
            } else if (isQuality) {
                canProcess = isValidMaterialForEquipment(equipment, material) 
                          && getQuality(equipment) < 20;
                currentProcessTime = PROCESS_TIME;
            }
        }

        if (canProcess) {
            if (!isProcessing) {
                this.isProcessing = true;
                markDirty();
            }

            processTick++;
            
            // Play looping sounds every 40 ticks (2 seconds) while processing
            if (processTick % 40 == 1) {
                if (isSigil) {
                    world.playSound(null, pos, ModSounds.QUALITY_STATION_1, SoundCategory.BLOCKS, 0.4f, 1.0f);
                    world.playSound(null, pos, ModSounds.QUALITY_STATION_2, SoundCategory.BLOCKS, 0.4f, 1.0f);
                } else {
                    world.playSound(null, pos, ModSounds.QUALITY_STATION_2, SoundCategory.BLOCKS, 0.4f, 1.0f);
                    world.playSound(null, pos, ModSounds.QUALITY_STATION_3, SoundCategory.BLOCKS, 0.4f, 1.0f);
                }
            }

            if (processTick >= currentProcessTime) {
                processTick = 0;
                material.decrement(1);
                
                if (isSigil) {
                    int currentSlots = EnchantmentSlotHelper.getEnchantmentSlots(equipment);
                    EnchantmentSlotHelper.setEnchantmentSlots(equipment, currentSlots + 1);
                } else {
                    int increase = (RandomUtil.nextInt(100) < 33) ? 2 : 1;
                    int newQuality = Math.min(20, getQuality(equipment) + increase);
                    setQuality(equipment, newQuality);
                    EquipmentStatApplier.updateItemAttributes(equipment);
                }
                
                markDirty();
            }
        } else {
            if (isProcessing || processTick > 0) {
                stopSounds();
                markDirty();
            }
        }
    }

    public void stopSounds() {
        this.isProcessing = false;
        this.processTick = 0;
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

    public boolean isValidMaterialForEquipment(ItemStack equipment, ItemStack material) {
        if (!(material.getItem() instanceof QualityMaterialItem qualityItem)) return false;
        
        return EquipmentType.getTypeForItem(equipment).map(type -> {
            boolean isWeapon = type == EquipmentType.SWORD || type == EquipmentType.SPEAR || 
                               type == EquipmentType.MACE || type == EquipmentType.TRIDENT || 
                               type == EquipmentType.BOW || type == EquipmentType.CROSSBOW;
                               
            boolean isArmor = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE || 
                              type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS || 
                              type == EquipmentType.SHIELD || type == EquipmentType.ELYTRA;
                              
            if (qualityItem.getMaterialType() == QualityMaterialItem.Type.WEAPON) return isWeapon;
            if (qualityItem.getMaterialType() == QualityMaterialItem.Type.ARMOR) return isArmor;
            return false;
        }).orElse(false);
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
