package com.cranks.dungeons.registry;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModAttributes {

    public static final String MOD_ID = "cranks-dungeons";

    // Defensive stats
    public static final RegistryEntry<EntityAttribute> FIRE_RESISTANCE = register("fire_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> COLD_RESISTANCE = register("cold_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_RESISTANCE = register("lightning_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> VOID_RESISTANCE = register("void_resistance", 0.0, -1.0, 0.75);
    public static final RegistryEntry<EntityAttribute> KNOCKBACK_RESISTANCE = register("knockback_resistance", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BONUS_ARMOR = register("bonus_armor", 0.0, 0.0, 120.0);
    public static final RegistryEntry<EntityAttribute> BONUS_ARMOR_TOUGHNESS = register("bonus_armor_toughness", 0.0, 0.0, 120.0);
    public static final RegistryEntry<EntityAttribute> LIFE_REGENERATION = register("life_regeneration", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> MAX_HEALTH_BONUS = register("max_health_bonus", 0.0, 0.0, 1024.0);
    public static final RegistryEntry<EntityAttribute> EVASION_RATING = register("evasion_rating", 0.0, 0.0, 2000.0);
    public static final RegistryEntry<EntityAttribute> ENERGY_SHIELD = register("energy_shield", 0.0, 0.0, 1024.0);

    // Offensive stats
    public static final RegistryEntry<EntityAttribute> CRIT_CHANCE = register("crit_chance", 0.05, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> CRIT_MULTIPLIER = register("crit_multiplier", 1.5, 1.0, 10.0);
    public static final RegistryEntry<EntityAttribute> BONUS_ATTACK_DAMAGE = register("bonus_attack_damage", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> FIRE_DAMAGE = register("fire_damage", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> COLD_DAMAGE = register("cold_damage", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_DAMAGE = register("lightning_damage", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> VOID_DAMAGE = register("void_damage", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> RELOAD_SPEED = register("reload_speed", 0.0, 0.0, 10.0);

    // Percentual damage (stored as item stats, not used as attributes directly)
    public static final RegistryEntry<EntityAttribute> ATTACK_DAMAGE_PERCENT = register("attack_damage_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> FIRE_DAMAGE_PERCENT = register("fire_damage_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> COLD_DAMAGE_PERCENT = register("cold_damage_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_DAMAGE_PERCENT = register("lightning_damage_percent", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> VOID_DAMAGE_PERCENT = register("void_damage_percent", 0.0, 0.0, 10.0);

    // Proportional Extra Damage percentages
    public static final RegistryEntry<EntityAttribute> PHYSICAL_EXTRA_PERCENT = register("physical_extra_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> FIRE_EXTRA_PERCENT = register("fire_extra_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> COLD_EXTRA_PERCENT = register("cold_extra_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> LIGHTNING_EXTRA_PERCENT = register("lightning_extra_percent", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> VOID_EXTRA_PERCENT = register("void_extra_percent", 0.0, 0.0, 5.0);

    public static final RegistryEntry<EntityAttribute> LIFE_STEAL = register("life_steal", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> CHANCE_TO_BURN = register("chance_to_burn", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BURN_BASE_DAMAGE = register("burn_base_damage", 4.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> BURN_DAMAGE_PERCENT = register("burn_damage_percent", 100.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> BURN_DURATION = register("burn_duration", 0.0, 0.0, 10.0);

    public static final RegistryEntry<EntityAttribute> CHANCE_TO_POISON = register("chance_to_poison", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> POISON_BASE_DAMAGE = register("poison_base_damage", 1.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> POISON_DAMAGE_PERCENT = register("poison_damage_percent", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> POISON_DURATION = register("poison_duration", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> POISON_MAX_STACKS = register("poison_max_stacks", 0.0, 0.0, 100.0);

    public static final RegistryEntry<EntityAttribute> CHANCE_TO_FREEZE = register("chance_to_freeze", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> FREEZE_DURATION = register("freeze_duration", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> CHANCE_TO_BLEED = register("chance_to_bleed", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BLEED_DURATION = register("bleed_duration", 0.0, 0.0, 10.0);

    public static final RegistryEntry<EntityAttribute> CHANCE_TO_SHOCK = register("chance_to_shock", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> SHOCK_DURATION = register("shock_duration", 3.0, 0.0, 60.0);

    public static final RegistryEntry<EntityAttribute> ATTACK_RANGE = register("attack_range", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> ATTACK_SPEED = register("attack_speed", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> KNOCKBACK = register("knockback", 0.0, 0.0, 2.0);

    // Utility stats
    public static final RegistryEntry<EntityAttribute> MINING_EFFICIENCY = register("mining_efficiency", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> FORTUNE = register("fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> DURABILITY_BONUS = register("durability_bonus", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> EXPERIENCE_BONUS = register("experience_bonus", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> PRECISION_MINING = register("precision_mining", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> BREAKING_RANGE = register("breaking_range", 0.0, 0.0, 5.0);
    public static final RegistryEntry<EntityAttribute> MOVEMENT_SPEED = register("movement_speed", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> FLYING_SPEED = register("flying_speed", 0.0, 0.0, 10.0);
    public static final RegistryEntry<EntityAttribute> LUCK = register("luck", 0.0, 0.0, 100.0);
    public static final RegistryEntry<EntityAttribute> CROP_FORTUNE = register("crop_fortune",0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> LOG_FORTUNE = register("log_fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> DIG_FORTUNE = register("dig_fortune", 0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> CROP_EXPERIENCE = register("crop_experience",0.0, 0.0, 1.0);
    public static final RegistryEntry<EntityAttribute> FEATHER_FALLING = register("feather_falling", 0.0, 0.0, 0.75);

    public static void registerAttributes() {
        // Attributes are registered via the static initializer
    }

    private static RegistryEntry<EntityAttribute> register(String name, double base, double min, double max) {
        Identifier id = Identifier.of(MOD_ID, name);

        EntityAttribute attribute = new ClampedEntityAttribute("attribute.name." + MOD_ID + "." + name, base, min, max).setTracked(true);

        Registry.register(Registries.ATTRIBUTE, id, attribute);

        return Registries.ATTRIBUTE.getEntry(id).orElseThrow(() -> new IllegalStateException("Failed to register attribute: " + name));
    }
}