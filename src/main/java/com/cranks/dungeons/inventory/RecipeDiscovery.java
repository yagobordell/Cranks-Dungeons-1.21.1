package com.cranks.dungeons.inventory;

import com.cranks.dungeons.component.ItemDiscoveryComponent;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.ArmorDyeRecipe;
import net.minecraft.recipe.BannerDuplicateRecipe;
import net.minecraft.recipe.BookCloningRecipe;
import net.minecraft.recipe.CraftingDecoratedPotRecipe;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.FireworkStarFadeRecipe;
import net.minecraft.recipe.FireworkStarRecipe;
import net.minecraft.recipe.MapCloningRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShieldDecorationRecipe;
import net.minecraft.recipe.ShulkerBoxColoringRecipe;
import net.minecraft.recipe.SuspiciousStewRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.registry.tag.ItemTags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Shared recipe visibility for stations. One discovered alternative satisfies an ingredient. */
public final class RecipeDiscovery {
    private RecipeDiscovery() {}

    public static boolean isDiscovered(Recipe<?> recipe, ItemDiscoveryComponent discovered) {
        recipe = MaterialCrafting.unwrap(recipe);
        var special = specialRequirements(recipe);
        return special.isEmpty() ? hasDeclaredIngredients(recipe, discovered)
                : special.stream().allMatch(discovered::hasMatching);
    }

    public static Predicate<Item> materialTest(Recipe<?> recipe) {
        recipe = MaterialCrafting.unwrap(recipe);
        var special = specialRequirements(recipe);
        if (special.isEmpty()) {
            Set<Item> materials = new HashSet<>();
            for (var ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                for (var candidate : ingredient.getMatchingStacks()) materials.add(candidate.getItem());
            }
            return materials::contains;
        }
        Recipe<?> resolved = recipe;
        return item -> special.stream().anyMatch(test -> test.test(item)) || optionalMaterial(resolved, item);
    }

    private static boolean optionalMaterial(Recipe<?> recipe, Item item) {
        if (recipe instanceof FireworkRocketRecipe) return item == Items.FIREWORK_STAR;
        return recipe instanceof FireworkStarRecipe && (item == Items.DIAMOND || item == Items.GLOWSTONE_DUST
                || item == Items.FIRE_CHARGE || item == Items.GOLD_NUGGET || item == Items.FEATHER
                || item.getRegistryEntry().isIn(ItemTags.SKULLS));
    }

    // Special recipes resolve their inputs at craft time and usually have no declared ingredients.
    static List<Predicate<Item>> specialRequirements(Recipe<?> recipe) {
        return switch (recipe) {
            case TailoringPatternRecipe pattern -> pattern.getIngredients().stream()
                    .<Predicate<Item>>map(ingredient -> item -> ingredient.test(item.getDefaultStack())).toList();
            case ArmorDyeRecipe ignored -> List.of(item -> item.getRegistryEntry().isIn(ItemTags.DYEABLE), DyeItem.class::isInstance);
            case BannerDuplicateRecipe ignored -> List.of(item -> item.getRegistryEntry().isIn(ItemTags.BANNERS));
            case BookCloningRecipe ignored -> List.of(Items.WRITTEN_BOOK::equals, Items.WRITABLE_BOOK::equals);
            case CraftingDecoratedPotRecipe ignored -> List.of(item -> item.getRegistryEntry().isIn(ItemTags.DECORATED_POT_INGREDIENTS));
            case FireworkRocketRecipe ignored -> List.of(Items.PAPER::equals, Items.GUNPOWDER::equals);
            case FireworkStarRecipe ignored -> List.of(Items.GUNPOWDER::equals, DyeItem.class::isInstance);
            case FireworkStarFadeRecipe ignored -> List.of(Items.FIREWORK_STAR::equals, DyeItem.class::isInstance);
            case MapCloningRecipe ignored -> List.of(Items.FILLED_MAP::equals, Items.MAP::equals);
            case MapExtendingRecipe ignored -> List.of(Items.FILLED_MAP::equals, Items.PAPER::equals);
            case ShieldDecorationRecipe ignored -> List.of(Items.SHIELD::equals, item -> item.getRegistryEntry().isIn(ItemTags.BANNERS));
            case ShulkerBoxColoringRecipe ignored -> List.of(DyeItem.class::isInstance,
                    item -> item instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock);
            case SuspiciousStewRecipe ignored -> List.of(Items.BOWL::equals, Items.BROWN_MUSHROOM::equals, Items.RED_MUSHROOM::equals,
                    item -> item.getRegistryEntry().isIn(ItemTags.SMALL_FLOWERS));
            case TippedArrowRecipe ignored -> List.of(item -> item == Items.LINGERING_POTION
                    || item instanceof com.cranks.dungeons.alchemy.AlchemyItems.Brew brew
                    && brew.delivery == com.cranks.dungeons.alchemy.AlchemyItems.Delivery.LINGERING, Items.ARROW::equals);
            default -> List.of();
        };
    }

    private static boolean hasDeclaredIngredients(Recipe<?> recipe, ItemDiscoveryComponent discovered) {
        boolean hasIngredient = false;
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            hasIngredient = true;
            boolean known = false;
            for (var stack : ingredient.getMatchingStacks()) {
                if (discovered.contains(stack.getItem())) {
                    known = true;
                    break;
                }
            }
            if (!known) return false;
        }
        return hasIngredient;
    }
}
