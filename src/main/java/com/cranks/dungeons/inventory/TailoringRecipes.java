package com.cranks.dungeons.inventory;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BannerPatternTags;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Tailoring owns leather gear, bags, accessories and banner work. Routing is reload-safe. */
public final class TailoringRecipes {
    private static final Set<Item> PRODUCTS = Set.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE,
            Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR,
            Items.BUNDLE, Items.LEAD, Items.SADDLE);

    private TailoringRecipes() {}

    public static List<RecipeEntry<?>> route(List<RecipeEntry<?>> entries, RegistryWrapper.WrapperLookup registries,
                                            ResourceManager resources) {
        List<RecipeEntry<?>> result = new ArrayList<>();
        Set<Identifier> ids = new HashSet<>();
        for (var entry : entries) {
            Recipe<?> recipe = entry.value();
            if (recipe instanceof CraftingRecipe crafting
                    && (recipe instanceof ArmorDyeRecipe || recipe instanceof BannerDuplicateRecipe
                    || recipe instanceof ShieldDecorationRecipe || (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                    && (PRODUCTS.contains(recipe.getResult(registries).getItem())
                    || recipe.getResult(registries).getItem() instanceof BannerItem
                    || recipe.getResult(registries).getItem() instanceof BannerPatternItem))) {
                entry = new RecipeEntry<>(entry.id(), new StationAssemblyRecipe(StationAssemblyRecipe.Station.TAILORING_BENCH, crafting));
            }
            result.add(entry);
            ids.add(entry.id());
        }
        var patterns = registries.getWrapperOrThrow(RegistryKeys.BANNER_PATTERN);
        // Tag binding happens after recipe reload. Resolve the incoming resource pack
        // tags here so a fresh world and /reload generate the same pattern catalogue.
        var tags = resources == null ? null : new TagGroupLoader<RegistryEntry<BannerPattern>>(
                id -> patterns.getOptional(net.minecraft.registry.RegistryKey.of(RegistryKeys.BANNER_PATTERN, id)),
                "tags/banner_pattern").load(resources);
        java.util.function.BiPredicate<TagKey<BannerPattern>, RegistryEntry<BannerPattern>> inTag = (tag, entry) -> tags == null
                ? patterns.getOptional(tag).map(values -> values.contains(entry)).orElse(false)
                : tags.getOrDefault(tag.id(), List.of()).contains(entry);
        patterns.streamEntries().forEach(pattern -> {
            Ingredient template;
            if (inTag.test(BannerPatternTags.NO_ITEM_REQUIRED, pattern)) template = Ingredient.EMPTY;
            else {
                var items = Registries.ITEM.stream().filter(item -> item instanceof BannerPatternItem design
                        && inTag.test(design.getPattern(), pattern)).toArray(Item[]::new);
                if (items.length == 0) return;
                template = Ingredient.ofItems(items);
            }
            Identifier key = pattern.registryKey().getValue();
            Identifier id = Identifier.of(CranksDungeons.MOD_ID, "tailoring/pattern/" + key.getNamespace() + "/" + key.getPath());
            if (ids.add(id)) result.add(new RecipeEntry<>(id, new StationAssemblyRecipe(
                    StationAssemblyRecipe.Station.TAILORING_BENCH, new TailoringPatternRecipe(key, template))));
        });
        return result;
    }
}
