package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.item.QualityMaterialItem;
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
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
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
    private static final int PROCESS_TIME = 600; // 30 seconds
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
                return 2;
            }
        };
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
            return EquipmentType.getTypeForItem(stack).isPresent();
        }
        if (slot == MATERIAL_SLOT) {
            return stack.getItem() instanceof QualityMaterialItem;
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public void setProcessing(boolean processing) {
        this.isProcessing = processing;
        if (!processing) {
            this.processTick = 0;
            if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                for (net.minecraft.server.network.ServerPlayerEntity p : serverWorld.getPlayers()) {
                    if (p.squaredDistanceTo(pos.toCenterPos()) < 1024.0) {
                        p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
                            net.minecraft.util.Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "quality_station_2"),
                            net.minecraft.sound.SoundCategory.BLOCKS
                        ));
                        p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
                            net.minecraft.util.Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "quality_station_3"),
                            net.minecraft.sound.SoundCategory.BLOCKS
                        ));
                    }
                }
            }
        }
        markDirty();
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) return;

        if (isProcessing) {
            ItemStack equipment = getStack(EQUIPMENT_SLOT);
            ItemStack material = getStack(MATERIAL_SLOT);

            if (equipment.isEmpty() || material.isEmpty() || !isValidMaterialForEquipment(equipment, material)) {
                setProcessing(false);
                return;
            }

            int currentQuality = getQuality(equipment);
            if (currentQuality >= 20) {
                setProcessing(false);
                return;
            }

            processTick++;
            
            // Play looping sounds every 40 ticks (2 seconds) while processing
            if (processTick % 40 == 1) {
                world.playSound(null, pos, com.cranks.dungeons.registry.ModSounds.QUALITY_STATION_2, net.minecraft.sound.SoundCategory.BLOCKS, 0.05f, 1.0f);
                world.playSound(null, pos, com.cranks.dungeons.registry.ModSounds.QUALITY_STATION_3, net.minecraft.sound.SoundCategory.BLOCKS, 0.05f, 1.0f);
            }

            if (processTick >= PROCESS_TIME) {
                processTick = 0;
                material.decrement(1);
                
                int increase = (RandomUtil.nextInt(100) < 33) ? 2 : 1;
                int newQuality = Math.min(20, currentQuality + increase);
                setQuality(equipment, newQuality);
                
                com.cranks.dungeons.equipment.EquipmentStatApplier.updateItemAttributes(equipment);
                markDirty();

                if (material.isEmpty() || newQuality >= 20) {
                    setProcessing(false);
                }
            }
        } else {
            if (processTick > 0) {
                processTick = 0;
                markDirty();
            }
        }
    }

    private boolean isValidMaterialForEquipment(ItemStack equipment, ItemStack material) {
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
