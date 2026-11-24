package com.cranks.dungeons.loot;

import com.cranks.dungeons.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.condition.DamageSourcePropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Act 1 Runic Tome, Quality Material, and Deepslate Sigil drops from mobs and chests.
 *
 * Tome Rarity Hierarchy (Common -> Ultra Rare):
 * 1. Transmutation (Most Common - Normal -> Magic)
 * 2. Augmentation (Uncommon - Adds stat to Magic)
 * 3. Regal (Uncommon/Rare - Magic -> Rare)
 * 4. Exalted (Rare - Adds stat to Rare)
 * 5. Alchemy (Very Rare - Normal -> Rare with 3-4 stats)
 * 6. Chaos (Very Rare - Rerolls all stats on Rare)
 * 7. Annulment (Super Rare - Removes 1 random stat)
 * 8. Divine (Ultra Rare - Rerolls stat values)
 * 9. Artificial (Mythic Jackpot)
 *
 * Thematic Elemental & Utility Tomes:
 * - Super rare (0.8% base, 0.1% for greater) due to high-tier rune unlocks.
 */
public class RunicTomeLootHandler {

    private static final Map<EntityType<?>, Integer> MOB_TIERS = new HashMap<>();

    // Chest loot table IDs
    private static final List<Identifier> TIER_1_CHESTS = List.of(
        Identifier.of("minecraft", "chests/abandoned_mineshaft"),
        Identifier.of("minecraft", "chests/spawn_bonus_chest"),
        Identifier.of("minecraft", "chests/village/village_weaponsmith"),
        Identifier.of("minecraft", "chests/village/village_armorer"),
        Identifier.of("minecraft", "chests/village/village_toolsmith")
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
        Identifier.of("minecraft", "chests/underwater_ruin_big"),
        Identifier.of("minecraft", "chests/trial_chambers/reward"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_common"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_rare"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_unique"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_ominous"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_ominous_common"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_ominous_rare"),
        Identifier.of("minecraft", "chests/trial_chambers/reward_ominous_unique")
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
                if (mobTier != null) {
                    addTomeDrops(tableBuilder, mobTier);
                    addThematicTomeDrops(tableBuilder, entityType);
                }
                int materialTier = entityType == EntityType.IRON_GOLEM ? 3 : mobTier != null ? mobTier : 1;

                // 2. Quality Materials Pool (Scraps & Whetstones)
                addQualityMaterialDrops(tableBuilder, materialTier, entityType);

                // 3. Deepslate Sigil Pool
                addDeepslateSigilDrops(tableBuilder, materialTier, entityType);
            }
            // Handle chest loot tables
            else if (path.startsWith("chests/")) {
                if (lootId.equals(Identifier.ofVanilla("chests/abandoned_mineshaft"))) {
                    tableBuilder.pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1))
                            .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION)));
                }
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
        // TIER 1 MOBS (Common Overworld & Surface enemies)
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
        registerTier1(EntityType.ZOMBIE_VILLAGER);

        // TIER 2 MOBS (Elite threats & Nether/Specialty enemies)
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

        // TIER 3 MOBS (Elite leaders, mini-bosses & apex threats)
        registerTier3(EntityType.RAVAGER);
        registerTier3(EntityType.EVOKER);
        registerTier3(EntityType.PIGLIN_BRUTE);
        registerTier3(EntityType.WITHER_SKELETON);
        registerTier3(EntityType.SHULKER);
        registerTier3(EntityType.WARDEN);
        registerTier3(EntityType.ELDER_GUARDIAN);
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

    /**
     * PRIMARY RUNIC TOME POOL
     */
    private static LootCondition.Builder playerCombatKill() {
        return DamageSourcePropertiesLootCondition.builder(DamageSourcePredicate.Builder.create()
                .sourceEntity(EntityPredicate.Builder.create().type(EntityType.PLAYER)));
    }

    private static void addTomeDrops(LootTable.Builder tableBuilder, int mobTier) {
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(playerCombatKill());

        switch (mobTier) {
            case 1 -> addTier1TomeDrops(poolBuilder);
            case 2 -> addTier2TomeDrops(poolBuilder);
            case 3 -> addTier3TomeDrops(poolBuilder);
        }

        tableBuilder.pool(poolBuilder);
    }

    /**
     * TIER 1 MOBS: ~12.5% overall Tome Drop Rate
     * Strict Rarity Order: Transmutation > Augmentation > Regal > Exalted > Alchemy > Chaos > Annulment > Divine
     */
    private static void addTier1TomeDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                // 1. Transmutation (Most Common)
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION).weight(5000).quality(0))
                // 2. Augmentation (Uncommon)
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION).weight(2800).quality(1))
                // 3. Regal (Uncommon/Rare)
                .with(ItemEntry.builder(ModItems.REGAL_TOME).weight(1400).quality(2))
                // 4. Exalted (Rare)
                .with(ItemEntry.builder(ModItems.EXALTED_TOME).weight(700).quality(3))
                // 5. Alchemy (Very Rare)
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY).weight(400).quality(4))
                // 6. Chaos (Very Rare)
                .with(ItemEntry.builder(ModItems.TOME_OF_CHAOS).weight(250).quality(5))
                // 7. Annulment (Super Rare)
                .with(ItemEntry.builder(ModItems.TOME_OF_ANNULMENT).weight(100).quality(6))
                // 8. Divine (Ultra Rare)
                .with(ItemEntry.builder(ModItems.DIVINE_TOME).weight(40).quality(7))
                // 9. Artificial (Mythic)
                .with(ItemEntry.builder(ModItems.ARTIFICIAL_TOME).weight(10).quality(8))
                // No Drop (Remaining ~87.5%)
                .with(EmptyEntry.builder().weight(79300).quality(0));
    }

    /**
     * TIER 2 MOBS: ~20.0% overall Tome Drop Rate
     * Strict Rarity Order: Transmutation > Augmentation > Regal > Exalted > Alchemy > Chaos > Annulment > Divine
     */
    private static void addTier2TomeDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION).weight(2000).quality(0))
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION).weight(3000).quality(1))
                .with(ItemEntry.builder(ModItems.REGAL_TOME).weight(2500).quality(2))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME).weight(1500).quality(3))
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY).weight(1000).quality(4))
                .with(ItemEntry.builder(ModItems.TOME_OF_CHAOS).weight(600).quality(5))
                .with(ItemEntry.builder(ModItems.TOME_OF_ANNULMENT).weight(350).quality(6))
                .with(ItemEntry.builder(ModItems.DIVINE_TOME).weight(150).quality(7))
                .with(ItemEntry.builder(ModItems.ARTIFICIAL_TOME).weight(50).quality(8))
                .with(ItemEntry.builder(ModItems.GREATER_TOME_OF_TRANSMUTATION).weight(200).quality(3))
                .with(ItemEntry.builder(ModItems.GREATER_TOME_OF_AUGMENTATION).weight(150).quality(4))
                // No Drop (80%)
                .with(EmptyEntry.builder().weight(46500).quality(0));
    }

    /**
     * TIER 3 MOBS (Elite leaders & Bosses): ~35.0% overall Tome Drop Rate
     * Strict Rarity Order: Transmutation > Augmentation > Regal > Exalted > Alchemy > Chaos > Annulment > Divine
     */
    private static void addTier3TomeDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION).weight(2000).quality(1))
                .with(ItemEntry.builder(ModItems.REGAL_TOME).weight(2500).quality(2))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME).weight(2500).quality(3))
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY).weight(1800).quality(4))
                .with(ItemEntry.builder(ModItems.TOME_OF_CHAOS).weight(1400).quality(5))
                .with(ItemEntry.builder(ModItems.TOME_OF_ANNULMENT).weight(800).quality(6))
                .with(ItemEntry.builder(ModItems.DIVINE_TOME).weight(500).quality(7))
                .with(ItemEntry.builder(ModItems.ARTIFICIAL_TOME).weight(200).quality(8))
                .with(ItemEntry.builder(ModItems.GREATER_REGAL_TOME).weight(500).quality(4))
                .with(ItemEntry.builder(ModItems.GREATER_EXALTED_TOME).weight(500).quality(5))
                .with(ItemEntry.builder(ModItems.GREATER_TOME_OF_CHAOS).weight(400).quality(5))
                // No Drop (65%)
                .with(EmptyEntry.builder().weight(23900).quality(0));
    }

    /**
     * QUALITY MATERIALS POOL (Armourer's Scrap & Blacksmith's Whetstone)
     * Separate pool so polishing gear doesn't compete with tome drops.
     */
    private static void addQualityMaterialDrops(LootTable.Builder tableBuilder, int mobTier, EntityType<?> entityType) {
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1));

        boolean isArmored = entityType == EntityType.ZOMBIE || entityType == EntityType.HUSK ||
                            entityType == EntityType.DROWNED || entityType == EntityType.PILLAGER ||
                            entityType == EntityType.VINDICATOR || entityType == EntityType.PIGLIN ||
                            entityType == EntityType.PIGLIN_BRUTE || entityType == EntityType.WITHER_SKELETON ||
                            entityType == EntityType.RAVAGER || entityType == EntityType.ARMADILLO;

        boolean isWeaponWielder = entityType == EntityType.SKELETON || entityType == EntityType.STRAY ||
                                  entityType == EntityType.BOGGED || entityType == EntityType.PILLAGER ||
                                  entityType == EntityType.VINDICATOR || entityType == EntityType.PIGLIN ||
                                  entityType == EntityType.PIGLIN_BRUTE || entityType == EntityType.WITHER_SKELETON ||
                                  entityType == EntityType.DROWNED;

        int scrapWeight = isArmored ? 1500 : 800;
        int whetstoneWeight = isWeaponWielder ? 1500 : 800;

        if (mobTier >= 2) {
            scrapWeight = (int) (scrapWeight * 1.5);
            whetstoneWeight = (int) (whetstoneWeight * 1.5);
        }
        if (mobTier >= 3) {
            scrapWeight *= 2;
            whetstoneWeight *= 2;
        }

        int totalWeight = scrapWeight + whetstoneWeight;
        int airWeight = 10000 - totalWeight;

        poolBuilder
                .with(ItemEntry.builder(ModItems.ARMOURERS_SCRAP).weight(scrapWeight).quality(2))
                .with(ItemEntry.builder(ModItems.BLACKSMITHS_WHETSTONE).weight(whetstoneWeight).quality(2))
                .with(EmptyEntry.builder().weight(Math.max(1000, airWeight)).quality(0));

        tableBuilder.pool(poolBuilder);
    }

    /**
     * DEEPSLATE SIGIL POOL
     * Drops from subterranean, cave, trial, and elite mobs.
     */
    private static void addDeepslateSigilDrops(LootTable.Builder tableBuilder, int mobTier, EntityType<?> entityType) {
        boolean isCaveOrElite = entityType == EntityType.CAVE_SPIDER || entityType == EntityType.SILVERFISH ||
                                entityType == EntityType.ENDERMITE || entityType == EntityType.SLIME ||
                                entityType == EntityType.WITCH || entityType == EntityType.BREEZE ||
                                entityType == EntityType.BOGGED || entityType == EntityType.WARDEN ||
                                mobTier >= 3;

        if (!isCaveOrElite) return;

        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1));

        int sigilWeight = (mobTier == 1) ? 350 : (mobTier == 2 ? 600 : 1200); // 3.5%, 6.0%, 12.0%
        int airWeight = 10000 - sigilWeight;

        poolBuilder
                .with(ItemEntry.builder(ModItems.DEEPSLATE_SIGIL).weight(sigilWeight).quality(3))
                .with(EmptyEntry.builder().weight(airWeight).quality(0));

        tableBuilder.pool(poolBuilder);
    }

    /**
     * THEMATIC ELEMENTAL & UTILITY TOMES POOL (SUPER RARE - ~0.8% base, ~0.1% greater)
     * These grant high-tier rune properties so they are strictly gatekept.
     */
    public record ThematicTomes(Item regular, Item greater) {}
    public static int guideMobTier(EntityType<?> type) { return MOB_TIERS.getOrDefault(type,0); }
    public static ThematicTomes thematicTomes(EntityType<?> entityType) {
        Item specificTome = null;
        Item greaterTome = null;

        // FIRE
        if (entityType == EntityType.BLAZE || entityType == EntityType.MAGMA_CUBE || entityType == EntityType.GHAST) {
            specificTome = ModItems.TOME_OF_FIRE;
            greaterTome = ModItems.GREATER_TOME_OF_FIRE;
        }
        // COLD
        else if (entityType == EntityType.STRAY || entityType == EntityType.SNOW_GOLEM || entityType == EntityType.POLAR_BEAR) {
            specificTome = ModItems.TOME_OF_COLD;
            greaterTome = ModItems.GREATER_TOME_OF_COLD;
        }
        // LIGHTNING
        else if (entityType == EntityType.GUARDIAN || entityType == EntityType.BREEZE) {
            specificTome = ModItems.TOME_OF_LIGHTNING;
            greaterTome = ModItems.GREATER_TOME_OF_LIGHTNING;
        }
        // VOID
        else if (entityType == EntityType.WITCH || entityType == EntityType.EVOKER ||
                 entityType == EntityType.ENDERMAN || entityType == EntityType.SHULKER ||
                 entityType == EntityType.PHANTOM || entityType == EntityType.WITHER_SKELETON) {
            specificTome = ModItems.TOME_OF_VOID;
            greaterTome = ModItems.GREATER_TOME_OF_VOID;
        }
        // PHYSICALITY
        else if (entityType == EntityType.IRON_GOLEM || entityType == EntityType.RAVAGER ||
                 entityType == EntityType.HOGLIN || entityType == EntityType.PIGLIN_BRUTE) {
            specificTome = ModItems.TOME_OF_PHYSICALITY;
            greaterTome = ModItems.GREATER_TOME_OF_PHYSICALITY;
        }
        // SWIFTNESS
        else if (entityType == EntityType.SPIDER || entityType == EntityType.CAVE_SPIDER ||
                 entityType == EntityType.SILVERFISH) {
            specificTome = ModItems.TOME_OF_SWIFTNESS;
        }
        // HEALTH BONUS
        else if (entityType == EntityType.HUSK || entityType == EntityType.ZOGLIN) {
            specificTome = ModItems.TOME_OF_HEALTH_BONUS;
        }

        return new ThematicTomes(specificTome,greaterTome);
    }

    private static void addThematicTomeDrops(LootTable.Builder tableBuilder, EntityType<?> entityType) {
        var theme=thematicTomes(entityType);Item specificTome=theme.regular(),greaterTome=theme.greater();
        if (specificTome == null) return;
        LootPool.Builder poolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(playerCombatKill());

        // 80 weight = 0.8% drop chance (Super Rare)
        poolBuilder.with(ItemEntry.builder(specificTome).weight(80).quality(5));
        if (greaterTome != null) {
            // 10 weight = 0.1% drop chance (Ultra Rare)
            poolBuilder.with(ItemEntry.builder(greaterTome).weight(10).quality(7));
            poolBuilder.with(EmptyEntry.builder().weight(9910).quality(0));
        } else {
            poolBuilder.with(EmptyEntry.builder().weight(9920).quality(0));
        }

        tableBuilder.pool(poolBuilder);
    }

    /**
     * CHEST LOOT TABLES
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
     * TIER 1 CHESTS (Mineshafts, Villages, Spawn Bonus): ~35% chance for loot
     */
    private static void addTier1ChestDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION).weight(1500).quality(0).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION).weight(800).quality(1))
                .with(ItemEntry.builder(ModItems.REGAL_TOME).weight(300).quality(2))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME).weight(100).quality(3))
                .with(ItemEntry.builder(ModItems.ARMOURERS_SCRAP).weight(500).quality(1).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3))))
                .with(ItemEntry.builder(ModItems.BLACKSMITHS_WHETSTONE).weight(500).quality(1).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3))))
                .with(ItemEntry.builder(ModItems.DEEPSLATE_SIGIL).weight(100).quality(3))
                .with(EmptyEntry.builder().weight(6200).quality(0));
    }

    /**
     * TIER 2 CHESTS (Dungeons, Temples, Shipwrecks, Ruined Portals, Trial Chambers): ~50% chance for loot
     */
    private static void addTier2ChestDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.TOME_OF_TRANSMUTATION).weight(1200).quality(0).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3))))
                .with(ItemEntry.builder(ModItems.TOME_OF_AUGMENTATION).weight(1200).quality(1).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(ModItems.REGAL_TOME).weight(800).quality(2))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME).weight(500).quality(3))
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY).weight(350).quality(4))
                .with(ItemEntry.builder(ModItems.TOME_OF_CHAOS).weight(200).quality(5))
                .with(ItemEntry.builder(ModItems.TOME_OF_ANNULMENT).weight(100).quality(6))
                .with(ItemEntry.builder(ModItems.ARMOURERS_SCRAP).weight(800).quality(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2, 4))))
                .with(ItemEntry.builder(ModItems.BLACKSMITHS_WHETSTONE).weight(800).quality(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2, 4))))
                .with(ItemEntry.builder(ModItems.DEEPSLATE_SIGIL).weight(300).quality(3))
                .with(EmptyEntry.builder().weight(4350).quality(0));
    }

    /**
     * TIER 3 CHESTS (Strongholds, Bastions, Ancient Cities, End Cities): ~75% chance for high tier loot
     */
    private static void addTier3ChestDrops(LootPool.Builder poolBuilder) {
        poolBuilder
                .with(ItemEntry.builder(ModItems.REGAL_TOME).weight(1500).quality(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(ModItems.EXALTED_TOME).weight(1500).quality(3).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(ModItems.TOME_OF_ALCHEMY).weight(1200).quality(4).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(ModItems.TOME_OF_CHAOS).weight(1000).quality(5).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(ModItems.TOME_OF_ANNULMENT).weight(600).quality(6))
                .with(ItemEntry.builder(ModItems.DIVINE_TOME).weight(300).quality(7))
                .with(ItemEntry.builder(ModItems.ARTIFICIAL_TOME).weight(150).quality(8))
                .with(ItemEntry.builder(ModItems.ARMOURERS_SCRAP).weight(1000).quality(3).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 6))))
                .with(ItemEntry.builder(ModItems.BLACKSMITHS_WHETSTONE).weight(1000).quality(3).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 6))))
                .with(ItemEntry.builder(ModItems.DEEPSLATE_SIGIL).weight(600).quality(4).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(EmptyEntry.builder().weight(2150).quality(0));
    }

    private static EntityType<?> getEntityTypeFromLootTable(RegistryKey<LootTable> key) {
        String path = key.getValue().getPath();

        if (!path.startsWith("entities/")) {
            return null;
        }

        String entityName = path.substring("entities/".length());
        Identifier entityId = Identifier.of(key.getValue().getNamespace(), entityName);

        return net.minecraft.registry.Registries.ENTITY_TYPE.getOrEmpty(entityId).orElse(null);
    }

    public static void registerCustomMob(EntityType<?> entityType, int tier) {
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Mob tier must be between 1 and 3");
        }
        MOB_TIERS.put(entityType, tier);
    }
}