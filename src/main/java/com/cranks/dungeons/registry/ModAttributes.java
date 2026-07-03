package com.cranks.dungeons.registry;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import com.cranks.dungeons.CranksDungeons;

public class ModAttributes {

    public static final String MOD_ID = CranksDungeons.MOD_ID;
    
    public static final List<RegistryEntry<EntityAttribute>> ALL_ATTRIBUTES = new ArrayList<>();

    // Defensive stats
    public static final RegistryEntry<EntityAttribute> FIRE_RESISTANCE = register("fire_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> COLD_RESISTANCE = register("cold_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_RESISTANCE = register("lightning_resistance", 0.0,
            -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> VOID_RESISTANCE = register("void_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> KNOCKBACK_RESISTANCE = register("knockback_resistance", 0.0, 0.0,
            1.0);
    public static final RegistryEntry<EntityAttribute> BONUS_ARMOR = register("bonus_armor", 0.0, 0.0, 1000.0);
    public static final RegistryEntry<EntityAttribute> ARMOR_APPLIES_TO_ELEMENTAL = register("armor_applies_to_elemental", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> LIFE_REGENERATION = register("life_regeneration", 0.0, 0.0,
            100.0);
    public static final RegistryEntry<EntityAttribute> MAX_HEALTH_BONUS = register("max_health_bonus", 0.0, 0.0,
            1024.0);
    public static final RegistryEntry<EntityAttribute> EVASION_RATING = register("evasion_rating", 0.0, 0.0, 2000.0);
    public static final RegistryEntry<EntityAttribute> ENERGY_SHIELD = register("energy_shield", 0.0, 0.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> ARMOR_CRIT_MITIGATION = register("armor_crit_mitigation", 0.0,
            0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> THORNS_DAMAGE = register("thorns_damage", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> ENCHANTMENT_EFFECTIVENESS = register("enchantment_effectiveness", 0.0, 0.0, 10.0);

    // Offensive stats
    public static final RegistryEntry<EntityAttribute> CRIT_CHANCE = register("crit_chance", 0.05, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> CRIT_MULTIPLIER = register("crit_multiplier", 1.5, 1.0, 10.0);
    public static final RegistryEntry<EntityAttribute> BONUS_ATTACK_DAMAGE = register("bonus_attack_damage", 0.0, 0.0,
            10000.0);
    public static final RegistryEntry<EntityAttribute> FIRE_DAMAGE = register("fire_damage", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> COLD_DAMAGE = register("cold_damage", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_DAMAGE = register("lightning_damage", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> VOID_DAMAGE = register("void_damage", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> RELOAD_SPEED = register("reload_speed", 1.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> PROJECTILE_SPEED = register("projectile_speed", 0.0, 0.0, 10.0);

    // Percentual damage (stored as item stats, not used as attributes directly)
    public static final RegistryEntry<EntityAttribute> ATTACK_DAMAGE_PERCENT = register("attack_damage_percent", 0.0,
            0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> FIRE_DAMAGE_PERCENT = register("fire_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> COLD_DAMAGE_PERCENT = register("cold_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_DAMAGE_PERCENT = register("lightning_damage_percent",
            0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> VOID_DAMAGE_PERCENT = register("void_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> DAMAGE_AGAINST_AFFLICTED_PERCENT = register("damage_against_afflicted_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> DAMAGE_WHILE_AFFLICTED_PERCENT = register("damage_while_afflicted_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> CRIT_CHANCE_AGAINST_AFFLICTED = register("crit_chance_against_afflicted", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> GLOBAL_CRIT_CHANCE_PERCENT = register("global_crit_chance_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ELEMENTAL_DAMAGE_PERCENT = register("elemental_damage_percent",
            0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> MELEE_DAMAGE_PERCENT = register("melee_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> RANGED_DAMAGE_PERCENT = register("ranged_damage_percent", 0.0,
            0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ONE_HANDED_DAMAGE_PERCENT = register("one_handed_damage_percent",
            0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> DUAL_WIELDING_DAMAGE_PERCENT = register("dual_wielding_damage_percent",
            0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> TWO_HANDING_DAMAGE_PERCENT = register("two_handing_damage_percent",
            0.0, 0.0, 10.0);

    // Proportional Extra Damage percentages
    public static final RegistryEntry<EntityAttribute> PHYSICAL_EXTRA_PERCENT = register("physical_extra_percent", 0.0,
            0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> FIRE_EXTRA_PERCENT = register("fire_extra_percent", 0.0, 0.0,
            5.0);
    public static final RegistryEntry<EntityAttribute> COLD_EXTRA_PERCENT = register("cold_extra_percent", 0.0, 0.0,
            5.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_EXTRA_PERCENT = register("lightning_extra_percent",
            0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> VOID_EXTRA_PERCENT = register("void_extra_percent", 0.0, 0.0,
            5.0);

    public static final RegistryEntry<EntityAttribute> PHYSICAL_AS_EXTRA_FIRE_PERCENT = register(
            "physical_as_extra_fire_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> PHYSICAL_AS_EXTRA_COLD_PERCENT = register(
            "physical_as_extra_cold_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> PHYSICAL_AS_EXTRA_LIGHTNING_PERCENT = register(
            "physical_as_extra_lightning_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> PHYSICAL_AS_EXTRA_VOID_PERCENT = register(
            "physical_as_extra_void_percent", 0.0, 0.0, 5.0);

    // Pure Damage Conversions
    public static final RegistryEntry<EntityAttribute> PHYSICAL_TO_FIRE_CONVERSION_PERCENT = register(
            "physical_to_fire_conversion_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> PHYSICAL_TO_COLD_CONVERSION_PERCENT = register(
            "physical_to_cold_conversion_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> PHYSICAL_TO_LIGHTNING_CONVERSION_PERCENT = register(
            "physical_to_lightning_conversion_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> PHYSICAL_TO_VOID_CONVERSION_PERCENT = register(
            "physical_to_void_conversion_percent", 0.0, 0.0, 1.0);

    public static final RegistryEntry<EntityAttribute> PHYSICAL_DAMAGE_LEECHED_AS_LIFE = register("physical_damage_leeched_as_life", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BURN_MAGNITUDE_PERCENT = register("burn_magnitude_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> BURN_DURATION = register("burn_duration", 0.0, 0.0, 10.0);

    public static final RegistryEntry<EntityAttribute> POISON_MAGNITUDE_PERCENT = register("poison_magnitude_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> POISON_DURATION = register("poison_duration", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> POISON_MAX_STACKS = register("poison_max_stacks", 0.0, 0.0, 100.0);

    public static final RegistryEntry<EntityAttribute> FREEZE_MAGNITUDE_PERCENT = register("freeze_magnitude_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> FREEZE_DURATION = register("freeze_duration", 0.0, 0.0, 10.0);
    
    public static final RegistryEntry<EntityAttribute> BLEED_DURATION = register("bleed_duration", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> BLEED_MAGNITUDE_PERCENT = register("bleed_magnitude_percent", 0.0, 0.0, 100.0);

    public static final RegistryEntry<EntityAttribute> SHOCK_DURATION = register("shock_duration", 3.0, 0.0, 60.0);
    public static final RegistryEntry<EntityAttribute> SHOCK_MAGNITUDE_PERCENT = register("shock_magnitude_percent", 0.0, 0.0, 10.0);
    
    public static final RegistryEntry<EntityAttribute> SHOCK_THRESHOLD_REDUCTION = register("shock_threshold_reduction", 0.0, 0.0, 0.99);
    public static final RegistryEntry<EntityAttribute> FREEZE_THRESHOLD_REDUCTION = register("freeze_threshold_reduction", 0.0, 0.0, 0.99);
    public static final RegistryEntry<EntityAttribute> ELECTROCUTE_THRESHOLD_REDUCTION = register("electrocute_threshold_reduction", 0.0, 0.0, 0.99);
    
    public static final RegistryEntry<EntityAttribute> ELECTROCUTE_MAGNITUDE_PERCENT = register("electrocute_magnitude_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ELECTROCUTE_DURATION = register("electrocute_duration", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> MAX_HEALTH_PERCENT = register("max_health_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ARMOR_PERCENT = register("armor_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ENERGY_SHIELD_PERCENT = register("energy_shield_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> EVASION_RATING_PERCENT = register("evasion_rating_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ARMOR_PENETRATION_PERCENT = register("armor_penetration_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> ARMOR_SPEED_PENALTY_REDUCTION = register("armor_speed_penalty_reduction", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> LOW_ARMOR_SPEED_BONUS = register("low_armor_speed_bonus", 0.0, 0.0, 1.0);

    // Keystones
    public static final RegistryEntry<EntityAttribute> KEYSTONE_VOID_INOCULATION = register("keystone_void_inoculation", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> KEYSTONE_AVATAR_OF_FIRE = register("keystone_avatar_of_fire", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> KEYSTONE_RESOLUTE_TECHNIQUE = register("keystone_resolute_technique", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> KEYSTONE_ELEMENTAL_OVERLOAD = register("keystone_elemental_overload", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> KEYSTONE_CRIMSON_PACT = register("keystone_crimson_pact", 0.0, 0.0, 1.0);

    public static final RegistryEntry<EntityAttribute> FIRE_PENETRATION_PERCENT = register("fire_penetration_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> COLD_PENETRATION_PERCENT = register("cold_penetration_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_PENETRATION_PERCENT = register("lightning_penetration_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> VOID_PENETRATION_PERCENT = register("void_penetration_percent", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> ELEMENTAL_PENETRATION_PERCENT = register("elemental_penetration_percent", 0.0, 0.0, 1.0);

    public static final RegistryEntry<EntityAttribute> ATTACK_RANGE = register("attack_range", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ATTACK_SPEED = register("attack_speed", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> KNOCKBACK = register("knockback", 0.0, 0.0, 2.0);

    // Utility stats
    public static final RegistryEntry<EntityAttribute> MINING_EFFICIENCY = register("mining_efficiency", 1.0, 0.0,
            100.0);
    public static final RegistryEntry<EntityAttribute> FORTUNE = register("fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> DURABILITY_BONUS = register("durability_bonus", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> EXPERIENCE_BONUS = register("experience_bonus", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> PRECISION_MINING = register("precision_mining", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BREAKING_RANGE = register("breaking_range", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> MOVEMENT_SPEED = register("movement_speed", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> REPAIR_CHANCE = register("repair_chance", 0.0, 0.0, 1.0);

    // Core Base Attributes
    public static final RegistryEntry<EntityAttribute> STRENGTH = register("strength", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> DEXTERITY = register("dexterity", 0.0, 0.0, 10000.0);
    public static final RegistryEntry<EntityAttribute> INTELLIGENCE = register("intelligence", 0.0, 0.0, 10000.0);

    // Shield Block Mechanics
    public static final RegistryEntry<EntityAttribute> CHANCE_TO_BLOCK = register("chance_to_block", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BLOCK_VALUE = register("block_value", 0.0, 0.0, 100000.0);
    public static final RegistryEntry<EntityAttribute> HEALTH_ON_BLOCK = register("health_on_block", 0.0, 0.0, 10000.0);

    // Weapon-specific Damage Percentages (Passive-tree only)
    public static final RegistryEntry<EntityAttribute> SWORD_DAMAGE_PERCENT = register("sword_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> AXE_DAMAGE_PERCENT = register("axe_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> MACE_DAMAGE_PERCENT = register("mace_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> SPEAR_DAMAGE_PERCENT = register("spear_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> BOW_DAMAGE_PERCENT = register("bow_damage_percent", 0.0, 0.0,
            10.0);
    public static final RegistryEntry<EntityAttribute> CROSSBOW_DAMAGE_PERCENT = register("crossbow_damage_percent",
            0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> TRIDENT_DAMAGE_PERCENT = register("trident_damage_percent", 0.0,
            0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> LEGGINGS_ARMOR_PERCENT = register("leggings_armor_percent", 0.0,
            0.0, 10.0);

    public static final RegistryEntry<EntityAttribute> FLYING_SPEED = register("flying_speed", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ITEM_RARITY_PERCENT = register("item_rarity_percent", 0.0, 0.0, 1000.0);
    public static final RegistryEntry<EntityAttribute> CROP_FORTUNE = register("crop_fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> LOG_FORTUNE = register("log_fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> DIG_FORTUNE = register("dig_fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> CROP_EXPERIENCE = register("crop_experience", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> FEATHER_FALLING = register("feather_falling", 0.0, 0.0, 0.75);

    public static void registerAttributes() {
        // Attributes are registered via the static initializer
    }

    private static RegistryEntry<EntityAttribute> register(String name, double base, double min, double max) {
        Identifier id = Identifier.of(MOD_ID, name);

        EntityAttribute attribute = new ClampedEntityAttribute("attribute.name." + MOD_ID + "." + name, base, min, max)
                .setTracked(true);

        Registry.register(Registries.ATTRIBUTE, id, attribute);

        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(id)
                .orElseThrow(() -> new IllegalStateException("Failed to register attribute: " + name));
                
        ALL_ATTRIBUTES.add(entry);
        
        return entry;
    }
}