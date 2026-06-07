package com.cranks.dungeons.loot;

import com.cranks.dungeons.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Runic Tome drops from mobs based on their tier.
 *
 * Drop Rates Philosophy (RARE):
 * - Tier 1 Mobs: 8% chance for T1-T2 tomes
 * - Tier 2 Mobs: 8% chance for T2-T4 tomes
 * - Tier 3 Mobs: 8% chance for T3-T5 tomes
 *
 * Tomes can be crafted: 9 tomes → 1 higher tier tome
 */
public class RunicTomeLootHandler {

    // Mob tier classification
    private static final Map<EntityType<?>, Integer> MOB_TIERS = new HashMap<>();
    
    // Chest loot table IDs
    private static final List<Identifier> TIER_1_CHESTS = List.of(
        Identifier.of("minecraft", "chests/abandoned_mineshaft"),
        Identifier.of("minecraft", "chests/spawn_bonus_chest")
    );
    
    private static final List<Identifier> TIER_2_CHESTS = List.of(
        Identifier.of("minecraft", "chests/simple_dungeon"),
        Identifier.of("minecraft", "chests/desert_pyramid"),
        Identifier.of("minecraft", "chests/jungle_temple"),
        Identifier.of("minecraft", "chests/igloo_chest"),
        Identifier.of("minecraft", "chests/ruined_portal"),
        Identifier.of("minecraft", "chests/shipwreck_map"),
        Identifier.of("minecraft", "chests/shipwreck_supply"),
        Identifier.of("minecraft", "chests/shipwreck_treasure"),
        Identifier.of("minecraft", "chests/buried_treasure"),
        Identifier.of("minecraft", "chests/underwater_ruin_small"),
        Identifier.of("minecraft", "chests/underwater_ruin_big")
    );
    
    private static final List<Identifier> TIER_3_CHESTS = List.of(
        Identifier.of("minecraft", "chests/stronghold_library"),
        Identifier.of("minecraft", "chests/stronghold_corridor"),
        Identifier.of("minecraft", "chests/stronghold_crossing"),
        Identifier.of("minecraft", "chests/end_city_treasure"),
        Identifier.of("minecraft", "chests/nether_bridge"),
        Identifier.of("minecraft", "chests/bastion_treasure"),
        Identifier.of("minecraft", "chests/bastion_other"),
        Identifier.of("minecraft", "chests/bastion_bridge"),
        Identifier.of("minecraft", "chests/bastion_hoglin_stable"),
        Identifier.of("minecraft", "chests/ancient_city"),
        Identifier.of("minecraft", "chests/ancient_city_ice_box")
    );

    public static void register() {
        registerMobTiers();

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            
            Identifier lootId = key.getValue();
            String path = lootId.getPath();
            
            // Handle entity loot tables
            if (path.startsWith("entities/")) {
                EntityType<?> entityType = getEntityTypeFromLootTable(key);
                if (entityType == null) return;

                Integer mobTier = MOB_TIERS.get(entityType);
                if (mobTier == null) return;

                // Add appropriate tome drops based on mob tier
                addTomeDrops(tableBuilder, mobTier);
            }
            // Handle chest loot tables
            else if (path.startsWith("chests/")) {
                if (TIER_3_CHESTS.contains(lootId)) {
                    addChestTomeDrops(tableBuilder, 3);
                } else if (TIER_2_CHESTS.contains(lootId)) {
                    addChestTomeDrops(tableBuilder, 2);
                } else if (TIER_1_CHESTS.contains(lootId)) {
                    addChestTomeDrops(tableBuilder, 1);
                }
            }
        });
    }

    private static void registerMobTiers() {
        // TIER 1 MOBS (Common enemies, basic threats)
        registerTier1(EntityType.ZOMBIE);
        registerTier1(EntityType.SKELETON);
        registerTier1(EntityType.SPIDER);
        registerTier1(EntityType.CAVE_SPIDER);
        registerTier1(EntityType.SLIME);
        registerTier1(EntityType.MAGMA_CUBE);
        registerTier1(EntityType.SILVERFISH);
        registerTier1(EntityType.ENDERMITE);
        registerTier1(EntityType.HUSK);
        registerTier1(EntityType.STRAY);
        registerTier1(EntityType.DROWNED);
        registerTier1(EntityType.CREEPER);

        // TIER 2 MOBS (Elite enemies, moderate threats)
        registerTier2(EntityType.PILLAGER);
        registerTier2(EntityType.VINDICATOR);
        registerTier2(EntityType.WITCH);
        registerTier2(EntityType.BLAZE);
        registerTier2(EntityType.GHAST);
        registerTier2(EntityType.ENDERMAN);
        registerTier2(EntityType.BOGGED);
        registerTier2(EntityType.BREEZE);
        registerTier2(EntityType.PIGLIN);
        registerTier2(EntityType.HOGLIN);
        registerTier2(EntityType.GUARDIAN);
        registerTier2(EntityType.PHANTOM);
        registerTier2(EntityType.ZOMBIFIED_PIGLIN);

        // TIER 3 MOBS (Elite enemies, major threats)
        registerTier3(EntityType.RAVAGER);
        registerTier3(EntityType.EVOKER);
        registerTier3(EntityType.PIGLIN_BRUTE);
        registerTier3(EntityType.WARDEN);
        registerTier3(EntityType.ELDER_GUARDIAN);
        registerTier3(EntityType.WITHER_SKELETON);
        registerTier3(EntityType.SHULKER);
    }

    private static void registerTier1(EntityType<?> entityType) {
        MOB_TIERS.put(entityType, 1);
    }

    private static void registerTier2(EntityType<?> entityType) {
        MOB_TIERS.put(entityType, 2);
    }

    private static void registerTier3(EntityType<?> entityType) {
        MOB_TIERS.put(entityType, 3);
    }

    private static void addTomeDrops(LootTable.Builder tableBuilder, int mobTier) {
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1));

        switch (mobTier) {
            case 1 -> addTier1Drops(poolBuilder);
            case 2 -> addTier2Drops(poolBuilder);
            case 3 -> addTier3Drops(poolBuilder);
        }

        tableBuilder.pool(poolBuilder);
    }

    /**
     * TIER 1 MOBS - Focus on T1 and T2 tomes
     * Total: 8% drop chance (RARE)
     * Distribution:
     * - T1: 65% of drops (5.2% overall)
     * - T2: 30% of drops (2.4% overall)
     * - T3: 5% of drops (0.4% overall) - Very lucky
     */
    private static void addTier1Drops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION)
                        .weight(6500)
                        .quality(6500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.REGAL_TOME)
                        .weight(3000)
                        .quality(3000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION)
                        .weight(500)
                        .quality(500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(net.minecraft.item.Items.AIR)
                        .weight(115000)
                        .quality(0));
    }

    /**
     * TIER 2 MOBS - Focus on T2, T3, and T4 tomes
     * Total: 8% drop chance (RARE)
     * Distribution:
     * - T2: 30% of drops (2.4% overall)
     * - T3: 45% of drops (3.6% overall)
     * - T4: 20% of drops (1.6% overall)
     * - T5: 5% of drops (0.4% overall) - Very lucky
     */
    private static void addTier2Drops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.REGAL_TOME)
                        .weight(3000)
                        .quality(3000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION)
                        .weight(4500)
                        .quality(4500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY)
                        .weight(2000)
                        .quality(2000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME)
                        .weight(500)
                        .quality(500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(net.minecraft.item.Items.AIR)
                        .weight(115000)
                        .quality(0));
    }

    /**
     * TIER 3 MOBS - Focus on T3, T4, and T5 tomes
     * Total: 8% drop chance (RARE - even bosses!)
     * Distribution:
     * - T3: 30% of drops (2.4% overall)
     * - T4: 40% of drops (3.2% overall)
     * - T5: 30% of drops (2.4% overall) - Still rare but farmable
     */
    private static void addTier3Drops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION)
                        .weight(3000)
                        .quality(3000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY)
                        .weight(4000)
                        .quality(4000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME)
                        .weight(3000)
                        .quality(3000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(net.minecraft.item.Items.AIR)
                        .weight(115000)
                        .quality(0));
    }

    /**
     * Add tome drops to chests based on tier.
     * Higher tier chests have better drop chances and higher tier tomes.
     */
    private static void addChestTomeDrops(LootTable.Builder tableBuilder, int chestTier) {
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1));

        switch (chestTier) {
            case 1 -> addTier1ChestDrops(poolBuilder);
            case 2 -> addTier2ChestDrops(poolBuilder);
            case 3 -> addTier3ChestDrops(poolBuilder);
        }

        tableBuilder.pool(poolBuilder);
    }

    /**
     * TIER 1 CHESTS (Mineshafts, Spawn Bonus) - T1-T2 tomes only
     * 15% chance for tomes
     */
    private static void addTier1ChestDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION)
                        .weight(9000)
                        .quality(9000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.REGAL_TOME)
                        .weight(1000)
                        .quality(1000)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(net.minecraft.item.Items.AIR)
                        .weight(85000)
                        .quality(0));
    }

    /**
     * TIER 2 CHESTS (Dungeons, Temples, Shipwrecks) - T1-T2 tomes only
     * 20% chance for tomes
     */
    private static void addTier2ChestDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION)
                        .weight(3500)
                        .quality(3500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.REGAL_TOME)
                        .weight(6500)
                        .quality(6500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(net.minecraft.item.Items.AIR)
                        .weight(80000)
                        .quality(0));
    }

    /**
     * TIER 3 CHESTS (Strongholds, End Cities, Bastions, Ancient Cities) - T1-T2 tomes only
     * 25% chance for tomes
     */
    private static void addTier3ChestDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION)
                        .weight(1500)
                        .quality(1500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(ModItems.REGAL_TOME)
                        .weight(8500)
                        .quality(8500)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                .with(ItemEntry.builder(net.minecraft.item.Items.AIR)
                        .weight(75000)
                        .quality(0));
    }

    private static EntityType<?> getEntityTypeFromLootTable(RegistryKey<LootTable> key) {
        String path = key.getValue().getPath();

        // Loot tables are usually "entities/entity_name"
        if (!path.startsWith("entities/")) {
            return null;
        }

        String entityName = path.substring("entities/".length());
        Identifier entityId = Identifier.of("minecraft", entityName);

        // Get EntityType from the registry
        return net.minecraft.registry.Registries.ENTITY_TYPE.get(entityId);
    }

    /**
     * Helper method to add new mob tiers programmatically
     */
    public static void registerCustomMob(EntityType<?> entityType, int tier) {
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Mob tier must be between 1 and 3");
        }
        MOB_TIERS.put(entityType, tier);
    }
}