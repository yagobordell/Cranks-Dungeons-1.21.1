package com.cranks.dungeons.treasure;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

import java.util.*;

public class TreasureFindConfig {

    private static final Map<Block, Identifier> BLOCK_SPECIFIC_TABLES = new LinkedHashMap<>();
    private static final Set<Block> VALID_BLOCKS = new LinkedHashSet<>();
    private static final Identifier DEFAULT_TABLE = Identifier.of("cranks-dungeons", "treasure_find/shovel_treasures");

    public static void init() {
        // === Sand blocks ===
        registerBlockTable(Blocks.SAND, "treasure_find/sand_treasures");
        registerBlockTable(Blocks.RED_SAND, "treasure_find/sand_treasures");

        // === Soul blocks (nether) ===
        registerBlockTable(Blocks.SOUL_SAND, "treasure_find/soul_sand_treasures");
        registerBlockTable(Blocks.SOUL_SOIL, "treasure_find/soul_sand_treasures");

        // === Gravel ===
        registerBlockTable(Blocks.GRAVEL, "treasure_find/gravel_treasures");

        // === Clay ===
        registerBlockTable(Blocks.CLAY, "treasure_find/clay_treasures");

        // === Dirt variants ===
        registerBlockTable(Blocks.DIRT, "treasure_find/dirt_treasures");
        registerBlockTable(Blocks.GRASS_BLOCK, "treasure_find/dirt_treasures");
        registerBlockTable(Blocks.COARSE_DIRT, "treasure_find/dirt_treasures");
        registerBlockTable(Blocks.ROOTED_DIRT, "treasure_find/dirt_treasures");
        registerBlockTable(Blocks.PODZOL, "treasure_find/dirt_treasures");
        registerBlockTable(Blocks.DIRT_PATH, "treasure_find/dirt_treasures");

        // === Mycelium ===
        registerBlockTable(Blocks.MYCELIUM, "treasure_find/mycelium_treasures");

        // === Mud variants ===
        registerBlockTable(Blocks.MUD, "treasure_find/mud_treasures");

        // === Mangrove ===
        registerBlockTable(Blocks.MUDDY_MANGROVE_ROOTS, "treasure_find/mangrove_treasures");

        // === Snow ===
        registerBlockTable(Blocks.SNOW_BLOCK, "treasure_find/snow_treasures");
        registerBlockTable(Blocks.POWDER_SNOW, "treasure_find/snow_treasures");

        // === Farmland ===
        registerBlockTable(Blocks.FARMLAND, "treasure_find/farmland_treasures");

        // === Suspicious blocks (use default, they already have their own archaeology) ===
        registerValidBlock(Blocks.SUSPICIOUS_SAND);
        registerValidBlock(Blocks.SUSPICIOUS_GRAVEL);

        System.out.println("[Dungeons] TreasureFindConfig initialized with "
                + VALID_BLOCKS.size() + " valid blocks and "
                + BLOCK_SPECIFIC_TABLES.size() + " specific tables.");
    }

    public static void registerBlockTable(Block block, String tablePath) {
        BLOCK_SPECIFIC_TABLES.put(block, Identifier.of("cranks-dungeons", tablePath));
        VALID_BLOCKS.add(block);
    }

    public static void registerValidBlock(Block block) {
        VALID_BLOCKS.add(block);
    }

    public static void removeValidBlock(Block block) {
        VALID_BLOCKS.remove(block);
        BLOCK_SPECIFIC_TABLES.remove(block);
    }

    public static boolean isValidBlock(Block block) {
        return VALID_BLOCKS.contains(block);
    }

    public static Identifier getLootTableForBlock(Block block) {
        return BLOCK_SPECIFIC_TABLES.getOrDefault(block, DEFAULT_TABLE);
    }

    public static Set<Block> getValidBlocks() {
        return Collections.unmodifiableSet(VALID_BLOCKS);
    }

    public static Map<Block, Identifier> getBlockSpecificTables() {
        return Collections.unmodifiableMap(BLOCK_SPECIFIC_TABLES);
    }
}