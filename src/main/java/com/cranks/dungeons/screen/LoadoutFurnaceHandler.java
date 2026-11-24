package com.cranks.dungeons.screen;

import com.cranks.dungeons.inventory.GearSalvage;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static net.minecraft.block.entity.AbstractFurnaceBlockEntity.BURN_TIME_PROPERTY_INDEX;
import static net.minecraft.block.entity.AbstractFurnaceBlockEntity.COOK_TIME_PROPERTY_INDEX;
import static net.minecraft.block.entity.AbstractFurnaceBlockEntity.COOK_TIME_TOTAL_PROPERTY_INDEX;
import static net.minecraft.block.entity.AbstractFurnaceBlockEntity.FUEL_TIME_PROPERTY_INDEX;
import static net.minecraft.block.entity.AbstractFurnaceBlockEntity.PROPERTY_COUNT;

public final class LoadoutFurnaceHandler extends LoadoutStationHandler {
    public static final int INPUT = PLAYER_SLOT_END;
    public static final int FUEL = INPUT + 1;
    public static final int OUTPUT = INPUT + 2;

    public enum Station {
        FURNACE(Blocks.FURNACE, RecipeType.SMELTING),
        BLAST_FURNACE(Blocks.BLAST_FURNACE, RecipeType.BLASTING),
        SMOKER(Blocks.SMOKER, RecipeType.SMOKING);

        private final Block block;
        private final RecipeType<? extends AbstractCookingRecipe> recipeType;

        Station(Block block, RecipeType<? extends AbstractCookingRecipe> recipeType) {
            this.block = block;
            this.recipeType = recipeType;
        }

        public Block block() {
            return block;
        }

        private ScreenHandlerType<LoadoutFurnaceHandler> handlerType() {
            return switch (this) {
                case FURNACE -> ModScreenHandlers.LOADOUT_FURNACE;
                case BLAST_FURNACE -> ModScreenHandlers.LOADOUT_BLAST_FURNACE;
                case SMOKER -> ModScreenHandlers.LOADOUT_SMOKER;
            };
        }
    }

    private final Inventory furnace;
    private final PropertyDelegate properties;
    private final World world;
    private final Station station;

    public LoadoutFurnaceHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, Station.FURNACE);
    }

    public LoadoutFurnaceHandler(int syncId, PlayerInventory inventory, Station station) {
        this(syncId, inventory, new SimpleInventory(3), new ArrayPropertyDelegate(PROPERTY_COUNT), station);
    }

    public LoadoutFurnaceHandler(int syncId, PlayerInventory inventory, Inventory furnace,
                                 PropertyDelegate properties, Station station) {
        super(station.handlerType(), syncId, inventory);
        checkSize(furnace, 3);
        checkDataCount(properties, PROPERTY_COUNT);
        this.furnace = furnace;
        this.properties = properties;
        this.world = inventory.player.getWorld();
        this.station = station;
        addSlot(new Slot(furnace, 0, 0, 0) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isSmeltable(stack) && !(isBlastFurnace() && GearSalvage.supports(stack, true)
                        && GearSalvage.needsConfirmation(stack));
            }
        });
        addSlot(new Slot(furnace, 1, 0, 0) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return AbstractFurnaceBlockEntity.canUseAsFuel(stack) || stack.isOf(Items.BUCKET);
            }

            @Override
            public int getMaxItemCount(ItemStack stack) {
                return stack.isOf(Items.BUCKET) ? 1 : super.getMaxItemCount(stack);
            }
        });
        addSlot(new FurnaceOutputSlot(inventory.player, furnace, 2, 0, 0));
        addProperties(properties);
    }

    private boolean isSmeltable(ItemStack stack) {
        return cookingRecipe(stack) != null;
    }

    private RecipeEntry<?> cookingRecipe(ItemStack stack) {
        return world.getRecipeManager().getFirstMatch(recipeType(), new SingleStackRecipeInput(stack), world)
                .filter(entry -> !GearSalvage.furnaceOutput(entry, stack, world.getRegistryManager()).isEmpty())
                .orElse(null);
    }

    public boolean canLoadSalvage(ItemStack stack) {
        return isBlastFurnace() && stack.getCount() == 1 && getSlot(INPUT).getStack().isEmpty()
                && GearSalvage.isBlastRecipe(cookingRecipe(stack));
    }

    public void loadSalvage(ItemStack stack) {
        // The request validates the exact source stack and input space before transferring it.
        getSlot(INPUT).setStack(stack);
    }

    public Station station() { return station; }

    public boolean isBlastFurnace() { return station == Station.BLAST_FURNACE; }

    public RecipeType<? extends AbstractCookingRecipe> recipeType() {
        return station.recipeType;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return furnace.canPlayerUse(player);
    }

    public boolean burning() {
        return properties.get(BURN_TIME_PROPERTY_INDEX) > 0;
    }

    public float cookProgress() {
        return progress(COOK_TIME_PROPERTY_INDEX, COOK_TIME_TOTAL_PROPERTY_INDEX);
    }

    public float fuelProgress() {
        return progress(BURN_TIME_PROPERTY_INDEX, FUEL_TIME_PROPERTY_INDEX);
    }

    private float progress(int value, int total) {
        return MathHelper.clamp((float) properties.get(value) / Math.max(1, properties.get(total)), 0, 1);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        if (index < 5 || index > OUTPUT) return ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (!slot.hasStack() || !slot.canTakeItems(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index >= INPUT) {
            insertItem(stack, 9, 36, false);
            insertItem(stack, PlayerLoadout.handlerSlot(PlayerLoadout.BAG_STORAGE),
                    PlayerLoadout.handlerSlot(PlayerLoadout.EQUIPPED_BAGS), false);
            insertItem(stack, 36, 45, false);
            insertItem(stack, PlayerLoadout.FIRST_HANDLER_SLOT, PlayerLoadout.FIRST_HANDLER_SLOT + 1, false);
            if (index == OUTPUT) slot.onQuickTransfer(stack, original);
        } else {
            int target = isSmeltable(stack) ? INPUT : FUEL;
            if (canLoadSalvage(stack) && GearSalvage.needsConfirmation(stack)) return ItemStack.EMPTY;
            if (!getSlot(target).canInsert(stack) || !insertItem(stack, target, target + 1, false)) {
                return super.quickMove(player, index);
            }
        }
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        slot.onTakeItem(player, stack);
        return original;
    }
}
