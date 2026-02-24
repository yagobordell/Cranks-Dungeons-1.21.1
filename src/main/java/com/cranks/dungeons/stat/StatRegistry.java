package com.cranks.dungeons.stat;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

public class StatRegistry {
    private static final Map<String, CustomStat> STATS = new HashMap<>();

    public static void registerStats() {
        // ===== HELMET STATS =====
        register(new CustomStat(
                "helmet_durability_bonus",
                "Durability Bonus",
                EquipmentType.HELMET,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "helmet_energy_shield",
                "Energy Shield",
                EquipmentType.HELMET,
                ModAttributes.ENERGY_SHIELD,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "helmet_max_health",
                "Max Health",
                EquipmentType.HELMET,
                EntityAttributes.MAX_HEALTH,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "helmet_life_regeneration",
                "Health Regeneration",
                EquipmentType.HELMET,
                ModAttributes.LIFE_REGENERATION,
                new double[]{0.1, 0.25, 0.5, 0.8, 1.2},
                new double[]{0.2, 0.45, 0.75, 1.2, 2.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "helmet_evasion_rating",
                "Evasion Rating",
                EquipmentType.HELMET,
                ModAttributes.EVASION_RATING,
                new double[]{10.0, 25.0, 50.0, 100.0, 150.0},
                new double[]{20.0, 40.0, 80.0, 140.0, 200.0},
                false, Formatting.GRAY
        ));

        register(new CustomStat(
                "helmet_armor",
                "Armor",
                EquipmentType.HELMET,
                EntityAttributes.ARMOR,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "helmet_armor_toughness",
                "Armor Toughness",
                EquipmentType.HELMET,
                EntityAttributes.ARMOR_TOUGHNESS,
                new double[]{0.3, 0.8, 1.5, 2.5, 4.0},
                new double[]{0.6, 1.4, 2.4, 4.0, 6.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "helmet_knockback_resistance",
                "Knockback Resistance",
                EquipmentType.HELMET,
                ModAttributes.KNOCKBACK_RESISTANCE,
                new double[]{0.05, 0.10, 0.15, 0.20, 0.25},
                new double[]{0.10, 0.15, 0.20, 0.25, 0.35},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "helmet_fire_resistance",
                "Fire Resistance",
                EquipmentType.HELMET,
                ModAttributes.FIRE_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "helmet_cold_resistance",
                "Cold Resistance",
                EquipmentType.HELMET,
                ModAttributes.COLD_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "helmet_lightning_resistance",
                "Lightning Resistance",
                EquipmentType.HELMET,
                ModAttributes.LIGHTNING_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "helmet_void_resistance",
                "Void Resistance",
                EquipmentType.HELMET,
                ModAttributes.VOID_RESISTANCE,
                new double[]{0.05, 0.1, 0.15, 0.2, 0.25},
                new double[]{0.1, 0.15, 0.2, 0.25, 0.3},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "helmet_crit_chance",
                "Critical Strike Chance",
                EquipmentType.HELMET,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "helmet_experience_bonus",
                "Experience Bonus",
                EquipmentType.HELMET,
                ModAttributes.EXPERIENCE_BONUS,
                new double[]{0.05, 0.12, 0.20, 0.38, 0.43},
                new double[]{0.10, 0.18, 0.28, 0.42, 0.69},
                true,
                true,
                Formatting.DARK_GREEN
        ));

        register(new CustomStat(
                "helmet_luck",
                "Luck",
                EquipmentType.HELMET,
                ModAttributes.LUCK,
                new double[]{0.25, 0.75, 1.5, 2.5, 4.0},
                new double[]{0.5, 1.25, 2.25, 3.75, 6.0},
                false,
                false,
                Formatting.DARK_GREEN
        ));

        // ===== CHESTPLATE STATS =====
        register(new CustomStat(
                "chestplate_durability_bonus",
                "Durability Bonus",
                EquipmentType.CHESTPLATE,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "chestplate_energy_shield",
                "Energy Shield",
                EquipmentType.CHESTPLATE,
                ModAttributes.ENERGY_SHIELD,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "chestplate_max_health",
                "Max Health",
                EquipmentType.CHESTPLATE,
                EntityAttributes.MAX_HEALTH,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "chestplate_life_regeneration",
                "Health Regeneration",
                EquipmentType.CHESTPLATE,
                ModAttributes.LIFE_REGENERATION,
                new double[]{0.1, 0.25, 0.5, 0.8, 1.2},
                new double[]{0.2, 0.45, 0.75, 1.2, 2.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "chestplate_evasion_rating",
                "Evasion Rating",
                EquipmentType.CHESTPLATE,
                ModAttributes.EVASION_RATING,
                new double[]{10.0, 25.0, 50.0, 100.0, 150.0},
                new double[]{20.0, 40.0, 80.0, 140.0, 200.0},
                false, Formatting.GRAY
        ));

        register(new CustomStat(
                "chestplate_armor",
                "Armor",
                EquipmentType.CHESTPLATE,
                EntityAttributes.ARMOR,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "chestplate_armor_toughness",
                "Armor Toughness",
                EquipmentType.CHESTPLATE,
                EntityAttributes.ARMOR_TOUGHNESS,
                new double[]{0.3, 0.8, 1.5, 2.5, 4.0},
                new double[]{0.6, 1.4, 2.4, 4.0, 6.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "chestplate_knockback_resistance",
                "Knockback Resistance",
                EquipmentType.CHESTPLATE,
                ModAttributes.KNOCKBACK_RESISTANCE,
                new double[]{0.05, 0.10, 0.15, 0.20, 0.25},
                new double[]{0.10, 0.15, 0.20, 0.25, 0.35},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "chestplate_fire_resistance",
                "Fire Resistance",
                EquipmentType.CHESTPLATE,
                ModAttributes.FIRE_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "chestplate_cold_resistance",
                "Cold Resistance",
                EquipmentType.CHESTPLATE,
                ModAttributes.COLD_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "chestplate_lightning_resistance",
                "Lightning Resistance",
                EquipmentType.CHESTPLATE,
                ModAttributes.LIGHTNING_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "chestplate_void_resistance",
                "Void Resistance",
                EquipmentType.CHESTPLATE,
                ModAttributes.VOID_RESISTANCE,
                new double[]{0.05, 0.1, 0.15, 0.2, 0.25},
                new double[]{0.1, 0.15, 0.2, 0.25, 0.3},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "chestplate_attack_damage",
                "Attack Damage",
                EquipmentType.CHESTPLATE,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "chestplate_attack_speed",
                "Attack Speed",
                EquipmentType.CHESTPLATE,
                EntityAttributes.ATTACK_SPEED,
                new double[]{0.10, 0.20, 0.30, 0.40, 0.50},
                new double[]{0.18, 0.28, 0.38, 0.48, 0.58},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "chestplate_crit_chance",
                "Critical Strike Chance",
                EquipmentType.CHESTPLATE,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "chestplate_chance_to_burn",
                "Chance to burn",
                EquipmentType.CHESTPLATE,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "chestplate_fire_damage",
                "Fire Damage",
                EquipmentType.CHESTPLATE,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "chestplate_cold_damage",
                "Cold Damage",
                EquipmentType.CHESTPLATE,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "chestplate_lightning_damage",
                "Lightning Damage",
                EquipmentType.CHESTPLATE,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "chestplate_void_damage",
                "Void Damage",
                EquipmentType.CHESTPLATE,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "chestplate_life_steal",
                "Life Steal",
                EquipmentType.CHESTPLATE,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.08, 0.11},
                new double[]{0.02, 0.05, 0.07, 0.10, 0.13},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "chestplate_knockback",
                "Knockback",
                EquipmentType.CHESTPLATE,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.2, 0.4, 0.7, 1.0},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.2},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "chestplate_reload_speed",
                "Reload Speed",
                EquipmentType.CHESTPLATE,
                ModAttributes.RELOAD_SPEED,
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                new double[]{0.4, 0.6, 0.8, 1.0, 1.27},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "chestplate_experience_bonus",
                "Experience Bonus",
                EquipmentType.CHESTPLATE,
                ModAttributes.EXPERIENCE_BONUS,
                new double[]{0.05, 0.12, 0.20, 0.38, 0.43},
                new double[]{0.10, 0.18, 0.28, 0.42, 0.69},
                true,
                true,
                Formatting.DARK_GREEN
        ));

        register(new CustomStat(
                "chestplate_luck",
                "Luck",
                EquipmentType.CHESTPLATE,
                ModAttributes.LUCK,
                new double[]{0.25, 0.75, 1.5, 2.5, 4.0},
                new double[]{0.5, 1.25, 2.25, 3.75, 6.0},
                false,
                false,
                Formatting.DARK_GREEN
        ));

        // ===== LEGGINGS STATS =====
        register(new CustomStat(
                "leggings_durability_bonus",
                "Durability Bonus",
                EquipmentType.LEGGINGS,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "leggings_energy_shield",
                "Energy Shield",
                EquipmentType.LEGGINGS,
                ModAttributes.ENERGY_SHIELD,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "leggings_max_health",
                "Max Health",
                EquipmentType.LEGGINGS,
                EntityAttributes.MAX_HEALTH,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "leggings_life_regeneration",
                "Health Regeneration",
                EquipmentType.LEGGINGS,
                ModAttributes.LIFE_REGENERATION,
                new double[]{0.1, 0.25, 0.5, 0.8, 1.2},
                new double[]{0.2, 0.45, 0.75, 1.2, 2.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "leggings_evasion_rating",
                "Evasion Rating",
                EquipmentType.LEGGINGS,
                ModAttributes.EVASION_RATING,
                new double[]{10.0, 25.0, 50.0, 100.0, 150.0},
                new double[]{20.0, 40.0, 80.0, 140.0, 200.0},
                false, Formatting.GRAY
        ));

        register(new CustomStat(
                "leggings_armor",
                "Armor",
                EquipmentType.LEGGINGS,
                EntityAttributes.ARMOR,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "leggings_armor_toughness",
                "Armor Toughness",
                EquipmentType.LEGGINGS,
                EntityAttributes.ARMOR_TOUGHNESS,
                new double[]{0.3, 0.8, 1.5, 2.5, 4.0},
                new double[]{0.6, 1.4, 2.4, 4.0, 6.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "leggings_knockback_resistance",
                "Knockback Resistance",
                EquipmentType.LEGGINGS,
                ModAttributes.KNOCKBACK_RESISTANCE,
                new double[]{0.05, 0.10, 0.15, 0.20, 0.25},
                new double[]{0.10, 0.15, 0.20, 0.25, 0.35},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "leggigns_fire_resistance",
                "Fire Resistance",
                EquipmentType.LEGGINGS,
                ModAttributes.FIRE_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "leggings_cold_resistance",
                "Cold Resistance",
                EquipmentType.LEGGINGS,
                ModAttributes.COLD_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "leggings_lightning_resistance",
                "Lightning Resistance",
                EquipmentType.LEGGINGS,
                ModAttributes.LIGHTNING_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "leggings_void_resistance",
                "Void Resistance",
                EquipmentType.LEGGINGS,
                ModAttributes.VOID_RESISTANCE,
                new double[]{0.05, 0.1, 0.15, 0.2, 0.25},
                new double[]{0.1, 0.15, 0.2, 0.25, 0.3},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "leggings_experience_bonus",
                "Experience Bonus",
                EquipmentType.LEGGINGS,
                ModAttributes.EXPERIENCE_BONUS,
                new double[]{0.05, 0.12, 0.20, 0.38, 0.43},
                new double[]{0.10, 0.18, 0.28, 0.42, 0.69},
                true,
                true,
                Formatting.DARK_GREEN
        ));

        register(new CustomStat(
                "leggings_luck",
                "Luck",
                EquipmentType.LEGGINGS,
                ModAttributes.LUCK,
                new double[]{0.25, 0.75, 1.5, 2.5, 4.0},
                new double[]{0.5, 1.25, 2.25, 3.75, 6.0},
                false,
                false,
                Formatting.DARK_GREEN
        ));

        // ===== BOOTS STATS =====
        register(new CustomStat(
                "boots_durability_bonus",
                "Durability Bonus",
                EquipmentType.BOOTS,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "boots_energy_shield",
                "Energy Shield",
                EquipmentType.BOOTS,
                ModAttributes.ENERGY_SHIELD,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "boots_max_health",
                "Max Health",
                EquipmentType.BOOTS,
                EntityAttributes.MAX_HEALTH,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "boots_life_regeneration",
                "Health Regeneration",
                EquipmentType.BOOTS,
                ModAttributes.LIFE_REGENERATION,
                new double[]{0.1, 0.25, 0.5, 0.8, 1.2},
                new double[]{0.2, 0.45, 0.75, 1.2, 2.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "boots_evasion_rating",
                "Evasion Rating",
                EquipmentType.BOOTS,
                ModAttributes.EVASION_RATING,
                new double[]{10.0, 25.0, 50.0, 100.0, 150.0},
                new double[]{20.0, 40.0, 80.0, 140.0, 200.0},
                false, Formatting.GRAY
        ));

        register(new CustomStat(
                "boots_armor",
                "Armor",
                EquipmentType.BOOTS,
                EntityAttributes.ARMOR,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "boots_armor_toughness",
                "Armor Toughness",
                EquipmentType.BOOTS,
                EntityAttributes.ARMOR_TOUGHNESS,
                new double[]{0.3, 0.8, 1.5, 2.5, 4.0},
                new double[]{0.6, 1.4, 2.4, 4.0, 6.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "boots_knockback_resistance",
                "Knockback Resistance",
                EquipmentType.BOOTS,
                ModAttributes.KNOCKBACK_RESISTANCE,
                new double[]{0.05, 0.10, 0.15, 0.20, 0.25},
                new double[]{0.10, 0.15, 0.20, 0.25, 0.35},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "boots_fire_resistance",
                "Fire Resistance",
                EquipmentType.BOOTS,
                ModAttributes.FIRE_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "boots_cold_resistance",
                "Cold Resistance",
                EquipmentType.BOOTS,
                ModAttributes.COLD_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "boots_lightning_resistance",
                "Lightning Resistance",
                EquipmentType.BOOTS,
                ModAttributes.LIGHTNING_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "boots_void_resistance",
                "Void Resistance",
                EquipmentType.BOOTS,
                ModAttributes.VOID_RESISTANCE,
                new double[]{0.05, 0.1, 0.15, 0.2, 0.25},
                new double[]{0.1, 0.15, 0.2, 0.25, 0.3},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "boots_movement_speed",
                "Movement Speed",
                EquipmentType.BOOTS,
                ModAttributes.MOVEMENT_SPEED,
                new double[]{0.2, 0.3, 0.4, 0.5, 0.6},
                new double[]{0.25, 0.35, 0.45, 0.55, 0.8},
                true,
                true,
                Formatting.GREEN
        ));

        register(new CustomStat(
                "boots_feather_falling",
                "Feather Falling",
                EquipmentType.BOOTS,
                ModAttributes.FEATHER_FALLING,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.55},
                new double[]{0.15, 0.25, 0.38, 0.55, 0.75},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "boots_experience_bonus",
                "Experience Bonus",
                EquipmentType.BOOTS,
                ModAttributes.EXPERIENCE_BONUS,
                new double[]{0.05, 0.12, 0.20, 0.38, 0.43},
                new double[]{0.10, 0.18, 0.28, 0.42, 0.69},
                true,
                true,
                Formatting.DARK_GREEN
        ));

        register(new CustomStat(
                "boots_luck",
                "Luck",
                EquipmentType.BOOTS,
                ModAttributes.LUCK,
                new double[]{0.25, 0.75, 1.5, 2.5, 4.0},
                new double[]{0.5, 1.25, 2.25, 3.75, 6.0},
                false,
                false,
                Formatting.DARK_GREEN
        ));

        // ===== SWORD STATS =====
        register(new CustomStat(
                "sword_durability_bonus",
                "Durability Bonus",
                EquipmentType.SWORD,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "sword_attack_damage",
                "Attack Damage",
                EquipmentType.SWORD,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "sword_attack_speed",
                "Attack Speed",
                EquipmentType.SWORD,
                EntityAttributes.ATTACK_SPEED,
                new double[]{0.10, 0.20, 0.30, 0.40, 0.50},
                new double[]{0.18, 0.28, 0.38, 0.48, 0.58},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "sword_crit_chance",
                "Critical Strike Chance",
                EquipmentType.SWORD,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "sword_chance_to_burn",
                "Chance to burn",
                EquipmentType.SWORD,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "sword_fire_damage",
                "Fire Damage",
                EquipmentType.SWORD,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "sword_cold_damage",
                "Cold Damage",
                EquipmentType.SWORD,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "sword_lightning_damage",
                "Lightning Damage",
                EquipmentType.SWORD,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "sword_void_damage",
                "Void Damage",
                EquipmentType.SWORD,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "sword_attack_damage_percent",
                "Increased Attack Damage",
                EquipmentType.SWORD,
                ModAttributes.ATTACK_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "sword_fire_damage_percent",
                "Increased Fire Damage",
                EquipmentType.SWORD,
                ModAttributes.FIRE_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "sword_cold_damage_percent",
                "Increased Cold Damage",
                EquipmentType.SWORD,
                ModAttributes.COLD_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "sword_lightning_damage_percent",
                "Increased Lightning Damage",
                EquipmentType.SWORD,
                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "sword_void_damage_percent",
                "Increased Void Damage",
                EquipmentType.SWORD,
                ModAttributes.VOID_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "sword_life_steal",
                "Life Steal",
                EquipmentType.SWORD,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.08, 0.11},
                new double[]{0.02, 0.05, 0.07, 0.10, 0.13},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "sword_knockback",
                "Knockback",
                EquipmentType.SWORD,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.2, 0.4, 0.7, 1.0},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.2},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "sword_attack_range",
                "Attack Range",
                EquipmentType.SWORD,
                ModAttributes.ATTACK_RANGE,
                new double[]{0.1, 0.3, 0.5, 0.7, 0.9},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                false,
                Formatting.GREEN
        ));

        // ===== SPEAR STATS =====
        register(new CustomStat(
                "spear_durability_bonus",
                "Durability Bonus",
                EquipmentType.SPEAR,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "spear_attack_range",
                "Attack Range",
                EquipmentType.SPEAR,
                ModAttributes.ATTACK_RANGE,
                new double[]{0.3, 0.5, 0.7, 0.9, 1.1},
                new double[]{0.4, 0.6, 0.8, 1.0, 1.2},
                false,
                Formatting.GREEN
        ));

        register(new CustomStat(
                "spear_attack_damage",
                "Attack Damage",
                EquipmentType.SPEAR,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "spear_attack_speed",
                "Attack Speed",
                EquipmentType.SPEAR,
                EntityAttributes.ATTACK_SPEED,
                new double[]{0.10, 0.20, 0.30, 0.40, 0.50},
                new double[]{0.18, 0.28, 0.38, 0.48, 0.58},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "spear_crit_chance",
                "Critical Strike Chance",
                EquipmentType.SPEAR,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "spear_chance_to_burn",
                "Chance to burn",
                EquipmentType.SPEAR,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "spear_fire_damage",
                "Fire Damage",
                EquipmentType.SPEAR,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "spear_cold_damage",
                "Cold Damage",
                EquipmentType.SPEAR,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "spear_lightning_damage",
                "Lightning Damage",
                EquipmentType.SPEAR,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "spear_void_damage",
                "Void Damage",
                EquipmentType.SPEAR,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "spear_attack_damage_percent",
                "Increased Attack Damage",
                EquipmentType.SPEAR,
                ModAttributes.ATTACK_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "spear_fire_damage_percent",
                "Increased Fire Damage",
                EquipmentType.SPEAR,
                ModAttributes.FIRE_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "spear_cold_damage_percent",
                "Increased Cold Damage",
                EquipmentType.SPEAR,
                ModAttributes.COLD_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "spear_lightning_damage_percent",
                "Increased Lightning Damage",
                EquipmentType.SPEAR,
                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "spear_void_damage_percent",
                "Increased Void Damage",
                EquipmentType.SPEAR,
                ModAttributes.VOID_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "spear_life_steal",
                "Life Steal",
                EquipmentType.SPEAR,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.08, 0.11},
                new double[]{0.02, 0.05, 0.07, 0.10, 0.13},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "spear_knockback",
                "Knockback",
                EquipmentType.SPEAR,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.3, 0.6, 0.9, 1.2},
                new double[]{0.2, 0.5, 0.8, 1.1, 1.4},
                true,
                true,
                Formatting.GRAY
        ));

        // ===== MACE STATS =====
        register(new CustomStat(
                "mace_durability_bonus",
                "Durability Bonus",
                EquipmentType.MACE,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "mace_attack_damage",
                "Attack Damage",
                EquipmentType.MACE,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "mace_attack_speed",
                "Attack Speed",
                EquipmentType.MACE,
                EntityAttributes.ATTACK_SPEED,
                new double[]{0.10, 0.20, 0.30, 0.40, 0.50},
                new double[]{0.18, 0.28, 0.38, 0.48, 0.58},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "mace_crit_chance",
                "Critical Strike Chance",
                EquipmentType.MACE,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "mace_chance_to_burn",
                "Chance to burn",
                EquipmentType.MACE,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "mace_fire_damage",
                "Fire Damage",
                EquipmentType.MACE,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "mace_cold_damage",
                "Cold Damage",
                EquipmentType.MACE,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "mace_lightning_damage",
                "Lightning Damage",
                EquipmentType.MACE,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "mace_void_damage",
                "Void Damage",
                EquipmentType.MACE,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "mace_attack_damage_percent",
                "Increased Attack Damage",
                EquipmentType.MACE,
                ModAttributes.ATTACK_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "mace_fire_damage_percent",
                "Increased Fire Damage",
                EquipmentType.MACE,
                ModAttributes.FIRE_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "mace_cold_damage_percent",
                "Increased Cold Damage",
                EquipmentType.MACE,
                ModAttributes.COLD_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "mace_lightning_damage_percent",
                "Increased Lightning Damage",
                EquipmentType.MACE,
                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "mace_void_damage_percent",
                "Increased Void Damage",
                EquipmentType.MACE,
                ModAttributes.VOID_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "mace_life_steal",
                "Life Steal",
                EquipmentType.MACE,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.08, 0.11},
                new double[]{0.02, 0.05, 0.07, 0.10, 0.13},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "mace_knockback",
                "Knockback",
                EquipmentType.MACE,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.2, 0.4, 0.7, 1.0},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.2},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "mace_attack_range",
                "Attack Range",
                EquipmentType.MACE,
                ModAttributes.ATTACK_RANGE,
                new double[]{0.1, 0.3, 0.5, 0.7, 0.9},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                false,
                Formatting.GREEN
        ));

        // ===== TRIDENT STATS =====
        register(new CustomStat(
                "trident_durability_bonus",
                "Durability Bonus",
                EquipmentType.TRIDENT,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "trident_attack_damage",
                "Attack Damage",
                EquipmentType.TRIDENT,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "trident_attack_speed",
                "Attack Speed",
                EquipmentType.TRIDENT,
                EntityAttributes.ATTACK_SPEED,
                new double[]{0.10, 0.20, 0.30, 0.40, 0.50},
                new double[]{0.18, 0.28, 0.38, 0.48, 0.58},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "trident_crit_chance",
                "Critical Strike Chance",
                EquipmentType.TRIDENT,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "trident_chance_to_burn",
                "Chance to burn",
                EquipmentType.TRIDENT,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "trident_fire_damage",
                "Fire Damage",
                EquipmentType.TRIDENT,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "trident_cold_damage",
                "Cold Damage",
                EquipmentType.MACE,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "trident_lightning_damage",
                "Lightning Damage",
                EquipmentType.TRIDENT,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "trident_void_damage",
                "Void Damage",
                EquipmentType.TRIDENT,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "trident_attack_damage_percent",
                "Increased Attack Damage",
                EquipmentType.TRIDENT,
                ModAttributes.ATTACK_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "trident_fire_damage_percent",
                "Increased Fire Damage",
                EquipmentType.TRIDENT,
                ModAttributes.FIRE_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "trident_cold_damage_percent",
                "Increased Cold Damage",
                EquipmentType.TRIDENT,
                ModAttributes.COLD_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "trident_lightning_damage_percent",
                "Increased Lightning Damage",
                EquipmentType.TRIDENT,
                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "trident_void_damage_percent",
                "Increased Void Damage",
                EquipmentType.TRIDENT,
                ModAttributes.VOID_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "trident_life_steal",
                "Life Steal",
                EquipmentType.TRIDENT,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.08, 0.11},
                new double[]{0.02, 0.05, 0.07, 0.10, 0.13},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "trident_knockback",
                "Knockback",
                EquipmentType.TRIDENT,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.2, 0.4, 0.7, 1.0},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.2},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "trident_attack_range",
                "Attack Range",
                EquipmentType.TRIDENT,
                ModAttributes.ATTACK_RANGE,
                new double[]{0.3, 0.5, 0.7, 0.9, 1.1},
                new double[]{0.4, 0.6, 0.8, 1.0, 1.2},
                false,
                Formatting.GREEN
        ));

        // ===== BOW STATS =====
        register(new CustomStat(
                "bow_durability_bonus",
                "Durability Bonus",
                EquipmentType.BOW,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "bow_attack_damage",
                "Attack Damage",
                EquipmentType.BOW,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "bow_crit_chance",
                "Critical Strike Chance",
                EquipmentType.BOW,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "bow_chance_to_burn",
                "Chance to burn",
                EquipmentType.BOW,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "bow_fire_damage",
                "Fire Damage",
                EquipmentType.BOW,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "bow_cold_damage",
                "Cold Damage",
                EquipmentType.BOW,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "bow_lightning_damage",
                "Lightning Damage",
                EquipmentType.BOW,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "bow_void_damage",
                "Void Damage",
                EquipmentType.BOW,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "bow_attack_damage_percent",
                "Increased Attack Damage",
                EquipmentType.BOW,
                ModAttributes.ATTACK_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "bow_fire_damage_percent",
                "Increased Fire Damage",
                EquipmentType.BOW,
                ModAttributes.FIRE_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "bow_cold_damage_percent",
                "Increased Cold Damage",
                EquipmentType.BOW,
                ModAttributes.COLD_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "bow_lightning_damage_percent",
                "Increased Lightning Damage",
                EquipmentType.BOW,
                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "bow_void_damage_percent",
                "Increased Void Damage",
                EquipmentType.BOW,
                ModAttributes.VOID_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "bow_life_steal",
                "Life Steal",
                EquipmentType.BOW,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.07, 0.09},
                new double[]{0.02, 0.04, 0.06, 0.08, 0.10},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "bow_knockback",
                "Knockback",
                EquipmentType.BOW,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.3, 0.5, 0.7, 0.9},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "bow_reload_speed",
                "Reload Speed",
                EquipmentType.BOW,
                ModAttributes.RELOAD_SPEED,
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                new double[]{0.4, 0.6, 0.8, 1.0, 1.27},
                true,
                true,
                Formatting.YELLOW
        ));

        // ===== CROSSBOW STATS ====
        register(new CustomStat(
                "crossbow_durability_bonus",
                "Durability Bonus",
                EquipmentType.CROSSBOW,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "crossbow_attack_damage",
                "Attack Damage",
                EquipmentType.CROSSBOW,
                EntityAttributes.ATTACK_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "crossbow_crit_chance",
                "Critical Strike Chance",
                EquipmentType.CROSSBOW,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.01, 0.03, 0.06, 0.10, 0.15},
                new double[]{0.02, 0.05, 0.09, 0.14, 0.22},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "crossbow_chance_to_burn",
                "Chance to burn",
                EquipmentType.CROSSBOW,
                ModAttributes.CHANCE_TO_BURN,
                new double[]{0.05, 0.12, 0.20, 0.30, 0.42},
                new double[]{0.10, 0.20, 0.30, 0.45, 0.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "crossbow_fire_damage",
                "Fire Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.FIRE_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.RED
        ));

        register(new CustomStat(
                "crossbow_cold_damage",
                "Cold Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.COLD_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "crossbow_lightning_damage",
                "Lightning Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.LIGHTNING_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "crossbow_void_damage",
                "Void Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.VOID_DAMAGE,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 3.0, 5.5, 9.0, 14.0},
                false,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "crossbow_attack_damage_percent",
                "Increased Attack Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.ATTACK_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "crossbow_fire_damage_percent",
                "Increased Fire Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.FIRE_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "crossbow_cold_damage_percent",
                "Increased Cold Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.COLD_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "crossbow_lightning_damage_percent",
                "Increased Lightning Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.LIGHTNING_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "crossbow_void_damage_percent",
                "Increased Void Damage",
                EquipmentType.CROSSBOW,
                ModAttributes.VOID_DAMAGE_PERCENT,
                new double[]{0.01, 0.20, 0.40, 0.60, 1.10},
                new double[]{0.20, 0.40, 0.60, 1.00, 1.65},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        register(new CustomStat(
                "crossbow_life_steal",
                "Life Steal",
                EquipmentType.CROSSBOW,
                ModAttributes.LIFE_STEAL,
                new double[]{0.01, 0.03, 0.05, 0.07, 0.09},
                new double[]{0.02, 0.04, 0.06, 0.08, 0.10},
                true,
                true,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "crossbow_knockback",
                "Knockback",
                EquipmentType.CROSSBOW,
                ModAttributes.KNOCKBACK,
                new double[]{0.1, 0.3, 0.5, 0.7, 0.9},
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "crossbow_reload_speed",
                "Reload Speed",
                EquipmentType.CROSSBOW,
                ModAttributes.RELOAD_SPEED,
                new double[]{0.2, 0.4, 0.6, 0.8, 1.0},
                new double[]{0.4, 0.6, 0.8, 1.0, 1.27},
                true,
                true,
                Formatting.YELLOW
        ));

        // ===== SHIELD STATS =====
        register(new CustomStat(
                "shield_durability_bonus",
                "Durability Bonus",
                EquipmentType.SHIELD,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "shield_armor",
                "Armor",
                EquipmentType.SHIELD,
                EntityAttributes.ARMOR,
                new double[]{0.5, 1.5, 3.0, 5.0, 8.0},
                new double[]{1.0, 2.5, 4.5, 7.5, 12.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "shield_armor_toughness",
                "Armor Toughness",
                EquipmentType.SHIELD,
                EntityAttributes.ARMOR_TOUGHNESS,
                new double[]{0.3, 0.8, 1.5, 2.5, 4.0},
                new double[]{0.6, 1.4, 2.4, 4.0, 6.0},
                false,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "shield_knockback_resistance",
                "Knockback Resistance",
                EquipmentType.SHIELD,
                ModAttributes.KNOCKBACK_RESISTANCE,
                new double[]{0.05, 0.10, 0.15, 0.20, 0.25},
                new double[]{0.10, 0.15, 0.20, 0.25, 0.35},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "shield_fire_resistance",
                "Fire Resistance",
                EquipmentType.SHIELD,
                ModAttributes.FIRE_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.RED
        ));

        register(new CustomStat(
                "shield_cold_resistance",
                "Cold Resistance",
                EquipmentType.SHIELD,
                ModAttributes.COLD_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "shield_lightning_resistance",
                "Lightning Resistance",
                EquipmentType.SHIELD,
                ModAttributes.LIGHTNING_RESISTANCE,
                new double[]{0.15, 0.19, 0.23, 0.26, 0.30},
                new double[]{0.19, 0.23, 0.26, 0.30, 0.38},
                true,
                true,
                Formatting.DARK_BLUE
        ));

        register(new CustomStat(
                "shield_void_resistance",
                "Void Resistance",
                EquipmentType.SHIELD,
                ModAttributes.VOID_RESISTANCE,
                new double[]{0.05, 0.1, 0.15, 0.2, 0.25},
                new double[]{0.1, 0.15, 0.2, 0.25, 0.3},
                true,
                true,
                Formatting.DARK_PURPLE
        ));

        // ===== ELYTRA STATS =====
        register(new CustomStat(
                "elytra_flying_speed",
                "Flying Speed",
                EquipmentType.ELYTRA,
                ModAttributes.FLYING_SPEED,
                new double[]{0.05, 0.10, 0.15, 0.20, 0.30},
                new double[]{0.08, 0.15, 0.22, 0.30, 0.45},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "elytra_durability_bonus",
                "Durability Bonus",
                EquipmentType.ELYTRA,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "elytra_energy_shield",
                "Energy Shield",
                EquipmentType.ELYTRA,
                ModAttributes.ENERGY_SHIELD,
                new double[]{1.0, 2.0, 3.0, 5.0, 7.0},
                new double[]{2.0, 3.0, 5.0, 7.0, 10.0},
                false,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "elytra_life_regeneration",
                "Health Regeneration",
                EquipmentType.ELYTRA,
                ModAttributes.LIFE_REGENERATION,
                new double[]{0.1, 0.25, 0.5, 0.8, 1.2},
                new double[]{0.2, 0.45, 0.75, 1.2, 2.0},
                false,
                Formatting.DARK_RED
        ));

        register(new CustomStat(
                "elytra_crit_chance",
                "Critical Strike Chance",
                EquipmentType.ELYTRA,
                ModAttributes.CRIT_CHANCE,
                new double[]{0.10, 0.13, 0.16, 0.19, 0.23},
                new double[]{0.12, 0.15, 0.18, 0.21, 0.30},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "elytra_movement_speed",
                "Movement Speed",
                EquipmentType.ELYTRA,
                ModAttributes.MOVEMENT_SPEED,
                new double[]{0.01, 0.1, 0.2, 0.3, 0.4},
                new double[]{0.05, 0.15, 0.25, 0.35, 0.6},
                true,
                true,
                Formatting.GREEN
        ));

        register(new CustomStat(
                "elytra_feather_falling",
                "Feather Falling",
                EquipmentType.ELYTRA,
                ModAttributes.FEATHER_FALLING,
                new double[]{0.05, 0.15, 0.25, 0.35, 0.45},
                new double[]{0.10, 0.20, 0.30, 0.40, 0.50},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "elytra_experience_bonus",
                "Experience Bonus",
                EquipmentType.ELYTRA,
                ModAttributes.EXPERIENCE_BONUS,
                new double[]{0.30, 0.40, 0.50, 0.60, 0.70},
                new double[]{0.40, 0.50, 0.60, 0.70, 0.80},
                true,
                true,
                Formatting.DARK_GREEN
        ));

        register(new CustomStat(
                "elytra_luck",
                "Luck",
                EquipmentType.ELYTRA,
                ModAttributes.LUCK,
                new double[]{2.25, 2.75, 3.5, 4.5, 6.0},
                new double[]{2.5, 3.25, 4.25, 5.75, 8.0},
                false,
                false,
                Formatting.DARK_GREEN
        ));

        // ===== PICKAXE STATS =====
        register(new CustomStat(
                "pickaxe_mining_efficiency",
                "Mining Speed",
                EquipmentType.PICKAXE,
                ModAttributes.MINING_EFFICIENCY,
                new double[]{0.5, 1.0, 1.5, 2.0, 2.5},
                new double[]{1.0, 1.5, 2.00, 2.50, 3.0},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "pickaxe_fortune",
                "Ore Fortune",
                EquipmentType.PICKAXE,
                ModAttributes.FORTUNE,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.55},
                new double[]{0.15, 0.28, 0.40, 0.55, 0.75},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "pickaxe_durability_bonus",
                "Durability Bonus",
                EquipmentType.PICKAXE,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.WHITE
        ));

        register(new CustomStat(
                "pickaxe_repair_chance",
                "Repair Chance",
                EquipmentType.PICKAXE,
                ModAttributes.PRECISION_MINING,
                new double[]{0.03, 0.08, 0.13, 0.18, 0.25},
                new double[]{0.05, 0.12, 0.18, 0.25, 0.35},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "pickaxe_breaking_range",
                "Breaking Range",
                EquipmentType.PICKAXE,
                ModAttributes.BREAKING_RANGE,
                new double[]{0.3, 0.7, 1.1, 1.5, 1.8},
                new double[]{0.5, 0.9, 1.3, 1.7, 2.0},
                false,
                Formatting.GREEN
        ));

        // ===== AXE STATS =====
        register(new CustomStat(
                "axe_mining_efficiency",
                "Chopping Speed",
                EquipmentType.AXE,
                ModAttributes.MINING_EFFICIENCY,
                new double[]{0.5, 1.0, 1.5, 2.0, 2.5},
                new double[]{1.0, 1.5, 2.00, 2.50, 3.0},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "axe_durability_bonus",
                "Durability Bonus",
                EquipmentType.AXE,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "axe_repair_chance",
                "Repair Chance",
                EquipmentType.AXE,
                ModAttributes.PRECISION_MINING,
                new double[]{0.03, 0.08, 0.13, 0.18, 0.25},
                new double[]{0.05, 0.12, 0.18, 0.25, 0.35},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "axe_breaking_range",
                "Breaking Range",
                EquipmentType.AXE,
                ModAttributes.BREAKING_RANGE,
                new double[]{0.3, 0.7, 1.1, 1.5, 1.8},
                new double[]{0.5, 0.9, 1.3, 1.7, 2.0},
                false,
                Formatting.GREEN
        ));

        register(new CustomStat(
                "axe_stick_drop_chance",
                "Stick Drop Chance",
                EquipmentType.AXE,
                ModAttributes.STICK_DROP_CHANCE,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.55},
                new double[]{0.15, 0.28, 0.40, 0.55, 0.75},
                true,
                true,
                Formatting.YELLOW
        ));

        // ===== SHOVEL STATS =====
        register(new CustomStat(
                "shovel_mining_efficiency",
                "Digging Speed",
                EquipmentType.SHOVEL,
                ModAttributes.MINING_EFFICIENCY,
                new double[]{0.5, 1.0, 1.5, 2.0, 2.5},
                new double[]{1.0, 1.5, 2.00, 2.50, 3.0},
                true,
                true,
                Formatting.GRAY
        ));

        register(new CustomStat(
                "shovel_durability_bonus",
                "Durability Bonus",
                EquipmentType.SHOVEL,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "shovel_repair_chance",
                "Repair Chance",
                EquipmentType.SHOVEL,
                ModAttributes.PRECISION_MINING,
                new double[]{0.03, 0.08, 0.13, 0.18, 0.25},
                new double[]{0.05, 0.12, 0.18, 0.25, 0.35},
                true,
                true,
                Formatting.AQUA
        ));
        register(new CustomStat(
                "shovel_treasure_find",
                "Treasure Find Chance",
                EquipmentType.SHOVEL,
                ModAttributes.TREASURE_FIND,
                new double[]{0.02, 0.04, 0.06, 0.08, 0.1},
                new double[]{0.04, 0.06, 0.08, 0.1, 0.15},
                true,
                true,
                Formatting.YELLOW
        ));

        register(new CustomStat(
                "shovel_breaking_range",
                "Breaking Range",
                EquipmentType.SHOVEL,
                ModAttributes.BREAKING_RANGE,
                new double[]{0.3, 0.7, 1.1, 1.5, 1.8},
                new double[]{0.5, 0.9, 1.3, 1.7, 2.0},
                false,
                Formatting.GREEN
        ));

        // ===== HOE STATS =====
        register(new CustomStat(
                "hoe_durability_bonus",
                "Durability Bonus",
                EquipmentType.HOE,
                ModAttributes.DURABILITY_BONUS,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.56},
                new double[]{0.18, 0.26, 0.36, 0.55, 0.76},
                true,
                true,
                Formatting.BLUE
        ));

        register(new CustomStat(
                "hoe_breaking_range",
                "Breaking Range",
                EquipmentType.HOE,
                ModAttributes.BREAKING_RANGE,
                new double[]{0.3, 0.7, 1.1, 1.5, 1.8},
                new double[]{0.5, 0.9, 1.3, 1.7, 2.0},
                false,
                Formatting.GREEN
        ));

        register(new CustomStat(
                "hoe_repair_chance",
                "Repair Chance",
                EquipmentType.HOE,
                ModAttributes.PRECISION_MINING,
                new double[]{0.03, 0.08, 0.13, 0.18, 0.25},
                new double[]{0.05, 0.12, 0.18, 0.25, 0.35},
                true,
                true,
                Formatting.AQUA
        ));

        register(new CustomStat(
                "hoe_crop_fortune",
                "Crop Fortune",
                EquipmentType.HOE,
                ModAttributes.CROP_FORTUNE,
                new double[]{0.10, 0.18, 0.28, 0.40, 0.55},
                new double[]{0.15, 0.28, 0.40, 0.55, 0.75},
                true,
                true,
                Formatting.GOLD
        ));

        register(new CustomStat(
                "hoe_crop_experience",
                "Harvest Experience",
                EquipmentType.HOE,
                ModAttributes.CROP_EXPERIENCE,
                new double[]{0.10, 0.25, 0.45, 0.70, 1.00},
                new double[]{0.20, 0.40, 0.70, 1.00, 1.50},
                false,
                Formatting.DARK_GREEN
        ));

        System.out.println("Registered " + STATS.size() + " custom stats");
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
        return stats.isEmpty() ? null : stats.get(new Random().nextInt(stats.size()));
    }
}