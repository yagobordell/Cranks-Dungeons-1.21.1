package com.cranks.dungeons.inventory;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class CartographyRecipes {
    private CartographyRecipes() {}
    public static List<RecipeEntry<?>> route(List<RecipeEntry<?>> entries, RegistryWrapper.WrapperLookup registries) {
        var result = new ArrayList<RecipeEntry<?>>();
        var ids = new HashSet<Identifier>();
        for (var entry : entries) {
            if (entry.value() instanceof MapCloningRecipe || entry.value() instanceof MapExtendingRecipe) continue;
            if ((entry.value() instanceof ShapedRecipe || entry.value() instanceof ShapelessRecipe)
                    && entry.value().getResult(registries).isOf(Items.MAP)) {
                entry = new RecipeEntry<>(entry.id(), new StationAssemblyRecipe(StationAssemblyRecipe.Station.CARTOGRAPHY_TABLE,
                        (CraftingRecipe) entry.value()));
            }
            result.add(entry);
            ids.add(entry.id());
        }
        for (var operation : CartographyRecipe.Operation.values()) {
            var id = Identifier.of(CranksDungeons.MOD_ID, "cartography/" + operation.asString());
            if (ids.add(id)) result.add(new RecipeEntry<>(id, new StationAssemblyRecipe(
                    StationAssemblyRecipe.Station.CARTOGRAPHY_TABLE, new CartographyRecipe(operation))));
        }
        return result;
    }
}
