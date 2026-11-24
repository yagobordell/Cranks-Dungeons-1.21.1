package com.cranks.dungeons.inventory;

import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.registry.RegistryWrapper;

public final class FletchingRecipes {
    private FletchingRecipes() {}
    /** Reassign all ordinary ranged recipes and retire automatic potion-to-arrow recipes. */
    public static RecipeEntry<?> assign(RecipeEntry<?> entry, RegistryWrapper.WrapperLookup registries) {
        if (entry.value() instanceof TippedArrowRecipe) return null;
        if (!(entry.value() instanceof ShapedRecipe || entry.value() instanceof ShapelessRecipe)) return entry;
        var recipe = (CraftingRecipe) entry.value();
        var item = recipe.getResult(registries).getItem();
        if (item == Items.SPECTRAL_ARROW || item == Items.TIPPED_ARROW) return null;
        return item == Items.BOW || item == Items.CROSSBOW || item == Items.ARROW
                ? new RecipeEntry<>(entry.id(), new StationAssemblyRecipe(StationAssemblyRecipe.Station.FLETCHING_TABLE, recipe)) : entry;
    }
}
