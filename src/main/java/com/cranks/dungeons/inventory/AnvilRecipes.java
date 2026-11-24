package com.cranks.dungeons.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.Set;

public final class AnvilRecipes {
    private static final Set<Item> OTHER_GEAR = Set.of(Items.SHEARS, Items.FLINT_AND_STEEL, Items.SHIELD, Items.MACE);
    private static final Set<String> MATERIALS = Set.of("iron", "golden", "diamond", "netherite");
    private static final Set<String> PIECES = Set.of("sword", "pickaxe", "axe", "shovel", "hoe",
            "helmet", "chestplate", "leggings", "boots", "horse_armor");

    private AnvilRecipes() {}

    public static RecipeEntry<?> assign(RecipeEntry<?> entry, RegistryWrapper.WrapperLookup registries) {
        if (!Identifier.DEFAULT_NAMESPACE.equals(entry.id().getNamespace())
                || !(entry.value() instanceof ShapedRecipe || entry.value() instanceof ShapelessRecipe)) return entry;
        CraftingRecipe recipe = (CraftingRecipe) entry.value();
        Item result = recipe.getResult(registries).getItem();
        Identifier id = Registries.ITEM.getId(result);
        if (!Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) return entry;
        String path = id.getPath();
        int split = path.indexOf('_');
        if (OTHER_GEAR.contains(result) || split >= 0 && MATERIALS.contains(path.substring(0, split))
                && PIECES.contains(path.substring(split + 1))) {
            return new RecipeEntry<>(entry.id(), new StationAssemblyRecipe(StationAssemblyRecipe.Station.ANVIL, recipe));
        }
        return entry;
    }
}
