package com.cranks.dungeons.inventory;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.BannerDuplicateRecipe;
import net.minecraft.recipe.BookCloningRecipe;
import net.minecraft.recipe.CraftingDecoratedPotRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.FireworkStarFadeRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShieldDecorationRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Finds a legal one-craft selection for the availability filter on variable recipes. */
public final class SpecialCraftingMaterials {
    private SpecialCraftingMaterials() {}

    public static boolean isAvailable(MaterialCrafting.Storage storage, RecipeEntry<CraftingRecipe> entry, World world, int grid) {
        var recipe = entry.value();
        if (recipe instanceof CartographyRecipe) {
            for (int i = 0; i < storage.size(); i++) {
                if (storage.stack(i).isOf(Items.FILLED_MAP) && valid(storage, entry, world, grid, List.of(storage.slot(i)))) return true;
            }
            return false;
        }
        var requirements = RecipeDiscovery.specialRequirements(recipe);
        if (requirements.isEmpty()) return false;
        var first = requirements.getFirst();
        if (recipe instanceof BannerDuplicateRecipe) {
            boolean banner = recipe instanceof BannerDuplicateRecipe;
            for (int i = 0; i < storage.size(); i++) {
                ItemStack original = storage.stack(i);
                if (original.isEmpty() || !first.test(original.getItem()) || banner && !hasPattern(original)) continue;
                for (int j = 0; j < storage.size(); j++) {
                    ItemStack other = storage.stack(j);
                    if (!other.isOf(original.getItem()) || i == j && original.getCount() < 2 || banner && hasPattern(other)) continue;
                    if (valid(storage, entry, world, grid, List.of(storage.slot(i), storage.slot(j)))) return true;
                }
            }
            return false;
        }
        if (recipe instanceof MapExtendingRecipe || recipe instanceof TippedArrowRecipe) {
            for (int i = 0; i < storage.size(); i++) {
                if (first.test(storage.stack(i).getItem()) && valid(storage, entry, world, grid, List.of(storage.slot(i)))) return true;
            }
            return false;
        }
        List<Integer> choices = new ArrayList<>();
        if (recipe instanceof CraftingDecoratedPotRecipe) {
            for (int i = 0; i < storage.size() && choices.size() < 4; i++) {
                ItemStack stack = storage.stack(i);
                if (!first.test(stack.getItem())) continue;
                for (int j = 0; j < stack.getCount() && choices.size() < 4; j++) choices.add(storage.slot(i));
            }
            return choices.size() == 4 && valid(storage, entry, world, grid, choices);
        }
        for (var requirement : requirements) {
            int before = choices.size();
            for (int i = 0; i < storage.size(); i++) {
                ItemStack stack = storage.stack(i);
                if (!stack.isEmpty() && requirement.test(stack.getItem()) && usable(recipe, stack)) {
                    choices.add(storage.slot(i));
                    break;
                }
            }
            if (before == choices.size()) return false;
        }
        return valid(storage, entry, world, grid, choices);
    }

    private static boolean usable(CraftingRecipe recipe, ItemStack stack) {
        if (recipe instanceof TailoringPatternRecipe && stack.getItem() instanceof net.minecraft.item.BannerItem) {
            return stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers().size() < 6;
        }
        if (recipe instanceof BookCloningRecipe && stack.isOf(Items.WRITTEN_BOOK)) {
            var book = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            return book != null && book.generation() < WrittenBookContentComponent.UNCOPIABLE_GENERATION;
        }
        if (recipe instanceof ShieldDecorationRecipe && stack.isOf(Items.SHIELD)) return !hasPattern(stack);
        return !(recipe instanceof FireworkStarFadeRecipe) || !stack.isOf(Items.FIREWORK_STAR)
                || stack.contains(DataComponentTypes.FIREWORK_EXPLOSION);
    }

    private static boolean hasPattern(ItemStack stack) {
        return !stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers().isEmpty();
    }

    private static boolean valid(MaterialCrafting.Storage storage, RecipeEntry<CraftingRecipe> entry,
                                 World world, int grid, List<Integer> choices) {
        return !new MaterialCrafting.Selection(storage, entry, world, grid, choices).preview().isEmpty();
    }
}
