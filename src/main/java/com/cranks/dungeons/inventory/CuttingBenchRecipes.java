package com.cranks.dungeons.inventory;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CuttingBenchRecipes {
    private static final List<String> WOOD_TYPES = List.of("oak", "spruce", "birch", "jungle", "acacia",
            "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped");
    private static final Set<Item> BUILDING_PRODUCTS = Set.of(
            Items.LADDER, Items.SCAFFOLDING, Items.IRON_BARS, Items.CHAIN,
            Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.SANDSTONE, Items.RED_SANDSTONE,
            Items.BRICKS, Items.MUD_BRICKS, Items.PACKED_MUD, Items.MUDDY_MANGROVE_ROOTS,
            Items.NETHER_BRICKS, Items.RED_NETHER_BRICKS, Items.MOSSY_COBBLESTONE, Items.MOSSY_STONE_BRICKS,
            Items.PRISMARINE, Items.PRISMARINE_BRICKS, Items.DARK_PRISMARINE, Items.PURPUR_BLOCK,
            Items.QUARTZ_BLOCK, Items.TINTED_GLASS,
            Items.WAXED_COPPER_BLOCK, Items.WAXED_EXPOSED_COPPER, Items.WAXED_WEATHERED_COPPER, Items.WAXED_OXIDIZED_COPPER);

    private CuttingBenchRecipes() {}

    public static List<RecipeEntry<?>> route(List<RecipeEntry<?>> entries, RegistryWrapper.WrapperLookup registries) {
        List<RecipeEntry<?>> cuts = new ArrayList<>();
        Set<Identifier> ids = new HashSet<>();
        for (var entry : entries) {
            ids.add(entry.id());
            if (entry.value() instanceof StonecuttingRecipe) cuts.add(entry);
        }

        int originalCuts = cuts.size();
        for (String wood : WOOD_TYPES) {
            addCut(cuts, ids, registries, wood + "_planks", wood + "_slab", 2);
            addCut(cuts, ids, registries, wood + "_planks", wood + "_stairs", 1);
        }
        addCut(cuts, ids, registries, "bamboo_planks", "bamboo_mosaic", 1);
        addCut(cuts, ids, registries, "bamboo_mosaic", "bamboo_mosaic_slab", 2);
        addCut(cuts, ids, registries, "bamboo_mosaic", "bamboo_mosaic_stairs", 1);
        addCut(cuts, ids, registries, "glass", "glass_pane", 3);
        for (DyeColor color : DyeColor.values()) {
            String glass = color.getName() + "_stained_glass";
            addCut(cuts, ids, registries, glass, glass + "_pane", 3);
        }

        Set<Item> cutProducts = new HashSet<>();
        for (var entry : cuts) cutProducts.add(entry.value().getResult(registries).getItem());

        List<RecipeEntry<?>> routed = new ArrayList<>(entries.size());
        for (var entry : entries) {
            RecipeEntry<?> moved = moveBuildingRecipe(entry, cutProducts, registries);
            if (moved != null) routed.add(moved);
        }
        routed.addAll(cuts.subList(originalCuts, cuts.size()));
        return routed;
    }

    private static RecipeEntry<?> moveBuildingRecipe(RecipeEntry<?> entry, Set<Item> cutProducts,
                                                      RegistryWrapper.WrapperLookup registries) {
        if (!Identifier.DEFAULT_NAMESPACE.equals(entry.id().getNamespace())
                || !(entry.value() instanceof ShapedRecipe || entry.value() instanceof ShapelessRecipe)) {
            return entry;
        }
        CraftingRecipe recipe = (CraftingRecipe) entry.value();
        Item output = recipe.getResult(registries).getItem();
        Identifier itemId = Registries.ITEM.getId(output);
        if (cutProducts.contains(output)) {
            // Prefer the existing direct cut over its base crafting recipe, while keeping material treatments.
            if (entry.id().equals(itemId)) return null;
        } else if (!isBuildingProduct(output, itemId.getPath())) {
            return entry;
        }
        return new RecipeEntry<>(entry.id(), new StationAssemblyRecipe(StationAssemblyRecipe.Station.CUTTING_BENCH, recipe));
    }

    private static boolean isBuildingProduct(Item output, String path) {
        if (BUILDING_PRODUCTS.contains(output)) return true;
        if (!(output instanceof BlockItem blockItem)) return false;
        var block = blockItem.getBlock();
        return block instanceof DoorBlock || block instanceof TrapdoorBlock || block instanceof FenceBlock
                || block instanceof FenceGateBlock || block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock
                || block instanceof SlabBlock || block instanceof StairsBlock || block instanceof WallBlock
                || block instanceof AbstractSignBlock
                || path.endsWith("_stained_glass") || path.endsWith("_terracotta") || path.endsWith("_concrete_powder")
                || path.endsWith("_wood") || path.endsWith("_hyphae");
    }

    private static void addCut(List<RecipeEntry<?>> cuts, Set<Identifier> ids, RegistryWrapper.WrapperLookup registries,
                               String inputPath, String outputPath, int count) {
        Item input = Registries.ITEM.get(Identifier.ofVanilla(inputPath));
        Item output = Registries.ITEM.get(Identifier.ofVanilla(outputPath));
        var id = Identifier.of(CranksDungeons.MOD_ID, "cutting/" + outputPath);
        if (ids.contains(id)) return;
        var example = new ItemStack(input);
        boolean exists = cuts.stream().anyMatch(entry -> entry.value().getResult(registries).isOf(output)
                && entry.value().getIngredients().stream().anyMatch(ingredient -> ingredient.test(example)));
        if (!exists) cuts.add(new RecipeEntry<>(id, new StonecuttingRecipe("", Ingredient.ofItems(input), new ItemStack(output, count))));
    }
}
