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

        private static final EquipmentType[] ALL_TOOLS = {
                        EquipmentType.PICKAXE, EquipmentType.AXE, EquipmentType.SHOVEL, EquipmentType.HOE
        };

        // ========== Shorthand helpers ==========

        private static double[] r(double t1, double t2, double t3, double t4, double t5) {
                return new double[] { t1, t2, t3, t4, t5 };
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
                        Formatting color,
                        EquipmentType... types) {
                for (EquipmentType type : types) {
                        String id = type.name().toLowerCase() + "_" + baseName;
                        register(new CustomStat(id, displayName, type, attribute,
                                        normalRanges, uncommonRanges, isPercentage, displayAsPercentage, color));
                }
        }

        /**
         * Overload for non-percentage stats (displayAsPercentage defaults to false).
         */
        private static void registerForTypes(String baseName, String displayName,
                        RegistryEntry<EntityAttribute> attribute,
                        double[] normalRanges, double[] uncommonRanges,
                        boolean isPercentage,
                        Formatting color,
                        EquipmentType... types) {
                registerForTypes(baseName, displayName, attribute, normalRanges, uncommonRanges,
                                isPercentage, false, color, types);
        }

        // ========== Registration ==========

        public static void registerStats() {
                // ===================================================================
                // SHARED STATS: Registered across multiple equipment type groups
                // ===================================================================

                // --- Durability Bonus (ALL equipment) ---
                registerForTypes("durability_bonus", "Durability Bonus",
                                ModAttributes.DURABILITY_BONUS,
                                r(0.10, 0.18, 0.28, 0.40, 0.56), r(0.18, 0.26, 0.36, 0.55, 0.76),
                                true, true, Formatting.WHITE,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SWORD, EquipmentType.SPEAR, EquipmentType.MACE, EquipmentType.TRIDENT,
                                EquipmentType.BOW, EquipmentType.CROSSBOW, EquipmentType.SHIELD, EquipmentType.ELYTRA,
                                EquipmentType.PICKAXE);
                // Axe, Shovel, Hoe have different color (BLUE instead of WHITE)
                registerForTypes("durability_bonus", "Durability Bonus",
                                ModAttributes.DURABILITY_BONUS,
                                r(0.10, 0.18, 0.28, 0.40, 0.56), r(0.18, 0.26, 0.36, 0.55, 0.76),
                                true, true, Formatting.BLUE,
                                EquipmentType.AXE, EquipmentType.SHOVEL, EquipmentType.HOE);

                // --- Energy Shield (Armor + Elytra) ---
                registerForTypes("energy_shield", "Energy Shield",
                                ModAttributes.ENERGY_SHIELD,
                                r(1.0, 2.0, 3.0, 5.0, 7.0), r(2.0, 3.0, 5.0, 7.0, 10.0),
                                false, Formatting.BLUE,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.ELYTRA);

                // --- Max Health (Armor) ---
                registerForTypes("max_health", "Max Health",
                                EntityAttributes.MAX_HEALTH,
                                r(1.0, 2.0, 3.0, 5.0, 7.0), r(2.0, 3.0, 5.0, 7.0, 10.0),
                                false, Formatting.DARK_RED,
                                ALL_ARMOR);

                // --- Health Regeneration (Armor + Elytra) ---
                registerForTypes("life_regeneration", "Health Regeneration",
                                ModAttributes.LIFE_REGENERATION,
                                r(0.1, 0.25, 0.5, 0.8, 1.2), r(0.2, 0.45, 0.75, 1.2, 2.0),
                                false, Formatting.DARK_RED,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.ELYTRA);

                // --- Evasion Rating (Armor only) ---
                registerForTypes("evasion_rating", "Evasion Rating",
                                ModAttributes.EVASION_RATING,
                                r(10.0, 25.0, 50.0, 100.0, 150.0), r(20.0, 40.0, 80.0, 140.0, 200.0),
                                false, Formatting.GRAY,
                                ALL_ARMOR);

                // --- Armor (Armor + Shield) ---
                registerForTypes("armor", "Armor",
                                EntityAttributes.ARMOR,
                                r(10.0, 30.0, 60.0, 100.0, 160.0), r(20.0, 50.0, 90.0, 150.0, 240.0),
                                false, Formatting.WHITE,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD);

                // --- Armor Toughness (Armor + Shield) ---
                registerForTypes("armor_toughness", "Armor Toughness",
                                EntityAttributes.ARMOR_TOUGHNESS,
                                r(6.0, 16.0, 30.0, 50.0, 80.0), r(12.0, 28.0, 48.0, 80.0, 120.0),
                                false, Formatting.WHITE,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD);

                // --- Knockback Resistance (Armor + Shield) ---
                registerForTypes("knockback_resistance", "Knockback Resistance",
                                ModAttributes.KNOCKBACK_RESISTANCE,
                                r(0.05, 0.10, 0.15, 0.20, 0.25), r(0.10, 0.15, 0.20, 0.25, 0.35),
                                true, true, Formatting.GRAY,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD);

                // --- Elemental Resistances (Armor + Shield) ---
                EquipmentType[] armorAndShield = {
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS,
                                EquipmentType.BOOTS,
                                EquipmentType.SHIELD
                };

                registerForTypes("fire_resistance", "Fire Resistance",
                                ModAttributes.FIRE_RESISTANCE,
                                r(0.15, 0.19, 0.23, 0.26, 0.30), r(0.19, 0.23, 0.26, 0.30, 0.38),
                                true, true, Formatting.RED,
                                armorAndShield);

                registerForTypes("cold_resistance", "Cold Resistance",
                                ModAttributes.COLD_RESISTANCE,
                                r(0.15, 0.19, 0.23, 0.26, 0.30), r(0.19, 0.23, 0.26, 0.30, 0.38),
                                true, true, Formatting.AQUA,
                                armorAndShield);

                registerForTypes("lightning_resistance", "Lightning Resistance",
                                ModAttributes.LIGHTNING_RESISTANCE,
                                r(0.15, 0.19, 0.23, 0.26, 0.30), r(0.19, 0.23, 0.26, 0.30, 0.38),
                                true, true, Formatting.DARK_BLUE,
                                armorAndShield);

                registerForTypes("void_resistance", "Void Resistance",
                                ModAttributes.VOID_RESISTANCE,
                                r(0.05, 0.1, 0.15, 0.2, 0.25), r(0.1, 0.15, 0.2, 0.25, 0.3),
                                true, true, Formatting.DARK_PURPLE,
                                armorAndShield);

                // --- Experience Bonus (Armor + Elytra) ---
                registerForTypes("experience_bonus", "Experience Bonus",
                                ModAttributes.EXPERIENCE_BONUS,
                                r(0.05, 0.12, 0.20, 0.38, 0.43), r(0.10, 0.18, 0.28, 0.42, 0.69),
                                true, true, Formatting.DARK_GREEN,
                                ALL_ARMOR);
                // Elytra has different ranges
                register(new CustomStat("elytra_experience_bonus", "Experience Bonus",
                                EquipmentType.ELYTRA, ModAttributes.EXPERIENCE_BONUS,
                                r(0.30, 0.40, 0.50, 0.60, 0.70), r(0.40, 0.50, 0.60, 0.70, 0.80),
                                true, true, Formatting.DARK_GREEN));

                // --- Luck (Armor + Elytra) ---
                registerForTypes("luck", "Luck",
                                ModAttributes.LUCK,
                                r(0.25, 0.75, 1.5, 2.5, 4.0), r(0.5, 1.25, 2.25, 3.75, 6.0),
                                false, false, Formatting.DARK_GREEN,
                                ALL_ARMOR);
                // Elytra has different ranges
                register(new CustomStat("elytra_luck", "Luck",
                                EquipmentType.ELYTRA, ModAttributes.LUCK,
                                r(2.25, 2.75, 3.5, 4.5, 6.0), r(2.5, 3.25, 4.25, 5.75, 8.0),
                                false, false, Formatting.DARK_GREEN));

                // --- Critical Strike Chance (Armor: Helmet + Chestplate, plus Elytra) ---
                registerForTypes("crit_chance", "Critical Strike Chance",
                                ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.03, 0.06, 0.10, 0.15), r(0.02, 0.05, 0.09, 0.14, 0.22),
                                true, true, Formatting.GOLD,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE);
                // Weapons share the same values
                registerForTypes("crit_chance", "Critical Strike Chance",
                                ModAttributes.CRIT_CHANCE,
                                r(0.01, 0.03, 0.06, 0.10, 0.15), r(0.02, 0.05, 0.09, 0.14, 0.22),
                                true, true, Formatting.GOLD,
                                ALL_WEAPONS);
                // Elytra has different ranges
                register(new CustomStat("elytra_crit_chance", "Critical Strike Chance",
                                EquipmentType.ELYTRA, ModAttributes.CRIT_CHANCE,
                                r(0.10, 0.13, 0.16, 0.19, 0.23), r(0.12, 0.15, 0.18, 0.21, 0.30),
                                true, true, Formatting.GOLD));
                // --- Critical Strike Multiplier (Armor: Helmet + Chestplate, plus Elytra) ---
                registerForTypes("crit_multiplier", "Critical Strike Multiplier",
                                ModAttributes.CRIT_MULTIPLIER,
                                r(0.05, 0.15, 0.25, 0.40, 0.60), r(0.10, 0.20, 0.35, 0.55, 0.85),
                                true, true, Formatting.GOLD,
                                EquipmentType.HELMET, EquipmentType.CHESTPLATE);
                // Weapons share the same values
                registerForTypes("crit_multiplier", "Critical Strike Multiplier",
                                ModAttributes.CRIT_MULTIPLIER,
                                r(0.05, 0.15, 0.25, 0.40, 0.60), r(0.10, 0.20, 0.35, 0.55, 0.85),
                                true, true, Formatting.GOLD,
                                ALL_WEAPONS);
                // Elytra has different ranges
                register(new CustomStat("elytra_crit_multiplier", "Critical Strike Multiplier",
                                EquipmentType.ELYTRA, ModAttributes.CRIT_MULTIPLIER,
                                r(0.30, 0.45, 0.65, 0.90, 1.25), r(0.40, 0.60, 0.85, 1.20, 1.75),
                                true, true, Formatting.GOLD));

                // ===================================================================
                // CHESTPLATE-ONLY OFFENSIVE STATS
                // ===================================================================

                register(new CustomStat("chestplate_attack_damage", "Physical Damage",
                                EquipmentType.CHESTPLATE, EntityAttributes.ATTACK_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 2.5, 4.5, 7.5, 12.0),
                                false, Formatting.WHITE));

                register(new CustomStat("chestplate_attack_speed", "Attack Speed",
                                EquipmentType.CHESTPLATE, EntityAttributes.ATTACK_SPEED,
                                r(0.10, 0.20, 0.30, 0.40, 0.50), r(0.18, 0.28, 0.38, 0.48, 0.58),
                                true, true, Formatting.YELLOW));

                // --- Chestplate elemental damages ---
                register(new CustomStat("chestplate_fire_damage", "Fire Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.FIRE_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.RED));

                register(new CustomStat("chestplate_cold_damage", "Cold Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.COLD_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.AQUA));

                register(new CustomStat("chestplate_lightning_damage", "Lightning Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.LIGHTNING_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.DARK_BLUE));

                register(new CustomStat("chestplate_void_damage", "Void Damage",
                                EquipmentType.CHESTPLATE, ModAttributes.VOID_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.DARK_PURPLE));

                register(new CustomStat("chestplate_knockback", "Knockback",
                                EquipmentType.CHESTPLATE, ModAttributes.KNOCKBACK,
                                r(0.1, 0.2, 0.4, 0.7, 1.0), r(0.2, 0.4, 0.6, 0.8, 1.2),
                                true, true, Formatting.GRAY));

                register(new CustomStat("chestplate_reload_speed", "Reload Speed",
                                EquipmentType.CHESTPLATE, ModAttributes.RELOAD_SPEED,
                                r(0.2, 0.4, 0.6, 0.8, 1.0), r(0.4, 0.6, 0.8, 1.0, 1.27),
                                true, true, Formatting.YELLOW));

                // ===================================================================
                // BOOTS-ONLY STATS
                // ===================================================================

                register(new CustomStat("boots_movement_speed", "Movement Speed",
                                EquipmentType.BOOTS, ModAttributes.MOVEMENT_SPEED,
                                r(0.2, 0.3, 0.4, 0.5, 0.6), r(0.25, 0.35, 0.45, 0.55, 0.8),
                                true, true, Formatting.GREEN));

                register(new CustomStat("boots_feather_falling", "Feather Falling",
                                EquipmentType.BOOTS, ModAttributes.FEATHER_FALLING,
                                r(0.10, 0.18, 0.28, 0.40, 0.55), r(0.15, 0.25, 0.38, 0.55, 0.75),
                                true, true, Formatting.WHITE));

                // ===================================================================
                // WEAPON STATS (Shared across weapon types)
                // ===================================================================

                // --- Physical Damage (all weapons) ---
                registerForTypes("attack_damage", "Physical Damage",
                                EntityAttributes.ATTACK_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 2.5, 4.5, 7.5, 12.0),
                                false, Formatting.WHITE,
                                ALL_WEAPONS);

                // --- Attack Speed (melee + trident) ---
                registerForTypes("attack_speed", "Attack Speed",
                                EntityAttributes.ATTACK_SPEED,
                                r(0.10, 0.20, 0.30, 0.40, 0.50), r(0.18, 0.28, 0.38, 0.48, 0.58),
                                true, true, Formatting.YELLOW,
                                ALL_MELEE_AND_TRIDENT);

                // --- Ailment Chances (all weapons) ---
                registerForTypes("chance_to_burn", "Burn Chance",
                                ModAttributes.CHANCE_TO_BURN,
                                r(0.01, 0.03, 0.05, 0.08, 0.12), r(0.02, 0.05, 0.08, 0.12, 0.18),
                                true, true, Formatting.RED,
                                ALL_WEAPONS);

                registerForTypes("chance_to_poison", "Poison Chance",
                                ModAttributes.CHANCE_TO_POISON,
                                r(0.01, 0.03, 0.05, 0.08, 0.12), r(0.02, 0.05, 0.08, 0.12, 0.18),
                                true, true, Formatting.GREEN,
                                ALL_WEAPONS);

                registerForTypes("chance_to_freeze", "Freeze Chance",
                                ModAttributes.CHANCE_TO_FREEZE,
                                r(0.01, 0.03, 0.05, 0.08, 0.12), r(0.02, 0.05, 0.08, 0.12, 0.18),
                                true, true, Formatting.AQUA,
                                ALL_WEAPONS);

                registerForTypes("chance_to_bleed", "Bleed Chance",
                                ModAttributes.CHANCE_TO_BLEED,
                                r(0.01, 0.03, 0.05, 0.08, 0.12), r(0.02, 0.05, 0.08, 0.12, 0.18),
                                true, true, Formatting.DARK_RED,
                                ALL_WEAPONS);

                registerForTypes("chance_to_shock", "Shock Chance",
                                ModAttributes.CHANCE_TO_SHOCK,
                                r(0.01, 0.03, 0.05, 0.08, 0.12), r(0.02, 0.05, 0.08, 0.12, 0.18),
                                true, true, Formatting.YELLOW,
                                ALL_WEAPONS);

                // --- Elemental Damages (all weapons) ---
                registerForTypes("fire_damage", "Fire Damage",
                                ModAttributes.FIRE_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.RED,
                                ALL_WEAPONS);

                registerForTypes("cold_damage", "Cold Damage",
                                ModAttributes.COLD_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.AQUA,
                                ALL_WEAPONS);

                registerForTypes("lightning_damage", "Lightning Damage",
                                ModAttributes.LIGHTNING_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.DARK_BLUE,
                                ALL_WEAPONS);

                registerForTypes("void_damage", "Void Damage",
                                ModAttributes.VOID_DAMAGE,
                                r(0.5, 1.5, 3.0, 5.0, 8.0), r(1.0, 3.0, 5.5, 9.0, 14.0),
                                false, Formatting.DARK_PURPLE,
                                ALL_WEAPONS);

                // --- Percent Damages (all weapons) ---
                registerForTypes("attack_damage_percent", "Increased Physical Damage",
                                ModAttributes.ATTACK_DAMAGE_PERCENT,
                                r(0.01, 0.20, 0.40, 0.60, 1.10), r(0.20, 0.40, 0.60, 1.00, 1.65),
                                true, true, Formatting.WHITE,
                                ALL_WEAPONS);

                registerForTypes("fire_damage_percent", "Increased Fire Damage",
                                ModAttributes.FIRE_DAMAGE_PERCENT,
                                r(0.01, 0.20, 0.40, 0.60, 1.10), r(0.20, 0.40, 0.60, 1.00, 1.65),
                                true, true, Formatting.RED,
                                ALL_WEAPONS);

                registerForTypes("cold_damage_percent", "Increased Cold Damage",
                                ModAttributes.COLD_DAMAGE_PERCENT,
                                r(0.01, 0.20, 0.40, 0.60, 1.10), r(0.20, 0.40, 0.60, 1.00, 1.65),
                                true, true, Formatting.AQUA,
                                ALL_WEAPONS);

                registerForTypes("lightning_damage_percent", "Increased Lightning Damage",
                                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                                r(0.01, 0.20, 0.40, 0.60, 1.10), r(0.20, 0.40, 0.60, 1.00, 1.65),
                                true, true, Formatting.DARK_BLUE,
                                ALL_WEAPONS);

                registerForTypes("void_damage_percent", "Increased Void Damage",
                                ModAttributes.VOID_DAMAGE_PERCENT,
                                r(0.01, 0.20, 0.40, 0.60, 1.10), r(0.20, 0.40, 0.60, 1.00, 1.65),
                                true, true, Formatting.DARK_PURPLE,
                                ALL_WEAPONS);

                // --- Proportional Extra Damage (Runes) ---
                registerForTypes("attack_extra_damage", "Damage as Extra Physical Damage",
                                ModAttributes.PHYSICAL_EXTRA_PERCENT,
                                r(0.05, 0.12, 0.20, 0.28, 0.35), r(0.08, 0.16, 0.24, 0.35, 0.40),
                                true, true, Formatting.WHITE,
                                ALL_WEAPONS);

                registerForTypes("fire_extra_damage", "Damage as Extra Fire Damage",
                                ModAttributes.FIRE_EXTRA_PERCENT,
                                r(0.05, 0.12, 0.20, 0.28, 0.35), r(0.08, 0.16, 0.24, 0.35, 0.40),
                                true, true, Formatting.RED,
                                ALL_WEAPONS);

                registerForTypes("cold_extra_damage", "Damage as Extra Cold Damage",
                                ModAttributes.COLD_EXTRA_PERCENT,
                                r(0.05, 0.12, 0.20, 0.28, 0.35), r(0.08, 0.16, 0.24, 0.35, 0.40),
                                true, true, Formatting.AQUA,
                                ALL_WEAPONS);

                registerForTypes("lightning_extra_damage", "Damage as Extra Lightning Damage",
                                ModAttributes.LIGHTNING_EXTRA_PERCENT,
                                r(0.05, 0.12, 0.20, 0.28, 0.35), r(0.08, 0.16, 0.24, 0.35, 0.40),
                                true, true, Formatting.DARK_BLUE,
                                ALL_WEAPONS);

                registerForTypes("void_extra_damage", "Damage as Extra Void Damage",
                                ModAttributes.VOID_EXTRA_PERCENT,
                                r(0.05, 0.12, 0.20, 0.28, 0.35), r(0.08, 0.16, 0.24, 0.35, 0.40),
                                true, true, Formatting.DARK_PURPLE,
                                ALL_WEAPONS);

                // --- Life Steal (melee + trident: one range, bow/crossbow: different) ---
                registerForTypes("life_steal", "Life Steal",
                                ModAttributes.LIFE_STEAL,
                                r(0.01, 0.03, 0.05, 0.08, 0.11), r(0.02, 0.05, 0.07, 0.10, 0.13),
                                true, true, Formatting.DARK_RED,
                                ALL_MELEE_AND_TRIDENT);
                registerForTypes("life_steal", "Life Steal",
                                ModAttributes.LIFE_STEAL,
                                r(0.01, 0.03, 0.05, 0.07, 0.09), r(0.02, 0.04, 0.06, 0.08, 0.10),
                                true, true, Formatting.DARK_RED,
                                ALL_RANGED_WEAPONS);

                // --- Knockback (Sword, Mace, Trident: same; Spear: different; Bow/Crossbow:
                // different) ---
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.1, 0.2, 0.4, 0.7, 1.0), r(0.2, 0.4, 0.6, 0.8, 1.2),
                                true, true, Formatting.GRAY,
                                EquipmentType.SWORD, EquipmentType.MACE, EquipmentType.TRIDENT);
                register(new CustomStat("spear_knockback", "Knockback",
                                EquipmentType.SPEAR, ModAttributes.KNOCKBACK,
                                r(0.1, 0.3, 0.6, 0.9, 1.2), r(0.2, 0.5, 0.8, 1.1, 1.4),
                                true, true, Formatting.GRAY));
                registerForTypes("knockback", "Knockback",
                                ModAttributes.KNOCKBACK,
                                r(0.1, 0.3, 0.5, 0.7, 0.9), r(0.2, 0.4, 0.6, 0.8, 1.0),
                                true, true, Formatting.GRAY,
                                ALL_RANGED_WEAPONS);

                // --- Attack Range (Sword, Mace: same; Spear, Trident: different) ---
                registerForTypes("attack_range", "Attack Range",
                                ModAttributes.ATTACK_RANGE,
                                r(0.1, 0.3, 0.5, 0.7, 0.9), r(0.2, 0.4, 0.6, 0.8, 1.0),
                                false, Formatting.GREEN,
                                EquipmentType.SWORD, EquipmentType.MACE);
                registerForTypes("attack_range", "Attack Range",
                                ModAttributes.ATTACK_RANGE,
                                r(0.3, 0.5, 0.7, 0.9, 1.1), r(0.4, 0.6, 0.8, 1.0, 1.2),
                                false, Formatting.GREEN,
                                EquipmentType.SPEAR, EquipmentType.TRIDENT);

                // --- Reload Speed (Bow, Crossbow, Chestplate already registered above) ---
                registerForTypes("reload_speed", "Reload Speed",
                                ModAttributes.RELOAD_SPEED,
                                r(0.2, 0.4, 0.6, 0.8, 1.0), r(0.4, 0.6, 0.8, 1.0, 1.27),
                                true, true, Formatting.YELLOW,
                                ALL_RANGED_WEAPONS);

                // ===================================================================
                // ELYTRA-ONLY STATS
                // ===================================================================

                register(new CustomStat("elytra_flying_speed", "Flying Speed",
                                EquipmentType.ELYTRA, ModAttributes.FLYING_SPEED,
                                r(0.05, 0.10, 0.15, 0.20, 0.30), r(0.08, 0.15, 0.22, 0.30, 0.45),
                                true, true, Formatting.AQUA));

                register(new CustomStat("elytra_movement_speed", "Movement Speed",
                                EquipmentType.ELYTRA, ModAttributes.MOVEMENT_SPEED,
                                r(0.01, 0.1, 0.2, 0.3, 0.4), r(0.05, 0.15, 0.25, 0.35, 0.6),
                                true, true, Formatting.GREEN));

                register(new CustomStat("elytra_feather_falling", "Feather Falling",
                                EquipmentType.ELYTRA, ModAttributes.FEATHER_FALLING,
                                r(0.05, 0.15, 0.25, 0.35, 0.45), r(0.10, 0.20, 0.30, 0.40, 0.50),
                                true, true, Formatting.WHITE));

                // ===================================================================
                // TOOL STATS
                // ===================================================================

                // --- Mining Efficiency (Pickaxe, Axe, Shovel — each has different display
                // name) ---
                register(new CustomStat("pickaxe_mining_efficiency", "Mining Speed",
                                EquipmentType.PICKAXE, ModAttributes.MINING_EFFICIENCY,
                                r(0.5, 1.0, 1.5, 2.0, 2.5), r(1.0, 1.5, 2.00, 2.50, 3.0),
                                true, true, Formatting.WHITE));

                register(new CustomStat("axe_mining_efficiency", "Chopping Speed",
                                EquipmentType.AXE, ModAttributes.MINING_EFFICIENCY,
                                r(0.5, 1.0, 1.5, 2.0, 2.5), r(1.0, 1.5, 2.00, 2.50, 3.0),
                                true, true, Formatting.GRAY));

                register(new CustomStat("shovel_mining_efficiency", "Digging Speed",
                                EquipmentType.SHOVEL, ModAttributes.MINING_EFFICIENCY,
                                r(0.5, 1.0, 1.5, 2.0, 2.5), r(1.0, 1.5, 2.00, 2.50, 3.0),
                                true, true, Formatting.GRAY));

                // --- Repair Chance (Pickaxe, Axe, Shovel, Hoe) ---
                registerForTypes("repair_chance", "Repair Chance",
                                ModAttributes.PRECISION_MINING,
                                r(0.03, 0.08, 0.13, 0.18, 0.25), r(0.05, 0.12, 0.18, 0.25, 0.35),
                                true, true, Formatting.AQUA,
                                EquipmentType.PICKAXE, EquipmentType.AXE, EquipmentType.SHOVEL, EquipmentType.HOE);

                // --- Breaking Range (Pickaxe, Axe, Shovel, Hoe) ---
                registerForTypes("breaking_range", "Breaking Range",
                                ModAttributes.BREAKING_RANGE,
                                r(0.3, 0.7, 1.1, 1.5, 1.8), r(0.5, 0.9, 1.3, 1.7, 2.0),
                                false, Formatting.GREEN,
                                EquipmentType.PICKAXE, EquipmentType.AXE, EquipmentType.SHOVEL, EquipmentType.HOE);

                // --- Ore Fortune (Pickaxe only) ---
                register(new CustomStat("pickaxe_fortune", "Ore Fortune",
                                EquipmentType.PICKAXE, ModAttributes.FORTUNE,
                                r(0.10, 0.18, 0.28, 0.40, 0.55), r(0.15, 0.28, 0.40, 0.55, 0.75),
                                true, true, Formatting.GOLD));

                // --- Log Fortune (Axe only) ---
                register(new CustomStat("axe_log_fortune", "Log Fortune",
                                EquipmentType.AXE, ModAttributes.LOG_FORTUNE,
                                r(0.10, 0.18, 0.28, 0.40, 0.55), r(0.15, 0.28, 0.40, 0.55, 0.75),
                                true, true, Formatting.DARK_GREEN));

                // --- Dig Fortune (Shovel only) ---
                register(new CustomStat("shovel_dig_fortune", "Dig Fortune",
                                EquipmentType.SHOVEL, ModAttributes.DIG_FORTUNE,
                                r(0.10, 0.20, 0.30, 0.45, 0.60), r(0.18, 0.30, 0.42, 0.58, 0.80),
                                true, true, Formatting.DARK_GREEN));

                // --- Crop Fortune (Hoe only) ---
                register(new CustomStat("hoe_crop_fortune", "Crop Fortune",
                                EquipmentType.HOE, ModAttributes.CROP_FORTUNE,
                                r(0.10, 0.18, 0.28, 0.40, 0.55), r(0.15, 0.28, 0.40, 0.55, 0.75),
                                true, true, Formatting.GOLD));

                // --- Harvest Experience (Hoe only) ---
                register(new CustomStat("hoe_crop_experience", "Harvest Experience",
                                EquipmentType.HOE, ModAttributes.CROP_EXPERIENCE,
                                r(0.10, 0.25, 0.45, 0.70, 1.00), r(0.20, 0.40, 0.70, 1.00, 1.50),
                                false, Formatting.DARK_GREEN));

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
                List<CustomStat> stats = getStatsByEquipmentType(equipmentType);
                return stats.isEmpty() ? null : stats.get(RandomUtil.nextInt(stats.size()));
        }

        public static Collection<String> getAllStatIds() {
                return STATS.keySet();
        }
}