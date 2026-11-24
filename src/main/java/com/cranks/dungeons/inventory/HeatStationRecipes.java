package com.cranks.dungeons.inventory;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Keep heat recipes at their assigned stations, with shared campfire foods. */
public final class HeatStationRecipes {
    private static final int VANILLA_CAMPFIRE_TIME = 600;
    private static final int CAMPFIRE_TIME = 500;

    private record Cooking(RecipeEntry<?> entry, AbstractCookingRecipe recipe, ItemStack result) {}

    private HeatStationRecipes() {}

    public static List<RecipeEntry<?>> route(Collection<RecipeEntry<?>> entries, RegistryWrapper.WrapperLookup registries) {
        List<RecipeEntry<?>> routed = new ArrayList<>(entries.size());
        List<Cooking> cooking = new ArrayList<>();
        Map<Item, RecipeType<?>> destinations = new HashMap<>();
        Map<Item, RecipeType<?>> firstStation = new HashMap<>();
        Set<Item> sharedProducts = new HashSet<>();
        for (var entry : entries) {
            if (!(entry.value() instanceof AbstractCookingRecipe recipe) || !isHeatStation(recipe.getType())) {
                routed.add(entry);
                continue;
            }
            ItemStack result = recipe.getResult(registries);
            if (result.isOf(Items.NETHERITE_SCRAP)) continue;
            cooking.add(new Cooking(entry, recipe, result));
            var prior = firstStation.putIfAbsent(result.getItem(), recipe.getType());
            if (prior != null && prior != recipe.getType()) sharedProducts.add(result.getItem());
        }
        for (Cooking candidate : cooking) {
            Item item = candidate.result.getItem();
            destinations.merge(item, destination(candidate.recipe.getType(), candidate.result, sharedProducts.contains(item)),
                    (first, second) -> priority(first) <= priority(second) ? first : second);
        }

        Map<Item, List<Cooking>> kept = new HashMap<>();
        for (Cooking candidate : cooking) {
            if (!keepsStation(candidate, destinations.get(candidate.result.getItem()))) continue;
            routed.add(withCampfireTime(candidate));
            kept.computeIfAbsent(candidate.result.getItem(), unused -> new ArrayList<>()).add(candidate);
        }
        for (Cooking candidate : cooking) {
            RecipeType<?> station = destinations.get(candidate.result.getItem());
            if (keepsStation(candidate, station)) continue;
            List<Cooking> sameProduct = kept.computeIfAbsent(candidate.result.getItem(), unused -> new ArrayList<>());
            if (sameProduct.stream().anyMatch(existing -> existing.recipe.getType() == station
                    && sameRecipe(existing, candidate))) continue;
            var moved = new RecipeEntry<>(candidate.entry.id(), move(candidate, station));
            routed.add(moved);
            sameProduct.add(new Cooking(moved, moved.value(), candidate.result));
        }
        return routed;
    }

    private static boolean keepsStation(Cooking candidate, RecipeType<?> destination) {
        RecipeType<?> original = candidate.recipe.getType();
        return original == destination || original == RecipeType.CAMPFIRE_COOKING;
    }

    private static RecipeEntry<?> withCampfireTime(Cooking candidate) {
        var recipe = candidate.recipe;
        if (recipe.getType() != RecipeType.CAMPFIRE_COOKING || recipe.getCookingTime() != VANILLA_CAMPFIRE_TIME
                || !candidate.entry.id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) return candidate.entry;
        return new RecipeEntry<>(candidate.entry.id(), new CampfireCookingRecipe(recipe.getGroup(), recipe.getCategory(),
                recipe.getIngredients().getFirst(), candidate.result.copy(), recipe.getExperience(), CAMPFIRE_TIME));
    }

    private static boolean sameRecipe(Cooking first, Cooking second) {
        return ItemStack.areItemsAndComponentsEqual(first.result, second.result)
                && first.recipe.getIngredients().equals(second.recipe.getIngredients());
    }

    private static boolean isHeatStation(RecipeType<?> type) {
        return type == RecipeType.SMELTING || type == RecipeType.BLASTING
                || type == RecipeType.SMOKING || type == RecipeType.CAMPFIRE_COOKING;
    }

    private static RecipeType<?> destination(RecipeType<?> original, ItemStack result, boolean shared) {
        if (result.contains(DataComponentTypes.FOOD)) {
            // Preserve dedicated cooking recipes authored for a single food station.
            if (!shared && (original == RecipeType.SMOKING || original == RecipeType.CAMPFIRE_COOKING)) return original;
            return RecipeType.SMOKING;
        }
        if (result.isOf(Items.IRON_NUGGET) || result.isOf(Items.GOLD_NUGGET)) {
            return RecipeType.BLASTING;
        }
        return original;
    }

    private static int priority(RecipeType<?> type) {
        if (type == RecipeType.SMELTING) return 0;
        if (type == RecipeType.BLASTING) return 1;
        return type == RecipeType.SMOKING ? 2 : 3;
    }

    private static int baseTime(RecipeType<?> type) {
        if (type == RecipeType.SMELTING) return 200;
        return type == RecipeType.CAMPFIRE_COOKING ? CAMPFIRE_TIME : 100;
    }

    private static AbstractCookingRecipe move(Cooking candidate, RecipeType<?> station) {
        var recipe = candidate.recipe;
        var input = recipe.getIngredients().getFirst();
        var result = candidate.result.copy();
        int time = (int) Math.clamp((long) recipe.getCookingTime() * baseTime(station) / baseTime(recipe.getType()),
                1, Integer.MAX_VALUE);
        if (station == RecipeType.SMELTING) {
            return new SmeltingRecipe(recipe.getGroup(), recipe.getCategory(), input, result, recipe.getExperience(), time);
        }
        if (station == RecipeType.BLASTING) {
            return new BlastingRecipe(recipe.getGroup(), recipe.getCategory(), input, result, recipe.getExperience(), time);
        }
        if (station == RecipeType.SMOKING) {
            return new SmokingRecipe(recipe.getGroup(), recipe.getCategory(), input, result, recipe.getExperience(), time);
        }
        return new CampfireCookingRecipe(recipe.getGroup(), recipe.getCategory(), input, result, recipe.getExperience(), time);
    }
}
