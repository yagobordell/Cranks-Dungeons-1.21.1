package com.cranks.dungeons.stat;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.RandomUtil;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class StatRegistry {
        private static final Logger LOGGER = LoggerFactory.getLogger("StatRegistry");
        private static final Map<String, CustomStat> STATS = new HashMap<>();

        // ========== Equipment Type Groups ==========

        private static final EquipmentType[] ALL_ARMOR = {
                        EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS
        };

        private static final EquipmentType[] ALL_MELEE_WEAPONS = {
                        EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE
        };

        private static final EquipmentType[] ALL_RANGED_WEAPONS = {
                        EquipmentType.BOW, EquipmentType.CROSSBOW
        };

        private static final EquipmentType[] ALL_MELEE_AND_TRIDENT = {
                        EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE, EquipmentType.TRIDENT
        };

        private static final EquipmentType[] ALL_WEAPONS = {
                        EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE,
                        EquipmentType.TRIDENT, EquipmentType.BOW, EquipmentType.CROSSBOW
        };
        private static final EquipmentType[] ALL_GEAR = {
                        EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS,
                        EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE,
                        EquipmentType.TRIDENT, EquipmentType.BOW, EquipmentType.CROSSBOW
        };

        private static final EquipmentType[] ALL_TOOLS = {
                        EquipmentType.PICKAXE, EquipmentType.AXE, EquipmentType.SHOVEL, EquipmentType.HOE
        };

        // ========== Shorthand helpers ==========

        private static double[] r(double t10, double t9, double t8, double t7, double t6, double t5, double t4,
                        double t3, double t2, double t1) {
                return new double[] { t10, t9, t8, t7, t6, t5, t4, t3, t2, t1 };
        }

        /**
         * Register the same stat definition for multiple equipment types.
         * The stat ID is generated as "{equipType}_{baseName}" (e.g.,
         * "helmet_durability_bonus").
         */
        private static void registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage, boolean displayAsPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        int level, int weight,
                        EquipmentType... types) {
                for (EquipmentType type : types) {
                        String id = type.name().toLowerCase() + "_" + baseName;
                        register(new CustomStat(id, displayName, type, attribute,
                                        normalRanges, uncommonRanges, isPercentage, displayAsPercentage, color,
                                        affixType, level, weight));
                }
        }

        private static void registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage, boolean displayAsPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        EquipmentType... types) {
                registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, displayAsPercentage, color, affixType, 1, 1000, types);
        }

        private static void registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        EquipmentType... types) {
                registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, false, color, affixType, 1, 1000, types);
        }

        private static void registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        int level, int weight,
                        EquipmentType... types) {
                registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, false, color, affixType, level, weight, types);
        }

        public static int rollRandomTier() {
                int roll = RandomUtil.nextInt(100);
                if (roll < 10)
                        return 10;
                if (roll < 20)
                        return 9;
                if (roll < 30)
                        return 8;
                if (roll < 40)
                        return 7;
                if (roll < 50)
                        return 6;
                if (roll < 60)
                        return 5;
                if (roll < 70)
                        return 4;
                if (roll < 80)
                        return 3;
                if (roll < 90) {
                        return 2;
                }
                return 1;
        }

        // ========== Registration ==========

        public static void registerStats() {
                // ===================================================================
                // SHARED STATS: Registered across multiple equipment type groups
                // ===================================================================

                // --- Durability Bonus (ALL equipment) ---
                registerForTypes("durability_bonus", "Durability Bonus",
                                ModAttributes.DURABILITY_BONUS,
                                r(0.05, 0.08, 0.12, 0.18, 0.25, 0.35, 0.45, 0.60, 0.80, 1.00),
                                r(0.10, 0.15, 0.22, 0.30, 0.42, 0.55, 0.70, 0.90, 1.20, 1.50),
                                true, true, Formatting.WHITE, CustomStat.AffixType.SUFFIX,
                                1, 15000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE, EquipmentType.TRIDENT,
                                EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.SHIELD, EquipmentType.ELYTRA);

                // --- Energy Shield (Armor + Elytra) ---
                registerForTypes("energy_shield", "Energy Shield",
                                ModAttributes.ENERGY_SHIELD,
                                r(10.0, 10.0, 10.0, 15.0, 20.0, 25.0, 31.0, 36.0, 42.0, 48.0),
                                r(10.0, 10.0, 15.0, 19.0, 24.0, 30.0, 35.0, 41.0, 47.0, 60.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("energy_shield", "Energy Shield",
                                ModAttributes.ENERGY_SHIELD,
                                r(18.0, 25.0, 31.0, 36.0, 42.0, 48.0, 61.0, 74.0, 81.0, 91.0),
                                r(24.0, 30.0, 35.0, 41.0, 47.0, 60.0, 73.0, 80.0, 90.0, 96.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.ELYTRA);

                // --- Max Health (Armor) ---
                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.MAX_HEALTH,
                                r(50.0, 60.0, 70.0, 85.0, 100.0, 120.0, 150.0, 175.0, 190.0, 200.0),
                                r(60.0, 70.0, 85.0, 100.0, 120.0, 150.0, 175.0, 190.0, 200.0, 250.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                80, 13000,
                                ALL_ARMOR);

                // --- Health Regeneration (Armor) ---
                registerForTypes("health_regen", "Health Regeneration",
                                ModAttributes.LIFE_REGENERATION,
                                r(1.0, 1.5, 2.0, 2.8, 3.5, 4.5, 5.8, 7.2, 9.0, 11.0),
                                r(1.5, 2.0, 2.8, 3.5, 4.5, 5.8, 7.2, 9.0, 11.0, 14.0),
                                false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                81, 11000,
                                ALL_ARMOR);

                // --- Evasion Rating (Armor only) ---
                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(19.0, 40.0, 57.0, 71.0, 89.0, 108.0, 143.0, 182.0, 205.0, 233.0),
                                r(39.0, 56.0, 70.0, 88.0, 107.0, 142.0, 181.0, 204.0, 232.0, 251.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS);

                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(1.0, 1.0, 15.0, 20.0, 30.0, 40.0, 57.0, 71.0, 89.0, 108.0),
                                r(1.0, 15.0, 19.0, 29.0, 39.0, 56.0, 70.0, 88.0, 107.0, 142.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                // --- Armor (Armor + Shield) ---
                registerForTypes("armor", "Armor",
                                EntityAttributes.ARMOR,
                                r(28.0, 51.0, 69.0, 83.0, 103.0, 123.0, 161.0, 203.0, 226.0, 257.0),
                                r(50.0, 68.0, 82.0, 102.0, 122.0, 160.0, 202.0, 225.0, 256.0, 276.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.SHIELD);

                registerForTypes("armor", "Armor",
                                EntityAttributes.ARMOR,
                                r(10.0, 10.0, 20.0, 30.0, 40.0, 51.0, 69.0, 83.0, 103.0, 123.0),
                                r(10.0, 19.0, 29.0, 39.0, 50.0, 68.0, 82.0, 102.0, 122.0, 160.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                // --- Increased Armor % (Armor + Shield) ---
                registerForTypes("armor_percent", "Increased Armor",
                                ModAttributes.ARMOR_PERCENT,
                                r(0.05, 0.10, 0.15, 0.21, 0.27, 0.43, 0.56, 0.68, 0.80, 0.92),
                                r(0.09, 0.14, 0.20, 0.26, 0.42, 0.55, 0.67, 0.79, 0.91, 1.0),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("armor_percent", "Increased Armor",
                                ModAttributes.ARMOR_PERCENT,
                                r(0.11, 0.21, 0.31, 0.37, 0.43, 0.56, 0.68, 0.80, 0.92, 1.01),
                                r(0.2, 0.3, 0.36, 0.42, 0.55, 0.67, 0.79, 0.91, 1.0, 1.1),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.SHIELD);

                // --- Increased Evasion Rating % (Armor) ---
                registerForTypes("evasion_rating_percent", "Increased Evasion Rating",
                                ModAttributes.EVASION_RATING_PERCENT,
                                r(0.05, 0.10, 0.15, 0.21, 0.27, 0.43, 0.56, 0.68, 0.80, 0.92),
                                r(0.09, 0.14, 0.20, 0.26, 0.42, 0.55, 0.67, 0.79, 0.91, 1.0),
                                true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("evasion_rating_percent", "Increased Evasion Rating",
                                ModAttributes.EVASION_RATING_PERCENT,
                                r(0.11, 0.21, 0.31, 0.37, 0.43, 0.56, 0.68, 0.80, 0.92, 1.01),
                                r(0.2, 0.3, 0.36, 0.42, 0.55, 0.67, 0.79, 0.91, 1.0, 1.1),
                                true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS);

                // --- Increased Energy Shield % (Armor + Elytra) ---
                registerForTypes("energy_shield_percent", "Increased Energy Shield",
                                ModAttributes.ENERGY_SHIELD_PERCENT,
                                r(0.05, 0.10, 0.15, 0.21, 0.27, 0.43, 0.56, 0.68, 0.80, 0.92),
                                r(0.09, 0.14, 0.20, 0.26, 0.42, 0.55, 0.67, 0.79, 0.91, 1.0),
                                true, true, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("energy_shield_percent", "Increased Energy Shield",
                                ModAttributes.ENERGY_SHIELD_PERCENT,
                                r(0.11, 0.21, 0.31, 0.37, 0.43, 0.56, 0.68, 0.80, 0.92, 1.01),
                                r(0.2, 0.3, 0.36, 0.42, 0.55, 0.67, 0.79, 0.91, 1.0, 1.1),
                                true, true, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.ELYTRA);

                // --- Knockback Resistance (Armor + Shield) ---
                registerForTypes("knockback_resistance", "Knockback Resistance",
                                ModAttributes.KNOCKBACK_RESISTANCE,
                                r(0.08, 0.10, 0.12, 0.14, 0.16, 0.18, 0.20, 0.22, 0.24, 0.26),
                                r(0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35, 0.40),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                45, 6000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD);

                // --- Elemental Resistances (Armor + Shield) ---
                EquipmentType[] armorAndShield = {
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD
                };

                // Rebalanced for ~25% T1 Normal (3 rolls to cap 75%)
                registerForTypes("fire_resistance", "Fire Resistance",
                                ModAttributes.FIRE_RESISTANCE,
                                r(0.06, 0.08, 0.1, 0.11, 0.16, 0.21, 0.26, 0.31, 0.36, 0.41),
                                r(0.06, 0.08, 0.11, 0.15, 0.2, 0.25, 0.30, 0.35, 0.4, 0.45),
                                true, true, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                registerForTypes("cold_resistance", "Cold Resistance",
                                ModAttributes.COLD_RESISTANCE,
                                r(0.06, 0.08, 0.1, 0.11, 0.16, 0.21, 0.26, 0.31, 0.36, 0.41),
                                r(0.06, 0.08, 0.11, 0.15, 0.2, 0.25, 0.30, 0.35, 0.4, 0.45),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                registerForTypes("lightning_resistance", "Lightning Resistance",
                                ModAttributes.LIGHTNING_RESISTANCE,
                                r(0.06, 0.08, 0.1, 0.11, 0.16, 0.21, 0.26, 0.31, 0.36, 0.41),
                                r(0.06, 0.08, 0.11, 0.15, 0.2, 0.25, 0.30, 0.35, 0.4, 0.45),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                // --- Void Resistance (Lowered) ---
                registerForTypes("void_resistance", "Void Resistance",
                                ModAttributes.VOID_RESISTANCE,
                                r(0.04, 0.05, 0.06, 0.07, 0.08, 0.12, 0.16, 0.2, 0.24, 0.27),
                                r(0.04, 0.05, 0.06, 0.07, 0.11, 0.15, 0.19, 0.23, 0.27, 0.30),
                                true, true, Formatting.DARK_PURPLE, CustomStat.AffixType.SUFFIX,
                                75, 8000,
                                ALL_ARMOR);

                // --- Experience Bonus (Armor + Elytra) ---
                registerForTypes("experience_bonus", "Experience Bonus",
                                ModAttributes.EXPERIENCE_BONUS,
                                r(0.05, 0.10, 0.18, 0.28, 0.42, 0.62, 0.82, 1.20, 1.60, 1.80),
                                r(0.10, 0.18, 0.28, 0.40, 0.55, 0.75, 1.0, 1.40, 1.80, 2.0),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                79, 11000,
                                ALL_ARMOR);
                // Elytra has different ranges
                register(new CustomStat("elytra_experience_bonus", "Experience Bonus",
                                EquipmentType.ELYTRA, ModAttributes.EXPERIENCE_BONUS,
                                r(0.05, 0.10, 0.18, 0.28, 0.42, 0.62, 0.82, 1.20, 1.60, 1.80),
                                r(0.10, 0.18, 0.28, 0.40, 0.55, 0.75, 1.0, 1.40, 1.80, 2.0),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                50, 3000));

                // --- Luck (Armor + Elytra) ---
                registerForTypes("luck", "Luck",
                                ModAttributes.LUCK,
                                r(0.25, 0.50, 1.00, 1.75, 2.75, 4.25, 6.50, 9.50, 14.00, 20.00),
                                r(0.50, 1.00, 1.75, 2.75, 4.25, 6.50, 9.50, 14.00, 20.00, 28.00),
                                false, false, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                40, 3000,
                                EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS);

                registerForTypes("luck", "Luck",
                                ModAttributes.LUCK,
                                r(0.25, 0.50, 1.00, 1.75, 2.75, 4.25, 6.50, 9.50, 14.00, 20.00),
                                r(0.50, 1.00, 1.75, 2.75, 4.25, 6.50, 9.50, 14.00, 20.00, 28.00),
                                false, false, Formatting.DARK_GREEN, CustomStat.AffixType.PREFIX,
                                40, 3000,
                                EquipmentType.HELMET, EquipmentType.ELYTRA);

                // --- Critical Strike Chance (Armor: Helmet + Chestplate, plus Elytra) ---
                registerForTypes("crit_chance", "Critical Strike Chance",
                                ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 3875,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE);
                // Weapons share the same values
                registerForTypes("crit_chance", "Critical Strike Chance",
                                ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 3875,
                                ALL_WEAPONS);
                // Elytra has different ranges
                register(new CustomStat("elytra_crit_chance", "Critical Strike Chance",
                                EquipmentType.ELYTRA, ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 3875));

                // ===================================================================
                // CHESTPLATE-ONLY OFFENSIVE STATS
                // ===================================================================
                // Categorized as SUFFIXES per user request

                register(new CustomStat("chestplate_attack_damage", "Physical Damage",
                                EquipmentType.CHESTPLATE, EntityAttributes.ATTACK_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, false, Formatting.WHITE, CustomStat.AffixType.SUFFIX,
                                10, 8000));

                register(new CustomStat("chestplate_attack_speed", "Attack Speed",
                                EquipmentType.CHESTPLATE, EntityAttributes.ATTACK_SPEED,
                                r(0.05, 0.1, 0.15, 0.2, 0.28, 0.38, 0.5, 0.65, 0.82, 1.0),
                                r(0.08, 0.12, 0.18, 0.25, 0.33, 0.42, 0.58, 0.81, 0.98, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                37, 3900));

                // --- Chestplate elemental damages ---
                register(new CustomStat("chestplate_fire_damage", "Fire Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.FIRE_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                20, 6000));

                register(new CustomStat("chestplate_cold_damage", "Cold Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.COLD_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, false, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                20, 6000));

                register(new CustomStat("chestplate_lightning_damage", "Lightning Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.LIGHTNING_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, false, Formatting.DARK_BLUE, CustomStat.AffixType.SUFFIX,
                                20, 6000));

                register(new CustomStat("chestplate_void_damage", "Void Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.VOID_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, false, Formatting.DARK_PURPLE, CustomStat.AffixType.SUFFIX,
                                35, 4000));

                register(new CustomStat("chestplate_knockback", "Knockback",
                                EquipmentType.CHESTPLATE, ModAttributes.KNOCKBACK,
                                r(0.1, 0.2, 0.35, 0.55, 0.80, 1.10, 1.45, 1.85, 2.30, 2.80),
                                r(0.2, 0.35, 0.55, 0.80, 1.10, 1.45, 1.85, 2.30, 2.80, 3.50),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 6000));

                register(new CustomStat("chestplate_reload_speed", "Reload Speed",
                                EquipmentType.CHESTPLATE, ModAttributes.RELOAD_SPEED,
                                r(0.05, 0.08, 0.12, 0.18, 0.25, 0.33, 0.42, 0.52, 0.63, 0.75),
                                r(0.10, 0.15, 0.20, 0.28, 0.38, 0.50, 0.65, 0.82, 1.00, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                35, 4000));

                // ===================================================================
                // BOOTS-ONLY STATS
                // ===================================================================

                register(new CustomStat("boots_movement_speed", "Movement Speed",
                                EquipmentType.BOOTS, ModAttributes.MOVEMENT_SPEED,
                                r(0.8, 0.9, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25, 1.30, 1.35),
                                r(0.8, 0.9, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25, 1.30, 1.35),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                35, 4000));

                // ===================================================================
                // WEAPON STATS (Shared across weapon types)
                // ===================================================================

                // --- Physical Damage (all weapons) ---
                registerForTypes("attack_damage", "Physical Damage",
                                EntityAttributes.ATTACK_DAMAGE,
                                r(0.5, 1.0, 2.0, 3.5, 5.5, 8.0, 11.0, 15.0, 20.0, 26.0),
                                r(1.0, 2.0, 3.5, 5.5, 8.0, 11.0, 15.0, 20.0, 26.0, 35.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                1, 12000,
                                ALL_WEAPONS);

                // --- Attack Speed (melee + trident) ---
                registerForTypes("attack_speed", "Attack Speed",
                                EntityAttributes.ATTACK_SPEED,
                                r(0.05, 0.1, 0.15, 0.2, 0.28, 0.38, 0.5, 0.65, 0.82, 1.0),
                                r(0.08, 0.12, 0.18, 0.25, 0.33, 0.42, 0.58, 0.81, 0.98, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                37, 3900,
                                ALL_MELEE_AND_TRIDENT);

                // --- Ailment Chances (all weapons) ---
                registerForTypes("chance_to_burn", "Burn Chance",
                                ModAttributes.CHANCE_TO_BURN,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                20, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_poison", "Poison Chance",
                                ModAttributes.CHANCE_TO_POISON,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                20, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_freeze", "Freeze Chance",
                                ModAttributes.CHANCE_TO_FREEZE,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                20, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_bleed", "Bleed Chance",
                                ModAttributes.CHANCE_TO_BLEED,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                20, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_shock", "Shock Chance",
                                ModAttributes.CHANCE_TO_SHOCK,
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                r(0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                20, 5000,
                                ALL_WEAPONS);

                // --- Elemental Damages (all weapons) ---
                registerForTypes("physical_damage", "Physical Damage",
                                ModAttributes.BONUS_ATTACK_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 12000,
                                ALL_WEAPONS);

                registerForTypes("fire_damage", "Fire Damage",
                                ModAttributes.FIRE_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                78, 9000,
                                ALL_WEAPONS);

                registerForTypes("cold_damage", "Cold Damage",
                                ModAttributes.COLD_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                81, 5880,
                                ALL_WEAPONS);

                registerForTypes("lightning_damage", "Lightning Damage",
                                ModAttributes.LIGHTNING_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                78, 9000,
                                ALL_WEAPONS);

                registerForTypes("void_damage", "Void Damage",
                                ModAttributes.VOID_DAMAGE,
                                r(2.0, 5.0, 10.0, 16.0, 25.0, 32.0, 46.0, 60.0, 84.0, 112.0),
                                r(3.0, 8.0, 14.0, 23.0, 30.0, 43.0, 57.0, 80.0, 107.0, 124.0),
                                false, Formatting.DARK_PURPLE, CustomStat.AffixType.PREFIX,
                                85, 3000,
                                ALL_WEAPONS);

                // --- Percent Damages (all weapons) ---
                registerForTypes("attack_damage_percent", "Increased Physical Damage",
                                ModAttributes.ATTACK_DAMAGE_PERCENT,
                                r(0.2, 0.3, 0.40, 0.5, 0.65, 0.85, 1.1, 1.35, 1.55, 1.7),
                                r(0.3, 0.4, 0.5, 0.65, 0.85, 1.1, 1.35, 1.55, 1.7, 1.79),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                70, 7000,
                                ALL_WEAPONS);

                // --- Proportional Extra Damage (Runes) ---
                registerForTypes("attack_extra_damage", "Damage as Extra Physical Damage",
                                ModAttributes.PHYSICAL_EXTRA_PERCENT,
                                r(0.08, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35),
                                r(0.10, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35, 0.40),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 1000,
                                ALL_WEAPONS);

                registerForTypes("fire_extra_damage", "Damage as Extra Fire Damage",
                                ModAttributes.FIRE_EXTRA_PERCENT,
                                r(0.08, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35),
                                r(0.10, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35, 0.40),
                                true, true, Formatting.RED, CustomStat.AffixType.PREFIX,
                                80, 1000,
                                ALL_WEAPONS);

                registerForTypes("cold_extra_damage", "Damage as Extra Cold Damage",
                                ModAttributes.COLD_EXTRA_PERCENT,
                                r(0.08, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35),
                                r(0.10, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35, 0.40),
                                true, true, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                80, 1000,
                                ALL_WEAPONS);

                registerForTypes("lightning_extra_damage", "Damage as Extra Lightning Damage",
                                ModAttributes.LIGHTNING_EXTRA_PERCENT,
                                r(0.08, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35),
                                r(0.10, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35, 0.40),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                80, 1000,
                                ALL_WEAPONS);

                registerForTypes("void_extra_damage", "Damage as Extra Void Damage",
                                ModAttributes.VOID_EXTRA_PERCENT,
                                r(0.08, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35),
                                r(0.10, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.30, 0.35, 0.40),
                                true, true, Formatting.DARK_PURPLE, CustomStat.AffixType.PREFIX,
                                85, 1000,
                                ALL_WEAPONS);

                // --- Life Steal (melee + trident: one range, bow/crossbow: different) ---
                registerForTypes("life_steal", "Life Steal",
                                ModAttributes.LIFE_STEAL,
                                r(0.05, 0.07, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22, 0.26),
                                r(0.06, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22, 0.26, 0.30),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                63, 4000,
                                ALL_MELEE_AND_TRIDENT);
                registerForTypes("life_steal", "Life Steal",
                                ModAttributes.LIFE_STEAL,
                                r(0.05, 0.06, 0.07, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22),
                                r(0.06, 0.07, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22, 0.26),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                63, 4000,
                                ALL_RANGED_WEAPONS);

                // --- Knockback (Sword, Mace, Trident: same; Spear: different; Bow/Crossbow:
                // different) ---
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.1, 0.2, 0.35, 0.55, 0.80, 1.10, 1.45, 1.85, 2.30, 2.80),
                                r(0.2, 0.35, 0.55, 0.80, 1.10, 1.45, 1.85, 2.30, 2.80, 3.50),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 700,
                                EquipmentType.SWORD, EquipmentType.MACE, EquipmentType.TRIDENT);
                register(new CustomStat("spear_knockback", "Knockback",
                                EquipmentType.SPEAR, ModAttributes.KNOCKBACK,
                                r(0.1, 0.25, 0.45, 0.70, 1.00, 1.35, 1.75, 2.20, 2.70, 3.30),
                                r(0.2, 0.40, 0.65, 0.95, 1.30, 1.70, 2.15, 2.65, 3.20, 4.00),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 700));
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.15, 0.20, 0.35, 0.5, 0.8, 1.05, 1.35, 1.5, 1.85, 2.1),
                                r(0.20, 0.35, 0.5, 0.80, 1.05, 1.35, 1.5, 1.85, 2.1, 2.35),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 700,
                                ALL_RANGED_WEAPONS);

                // --- Attack Range (Sword, Mace: same; Spear, Trident: different) ---
                registerForTypes("attack_range", "Attack Range",
                                ModAttributes.ATTACK_RANGE,
                                r(0.15, 0.20, 0.35, 0.5, 0.8, 1.05, 1.35, 1.5, 1.85, 2.1),
                                r(0.20, 0.35, 0.5, 0.80, 1.05, 1.35, 1.5, 1.85, 2.1, 2.35),
                                false, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                25, 500,
                                EquipmentType.SWORD, EquipmentType.MACE);
                registerForTypes("attack_range", "Attack Range",
                                ModAttributes.ATTACK_RANGE,
                                r(0.1, 0.2, 0.35, 0.55, 0.80, 1.10, 1.45, 1.85, 2.30, 2.80),
                                r(0.2, 0.35, 0.55, 0.80, 1.10, 1.45, 1.85, 2.30, 2.80, 3.50),
                                false, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                25, 500,
                                EquipmentType.SPEAR, EquipmentType.TRIDENT);

                // --- Reload Speed (Bow, Crossbow, Chestplate already registered above) ---
                registerForTypes("reload_speed", "Reload Speed",
                                ModAttributes.RELOAD_SPEED,
                                r(0.05, 0.08, 0.12, 0.18, 0.25, 0.33, 0.42, 0.52, 0.63, 0.75),
                                r(0.10, 0.15, 0.20, 0.28, 0.38, 0.50, 0.65, 0.82, 1.00, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                25, 400,
                                ALL_RANGED_WEAPONS);

                // ===================================================================
                // ELYTRA-ONLY STATS
                // ===================================================================

                register(new CustomStat("elytra_flying_speed", "Flying Speed",
                                EquipmentType.ELYTRA, ModAttributes.FLYING_SPEED,
                                r(0.05, 0.10, 0.18, 0.28, 0.40, 0.55, 0.72, 0.92, 1.15, 1.40),
                                r(0.08, 0.15, 0.25, 0.38, 0.55, 0.75, 1.00, 1.30, 1.70, 2.20),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                40, 300));

                register(new CustomStat("elytra_movement_speed", "Movement Speed",
                                EquipmentType.ELYTRA, ModAttributes.MOVEMENT_SPEED,
                                r(0.8, 0.9, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25, 1.30, 1.35),
                                r(0.8, 0.9, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25, 1.30, 1.35),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                35, 400));

                LOGGER.info("Registered {} custom stats", STATS.size());
        }

        private static void register(CustomStat stat) {
                STATS.put(stat.getId(), stat);
        }

        public static CustomStat getStat(String id) {
                return STATS.get(id);
        }

        public static Collection<CustomStat> getAllStats() {
                return STATS.values();
        }

        public static List<CustomStat> getStatsByEquipmentType(EquipmentType equipmentType) {
                return STATS.values().stream()
                                .filter(stat -> stat.getEquipmentType() == equipmentType)
                                .collect(Collectors.toList());
        }

        public static CustomStat getRandomStatForEquipmentType(EquipmentType equipmentType) {
                return getRandomStatForEquipmentType(equipmentType, 1, 100);
        }

        public static CustomStat getRandomStatForEquipmentType(EquipmentType equipmentType, int minLevel,
                        int maxLevel) {
                List<CustomStat> stats = getStatsByEquipmentType(equipmentType).stream()
                                .filter(stat -> stat.getLevel() >= minLevel && stat.getLevel() <= maxLevel)
                                .collect(Collectors.toList());
                return getRandomWeightedStat(stats);
        }

        public static CustomStat getRandomStatForEquipmentType(EquipmentType equipmentType,
                        CustomStat.AffixType affixType) {
                return getRandomStatForEquipmentType(equipmentType, affixType, 1, 100);
        }

        public static CustomStat getRandomStatForEquipmentType(EquipmentType equipmentType,
                        CustomStat.AffixType affixType, int minLevel, int maxLevel) {
                List<CustomStat> stats = getStatsByEquipmentType(equipmentType).stream()
                                .filter(stat -> stat.getAffixType() == affixType)
                                .filter(stat -> stat.getLevel() >= minLevel && stat.getLevel() <= maxLevel)
                                .collect(Collectors.toList());
                return getRandomWeightedStat(stats);
        }

        private static CustomStat getRandomWeightedStat(List<CustomStat> stats) {
                if (stats.isEmpty())
                        return null;

                int totalWeight = stats.stream().mapToInt(CustomStat::getWeight).sum();
                if (totalWeight <= 0)
                        return stats.get(RandomUtil.nextInt(stats.size()));

                int roll = RandomUtil.nextInt(totalWeight);
                int currentWeight = 0;
                for (CustomStat stat : stats) {
                        currentWeight += stat.getWeight();
                        if (roll < currentWeight) {
                                return stat;
                        }
                }
                return stats.get(0);
        }

        public static Collection<String> getAllStatIds() {
                return STATS.keySet();
        }
}