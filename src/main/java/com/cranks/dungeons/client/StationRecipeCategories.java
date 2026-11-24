package com.cranks.dungeons.client;

import com.cranks.dungeons.block.ModBlocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

enum StationRecipeCategories {
    CARTOGRAPHY_TABLE(Category.MAPS, Category.COPY_MAPS, Category.EXPAND_MAPS, Category.LOCK_MAPS),
    TAILORING_BENCH(Category.LEATHER_EQUIPMENT, Category.BAGS, Category.ACCESSORIES, Category.BANNERS, Category.PATTERNS, Category.DYEING),
    FLETCHING_TABLE(Category.BOWS, Category.CROSSBOWS, Category.AMMUNITION),
    ANVIL(Category.WEAPONS, Category.TOOLS, Category.ARMOR, Category.HORSE_ARMOR),
    CUTTING_BENCH(Category.WOOD, Category.STONE, Category.QUARTZ, Category.GLASS,
            Category.CLAY_AND_MUD, Category.METALS, Category.OTHER_MATERIALS),
    CRAFTING_TABLE(Category.MATERIALS, Category.GEAR, Category.STATIONS_AND_STORAGE,
            Category.REDSTONE, Category.TRANSPORT, Category.BLOCKS),
    HAND_CRAFTING(Category.MATERIALS, Category.SURVIVAL_GEAR, Category.UTILITY),
    FURNACE(Category.METALS, Category.GLASS_AND_CERAMICS, Category.OTHER_MATERIALS),
    COOKSTOVE(Category.MEAT, Category.FISH, Category.VEGETABLES, Category.PREPARED_MEALS),
    FOUNDRY;

    enum Category {
        ALL("all", Items.BOOK),
        MAPS("maps", Items.MAP),
        COPY_MAPS("copy_maps", Items.FILLED_MAP),
        EXPAND_MAPS("expand_maps", Items.PAPER),
        LOCK_MAPS("lock_maps", Items.GLASS_PANE),
        LEATHER_EQUIPMENT("leather_equipment", Items.LEATHER_CHESTPLATE),
        BAGS("bags", Items.BUNDLE),
        ACCESSORIES("accessories", Items.SADDLE),
        BANNERS("banners", Items.WHITE_BANNER),
        PATTERNS("patterns", Items.FLOWER_BANNER_PATTERN),
        DYEING("dyeing", Items.RED_DYE),
        BOWS("bows", Items.BOW),
        CROSSBOWS("crossbows", Items.CROSSBOW),
        AMMUNITION("ammunition", Items.ARROW),
        WEAPONS("weapons", Items.IRON_SWORD),
        TOOLS("tools", Items.IRON_PICKAXE),
        ARMOR("armor", Items.IRON_CHESTPLATE),
        HORSE_ARMOR("horse_armor", Items.IRON_HORSE_ARMOR),
        WOOD("wood", Items.OAK_PLANKS),
        STONE("stone", Items.STONE_BRICKS),
        QUARTZ("quartz", Items.QUARTZ_BLOCK),
        GLASS("glass", Items.GLASS),
        CLAY_AND_MUD("clay_and_mud", Items.MUD_BRICKS),
        METALS("metals", Items.IRON_INGOT),
        OTHER_MATERIALS("other_materials", Items.CHEST),
        MATERIALS("materials", Items.IRON_NUGGET),
        GEAR("gear", Items.STONE_AXE),
        STATIONS_AND_STORAGE("stations_and_storage", Items.CHEST),
        REDSTONE("redstone", Items.REDSTONE),
        TRANSPORT("transport", Items.MINECART),
        BLOCKS("blocks", Items.OAK_PLANKS),
        SURVIVAL_GEAR("survival_gear", Items.TORCH),
        UTILITY("utility", Items.CRAFTING_TABLE),
        GLASS_AND_CERAMICS("glass_and_ceramics", Items.DECORATED_POT),
        MEAT("meat", Items.COOKED_BEEF),
        FISH("fish", Items.COOKED_COD),
        VEGETABLES("vegetables", Items.BAKED_POTATO),
        PREPARED_MEALS("prepared_meals", Items.RABBIT_STEW);

        final Text label;
        final ItemStack icon;

        Category(String key, Item icon) {
            label = MaterialCraftingPanel.message("category_" + key);
            this.icon = icon.getDefaultStack();
        }
    }

    private static final Pattern STONE_FAMILY = Pattern.compile(
            "(?:^|_)(?:stone|cobblestone|tuff|granite|diorite|andesite|deepslate|sandstone|blackstone|basalt|nether_bricks?)(?:_|$)");
    private static final Set<Item> STATIONS_AND_STORAGE = Set.of(
            Items.CRAFTING_TABLE, Items.CHEST, Items.TRAPPED_CHEST, Items.ENDER_CHEST, Items.BARREL, Items.BUNDLE,
            Items.CHISELED_BOOKSHELF, Items.FURNACE, Items.BLAST_FURNACE, Items.SMOKER, Items.CAMPFIRE,
            Items.SOUL_CAMPFIRE, Items.ANVIL, Items.STONECUTTER, Items.GRINDSTONE, Items.SMITHING_TABLE,
            Items.LOOM, Items.CARTOGRAPHY_TABLE, Items.FLETCHING_TABLE, Items.ENCHANTING_TABLE,
            Items.BREWING_STAND, Items.CAULDRON, Items.COMPOSTER);
    private static final Set<Item> PLACEABLE_DECORATIONS = Set.of(
            Items.ARMOR_STAND, Items.PAINTING, Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME);

    private final List<Category> tabs;

    StationRecipeCategories(Category... tabs) {
        this.tabs = List.of(tabs);
    }

    List<Category> tabs() {
        return tabs;
    }

    Category classify(Recipe<?> recipe, ItemStack result) {
        Item item = result.getItem();
        String path = Registries.ITEM.getId(item).getPath();
        return switch (this) {
            case CARTOGRAPHY_TABLE -> com.cranks.dungeons.inventory.MaterialCrafting.unwrap(recipe) instanceof com.cranks.dungeons.inventory.CartographyRecipe operation
                    ? switch (operation.operation()) {
                        case COPY -> Category.COPY_MAPS;
                        case EXPAND -> Category.EXPAND_MAPS;
                        case LOCK -> Category.LOCK_MAPS;
                    } : Category.MAPS;
            case TAILORING_BENCH -> {
                var assembly = com.cranks.dungeons.inventory.MaterialCrafting.unwrap(recipe);
                if (assembly instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe) yield Category.PATTERNS;
                if (assembly instanceof net.minecraft.recipe.ArmorDyeRecipe) yield Category.DYEING;
                if (item == Items.BUNDLE) yield Category.BAGS;
                if (item == Items.LEAD || item == Items.SADDLE) yield Category.ACCESSORIES;
                if (item instanceof net.minecraft.item.BannerItem || item instanceof net.minecraft.item.BannerPatternItem
                        || assembly instanceof net.minecraft.recipe.BannerDuplicateRecipe
                        || assembly instanceof net.minecraft.recipe.ShieldDecorationRecipe) yield Category.BANNERS;
                yield Category.LEATHER_EQUIPMENT;
            }
            case FLETCHING_TABLE -> item == Items.BOW ? Category.BOWS : item == Items.CROSSBOW ? Category.CROSSBOWS : Category.AMMUNITION;
            case ANVIL -> anvil(item, path);
            case CUTTING_BENCH -> cuttingBench(item, path);
            case CRAFTING_TABLE -> craftingTable(recipe, item, path);
            case HAND_CRAFTING -> handCrafting(recipe, result);
            case FURNACE -> furnace(item, path);
            case COOKSTOVE -> cookstove(item);
            case FOUNDRY -> Category.ALL;
        };
    }

    private static Category anvil(Item item, String path) {
        if (item instanceof SwordItem || item == Items.MACE) return Category.WEAPONS;
        if (path.endsWith("_horse_armor")) return Category.HORSE_ARMOR;
        if (item instanceof ArmorItem || item == Items.SHIELD) return Category.ARMOR;
        return Category.TOOLS;
    }

    private static Category cuttingBench(Item item, String path) {
        if (path.contains("quartz")) return Category.QUARTZ;
        if (path.contains("glass")) return Category.GLASS;
        if (path.contains("copper") || path.startsWith("iron_") || item == Items.CHAIN
                || item instanceof BlockItem block && block.getBlock() instanceof WeightedPressurePlateBlock) {
            return Category.METALS;
        }
        if (STONE_FAMILY.matcher(path).find()) return Category.STONE;
        if (isCeramic(item, path) || path.contains("mud") || path.startsWith("brick_")) return Category.CLAY_AND_MUD;
        var entry = item.getRegistryEntry();
        if (entry.isIn(ItemTags.LOGS) || entry.isIn(ItemTags.PLANKS) || entry.isIn(ItemTags.WOODEN_SLABS)
                || entry.isIn(ItemTags.WOODEN_STAIRS) || entry.isIn(ItemTags.WOODEN_DOORS) || entry.isIn(ItemTags.WOODEN_TRAPDOORS)
                || entry.isIn(ItemTags.WOODEN_FENCES) || entry.isIn(ItemTags.FENCE_GATES) || entry.isIn(ItemTags.WOODEN_BUTTONS)
                || entry.isIn(ItemTags.WOODEN_PRESSURE_PLATES) || entry.isIn(ItemTags.SIGNS) || entry.isIn(ItemTags.HANGING_SIGNS)
                || path.startsWith("bamboo_mosaic") || item == Items.LADDER || item == Items.SCAFFOLDING) {
            return Category.WOOD;
        }
        return Category.OTHER_MATERIALS;
    }

    private static Category craftingTable(Recipe<?> recipe, Item item, String path) {
        if (item.getRegistryEntry().isIn(ItemTags.BOATS) || item.getRegistryEntry().isIn(ItemTags.CHEST_BOATS)
                || item.getRegistryEntry().isIn(ItemTags.RAILS) || path.endsWith("minecart") || item == Items.SADDLE) {
            return Category.TRANSPORT;
        }
        if (isStationOrStorage(item)) return Category.STATIONS_AND_STORAGE;
        var category = declaredCategory(recipe);
        if (category == CraftingRecipeCategory.REDSTONE) return Category.REDSTONE;
        if (item instanceof BlockItem || PLACEABLE_DECORATIONS.contains(item)) return Category.BLOCKS;
        if (category == CraftingRecipeCategory.EQUIPMENT || item instanceof ArmorItem || item == Items.SHIELD
                || item == Items.TIPPED_ARROW || item == Items.MAP || item == Items.FILLED_MAP) return Category.GEAR;
        return Category.MATERIALS;
    }

    private static Category handCrafting(Recipe<?> recipe, ItemStack result) {
        Item item = result.getItem();
        var category = declaredCategory(recipe);
        if (category == CraftingRecipeCategory.EQUIPMENT || result.contains(DataComponentTypes.FOOD)
                || item == Items.TORCH || item == Items.SOUL_TORCH) return Category.SURVIVAL_GEAR;
        if (isStationOrStorage(item) || category == CraftingRecipeCategory.REDSTONE
                || item instanceof BlockItem && category == CraftingRecipeCategory.MISC) return Category.UTILITY;
        return Category.MATERIALS;
    }

    private static CraftingRecipeCategory declaredCategory(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe crafting ? crafting.getCategory() : CraftingRecipeCategory.MISC;
    }

    private static boolean isStationOrStorage(Item item) {
        return STATIONS_AND_STORAGE.contains(item) || item == ModBlocks.RUNIC_ENHANCEMENT_ALTAR.asItem()
                || item == ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR.asItem()
                || item instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock;
    }

    private static Category furnace(Item item, String path) {
        if (path.endsWith("_ingot") || path.endsWith("_nugget")) return Category.METALS;
        if (path.contains("glass") || isCeramic(item, path)) return Category.GLASS_AND_CERAMICS;
        return Category.OTHER_MATERIALS;
    }

    private static boolean isCeramic(Item item, String path) {
        return item.getRegistryEntry().isIn(ItemTags.TERRACOTTA) || path.contains("terracotta")
                || item == Items.BRICK || item == Items.BRICKS || item == Items.NETHER_BRICK;
    }

    private static Category cookstove(Item item) {
        if (item == Items.COOKED_BEEF || item == Items.COOKED_PORKCHOP || item == Items.COOKED_CHICKEN
                || item == Items.COOKED_MUTTON || item == Items.COOKED_RABBIT) return Category.MEAT;
        if (item == Items.COOKED_COD || item == Items.COOKED_SALMON || item.getRegistryEntry().isIn(ItemTags.FISHES)) {
            return Category.FISH;
        }
        if (item == Items.BAKED_POTATO || item == Items.DRIED_KELP) return Category.VEGETABLES;
        return Category.PREPARED_MEALS;
    }
}
