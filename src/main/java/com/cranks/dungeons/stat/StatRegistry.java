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
                if (roll < 20) return 10;
                if (roll < 38) return 9;
                if (roll < 53) return 8;
                if (roll < 65) return 7;
                if (roll < 75) return 6;
                if (roll < 83) return 5;
                if (roll < 89) return 4;
                if (roll < 94) return 3;
                if (roll < 98) return 2;
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
                                r(0.05, 0.06, 0.10, 0.16, 0.24, 0.34, 0.47, 0.62, 0.80, 1.00),
                                r(0.10, 0.12, 0.17, 0.26, 0.38, 0.53, 0.72, 0.95, 1.21, 1.50),
                                true, true, Formatting.WHITE, CustomStat.AffixType.SUFFIX,
                                1, 15000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE, EquipmentType.TRIDENT,
                                EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.SHIELD, EquipmentType.ELYTRA);

                // --- Energy Shield (Armor + Elytra) ---
                registerForTypes("energy_shield", "Energy Shield",
                                ModAttributes.ENERGY_SHIELD,
                                r(10.0, 10.5, 11.9, 14.2, 17.5, 21.7, 26.9, 33.0, 40.0, 48.0),
                                r(10.0, 10.6, 12.5, 15.6, 19.9, 25.4, 32.2, 40.2, 49.5, 60.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("energy_shield", "Energy Shield",
                                ModAttributes.ENERGY_SHIELD,
                                r(18.0, 18.9, 21.6, 26.1, 32.4, 40.5, 50.4, 62.2, 75.7, 91.0),
                                r(24.0, 24.9, 27.6, 32.0, 38.2, 46.2, 56.0, 67.6, 80.9, 96.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.ELYTRA);

                // --- Max Health (Armor) ---
                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.MAX_HEALTH,
                                r(50.0, 51.9, 57.4, 66.7, 79.6, 96.3, 116.7, 140.7, 168.5, 200.0),
                                r(60.0, 62.3, 69.4, 81.1, 97.5, 118.6, 144.4, 174.9, 210.1, 250.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                80, 13000,
                                ALL_ARMOR);

                // --- Health Regeneration (Armor) ---
                registerForTypes("health_regen", "Health Regeneration",
                                ModAttributes.LIFE_REGENERATION,
                                r(1.0, 1.1, 1.5, 2.1, 3.0, 4.1, 5.4, 7.0, 8.9, 11.0),
                                r(1.5, 1.7, 2.1, 2.9, 4.0, 5.4, 7.1, 9.1, 11.4, 14.0),
                                false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                81, 11000,
                                ALL_ARMOR);

                // --- Evasion Rating (Armor only) ---
                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(19.0, 21.6, 29.6, 42.8, 61.3, 85.0, 114.1, 148.5, 188.1, 233.0),
                                r(39.0, 41.6, 49.5, 62.6, 80.9, 104.4, 133.2, 167.2, 206.5, 251.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS);

                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(1.0, 2.3, 6.3, 12.9, 22.1, 34.0, 48.6, 65.7, 85.5, 108.0),
                                r(1.0, 2.7, 8.0, 16.7, 28.9, 44.5, 63.7, 86.3, 112.4, 142.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                // --- Armor (Armor + Shield) ---
                registerForTypes("armor", "Armor",
                                EntityAttributes.ARMOR,
                                r(28.0, 30.8, 39.3, 53.4, 73.2, 98.7, 129.8, 166.5, 208.9, 257.0),
                                r(50.0, 52.8, 61.2, 75.1, 94.6, 119.8, 150.4, 186.7, 228.6, 276.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.SHIELD);

                registerForTypes("armor", "Armor",
                                EntityAttributes.ARMOR,
                                r(10.0, 11.4, 15.6, 22.6, 32.3, 44.9, 60.2, 78.4, 99.3, 123.0),
                                r(10.0, 11.9, 17.4, 26.7, 39.6, 56.3, 76.7, 100.7, 128.5, 160.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                79, 11000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                // --- Increased Armor % (Armor + Shield) ---
                registerForTypes("armor_percent", "Increased Armor",
                                ModAttributes.ARMOR_PERCENT,
                                r(0.05, 0.06, 0.09, 0.15, 0.22, 0.32, 0.44, 0.58, 0.74, 0.92),
                                r(0.09, 0.10, 0.13, 0.19, 0.27, 0.37, 0.49, 0.64, 0.81, 1.00),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("armor_percent", "Increased Armor",
                                ModAttributes.ARMOR_PERCENT,
                                r(0.11, 0.12, 0.15, 0.21, 0.29, 0.39, 0.51, 0.65, 0.82, 1.01),
                                r(0.20, 0.21, 0.24, 0.30, 0.38, 0.48, 0.60, 0.74, 0.91, 1.10),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.SHIELD);

                // --- Increased Evasion Rating % (Armor) ---
                registerForTypes("evasion_rating_percent", "Increased Evasion Rating",
                                ModAttributes.EVASION_RATING_PERCENT,
                                r(0.05, 0.06, 0.09, 0.15, 0.22, 0.32, 0.44, 0.58, 0.74, 0.92),
                                r(0.09, 0.10, 0.13, 0.19, 0.27, 0.37, 0.49, 0.64, 0.81, 1.00),
                                true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("evasion_rating_percent", "Increased Evasion Rating",
                                ModAttributes.EVASION_RATING_PERCENT,
                                r(0.11, 0.12, 0.15, 0.21, 0.29, 0.39, 0.51, 0.65, 0.82, 1.01),
                                r(0.20, 0.21, 0.24, 0.30, 0.38, 0.48, 0.60, 0.74, 0.91, 1.10),
                                true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS);

                // --- Increased Energy Shield % (Armor + Elytra) ---
                registerForTypes("energy_shield_percent", "Increased Energy Shield",
                                ModAttributes.ENERGY_SHIELD_PERCENT,
                                r(0.05, 0.06, 0.09, 0.15, 0.22, 0.32, 0.44, 0.58, 0.74, 0.92),
                                r(0.09, 0.10, 0.13, 0.19, 0.27, 0.37, 0.49, 0.64, 0.81, 1.00),
                                true, true, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("energy_shield_percent", "Increased Energy Shield",
                                ModAttributes.ENERGY_SHIELD_PERCENT,
                                r(0.11, 0.12, 0.15, 0.21, 0.29, 0.39, 0.51, 0.65, 0.82, 1.01),
                                r(0.20, 0.21, 0.24, 0.30, 0.38, 0.48, 0.60, 0.74, 0.91, 1.10),
                                true, true, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                75, 8000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.ELYTRA);

                // --- Knockback Resistance (Armor + Shield) ---
                registerForTypes("knockback_resistance", "Knockback Resistance",
                                ModAttributes.KNOCKBACK_RESISTANCE,
                                r(0.08, 0.08, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22, 0.26),
                                r(0.12, 0.12, 0.13, 0.15, 0.18, 0.21, 0.24, 0.29, 0.34, 0.40),
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
                                r(0.06, 0.06, 0.08, 0.10, 0.13, 0.17, 0.22, 0.27, 0.34, 0.41),
                                r(0.06, 0.06, 0.08, 0.10, 0.14, 0.18, 0.23, 0.30, 0.37, 0.45),
                                true, true, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                registerForTypes("cold_resistance", "Cold Resistance",
                                ModAttributes.COLD_RESISTANCE,
                                r(0.06, 0.06, 0.08, 0.10, 0.13, 0.17, 0.22, 0.27, 0.34, 0.41),
                                r(0.06, 0.06, 0.08, 0.10, 0.14, 0.18, 0.23, 0.30, 0.37, 0.45),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                registerForTypes("lightning_resistance", "Lightning Resistance",
                                ModAttributes.LIGHTNING_RESISTANCE,
                                r(0.06, 0.06, 0.08, 0.10, 0.13, 0.17, 0.22, 0.27, 0.34, 0.41),
                                r(0.06, 0.06, 0.08, 0.10, 0.14, 0.18, 0.23, 0.30, 0.37, 0.45),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                // --- Void Resistance (Lowered) ---
                registerForTypes("void_resistance", "Void Resistance",
                                ModAttributes.VOID_RESISTANCE,
                                r(0.04, 0.04, 0.05, 0.07, 0.09, 0.11, 0.14, 0.18, 0.22, 0.27),
                                r(0.04, 0.04, 0.05, 0.07, 0.09, 0.12, 0.16, 0.20, 0.25, 0.30),
                                true, true, Formatting.DARK_PURPLE, CustomStat.AffixType.SUFFIX,
                                75, 8000,
                                ALL_ARMOR);

                // --- Thorns (Armor) ---
                registerForTypes("thorns", "Thorns",
                                ModAttributes.THORNS_DAMAGE,
                                r(2.0, 3.0, 6.1, 11.2, 18.4, 27.6, 38.9, 52.2, 67.6, 85.0),
                                r(3.0, 4.2, 7.8, 13.8, 22.2, 32.9, 46.1, 61.7, 79.6, 100.0),
                                false, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                10, 6000,
                                ALL_ARMOR);

                // --- Experience Bonus (Armor + Elytra) ---
                registerForTypes("experience_bonus", "Experience Bonus",
                                ModAttributes.EXPERIENCE_BONUS,
                                r(0.05, 0.07, 0.14, 0.24, 0.40, 0.59, 0.83, 1.11, 1.43, 1.80),
                                r(0.10, 0.12, 0.19, 0.31, 0.48, 0.69, 0.94, 1.25, 1.60, 2.00),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                30, 6000,
                                ALL_ARMOR);
                // Elytra has different ranges
                register(new CustomStat("elytra_experience_bonus", "Experience Bonus",
                                EquipmentType.ELYTRA, ModAttributes.EXPERIENCE_BONUS,
                                r(0.05, 0.07, 0.14, 0.24, 0.40, 0.59, 0.83, 1.11, 1.43, 1.80),
                                r(0.10, 0.12, 0.19, 0.31, 0.48, 0.69, 0.94, 1.25, 1.60, 2.00),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                30, 1500));

                // --- Item Rarity (Helmets + Boots) ---
                registerForTypes("item_rarity", "Increased Rarity of Items Found",
                                ModAttributes.ITEM_RARITY_PERCENT,
                                r(0.04, 0.05, 0.06, 0.07, 0.09, 0.10, 0.12, 0.13, 0.15, 0.17),
                                r(0.06, 0.07, 0.08, 0.10, 0.11, 0.13, 0.14, 0.15, 0.17, 0.18),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                30, 1500,
                                EquipmentType.HELMET, EquipmentType.BOOTS);

                // --- Critical Strike Chance (Armor: Helmet + Chestplate, plus Elytra) ---
                registerForTypes("crit_chance", "Critical Strike Chance",
                                ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 1200,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE);
                // Weapons share the same values
                registerForTypes("crit_chance", "Critical Strike Chance",
                                ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 1200,
                                ALL_WEAPONS);
                // Elytra has different ranges
                register(new CustomStat("elytra_crit_chance", "Critical Strike Chance",
                                EquipmentType.ELYTRA, ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 1200));

                // ===================================================================
                // CHESTPLATE-ONLY OFFENSIVE STATS
                // ===================================================================
                // Categorized as SUFFIXES per user request

                register(new CustomStat("chestplate_attack_damage", "Physical Damage",
                                EquipmentType.CHESTPLATE, EntityAttributes.ATTACK_DAMAGE,
                                r(0.5, 0.8, 1.8, 3.3, 5.5, 8.4, 11.8, 15.9, 20.6, 26.0),
                                r(0.5, 0.9, 2.2, 4.3, 7.3, 11.1, 15.8, 21.4, 27.8, 35.0),
                                false, false, Formatting.WHITE, CustomStat.AffixType.SUFFIX,
                                30, 3000));

                register(new CustomStat("chestplate_attack_speed", "Attack Speed",
                                EquipmentType.CHESTPLATE, EntityAttributes.ATTACK_SPEED,
                                r(0.05, 0.06, 0.10, 0.16, 0.24, 0.34, 0.47, 0.62, 0.80, 1.00),
                                r(0.08, 0.09, 0.14, 0.21, 0.31, 0.44, 0.60, 0.79, 1.00, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                30, 2000));

                // --- Chestplate elemental damages ---
                register(new CustomStat("chestplate_fire_damage", "Fire Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.FIRE_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                30, 2500));

                register(new CustomStat("chestplate_cold_damage", "Cold Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.COLD_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, false, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                30, 2500));

                register(new CustomStat("chestplate_lightning_damage", "Lightning Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.LIGHTNING_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, false, Formatting.DARK_BLUE, CustomStat.AffixType.SUFFIX,
                                30, 2500));

                register(new CustomStat("chestplate_void_damage", "Void Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.VOID_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, false, Formatting.DARK_PURPLE, CustomStat.AffixType.SUFFIX,
                                30, 1500));

                register(new CustomStat("chestplate_knockback", "Knockback",
                                EquipmentType.CHESTPLATE, ModAttributes.KNOCKBACK,
                                r(0.10, 0.13, 0.23, 0.40, 0.63, 0.93, 1.30, 1.73, 2.23, 2.80),
                                r(0.20, 0.24, 0.36, 0.57, 0.85, 1.22, 1.67, 2.20, 2.81, 3.50),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 1500));

                register(new CustomStat("chestplate_reload_speed", "Reload Speed",
                                EquipmentType.CHESTPLATE, ModAttributes.RELOAD_SPEED,
                                r(0.05, 0.06, 0.08, 0.13, 0.19, 0.27, 0.36, 0.47, 0.60, 0.75),
                                r(0.10, 0.11, 0.16, 0.23, 0.33, 0.45, 0.61, 0.80, 1.01, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                15, 2000));

                // ===================================================================
                // BOOTS-ONLY STATS
                // ===================================================================

                register(new CustomStat("boots_movement_speed", "Movement Speed",
                                EquipmentType.BOOTS, ModAttributes.MOVEMENT_SPEED,
                                r(0.80, 0.81, 0.83, 0.86, 0.91, 0.97, 1.04, 1.13, 1.23, 1.35),
                                r(0.80, 0.81, 0.83, 0.86, 0.91, 0.97, 1.04, 1.13, 1.23, 1.35),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                60, 4000));

                // ===================================================================
                // WEAPON STATS (Shared across weapon types)
                // ===================================================================

                // --- Physical Damage (all weapons) ---
                registerForTypes("attack_damage", "Physical Damage",
                                EntityAttributes.ATTACK_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(2.0, 3.5, 8.0, 15.6, 26.1, 39.7, 56.2, 75.8, 98.4, 124.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                60, 12000,
                                ALL_WEAPONS);

                // --- Attack Speed (melee + trident) ---
                registerForTypes("attack_speed", "Attack Speed",
                                EntityAttributes.ATTACK_SPEED,
                                r(0.05, 0.06, 0.10, 0.16, 0.24, 0.34, 0.47, 0.62, 0.80, 1.00),
                                r(0.08, 0.09, 0.14, 0.21, 0.31, 0.44, 0.60, 0.79, 1.00, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                60, 2000,
                                ALL_MELEE_AND_TRIDENT);

                // --- Ailment Chances (all weapons) ---
                registerForTypes("chance_to_burn", "Burn Chance",
                                ModAttributes.CHANCE_TO_BURN,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                30, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_poison", "Poison Chance",
                                ModAttributes.CHANCE_TO_POISON,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                30, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_freeze", "Freeze Chance",
                                ModAttributes.CHANCE_TO_FREEZE,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                30, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_bleed", "Bleed Chance",
                                ModAttributes.CHANCE_TO_BLEED,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                30, 5000,
                                ALL_WEAPONS);

                registerForTypes("chance_to_shock", "Shock Chance",
                                ModAttributes.CHANCE_TO_SHOCK,
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                30, 5000,
                                ALL_WEAPONS);

                // --- Elemental Damages (all weapons) ---

                registerForTypes("fire_damage", "Fire Damage",
                                ModAttributes.FIRE_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                78, 9000,
                                ALL_WEAPONS);

                registerForTypes("cold_damage", "Cold Damage",
                                ModAttributes.COLD_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                81, 5880,
                                ALL_WEAPONS);

                registerForTypes("lightning_damage", "Lightning Damage",
                                ModAttributes.LIGHTNING_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                78, 9000,
                                ALL_WEAPONS);

                registerForTypes("void_damage", "Void Damage",
                                ModAttributes.VOID_DAMAGE,
                                r(2.0, 3.4, 7.4, 14.2, 23.7, 36.0, 50.9, 68.5, 88.9, 112.0),
                                r(3.0, 4.5, 9.0, 16.4, 26.9, 40.3, 56.8, 76.2, 98.6, 124.0),
                                false, Formatting.DARK_PURPLE, CustomStat.AffixType.PREFIX,
                                85, 3000,
                                ALL_WEAPONS);

                // --- Percent Damages (all weapons) ---
                registerForTypes("attack_damage_percent", "Increased Physical Damage",
                                ModAttributes.ATTACK_DAMAGE_PERCENT,
                                r(0.20, 0.22, 0.27, 0.37, 0.50, 0.66, 0.87, 1.11, 1.39, 1.70),
                                r(0.30, 0.32, 0.37, 0.47, 0.59, 0.76, 0.96, 1.20, 1.48, 1.79),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                70, 7000,
                                ALL_WEAPONS);

                // --- Proportional Extra Damage (Runes) ---
                registerForTypes("attack_extra_damage", "Damage as Extra Physical Damage",
                                ModAttributes.PHYSICAL_EXTRA_PERCENT,
                                r(0.08, 0.08, 0.09, 0.11, 0.13, 0.16, 0.20, 0.24, 0.29, 0.35),
                                r(0.10, 0.10, 0.11, 0.13, 0.16, 0.19, 0.23, 0.28, 0.34, 0.40),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 1000,
                                ALL_WEAPONS);

                registerForTypes("fire_extra_damage", "Damage as Extra Fire Damage",
                                ModAttributes.FIRE_EXTRA_PERCENT,
                                r(0.08, 0.08, 0.09, 0.11, 0.13, 0.16, 0.20, 0.24, 0.29, 0.35),
                                r(0.10, 0.10, 0.11, 0.13, 0.16, 0.19, 0.23, 0.28, 0.34, 0.40),
                                true, true, Formatting.RED, CustomStat.AffixType.PREFIX,
                                80, 1000,
                                ALL_WEAPONS);

                registerForTypes("cold_extra_damage", "Damage as Extra Cold Damage",
                                ModAttributes.COLD_EXTRA_PERCENT,
                                r(0.08, 0.08, 0.09, 0.11, 0.13, 0.16, 0.20, 0.24, 0.29, 0.35),
                                r(0.10, 0.10, 0.11, 0.13, 0.16, 0.19, 0.23, 0.28, 0.34, 0.40),
                                true, true, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                80, 1000,
                                ALL_WEAPONS);

                registerForTypes("lightning_extra_damage", "Damage as Extra Lightning Damage",
                                ModAttributes.LIGHTNING_EXTRA_PERCENT,
                                r(0.08, 0.08, 0.09, 0.11, 0.13, 0.16, 0.20, 0.24, 0.29, 0.35),
                                r(0.10, 0.10, 0.11, 0.13, 0.16, 0.19, 0.23, 0.28, 0.34, 0.40),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                80, 1000,
                                ALL_WEAPONS);

                registerForTypes("void_extra_damage", "Damage as Extra Void Damage",
                                ModAttributes.VOID_EXTRA_PERCENT,
                                r(0.08, 0.08, 0.09, 0.11, 0.13, 0.16, 0.20, 0.24, 0.29, 0.35),
                                r(0.10, 0.10, 0.11, 0.13, 0.16, 0.19, 0.23, 0.28, 0.34, 0.40),
                                true, true, Formatting.DARK_PURPLE, CustomStat.AffixType.PREFIX,
                                85, 1000,
                                ALL_WEAPONS);

                // --- Life Steal (melee + trident: one range, bow/crossbow: different) ---
                registerForTypes("life_steal", "Life Steal",
                                ModAttributes.LIFE_STEAL,
                                r(0.01, 0.01, 0.01, 0.02, 0.02, 0.03, 0.04, 0.05, 0.07, 0.08),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                63, 800,
                                ALL_MELEE_AND_TRIDENT);
                registerForTypes("life_steal", "Life Steal",
                                ModAttributes.LIFE_STEAL,
                                r(0.01, 0.01, 0.01, 0.02, 0.02, 0.03, 0.04, 0.05, 0.07, 0.08),
                                r(0.01, 0.01, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.08, 0.10),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                63, 800,
                                ALL_RANGED_WEAPONS);

                // --- Knockback (Sword, Mace, Trident: same; Spear: different; Bow/Crossbow:
                // different) ---
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.10, 0.13, 0.23, 0.40, 0.63, 0.93, 1.30, 1.73, 2.23, 2.80),
                                r(0.20, 0.24, 0.36, 0.57, 0.85, 1.22, 1.67, 2.20, 2.81, 3.50),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 1500,
                                EquipmentType.SWORD, EquipmentType.MACE, EquipmentType.TRIDENT);
                register(new CustomStat("spear_knockback", "Knockback",
                                EquipmentType.SPEAR, ModAttributes.KNOCKBACK,
                                r(0.10, 0.14, 0.26, 0.46, 0.73, 1.09, 1.52, 2.04, 2.63, 3.30),
                                r(0.20, 0.25, 0.39, 0.62, 0.95, 1.37, 1.89, 2.50, 3.20, 4.00),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 1500));
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.15, 0.17, 0.25, 0.37, 0.54, 0.75, 1.02, 1.33, 1.69, 2.10),
                                r(0.20, 0.23, 0.31, 0.44, 0.62, 0.86, 1.16, 1.50, 1.90, 2.35),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 1500,
                                ALL_RANGED_WEAPONS);

                // --- Breaking Range (Tools) ---
                registerForTypes("breaking_range", "Breaking Range",
                                ModAttributes.BREAKING_RANGE,
                                r(0.5, 0.6, 0.8, 1.1, 1.6, 2.3, 3.0, 3.9, 5.0, 6.2),
                                r(0.8, 0.9, 1.1, 1.5, 2.1, 2.9, 3.8, 4.9, 6.1, 7.5),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                30, 2000,
                                EquipmentType.PICKAXE, EquipmentType.AXE, EquipmentType.SHOVEL, EquipmentType.HOE);

                // --- Reload Speed (Bow, Crossbow, Chestplate already registered above) ---
                registerForTypes("reload_speed", "Reload Speed",
                                ModAttributes.RELOAD_SPEED,
                                r(0.05, 0.06, 0.08, 0.13, 0.19, 0.27, 0.36, 0.47, 0.60, 0.75),
                                r(0.10, 0.11, 0.16, 0.23, 0.33, 0.45, 0.61, 0.80, 1.01, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                25, 2000,
                                ALL_RANGED_WEAPONS);

                // --- Projectile Speed (Ranged) ---
                registerForTypes("projectile_speed", "Projectile Speed",
                                ModAttributes.PROJECTILE_SPEED,
                                r(0.10, 0.11, 0.16, 0.23, 0.33, 0.45, 0.61, 0.80, 1.01, 1.25),
                                r(0.15, 0.17, 0.22, 0.30, 0.42, 0.57, 0.75, 0.97, 1.22, 1.50),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.PREFIX,
                                35, 4000,
                                EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.TRIDENT);

                // ===================================================================
                // ELYTRA-ONLY STATS
                // ===================================================================

                register(new CustomStat("elytra_flying_speed", "Flying Speed",
                                EquipmentType.ELYTRA, ModAttributes.FLYING_SPEED,
                                r(0.05, 0.07, 0.12, 0.20, 0.32, 0.47, 0.65, 0.87, 1.12, 1.40),
                                r(0.08, 0.11, 0.18, 0.32, 0.50, 0.73, 1.02, 1.36, 1.76, 2.20),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                40, 300));

                register(new CustomStat("elytra_movement_speed", "Movement Speed",
                                EquipmentType.ELYTRA, ModAttributes.MOVEMENT_SPEED,
                                r(0.80, 0.81, 0.83, 0.86, 0.91, 0.97, 1.04, 1.13, 1.23, 1.35),
                                r(0.80, 0.81, 0.83, 0.86, 0.91, 0.97, 1.04, 1.13, 1.23, 1.35),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                35, 400));

                // --- Enchantment Effectiveness (All Gear) ---
                registerForTypes("enchantment_effectiveness", "Increased Enchantment Effectiveness",
                                ModAttributes.ENCHANTMENT_EFFECTIVENESS,
                                r(0.05, 0.06, 0.09, 0.13, 0.20, 0.28, 0.38, 0.50, 0.64, 0.80),
                                r(0.08, 0.09, 0.13, 0.18, 0.26, 0.36, 0.49, 0.64, 0.81, 1.00),
                                true, true, Formatting.LIGHT_PURPLE, CustomStat.AffixType.SUFFIX,
                                70, 1500,
                                ALL_GEAR);
                // --- Core Attributes (All Gear) ---
                registerForTypes("strength", "Strength",
                                ModAttributes.STRENGTH,
                                r(5.0, 5.6, 7.5, 10.6, 14.9, 20.4, 27.2, 35.2, 44.5, 55.0),
                                r(10.0, 10.6, 12.2, 15.0, 18.9, 23.9, 30.0, 37.2, 45.6, 55.0),
                                false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                30, 8000,
                                ALL_GEAR);

                registerForTypes("dexterity", "Dexterity",
                                ModAttributes.DEXTERITY,
                                r(5.0, 5.6, 7.5, 10.6, 14.9, 20.4, 27.2, 35.2, 44.5, 55.0),
                                r(10.0, 10.6, 12.2, 15.0, 18.9, 23.9, 30.0, 37.2, 45.6, 55.0),
                                false, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                30, 8000,
                                ALL_GEAR);

                registerForTypes("intelligence", "Intelligence",
                                ModAttributes.INTELLIGENCE,
                                r(5.0, 5.6, 7.5, 10.6, 14.9, 20.4, 27.2, 35.2, 44.5, 55.0),
                                r(10.0, 10.6, 12.2, 15.0, 18.9, 23.9, 30.0, 37.2, 45.6, 55.0),
                                false, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                30, 8000,
                                ALL_GEAR);

                // --- Shield Blocking Mechanics (Shield only) ---
                registerForTypes("chance_to_block", "Chance to Block",
                                ModAttributes.CHANCE_TO_BLOCK,
                                r(0.02, 0.02, 0.03, 0.03, 0.05, 0.06, 0.08, 0.10, 0.12, 0.15),
                                r(0.02, 0.02, 0.03, 0.03, 0.05, 0.06, 0.08, 0.10, 0.12, 0.15),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                50, 4000,
                                EquipmentType.SHIELD);

                registerForTypes("block_value", "Block Value",
                                ModAttributes.BLOCK_VALUE,
                                r(10.0, 11.7, 16.9, 25.6, 37.7, 53.2, 72.2, 94.7, 120.6, 150.0),
                                r(20.0, 22.2, 28.9, 40.0, 55.6, 75.6, 100.0, 128.9, 162.2, 200.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                50, 4000,
                                EquipmentType.SHIELD);

                registerForTypes("health_on_block", "Health Gained on Block",
                                ModAttributes.HEALTH_ON_BLOCK,
                                r(2.0, 2.5, 3.9, 6.2, 9.5, 13.7, 18.9, 25.0, 32.0, 40.0),
                                r(4.0, 4.6, 6.3, 9.1, 13.1, 18.2, 24.4, 31.8, 40.3, 50.0),
                                false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                60, 2000,
                                EquipmentType.SHIELD);

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