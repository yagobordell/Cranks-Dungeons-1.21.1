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
            EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.TRIDENT
        };
        private static final EquipmentType[] CRAFTABLE_WEAPONS = {
            EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.BOW, EquipmentType.CROSSBOW
        };
        private static final EquipmentType[] ONE_HANDED_WEAPONS = {
            EquipmentType.SWORD, EquipmentType.MACE
        };
        private static final EquipmentType[] TWO_HANDED_WEAPONS = {
            EquipmentType.SPEAR, EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.TRIDENT
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
        private static java.util.List<CustomStat> registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage, boolean displayAsPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        int level, int weight,
                        EquipmentType... types) {
                java.util.List<CustomStat> registered = new java.util.ArrayList<>();
                for (EquipmentType type : types) {
                        String id = type.name().toLowerCase() + "_" + baseName;
                        CustomStat stat = new CustomStat(id, baseName, displayName, type, attribute,
                                        normalRanges, uncommonRanges, isPercentage, displayAsPercentage, color,
                                        affixType, level, weight);
                        register(stat);
                        registered.add(stat);
                }
                return registered;
        }

        private static java.util.List<CustomStat> registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage, boolean displayAsPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        EquipmentType... types) {
                return registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, displayAsPercentage, color, affixType, 1, 1000, types);
        }

        private static java.util.List<CustomStat> registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        EquipmentType... types) {
                return registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, false, color, affixType, 1, 1000, types);
        }

        private static java.util.List<CustomStat> registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage,
                        Formatting color, CustomStat.AffixType affixType,
                        int level, int weight,
                        EquipmentType... types) {
                return registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, false, color, affixType, level, weight, types);
        }

        private static java.util.List<CustomStat> registerHybridForTypes(
                        String baseName, com.cranks.dungeons.equipment.ArmorBaseType armorBaseType,
                        java.util.List<CustomStat.StatComponent> components,
                        Formatting color, CustomStat.AffixType affixType,
                        int level, int weight,
                        EquipmentType... types) {
                java.util.List<CustomStat> registered = new java.util.ArrayList<>();
                for (EquipmentType type : types) {
                        String id = type.name().toLowerCase() + "_" + baseName;
                        CustomStat stat = new CustomStat(id, baseName, type, components, color, affixType, level, weight)
                                .setArmorBaseType(armorBaseType);
                        register(stat);
                        registered.add(stat);
                }
                return registered;
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
                                1, 1500,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE, EquipmentType.TRIDENT,
                                EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.SHIELD, EquipmentType.ELYTRA);

                // --- Reduced Attribute Requirements (ALL equipment) ---
                registerForTypes("reduced_requirements", "% reduced Attribute Requirements",
                                null,
                                r(0.10, 0.13, 0.16, 0.18, 0.21, 0.24, 0.27, 0.29, 0.32, 0.35),
                                r(0.10, 0.13, 0.16, 0.18, 0.21, 0.24, 0.27, 0.29, 0.32, 0.35),
                                true, true, Formatting.WHITE, CustomStat.AffixType.SUFFIX,
                                60, 5000,
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
                                1, 7000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.INT));

                registerForTypes("energy_shield", "Energy Shield",
                                ModAttributes.ENERGY_SHIELD,
                                r(18.0, 18.9, 21.6, 26.1, 32.4, 40.5, 50.4, 62.2, 75.7, 91.0),
                                r(24.0, 24.9, 27.6, 32.0, 38.2, 46.2, 56.0, 67.6, 80.9, 96.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.ELYTRA)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.INT));

                // --- Max Health (Armor) ---
                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                r(10.0, 20.0, 33.0, 41.0, 60.0, 70.0, 100.0, 140.0, 175.0, 200.0),
                                r(19.0, 32.0, 40.0, 59.0, 69.0, 99.0, 139.0, 174.0, 199.0, 214.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                1, 9000,
                                EquipmentType.LEGGINGS);

                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                r(10.0, 20.0, 33.0, 41.0, 60.0, 70.0, 85.0, 100.0, 115.0, 130.0),
                                r(9.0, 24.0, 39.0, 54.0, 69.0, 84.0, 99.0, 114.0, 129.0, 144.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                1, 9000,
                                EquipmentType.HELMET);

                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                r(10.0, 20.0, 33.0, 41.0, 60.0, 70.0, 85.0, 100.0, 101.0, 115.0),
                                r(9.0, 22.0, 35.0, 48.0, 61.0, 74.0, 87.0, 100.0, 114.0, 129.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                1, 9000,
                                EquipmentType.CHESTPLATE);

                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                r(10.0, 20.0, 33.0, 41.0, 60.0, 70.0, 85.0, 100.0, 101.0, 115.0),
                                r(9.0, 22.0, 35.0, 48.0, 61.0, 74.0, 87.0, 100.0, 114.0, 129.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                1, 9000,
                                EquipmentType.BOOTS);

                registerForTypes("max_health", "Maximum health",
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                r(10.0, 20.0, 33.0, 41.0, 60.0, 70.0, 85.0, 100.0, 101.0, 115.0),
                                r(9.0, 22.0, 35.0, 48.0, 61.0, 74.0, 87.0, 100.0, 114.0, 129.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                70, 11000,
                                EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                registerForTypes("max_mana", "Maximum Mana",
                                ModAttributes.MAX_MANA,
                                r(10.0, 21.0, 31.0, 41.0, 56.0, 66.0, 76.0, 86.0, 99.0, 111.0),
                                r(20.0, 30.0, 40.0, 55.0, 65.0, 75.0, 85.0, 98.0, 110.0, 124.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                60, 9000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS);

                registerForTypes("max_mana", "Maximum Mana",
                                ModAttributes.MAX_MANA,
                                r(10.0, 21.0, 31.0, 41.0, 56.0, 66.0, 76.0, 86.0, 99.0, 111.0),
                                r(20.0, 30.0, 40.0, 55.0, 65.0, 75.0, 85.0, 98.0, 110.0, 149.0),
                                false, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                60, 9000,
                                EquipmentType.HELMET);

                // --- Health Regeneration (Armor) ---
                registerForTypes("health_regen", "Health Regeneration",
                                ModAttributes.LIFE_REGENERATION,
                                r(1.0, 1.1, 1.5, 2.1, 3.0, 4.1, 5.4, 7.0, 8.9, 11.0),
                                r(1.5, 1.7, 2.1, 2.9, 4.0, 5.4, 7.1, 9.1, 11.4, 14.0),
                                false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                81, 4000,
                                ALL_ARMOR);

                // --- Evasion Rating (Armor only) ---
                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(19.0, 21.6, 29.6, 42.8, 61.3, 85.0, 114.1, 148.5, 188.1, 233.0),
                                r(39.0, 41.6, 49.5, 62.6, 80.9, 104.4, 133.2, 167.2, 206.5, 251.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.DEX));

                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(18.0, 29.0, 37.0, 47.0, 68.0, 78.0, 99.0, 114.0, 135.0, 156.0),
                                r(28.0, 36.0, 46.0, 67.0, 77.0, 98.0, 113.0, 134.0, 155.0, 176.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.DEX));

                // --- Armor (Armor + Shield) ---
                registerForTypes("armor", "Armor",
                                EntityAttributes.GENERIC_ARMOR,
                                r(28.0, 30.8, 39.3, 53.4, 73.2, 98.7, 129.8, 166.5, 208.9, 257.0),
                                r(50.0, 52.8, 61.2, 75.1, 94.6, 119.8, 150.4, 186.7, 228.6, 276.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.HELMET, EquipmentType.LEGGINGS, EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                registerForTypes("armor", "Armor",
                                EntityAttributes.GENERIC_ARMOR,
                                r(16.0, 19.4, 25.6, 38.6, 56.3, 78.9, 99.2, 127.4, 138.3, 160.0),
                                r(18.0, 24.9, 37.4, 55.7, 77.6, 98.3, 126.0, 137.0, 159.0, 190.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.CHESTPLATE, EquipmentType.BOOTS)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                registerForTypes("local_armor_percent", "Increased Armor",
                                ModAttributes.ARMOR_PERCENT,
                                r(0.11, 0.12, 0.15, 0.21, 0.29, 0.39, 0.51, 0.65, 0.82, 1.01),
                                r(0.20, 0.21, 0.24, 0.30, 0.38, 0.48, 0.60, 0.74, 0.91, 1.10),
                                true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS, EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                // --- Increased Evasion Rating % (Armor) ---
                registerForTypes("local_evasion_percent", "Increased Evasion Rating",
                                ModAttributes.EVASION_RATING_PERCENT,
                                r(0.11, 0.12, 0.15, 0.21, 0.29, 0.39, 0.51, 0.65, 0.82, 1.01),
                                r(0.20, 0.21, 0.24, 0.30, 0.38, 0.48, 0.60, 0.74, 0.91, 1.10),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.DEX));

                // --- Increased Energy Shield % (Armor + Elytra) ---
                registerForTypes("local_energy_shield_percent", "Increased Energy Shield",
                                ModAttributes.ENERGY_SHIELD_PERCENT,
                                r(0.11, 0.12, 0.15, 0.21, 0.29, 0.39, 0.51, 0.65, 0.82, 1.01),
                                r(0.20, 0.21, 0.24, 0.30, 0.38, 0.48, 0.60, 0.74, 0.91, 1.10),
                                true, true, Formatting.BLUE, CustomStat.AffixType.PREFIX,
                                1, 7000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS, EquipmentType.ELYTRA)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.INT));

                // --- Energy Shield Recharge Rate % (Armor + Elytra) ---
                registerForTypes("energy_shield_recharge_rate_percent", "Increased Energy Shield Recharge Rate",
                                ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT,
                                r(0.02, 0.04, 0.06, 0.09, 0.12, 0.15, 0.18, 0.21, 0.24, 0.27),
                                r(0.04, 0.06, 0.08, 0.11, 0.14, 0.17, 0.20, 0.23, 0.26, 0.30),
                                true, true, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS, EquipmentType.ELYTRA)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.INT));

                // --- Knockback Resistance (Armor + Shield) ---
                registerForTypes("knockback_resistance", "Knockback Resistance",
                                ModAttributes.KNOCKBACK_RESISTANCE,
                                r(0.08, 0.08, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22, 0.26),
                                r(0.12, 0.12, 0.13, 0.15, 0.18, 0.21, 0.24, 0.29, 0.34, 0.40),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                45, 6000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS);

                registerForTypes("knockback_resistance", "Knockback Resistance",
                                ModAttributes.KNOCKBACK_RESISTANCE,
                                r(0.08, 0.08, 0.09, 0.10, 0.12, 0.14, 0.16, 0.19, 0.22, 0.26),
                                r(0.12, 0.12, 0.13, 0.15, 0.18, 0.21, 0.24, 0.29, 0.34, 0.40),
                                true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                74, 6000,
                                EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                // --- Elemental Resistances (Armor + Shield) ---
                EquipmentType[] armorAndShield = {
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD
                };

                // Rebalanced for ~25% T1 Normal (3 rolls to cap 75%)
                registerForTypes("fire_resistance", "Fire Resistance",
                                ModAttributes.FIRE_RESISTANCE,
                                r(0.06, 0.08, 0.10, 0.13, 0.17, 0.21, 0.25, 0.29, 0.34, 0.39),
                                r(0.08, 0.10, 0.13, 0.17, 0.21, 0.25, 0.29, 0.34, 0.39, 0.45),
                                true, true, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                registerForTypes("physical_taken_as_void", "Physical Damage from Hits taken as Void Damage",
                                ModAttributes.PHYSICAL_TAKEN_AS_VOID,
                                r(0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10),
                                r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                                true, Formatting.DARK_PURPLE, CustomStat.AffixType.PREFIX, 85, 0, ALL_ARMOR);

                registerForTypes("cold_resistance", "Cold Resistance",
                                ModAttributes.COLD_RESISTANCE,
                                r(0.06, 0.08, 0.10, 0.13, 0.17, 0.21, 0.25, 0.29, 0.34, 0.39),
                                r(0.08, 0.10, 0.13, 0.17, 0.21, 0.25, 0.29, 0.34, 0.39, 0.45),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                registerForTypes("lightning_resistance", "Lightning Resistance",
                                ModAttributes.LIGHTNING_RESISTANCE,
                                r(0.06, 0.08, 0.10, 0.13, 0.17, 0.21, 0.25, 0.29, 0.34, 0.39),
                                r(0.08, 0.10, 0.13, 0.17, 0.21, 0.25, 0.29, 0.34, 0.39, 0.45),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.SUFFIX,
                                82, 8000,
                                armorAndShield);

                // --- Void Resistance (Lowered) ---
                registerForTypes("void_resistance", "Void Resistance",
                                ModAttributes.VOID_RESISTANCE,
                                r(0.04, 0.05, 0.07, 0.09, 0.11, 0.13, 0.16, 0.19, 0.21, 0.24),
                                r(0.05, 0.07, 0.09, 0.11, 0.13, 0.16, 0.19, 0.21, 0.24, 0.27),
                                true, true, Formatting.DARK_PURPLE, CustomStat.AffixType.SUFFIX,
                                75, 8000,
                                armorAndShield);

                // --- Thorns (Armor) ---
                registerForTypes("thorns", "Physical Thorns Damage",
                                ModAttributes.THORNS_DAMAGE,
                                r(2.0, 3.0, 6.1, 11.2, 18.4, 27.6, 38.9, 52.2, 67.6, 85.0),
                                r(3.0, 4.2, 7.8, 13.8, 22.2, 32.9, 46.1, 61.7, 79.6, 100.0),
                                false, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                10, 3000,
                                ALL_ARMOR);

                registerForTypes("thorns", "Physical Thorns Damage",
                                ModAttributes.THORNS_DAMAGE,
                                r(2.0, 3.0, 6.1, 11.2, 18.4, 27.6, 38.9, 52.2, 67.6, 85.0),
                                r(3.0, 4.2, 7.8, 13.8, 22.2, 32.9, 46.1, 61.7, 79.6, 100.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                74, 7000,
                                EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                // --- Armor Applies to Elemental Damage (Armor Suffix) ---
                registerForTypes("armor_applies_to_elemental", "% of Armour also applies to Elemental Damage",
                                ModAttributes.ARMOR_APPLIES_TO_ELEMENTAL,
                                r(0.14, 0.17, 0.20, 0.23, 0.27, 0.31, 0.35, 0.39, 0.43, 0.47),
                                r(0.16, 0.19, 0.22, 0.26, 0.30, 0.34, 0.38, 0.42, 0.46, 0.50),
                                true, true, Formatting.DARK_AQUA, CustomStat.AffixType.SUFFIX,
                                35, 1000,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS, EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                // --- Experience Bonus (Armor + Elytra) ---
                registerForTypes("experience_bonus", "Experience Bonus",
                                ModAttributes.EXPERIENCE_BONUS,
                                r(0.01, 0.02, 0.03, 0.04, 0.06, 0.08, 0.10, 0.12, 0.13, 0.15),
                                r(0.02, 0.03, 0.04, 0.06, 0.08, 0.10, 0.12, 0.14, 0.16, 0.18),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                30, 6000,
                                ALL_ARMOR);
                // Elytra has different ranges
                register(new CustomStat("elytra_experience_bonus", "Experience Bonus",
                                EquipmentType.ELYTRA, ModAttributes.EXPERIENCE_BONUS,
                                r(0.01, 0.02, 0.03, 0.04, 0.06, 0.08, 0.10, 0.12, 0.13, 0.15),
                                r(0.02, 0.03, 0.04, 0.06, 0.08, 0.10, 0.12, 0.14, 0.16, 0.18),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                30, 1500));

                // --- Item Rarity (Helmets + Boots) ---
                registerForTypes("item_rarity", "Increased Rarity of Items Found",
                                ModAttributes.ITEM_RARITY_PERCENT,
                                r(0.04, 0.05, 0.06, 0.07, 0.09, 0.10, 0.12, 0.13, 0.15, 0.17),
                                r(0.06, 0.07, 0.08, 0.10, 0.11, 0.13, 0.14, 0.15, 0.17, 0.18),
                                true, true, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                30, 1500,
                                EquipmentType.HELMET, EquipmentType.BOOTS, EquipmentType.CHESTPLATE);

                // --- Critical Strike Chance (Armor: Helmet + Chestplate, plus Elytra) ---
                registerForTypes("global_crit_chance", "Critical Strike Chance",
                                ModAttributes.GLOBAL_CRIT_CHANCE_PERCENT,
                                r(0.10, 0.12, 0.15, 0.18, 0.22, 0.26, 0.30, 0.34, 0.37, 0.40),
                                r(0.15, 0.18, 0.22, 0.26, 0.30, 0.34, 0.38, 0.42, 0.45, 0.48),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 3875,
                                EquipmentType.HELMET);
                // Weapons use LOCAL crit chance
                registerForTypes("local_crit_chance", "Critical Strike Chance",
                                null,
                                r(0.20, 0.23, 0.27, 0.32, 0.37, 0.43, 0.49, 0.54, 0.57, 0.60),
                                r(0.25, 0.29, 0.34, 0.39, 0.45, 0.51, 0.57, 0.62, 0.65, 0.68),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 3875,
                                ALL_WEAPONS);

                // Weapons use Critical Strike Multiplier
                registerForTypes("crit_multiplier", "Critical Strike Multiplier",
                                ModAttributes.CRIT_MULTIPLIER,
                                r(0.10, 0.11, 0.13, 0.14, 0.16, 0.17, 0.19, 0.20, 0.22, 0.24),
                                r(0.12, 0.13, 0.15, 0.16, 0.18, 0.19, 0.21, 0.22, 0.24, 0.25),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 3875,
                                ALL_WEAPONS);
                
                registerForTypes("crit_multiplier", "Critical Strike Multiplier",
                                ModAttributes.CRIT_MULTIPLIER,
                                r(0.10, 0.12, 0.14, 0.17, 0.19, 0.22, 0.25, 0.27, 0.30, 0.32),
                                r(0.12, 0.14, 0.17, 0.19, 0.22, 0.25, 0.27, 0.30, 0.32, 0.34),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                59, 3750,
                                EquipmentType.CHESTPLATE);
                // Elytra uses GLOBAL crit chance
                register(new CustomStat("elytra_global_crit_chance", "Critical Strike Chance",
                                EquipmentType.ELYTRA, ModAttributes.GLOBAL_CRIT_CHANCE_PERCENT,
                                r(0.10, 0.12, 0.15, 0.18, 0.22, 0.26, 0.30, 0.34, 0.37, 0.40),
                                r(0.15, 0.18, 0.22, 0.26, 0.30, 0.34, 0.38, 0.42, 0.45, 0.48),
                                true, true, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                73, 1200));

                // ===================================================================
                // CHESTPLATE-ONLY OFFENSIVE STATS
                // ===================================================================
                // Categorized as SUFFIXES per user request

                register(new CustomStat("chestplate_attack_damage", "Physical Damage",
                                EquipmentType.CHESTPLATE, EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                r(1.0, 3.0, 4.0, 5.0, 8.0, 9.0, 12.0, 14.0, 18.0, 22.0),
                                r(2.0, 3.0, 6.0, 8.0, 11.0, 13.0, 17.0, 20.0, 26.0, 32.0),
                                false, false, Formatting.WHITE, CustomStat.AffixType.SUFFIX,
                                30, 7800));

                register(new CustomStat("chestplate_attack_speed", "Attack Speed",
                                EquipmentType.CHESTPLATE, EntityAttributes.GENERIC_ATTACK_SPEED,
                                r(0.05, 0.06, 0.10, 0.16, 0.24, 0.34, 0.47, 0.62, 0.80, 1.00),
                                r(0.08, 0.09, 0.14, 0.21, 0.31, 0.44, 0.60, 0.79, 1.00, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                30, 2000));

                // --- Chestplate elemental damages ---
                register(new CustomStat("chestplate_fire_damage", "Fire Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.FIRE_DAMAGE,
                                r(4.0, 4.0, 10.0, 16.0, 23.0, 28.0, 33.0, 41.0, 48.0, 60.0),
                                r(6.0, 6.0, 15.0, 22.0, 27.0, 32.0, 40.0, 47.0, 59.0, 71.0),
                                false, false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                30, 3900));

                register(new CustomStat("chestplate_cold_damage", "Cold Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.COLD_DAMAGE,
                                r(2.0, 2.0, 5.0, 9.0, 12.0, 15.0, 18.0, 22.0, 25.0, 32.0),
                                r(3.0, 3.0, 8.0, 11.0, 14.0, 17.0, 21.0, 24.0, 31.0, 37.0),
                                false, false, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                30, 3900));

                register(new CustomStat("chestplate_lightning_damage", "Lightning Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.LIGHTNING_DAMAGE,
                                r(3.0, 3.0, 6.0, 10.0, 14.0, 18.0, 21.0, 27.0, 33.0, 37.0),
                                r(3.0, 3.0, 9.0, 13.0, 17.0, 20.0, 26.0, 32.0, 36.0, 45.0),
                                false, false, Formatting.DARK_BLUE, CustomStat.AffixType.SUFFIX,
                                30, 3900));

                register(new CustomStat("chestplate_knockback", "Knockback",
                                EquipmentType.CHESTPLATE, ModAttributes.KNOCKBACK,
                                r(0.10, 0.13, 0.23, 0.40, 0.63, 0.93, 1.30, 1.73, 2.23, 2.80),
                                r(0.20, 0.24, 0.36, 0.57, 0.85, 1.22, 1.67, 2.20, 2.81, 3.50),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 1500));



                // ===================================================================
                // LEGGINGS-ONLY STATS
                // ===================================================================

                register(new CustomStat("leggings_physical_thorns", "Physical Thorns Damage",
                                EquipmentType.LEGGINGS, ModAttributes.THORNS_DAMAGE,
                                r(1.0, 6.0, 13.0, 23.0, 36.0, 51.0, 71.0, 96.0, 126.0, 161.0),
                                r(5.0, 12.0, 22.0, 35.0, 50.0, 70.0, 95.0, 125.0, 160.0, 220.0),
                                false, false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                10, 3000));

                register(new CustomStat("reduced_bleed_duration", "Reduced Bleed Duration on you",
                                EquipmentType.LEGGINGS, ModAttributes.REDUCED_BLEED_DURATION_ON_YOU,
                                r(0.36, 0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58),
                                r(0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58, 0.60),
                                true, false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                76, 2500));

                register(new CustomStat("reduced_burn_duration", "Reduced Burn Duration on you",
                                EquipmentType.LEGGINGS, ModAttributes.REDUCED_BURN_DURATION_ON_YOU,
                                r(0.36, 0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58),
                                r(0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58, 0.60),
                                true, false, Formatting.GOLD, CustomStat.AffixType.SUFFIX,
                                76, 2500));

                register(new CustomStat("reduced_poison_duration", "Reduced Poison Duration on you",
                                EquipmentType.LEGGINGS, ModAttributes.REDUCED_POISON_DURATION_ON_YOU,
                                r(0.36, 0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58),
                                r(0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58, 0.60),
                                true, false, Formatting.DARK_GREEN, CustomStat.AffixType.SUFFIX,
                                76, 2500));

                // ===================================================================
                // BOOTS-ONLY STATS
                // ===================================================================

                register(new CustomStat("boots_movement_speed", "Movement Speed",
                                EquipmentType.BOOTS, ModAttributes.MOVEMENT_SPEED,
                                r(0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.10, 0.12, 0.15, 0.20),
                                r(0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.10, 0.12, 0.15, 0.20),
                                true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                                60, 2500));

                register(new CustomStat("reduced_chill_duration", "Reduced Chill Duration on you",
                                EquipmentType.BOOTS, ModAttributes.REDUCED_CHILL_DURATION_ON_YOU,
                                r(0.36, 0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58),
                                r(0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58, 0.60),
                                true, false, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                75, 2500));

                register(new CustomStat("reduced_freeze_duration", "Reduced Freeze Duration on you",
                                EquipmentType.BOOTS, ModAttributes.REDUCED_FREEZE_DURATION_ON_YOU,
                                r(0.36, 0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58),
                                r(0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58, 0.60),
                                true, false, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                75, 2500));

                register(new CustomStat("reduced_shock_duration", "Reduced Shock Duration on you",
                                EquipmentType.BOOTS, ModAttributes.REDUCED_SHOCK_DURATION_ON_YOU,
                                r(0.36, 0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58),
                                r(0.38, 0.40, 0.43, 0.45, 0.48, 0.50, 0.53, 0.55, 0.58, 0.60),
                                true, false, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                75, 2500));

                // ===================================================================
                // WEAPON STATS (Shared across weapon types)
                // ===================================================================

                // --- Physical Damage (all weapons) ---
                registerForTypes("attack_damage", "Physical Damage",
                                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                r(2.0, 4.0, 7.0, 11.0, 14.0, 18.0, 23.0, 28.0, 36.0, 44.0),
                                r(3.0, 5.0, 11.0, 16.0, 21.0, 26.0, 35.0, 42.0, 53.0, 66.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                60, 6300,
                                ONE_HANDED_WEAPONS);
                registerForTypes("attack_damage", "Physical Damage",
                                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                r(5.0, 10.0, 15.0, 20.0, 25.0, 29.0, 33.0, 39.0, 50.0, 63.0),
                                r(7.0, 15.0, 22.0, 30.0, 37.0, 43.0, 49.0, 59.0, 75.0, 94.0),
                                false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                60, 6300,
                                TWO_HANDED_WEAPONS);

                // --- Attack Speed (melee + trident) ---
                registerForTypes("attack_speed", "Attack Speed",
                                EntityAttributes.GENERIC_ATTACK_SPEED,
                                r(0.05, 0.06, 0.10, 0.16, 0.24, 0.34, 0.47, 0.62, 0.80, 1.00),
                                r(0.08, 0.09, 0.14, 0.21, 0.31, 0.44, 0.60, 0.79, 1.00, 1.25),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                60, 5300,
                                ALL_WEAPONS);

                // --- Ailment Magnitudes (all weapons) ---
                registerForTypes("burn_damage_percent", "Burn Magnitude",
                                ModAttributes.BURN_MAGNITUDE_PERCENT,
                                r(0.10, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.26, 0.28, 0.30),
                                r(0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.29, 0.32, 0.35),
                                true, true, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                30, 0,
                                ALL_WEAPONS);

                registerForTypes("poison_damage_percent", "Poison Magnitude",
                                ModAttributes.POISON_MAGNITUDE_PERCENT,
                                r(0.10, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.26, 0.28, 0.30),
                                r(0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.29, 0.32, 0.35),
                                true, true, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                30, 0,
                                ALL_WEAPONS);

                registerForTypes("freeze_magnitude_percent", "Freeze Magnitude",
                                ModAttributes.FREEZE_MAGNITUDE_PERCENT,
                                r(0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50),
                                r(0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50),
                                true, true, Formatting.AQUA, CustomStat.AffixType.SUFFIX,
                                30, 0,
                                ALL_WEAPONS);

                registerForTypes("bleed_damage_percent", "Bleed Magnitude",
                                ModAttributes.BLEED_MAGNITUDE_PERCENT,
                                r(0.10, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.26, 0.28, 0.30),
                                r(0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.29, 0.32, 0.35),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                30, 0,
                                ALL_WEAPONS);

                registerForTypes("shock_magnitude_percent", "Shock Magnitude",
                                ModAttributes.SHOCK_MAGNITUDE_PERCENT,
                                r(0.10, 0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.26, 0.28, 0.30),
                                r(0.12, 0.14, 0.16, 0.18, 0.21, 0.24, 0.27, 0.29, 0.32, 0.35),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                30, 0,
                                ALL_WEAPONS);

                registerForTypes("electrocute_magnitude_percent", "Electrocute Magnitude",
                                ModAttributes.ELECTROCUTE_MAGNITUDE_PERCENT,
                                r(0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50),
                                r(0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50),
                                true, true, Formatting.YELLOW, CustomStat.AffixType.SUFFIX,
                                30, 0,
                                ALL_WEAPONS);

                // --- Elemental Damages (all weapons) ---

                registerForTypes("local_fire_damage", "Fire Damage",
                                ModAttributes.FIRE_DAMAGE,
                                r(4.0, 13.0, 20.0, 36.0, 55.0, 63.0, 85.0, 111.0, 157.0, 202.0),
                                r(6.0, 19.0, 30.0, 52.0, 60.0, 82.0, 107.0, 152.0, 196.0, 234.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                1, 5880,
                                ONE_HANDED_WEAPONS);
                registerForTypes("local_fire_damage", "Fire Damage",
                                ModAttributes.FIRE_DAMAGE,
                                r(7.0, 19.0, 31.0, 53.0, 80.0, 93.0, 128.0, 168.0, 239.0, 310.0),
                                r(10.0, 27.0, 43.0, 76.0, 88.0, 122.0, 162.0, 231.0, 300.0, 358.0),
                                false, Formatting.RED, CustomStat.AffixType.PREFIX,
                                1, 5880,
                                TWO_HANDED_WEAPONS);

                registerForTypes("local_cold_damage", "Cold Damage",
                                ModAttributes.COLD_DAMAGE,
                                r(3.0, 6.0, 10.0, 17.0, 26.0, 34.0, 47.0, 62.0, 83.0, 110.0),
                                r(4.0, 9.0, 16.0, 24.0, 32.0, 44.0, 59.0, 80.0, 106.0, 123.0),
                                false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                1, 6615,
                                ONE_HANDED_WEAPONS);
                registerForTypes("local_cold_damage", "Cold Damage",
                                ModAttributes.COLD_DAMAGE,
                                r(4.0, 9.0, 15.0, 25.0, 38.0, 49.0, 70.0, 92.0, 126.0, 168.0),
                                r(6.0, 14.0, 23.0, 35.0, 46.0, 66.0, 88.0, 121.0, 161.0, 189.0),
                                false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                1, 6615,
                                TWO_HANDED_WEAPONS);

                registerForTypes("local_lightning_damage", "Lightning Damage",
                                ModAttributes.LIGHTNING_DAMAGE,
                                r(3.0, 7.0, 13.0, 21.0, 32.0, 38.0, 56.0, 74.0, 101.0, 133.0),
                                r(5.0, 10.0, 19.0, 29.0, 37.0, 54.0, 71.0, 97.0, 129.0, 154.0),
                                false, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                1, 8085,
                                ONE_HANDED_WEAPONS);
                registerForTypes("local_lightning_damage", "Lightning Damage",
                                ModAttributes.LIGHTNING_DAMAGE,
                                r(5.0, 10.0, 19.0, 30.0, 45.0, 59.0, 84.0, 112.0, 155.0, 205.0),
                                r(7.0, 16.0, 28.0, 42.0, 56.0, 80.0, 107.0, 149.0, 198.0, 236.0),
                                false, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                1, 8085,
                                TWO_HANDED_WEAPONS);

                // --- Percent Damages (all weapons) ---
                registerForTypes("local_attack_damage_percent", "Increased Physical Damage",
                                ModAttributes.PHYSICAL_DAMAGE_PERCENT,
                                r(0.20, 0.22, 0.27, 0.37, 0.50, 0.66, 0.87, 1.11, 1.39, 1.70),
                                r(0.30, 0.32, 0.37, 0.47, 0.59, 0.76, 0.96, 1.20, 1.48, 1.79),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                70, 3775,
                                ALL_WEAPONS);

                // --- Flat Armor Penetration (craftable weapons) ---
                registerForTypes("armor_penetration", "Armor Penetration",
                                ModAttributes.FLAT_ARMOR_PENETRATION,
                                r(21, 57, 94, 131, 167, 204, 241, 277, 314, 350),
                                r(21, 57, 94, 131, 167, 204, 241, 277, 314, 350),
                                false, false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                76, 5700,
                                CRAFTABLE_WEAPONS);

                // --- Hybrid % Phys + Flat Armor Pen (craftable weapons) ---
                registerHybridForTypes("hybrid_phys_armor_pen", com.cranks.dungeons.equipment.ArmorBaseType.ALL,
                                List.of(
                                                new CustomStat.StatComponent("Increased Physical Damage", ModAttributes.PHYSICAL_DAMAGE_PERCENT,
                                                                r(0.19, 0.25, 0.32, 0.39, 0.45, 0.52, 0.59, 0.65, 0.72, 0.79),
                                                                r(0.24, 0.31, 0.38, 0.44, 0.51, 0.58, 0.64, 0.71, 0.78, 0.85),
                                                                true, true),
                                                new CustomStat.StatComponent("Armor Penetration", ModAttributes.FLAT_ARMOR_PENETRATION,
                                                                r(16, 33, 51, 68, 86, 104, 121, 139, 156, 174),
                                                                r(32, 50, 67, 85, 103, 120, 138, 155, 173, 190),
                                                                false, false)
                                ),
                                Formatting.GRAY, CustomStat.AffixType.PREFIX, 81, 5300,
                                CRAFTABLE_WEAPONS);

                // --- On Kill Stats (all weapons) ---
                registerForTypes("health_on_kill", "Gain # Health per enemy killed",
                                ModAttributes.HEALTH_ON_KILL,
                                r(4.0, 7.0, 10.0, 14.0, 18.0, 23.0, 28.0, 32.0, 36.0, 41.0),
                                r(6.0, 11.0, 17.0, 24.0, 32.0, 41.0, 51.0, 61.0, 72.0, 84.0),
                                false, false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                77, 6000,
                                ALL_WEAPONS);
                registerForTypes("mana_on_kill", "Gain # Mana per enemy killed",
                                ModAttributes.MANA_ON_KILL,
                                r(3.0, 6.0, 9.0, 13.0, 17.0, 22.0, 27.0, 32.0, 37.0, 42.0),
                                r(5.0, 8.0, 12.0, 16.0, 21.0, 26.0, 31.0, 36.0, 41.0, 45.0),
                                false, false, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                77, 6000,
                                ALL_WEAPONS);

                // --- Proportional Extra Damage (Runes) ---
                registerForTypes("attack_extra_damage", "Damage as Extra Physical Damage",
                                ModAttributes.PHYSICAL_EXTRA_PERCENT,
                                r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                                r(0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 0,
                                ONE_HANDED_WEAPONS);
                registerForTypes("attack_extra_damage", "Damage as Extra Physical Damage",
                                ModAttributes.PHYSICAL_EXTRA_PERCENT,
                                r(0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25),
                                r(0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33),
                                true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                                75, 0,
                                TWO_HANDED_WEAPONS);

                registerForTypes("fire_extra_damage", "Damage as Extra Fire Damage",
                                ModAttributes.FIRE_EXTRA_PERCENT,
                                r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                                r(0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20),
                                true, true, Formatting.RED, CustomStat.AffixType.PREFIX,
                                80, 0,
                                ONE_HANDED_WEAPONS);
                registerForTypes("fire_extra_damage", "Damage as Extra Fire Damage",
                                ModAttributes.FIRE_EXTRA_PERCENT,
                                r(0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25),
                                r(0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33),
                                true, true, Formatting.RED, CustomStat.AffixType.PREFIX,
                                80, 0,
                                TWO_HANDED_WEAPONS);

                registerForTypes("cold_extra_damage", "Damage as Extra Cold Damage",
                                ModAttributes.COLD_EXTRA_PERCENT,
                                r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                                r(0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20),
                                true, true, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                80, 0,
                                ONE_HANDED_WEAPONS);
                registerForTypes("cold_extra_damage", "Damage as Extra Cold Damage",
                                ModAttributes.COLD_EXTRA_PERCENT,
                                r(0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25),
                                r(0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33),
                                true, true, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                                80, 0,
                                TWO_HANDED_WEAPONS);

                registerForTypes("lightning_extra_damage", "Damage as Extra Lightning Damage",
                                ModAttributes.LIGHTNING_EXTRA_PERCENT,
                                r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                                r(0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                80, 0,
                                ONE_HANDED_WEAPONS);
                registerForTypes("lightning_extra_damage", "Damage as Extra Lightning Damage",
                                ModAttributes.LIGHTNING_EXTRA_PERCENT,
                                r(0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25),
                                r(0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33, 0.33),
                                true, true, Formatting.DARK_BLUE, CustomStat.AffixType.PREFIX,
                                80, 0,
                                TWO_HANDED_WEAPONS);



                // --- Life Steal (Damage Leeched as Life) ---
                registerForTypes("physical_damage_leeched_as_life", "Leeches # of Physical Damage as Health",
                                ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE,
                                r(0.015, 0.018, 0.021, 0.024, 0.027, 0.03, 0.032, 0.035, 0.037, 0.04),
                                r(0.02, 0.022, 0.025, 0.028, 0.031, 0.034, 0.037, 0.040, 0.043, 0.045),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                63, 4000,
                                ALL_WEAPONS);
                registerForTypes("physical_damage_leeched_as_life", "Leeches # of Physical Damage as Health",
                                ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE,
                                r(0.005, 0.006, 0.007, 0.008, 0.009, 0.010, 0.011, 0.012, 0.013, 0.015),
                                r(0.007, 0.008, 0.009, 0.010, 0.012, 0.013, 0.014, 0.015, 0.016, 0.018),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                63, 4000,
                                ALL_RANGED_WEAPONS);

                registerForTypes("physical_damage_leeched_as_mana", "Leeches # of Physical Damage as Mana",
                                ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_MANA,
                                r(0.011, 0.013, 0.015, 0.018, 0.020, 0.022, 0.024, 0.026, 0.027, 0.030),
                                r(0.015, 0.016, 0.018, 0.021, 0.023, 0.025, 0.027, 0.030, 0.032, 0.033),
                                true, true, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                63, 4000,
                                ALL_WEAPONS);
                registerForTypes("physical_damage_leeched_as_mana", "Leeches # of Physical Damage as Mana",
                                ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_MANA,
                                r(0.003, 0.004, 0.005, 0.006, 0.006, 0.007, 0.008, 0.009, 0.009, 0.011),
                                r(0.005, 0.006, 0.006, 0.007, 0.009, 0.009, 0.010, 0.011, 0.012, 0.013),
                                true, true, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                63, 4000,
                                ALL_RANGED_WEAPONS);

                registerForTypes("physical_damage_leeched_as_life", "Leeches # of Physical Damage as Health",
                                ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE,
                                r(0.015, 0.018, 0.021, 0.024, 0.027, 0.03, 0.032, 0.035, 0.037, 0.04),
                                r(0.02, 0.022, 0.025, 0.028, 0.031, 0.034, 0.037, 0.040, 0.043, 0.045),
                                true, true, Formatting.DARK_RED, CustomStat.AffixType.SUFFIX,
                                65, 4000,
                                EquipmentType.CHESTPLATE);
                registerForTypes("physical_damage_leeched_as_mana", "Leeches # of Physical Damage as Mana",
                                ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_MANA,
                                r(0.011, 0.013, 0.015, 0.018, 0.020, 0.022, 0.024, 0.026, 0.027, 0.030),
                                r(0.015, 0.016, 0.018, 0.021, 0.023, 0.025, 0.027, 0.030, 0.032, 0.033),
                                true, true, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                65, 4000,
                                EquipmentType.CHESTPLATE);

                // --- Knockback (Sword, Mace, Trident: same; Spear: different; Bow/Crossbow:
                // different) ---
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.10, 0.13, 0.23, 0.40, 0.63, 0.93, 1.30, 1.73, 2.23, 2.80),
                                r(0.20, 0.24, 0.36, 0.57, 0.85, 1.22, 1.67, 2.20, 2.81, 3.50),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 6000,
                                EquipmentType.SWORD, EquipmentType.MACE, EquipmentType.TRIDENT);
                register(new CustomStat("spear_knockback", "Knockback",
                                EquipmentType.SPEAR, ModAttributes.KNOCKBACK,
                                r(0.10, 0.14, 0.26, 0.46, 0.73, 1.09, 1.52, 2.04, 2.63, 3.30),
                                r(0.20, 0.25, 0.39, 0.62, 0.95, 1.37, 1.89, 2.50, 3.20, 4.00),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 6000));
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.15, 0.17, 0.25, 0.37, 0.54, 0.75, 1.02, 1.33, 1.69, 2.10),
                                r(0.20, 0.23, 0.31, 0.44, 0.62, 0.86, 1.16, 1.50, 1.90, 2.35),
                                true, true, Formatting.GRAY, CustomStat.AffixType.SUFFIX,
                                15, 6000,
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


                register(new CustomStat("elytra_movement_speed", "Movement Speed",
                                EquipmentType.ELYTRA, ModAttributes.MOVEMENT_SPEED,
                                r(0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.10, 0.12, 0.15, 0.20),
                                r(0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.10, 0.12, 0.15, 0.20),
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
                // --- Core Attributes (Armor only) ---
                registerForTypes("strength", "Strength",
                                ModAttributes.STRENGTH,
                                r(5.0, 5.6, 7.5, 10.6, 14.9, 20.4, 27.2, 35.2, 44.5, 55.0),
                                r(10.0, 10.6, 12.2, 15.0, 18.9, 23.9, 30.0, 37.2, 45.6, 55.0),
                                false, Formatting.RED, CustomStat.AffixType.SUFFIX,
                                30, 9000,
                                ALL_ARMOR)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                registerForTypes("dexterity", "Dexterity",
                                ModAttributes.DEXTERITY,
                                r(5.0, 5.6, 7.5, 10.6, 14.9, 20.4, 27.2, 35.2, 44.5, 55.0),
                                r(10.0, 10.6, 12.2, 15.0, 18.9, 23.9, 30.0, 37.2, 45.6, 55.0),
                                false, Formatting.GREEN, CustomStat.AffixType.SUFFIX,
                                30, 9000,
                                ALL_ARMOR)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.DEX));

                registerForTypes("intelligence", "Intelligence",
                                ModAttributes.INTELLIGENCE,
                                r(5.0, 5.6, 7.5, 10.6, 14.9, 20.4, 27.2, 35.2, 44.5, 55.0),
                                r(10.0, 10.6, 12.2, 15.0, 18.9, 23.9, 30.0, 37.2, 45.6, 55.0),
                                false, Formatting.BLUE, CustomStat.AffixType.SUFFIX,
                                30, 9000,
                                ALL_ARMOR)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.INT));

                // --- Shield Blocking Mechanics (Shield only) ---
                registerForTypes("block_value", "Block Value",
                                ModAttributes.BLOCK_VALUE,
                                r(10.0, 30.0, 50.0, 70.0, 90.0, 110.0, 130.0, 150.0, 170.0, 189.0),
                                r(15.0, 35.0, 55.0, 75.0, 95.0, 115.0, 135.0, 155.0, 175.0, 195.0),
                                false, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                                65, 3000,
                                EquipmentType.SHIELD)
                        .forEach(s -> s.setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType.STR));

                // --- Hybrid Stats ---
                registerHybridForTypes("hybrid_dex_eva_life", com.cranks.dungeons.equipment.ArmorBaseType.DEX,
                        List.of(
                                new CustomStat.StatComponent("Increased Evasion Rating", ModAttributes.EVASION_RATING_PERCENT, r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), r(0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6), true, true),
                                new CustomStat.StatComponent("Maximum Health", EntityAttributes.GENERIC_MAX_HEALTH, r(18.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0), r(20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 6000, ALL_ARMOR);

                registerHybridForTypes("hybrid_dex_int_eva_es_pct", com.cranks.dungeons.equipment.ArmorBaseType.DEX_INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Evasion and Energy Shield", List.of(ModAttributes.EVASION_RATING_PERCENT, ModAttributes.ENERGY_SHIELD_PERCENT), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), r(0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6), true, true)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, ALL_ARMOR);

                registerHybridForTypes("hybrid_dex_int_eva_es_pct_life", com.cranks.dungeons.equipment.ArmorBaseType.DEX_INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Evasion and Energy Shield", List.of(ModAttributes.EVASION_RATING_PERCENT, ModAttributes.ENERGY_SHIELD_PERCENT), r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Maximum Health", EntityAttributes.GENERIC_MAX_HEALTH, r(18.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0), r(20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 6000, ALL_ARMOR);
                        
                registerHybridForTypes("hybrid_dex_int_eva_es_flat", com.cranks.dungeons.equipment.ArmorBaseType.DEX_INT,
                        List.of(
                                new CustomStat.StatComponent("Evasion and Energy Shield", List.of(ModAttributes.EVASION_RATING, ModAttributes.ENERGY_SHIELD), r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_armor_life", com.cranks.dungeons.equipment.ArmorBaseType.STR,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor", ModAttributes.ARMOR_PERCENT, r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), r(0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6), true, true),
                                new CustomStat.StatComponent("Maximum Health", EntityAttributes.GENERIC_MAX_HEALTH, r(18.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0), r(20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0), false, false)
                        ),
                        Formatting.RED, CustomStat.AffixType.PREFIX, 1, 6000, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_int_armor_es_pct", com.cranks.dungeons.equipment.ArmorBaseType.STR_INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor and Energy Shield", List.of(ModAttributes.ARMOR_PERCENT, ModAttributes.ENERGY_SHIELD_PERCENT), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), r(0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6), true, true)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_int_armor_es_pct_life", com.cranks.dungeons.equipment.ArmorBaseType.STR_INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor and Energy Shield", List.of(ModAttributes.ARMOR_PERCENT, ModAttributes.ENERGY_SHIELD_PERCENT), r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Maximum Health", EntityAttributes.GENERIC_MAX_HEALTH, r(18.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0), r(20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 6000, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_int_armor_es_flat", com.cranks.dungeons.equipment.ArmorBaseType.STR_INT,
                        List.of(
                                new CustomStat.StatComponent("Armor and Energy Shield", List.of(EntityAttributes.GENERIC_ARMOR, ModAttributes.ENERGY_SHIELD), r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, ALL_ARMOR);

                registerHybridForTypes("hybrid_int_es_life", com.cranks.dungeons.equipment.ArmorBaseType.INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Energy Shield", ModAttributes.ENERGY_SHIELD_PERCENT, r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), r(0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6), true, true),
                                new CustomStat.StatComponent("Maximum Health", EntityAttributes.GENERIC_MAX_HEALTH, r(18.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0), r(20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0), false, false)
                        ),
                        Formatting.BLUE, CustomStat.AffixType.PREFIX, 1, 6000, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_dex_armor_eva_pct", com.cranks.dungeons.equipment.ArmorBaseType.STR_DEX,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor and Evasion", List.of(ModAttributes.ARMOR_PERCENT, ModAttributes.EVASION_RATING_PERCENT), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), r(0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6), true, true)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_dex_armor_eva_pct_life", com.cranks.dungeons.equipment.ArmorBaseType.STR_DEX,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor and Evasion", List.of(ModAttributes.ARMOR_PERCENT, ModAttributes.EVASION_RATING_PERCENT), r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Maximum Health", EntityAttributes.GENERIC_MAX_HEALTH, r(18.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0), r(20.0, 22.0, 24.0, 26.0, 28.0, 30.0, 32.0, 34.0, 36.0, 38.0), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 6000, ALL_ARMOR);

                registerHybridForTypes("hybrid_str_dex_armor_eva_flat", com.cranks.dungeons.equipment.ArmorBaseType.STR_DEX,
                        List.of(
                                new CustomStat.StatComponent("Armor and Evasion Rating", List.of(EntityAttributes.GENERIC_ARMOR, ModAttributes.EVASION_RATING), r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, ALL_ARMOR);

                // --- Leggings Exclusives ---
                registerHybridForTypes("leggings_hybrid_dex_int", com.cranks.dungeons.equipment.ArmorBaseType.DEX_INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Evasion and Energy Shield", List.of(ModAttributes.EVASION_RATING_PERCENT, ModAttributes.ENERGY_SHIELD_PERCENT), r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Evasion and Energy Shield", List.of(ModAttributes.EVASION_RATING, ModAttributes.ENERGY_SHIELD), r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, EquipmentType.LEGGINGS);

                registerHybridForTypes("leggings_hybrid_str_dex", com.cranks.dungeons.equipment.ArmorBaseType.STR_DEX,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor and Evasion", List.of(ModAttributes.ARMOR_PERCENT, ModAttributes.EVASION_RATING_PERCENT), r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Armor and Evasion Rating", List.of(EntityAttributes.GENERIC_ARMOR, ModAttributes.EVASION_RATING), r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, EquipmentType.LEGGINGS);

                registerHybridForTypes("leggings_hybrid_str_int", com.cranks.dungeons.equipment.ArmorBaseType.STR_INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor and Energy Shield", List.of(ModAttributes.ARMOR_PERCENT, ModAttributes.ENERGY_SHIELD_PERCENT), r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Armor and Energy Shield", List.of(EntityAttributes.GENERIC_ARMOR, ModAttributes.ENERGY_SHIELD), r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 3500, EquipmentType.LEGGINGS);

                registerHybridForTypes("leggings_hybrid_str", com.cranks.dungeons.equipment.ArmorBaseType.STR,
                        List.of(
                                new CustomStat.StatComponent("Increased Armor", ModAttributes.ARMOR_PERCENT, r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Armor", EntityAttributes.GENERIC_ARMOR, r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.RED, CustomStat.AffixType.PREFIX, 1, 6000, EquipmentType.LEGGINGS);

                registerHybridForTypes("leggings_hybrid_dex", com.cranks.dungeons.equipment.ArmorBaseType.DEX,
                        List.of(
                                new CustomStat.StatComponent("Increased Evasion Rating", ModAttributes.EVASION_RATING_PERCENT, r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Evasion Rating", ModAttributes.EVASION_RATING, r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.GRAY, CustomStat.AffixType.PREFIX, 1, 6000, EquipmentType.LEGGINGS);

                registerHybridForTypes("leggings_hybrid_int", com.cranks.dungeons.equipment.ArmorBaseType.INT,
                        List.of(
                                new CustomStat.StatComponent("Increased Energy Shield", ModAttributes.ENERGY_SHIELD_PERCENT, r(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5), r(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55), true, true),
                                new CustomStat.StatComponent("Maximum Energy Shield", ModAttributes.ENERGY_SHIELD, r(10, 15, 20, 25, 30, 35, 40, 45, 50, 55), r(15, 20, 25, 30, 35, 40, 45, 50, 55, 60), false, false)
                        ),
                        Formatting.BLUE, CustomStat.AffixType.PREFIX, 1, 6000, EquipmentType.LEGGINGS);
                // ===================================================================
                // UNIQUE ITEMS EXCLUSIVE STATS (Elytra, Trident, Mace)
                // ===================================================================

                // Elytra specific unique stats
                register(new CustomStat("elytra_flying_speed", "Gliding Speed",
                        EquipmentType.ELYTRA, ModAttributes.FLYING_SPEED,
                        r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                        r(0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35),
                        true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                        1, 0)); // Weight 0 so it doesn't roll randomly
                        
                // Trident specific unique stats
                register(new CustomStat("trident_return", "Returns when thrown",
                        EquipmentType.TRIDENT, ModAttributes.TRIDENT_RETURN,
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        false, false, Formatting.GOLD, CustomStat.AffixType.PREFIX,
                        1, 0));
                        
                register(new CustomStat("trident_chance_to_shock", "Increased Chance to Shock",
                        EquipmentType.TRIDENT, ModAttributes.CHANCE_TO_SHOCK,
                        r(0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10),
                        r(0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20, 0.20),
                        true, true, Formatting.YELLOW, CustomStat.AffixType.PREFIX,
                        1, 0));
                
                // Mace specific unique stats
                register(new CustomStat("mace_falling_damage", "Damage per Fallen Block",
                        EquipmentType.MACE, ModAttributes.MACE_FALLING_DAMAGE,
                        r(0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10),
                        r(0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30),
                        true, true, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                        1, 0));
                        
                register(new CustomStat("mace_wind_bounce", "Bounces on Hit",
                        EquipmentType.MACE, ModAttributes.MACE_WIND_BOUNCE,
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        false, false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                        1, 0));

                register(new CustomStat("mace_armor_penetration", "Armor Penetration",
                        EquipmentType.MACE, ModAttributes.FLAT_ARMOR_PENETRATION,
                        r(86, 86, 86, 86, 86, 86, 86, 86, 86, 86),
                        r(103, 103, 103, 103, 103, 103, 103, 103, 103, 103),
                        false, false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                        1, 0));

                // Turtle Helmet specific unique stats
                register(new CustomStat("turtle_helmet_respiration", "Extended Breathing Time",
                        EquipmentType.HELMET, EntityAttributes.GENERIC_OXYGEN_BONUS,
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        false, false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                        1, 0));

                register(new CustomStat("turtle_helmet_aqua_affinity", "Increases Underwater Mining Speed",
                        EquipmentType.HELMET, EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED,
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        r(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                        false, false, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                        1, 0));

                register(new CustomStat("turtle_helmet_cold_resistance", "Cold Resistance",
                        EquipmentType.HELMET, ModAttributes.COLD_RESISTANCE,
                        r(0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30),
                        r(0.45, 0.45, 0.45, 0.45, 0.45, 0.45, 0.45, 0.45, 0.45, 0.45),
                        true, true, Formatting.AQUA, CustomStat.AffixType.PREFIX,
                        1, 0));

                register(new CustomStat("turtle_helmet_movement_speed", "Movement Speed",
                        EquipmentType.HELMET, ModAttributes.MOVEMENT_SPEED,
                        r(-0.05, -0.05, -0.05, -0.05, -0.05, -0.05, -0.05, -0.05, -0.05, -0.05),
                        r(-0.15, -0.15, -0.15, -0.15, -0.15, -0.15, -0.15, -0.15, -0.15, -0.15),
                        true, true, Formatting.GREEN, CustomStat.AffixType.PREFIX,
                        1, 0));

                register(new CustomStat("turtle_helmet_armor", "Armor",
                        EquipmentType.HELMET, EntityAttributes.GENERIC_ARMOR,
                        r(50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0),
                        r(90.0, 90.0, 90.0, 90.0, 90.0, 90.0, 90.0, 90.0, 90.0, 90.0),
                        false, false, Formatting.WHITE, CustomStat.AffixType.PREFIX,
                        1, 0));

                register(new CustomStat("turtle_helmet_local_armor_percent", "Increased Armor",
                        EquipmentType.HELMET, ModAttributes.ARMOR_PERCENT,
                        r(0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15),
                        r(0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30),
                        true, true, Formatting.GRAY, CustomStat.AffixType.PREFIX,
                        1, 0));

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

        public static class StatAndTier {
                public final CustomStat stat;
                public final int tier;
                public StatAndTier(CustomStat stat, int tier) {
                        this.stat = stat;
                        this.tier = tier;
                }
        }

        public static StatAndTier getRandomStatForEquipmentType(EquipmentType equipmentType) {
                return getRandomStatForEquipmentType(equipmentType, com.cranks.dungeons.equipment.ArmorBaseType.ANY, 1, 100, 1);
        }

        public static StatAndTier getRandomStatForEquipmentType(EquipmentType equipmentType, com.cranks.dungeons.equipment.ArmorBaseType armorBase, int minLevel,
                        int maxLevel, int bestAllowedTier) {
                List<CustomStat> stats = getStatsByEquipmentType(equipmentType).stream()
                                .filter(stat -> stat.getLevel() >= minLevel && stat.getLevel() <= maxLevel)
                                .filter(stat -> armorBase.canRoll(stat.getArmorBaseType()))
                                .filter(StatRegistry::isRandomlyRollable)
                                .collect(Collectors.toList());
                return getRandomWeightedStatAndTier(stats, bestAllowedTier);
        }

        public static StatAndTier getRandomStatForEquipmentType(EquipmentType equipmentType,
                        CustomStat.AffixType affixType) {
                return getRandomStatForEquipmentType(equipmentType, com.cranks.dungeons.equipment.ArmorBaseType.ANY, affixType, 1, 100, 1);
        }

        public static StatAndTier getRandomStatForEquipmentType(EquipmentType equipmentType, com.cranks.dungeons.equipment.ArmorBaseType armorBase,
                        CustomStat.AffixType affixType, int minLevel, int maxLevel, int bestAllowedTier) {
                List<CustomStat> stats = getStatsByEquipmentType(equipmentType).stream()
                                .filter(stat -> stat.getAffixType() == affixType)
                                .filter(stat -> stat.getLevel() >= minLevel && stat.getLevel() <= maxLevel)
                                .filter(stat -> armorBase.canRoll(stat.getArmorBaseType()))
                                .filter(StatRegistry::isRandomlyRollable)
                                .collect(Collectors.toList());
                return getRandomWeightedStatAndTier(stats, bestAllowedTier);
        }

        /**
         * Whether an affix belongs to the ordinary random-tome and mob-equipment pool.
         * Zero-weight definitions are reserved for targeted or fixed Unique behavior.
         */
        public static boolean isRandomlyRollable(CustomStat stat) {
                if (stat == null || stat.getWeight() <= 0) return false;
                String id = stat.getId();
                return !id.contains("experience_bonus") && !id.contains("physical_taken_as_void");
        }

        private static StatAndTier getRandomWeightedStatAndTier(List<CustomStat> stats, int bestAllowedTier) {
                if (stats.isEmpty())
                        return null;

                int totalWeight = 0;
                for (CustomStat stat : stats) {
                        for (int t = bestAllowedTier; t <= 10; t++) {
                                totalWeight += stat.getTierWeight(t);
                        }
                }

                if (totalWeight <= 0) {
                        CustomStat randomStat = stats.get(RandomUtil.nextInt(stats.size()));
                        int randomTier = RandomUtil.nextInt(10 - bestAllowedTier + 1) + bestAllowedTier;
                        return new StatAndTier(randomStat, randomTier);
                }

                int roll = RandomUtil.nextInt(totalWeight);
                int currentWeight = 0;
                for (CustomStat stat : stats) {
                        for (int t = bestAllowedTier; t <= 10; t++) {
                                currentWeight += stat.getTierWeight(t);
                                if (roll < currentWeight) {
                                        return new StatAndTier(stat, t);
                                }
                        }
                }
                return null;
        }

        public static Collection<String> getAllStatIds() {
                return STATS.keySet();
        }
}
