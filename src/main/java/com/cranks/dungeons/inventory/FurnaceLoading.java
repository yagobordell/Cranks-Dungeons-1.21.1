package com.cranks.dungeons.inventory;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.function.Predicate;

/** Plans inventory transfers into the real furnace input slot. */
public final class FurnaceLoading {
    private FurnaceLoading() {}

    public static int available(MaterialCrafting.Storage storage, AbstractCookingRecipe recipe, World world) {
        int count = 0;
        for (int i = 0; i < storage.size(); i++) {
            if (matches(recipe, storage.stack(i), world)) count += storage.stack(i).getCount();
        }
        return count;
    }

    public static int limit(MaterialCrafting.Storage storage, AbstractCookingRecipe recipe, Slot input, World world) {
        ItemStack loaded = input.getStack();
        if (!loaded.isEmpty()) {
            return matches(recipe, loaded, world) ? roomFor(storage, input, loaded) : 0;
        }
        int best = 0;
        for (int i = 0; i < storage.size(); i++) {
            ItemStack candidate = storage.stack(i);
            if (matches(recipe, candidate, world)) best = Math.max(best, roomFor(storage, input, candidate));
        }
        return best;
    }

    private static boolean matches(AbstractCookingRecipe recipe, ItemStack stack, World world) {
        return !stack.isEmpty() && recipe.matches(new SingleStackRecipeInput(stack), world);
    }

    private static int roomFor(MaterialCrafting.Storage storage, Slot input, ItemStack variant) {
        int room = input.getMaxItemCount(variant) - input.getStack().getCount();
        int available = 0;
        for (int i = 0; i < storage.size() && available < room; i++) {
            ItemStack stack = storage.stack(i);
            if (ItemStack.areItemsAndComponentsEqual(variant, stack)) available += stack.getCount();
        }
        return Math.max(0, Math.min(room, available));
    }

    public static ItemStack chooseInput(MaterialCrafting.Storage storage, LoadoutFurnaceHandler furnace,
                                        RecipeEntry<?> entry, World world, int quantity) {
        if (!(entry.value() instanceof AbstractCookingRecipe recipe) || quantity < 1) return ItemStack.EMPTY;
        Slot input = furnace.getSlot(LoadoutFurnaceHandler.INPUT);
        Predicate<ItemStack> fits = stack -> matches(recipe, stack, world)
                && roomFor(storage, input, stack) >= quantity
                && world.getRecipeManager().getFirstMatch(furnace.recipeType(), new SingleStackRecipeInput(stack), world)
                        .filter(match -> match.id().equals(entry.id())).isPresent();
        ItemStack loaded = input.getStack();
        if (!loaded.isEmpty()) return fits.test(loaded) ? loaded : ItemStack.EMPTY;
        for (int i = 0; i < storage.size(); i++) {
            ItemStack candidate = storage.stack(i);
            if (fits.test(candidate)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    public static int load(ServerPlayerEntity player, LoadoutFurnaceHandler furnace,
                           RecipeEntry<?> entry, int quantity) {
        if (quantity < 1 || entry == null || GearSalvage.isBlastRecipe(entry) || !(entry.value() instanceof AbstractCookingRecipe recipe)
                || recipe.getType() != furnace.recipeType()) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        var world = player.getServerWorld();
        if (!recipe.getResult(world.getRegistryManager()).isItemEnabled(world.getEnabledFeatures())) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        var discoveries = ModComponents.ITEM_DISCOVERIES.get(player);
        discoveries.recordInventory();
        if (!RecipeDiscovery.isDiscovered(recipe, discoveries)) return CraftMaterialsResultPayload.LOCKED;

        PlayerInventory inventory = player.getInventory();
        var storage = new MaterialCrafting.Storage(inventory);
        Slot input = furnace.getSlot(LoadoutFurnaceHandler.INPUT);
        ItemStack loaded = input.getStack();
        ItemStack chosen = chooseInput(storage, furnace, entry, world, quantity);
        if (chosen.isEmpty()) return CraftMaterialsResultPayload.INVENTORY_CHANGED;

        int remaining = quantity;
        for (int i = 0; i < storage.size() && remaining > 0; i++) {
            ItemStack stack = storage.stack(i);
            if (!ItemStack.areItemsAndComponentsEqual(chosen, stack)) continue;
            int used = Math.min(remaining, stack.getCount());
            inventory.setStack(storage.slot(i), stack.copyWithCount(stack.getCount() - used));
            remaining -= used;
        }
        input.setStack(chosen.copyWithCount(loaded.getCount() + quantity));
        inventory.markDirty();
        furnace.sendContentUpdates();
        return quantity;
    }
}
