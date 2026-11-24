package com.cranks.dungeons.stat.passive;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Formats a node's additive bonus, never a player's total or an equipment-local roll. */
public final class NodeBonusFormat {
    private static final Set<String> RATES = Set.of("attack_speed", "movement_speed", "projectile_speed",
            "mana_regeneration_rate", "mana_recovery_rate", "mining_efficiency", "world_discovery_experience",
            "riding_speed", "mount_recovery", "riding_agility", "knockback", "experience_bonus");
    private NodeBonusFormat() {}

    public static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
    private static String percent(double value) {
        return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
    }
    private static String signed(double value) { return (value >= 0 ? "+" : "") + number(value); }
    private static String rate(double value, String name) {
        return percent(Math.abs(value)) + "% " + (value >= 0 ? "increased " : "reduced ") + name;
    }
    private static String reduction(double value, String name) { return rate(-value, name); }
    private static String points(double value, String name) { return (value >= 0 ? "+" : "") + percent(value) + "% to " + name; }

    public static String name(String id) {
        id = id.substring(id.indexOf(':') + 1);
        return switch (id) {
            case "bonus_armor" -> "Armor";
            case "max_health_bonus", "max_health_percent" -> "maximum Health";
            case "max_mana" -> "maximum Mana";
            case "energy_shield", "energy_shield_percent" -> "maximum Energy Shield";
            case "global_crit_chance_percent", "crit_chance" -> "Critical Hit Chance";
            case "crit_multiplier" -> "Critical Damage Bonus";
            case "flat_armor_penetration" -> "Armor Penetration";
            case "mana_regeneration_rate" -> "Mana Regeneration Rate";
            case "riding_speed" -> "Mount Movement Speed";
            case "mount_recovery" -> "Mount Resource Regeneration";
            case "riding_agility" -> "Mount Acceleration and Braking";
            case "mining_efficiency" -> "Gathering Speed";
            default -> Stream.of(id.replaceFirst("^(keystone_|increased_)", "").replaceFirst("_percent$", "").split("_"))
                    .map(word -> word.isEmpty() ? word : word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                    .collect(Collectors.joining(" "));
        };
    }

    public static String format(String id, double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Node bonuses must be finite");
        id = id.substring(id.indexOf(':') + 1);
        String label = name(id), pct = percent(value), amount = number(value);
        if (id.startsWith("keystone_") || id.equals("ailment_threshold_from_boots_lowest_armor_evasion"))
            return (value > 0 ? "Grants " : "Does not grant ") + label;
        String specific = switch (id) {
            case "crit_multiplier", "crit_chance" -> points(value, label);
            case "health_on_kill" -> "Recover " + amount + " Health on Kill";
            case "mana_on_kill" -> "Recover " + amount + " Mana on Kill";
            case "health_on_kill_percent" -> "Recover " + pct + "% of maximum Health on Kill";
            case "life_regeneration" -> "Regenerate " + amount + " Health per second";
            case "mana_regeneration" -> "Regenerate " + amount + " Mana per second";
            case "max_health_regeneration_percent" -> "Regenerate " + pct + "% of maximum Health per second";
            case "mana_regeneration_percent" -> "Regenerate " + pct + "% of maximum Mana per second";
            case "physical_damage_leeched_as_life" -> pct + "% of Physical Damage Leeched as Health";
            case "physical_damage_leeched_as_mana" -> pct + "% of Physical Damage Leeched as Mana";
            case "faster_start_of_energy_shield_recharge" -> pct + "% faster start of Energy Shield Recharge";
            case "leggings_armor_percent" -> rate(value, "Armor from equipped Leggings");
            case "armor_applies_to_elemental" -> pct + "% of Armor also applies to Elemental Damage";
            case "armor_speed_penalty_reduction" -> reduction(value, "Movement Penalty from Armor");
            case "low_armor_speed_bonus" -> pct + "% increased Movement Speed per Armor below 150";
            case "armor_crit_mitigation" -> pct + "% Critical Protection per Armor";
            case "damage_after_crit_percent" -> rate(value, "Damage for 8 seconds after a Critical Hit");
            case "damage_against_afflicted_percent" -> rate(value, "Damage against Enemies affected by Ailments");
            case "damage_while_afflicted_percent" -> rate(value, "Damage while affected by an Ailment");
            case "reduced_crit_damage_taken_percent" -> reduction(value, "Critical Damage Bonus taken");
            case "knockback_resistance_when_full_life" -> points(value, "Knockback Resistance while on Full Health");
            case "knockback_resistance_per_250_energy_shield" -> points(value, "Knockback Resistance per 250 maximum Energy Shield");
            case "close_range_knockback_percent" -> rate(value, "Knockback against enemies within 4 blocks");
            case "regeneration_rate_while_moving_percent" -> rate(value, "Health Regeneration Rate while moving");
            case "crit_non_damaging_ailment_magnitude_percent" -> rate(value, "Non-Damaging Ailment Magnitude from Critical Hits");
            case "increased_ailment_chance_percent" -> rate(value, "chance to inflict Poison, Bleed and Shock");
            case "crit_chance_against_afflicted" -> points(value, "Critical Hit Chance against afflicted enemies");
            case "projectile_ricochet_chance" -> pct + "% chance for projectiles to redirect from terrain to a visible enemy within 12 blocks";
            case "projectile_pierce_chance" -> pct + "% chance for arrows to pierce an additional enemy";
            case "ailment_threshold_percent_of_max_es" -> "Gain " + pct + "% of maximum Energy Shield as additional Ailment Threshold";
            case "ailment_threshold_percent_of_max_health" -> "Gain " + pct + "% of maximum Health as additional Ailment Threshold";
            case "elemental_ailment_threshold_per_dexterity" -> signed(value) + " to Elemental Ailment Threshold per Dexterity";
            case "poison_max_stacks" -> signed(value) + " to maximum Poison Stacks";
            case "physical_taken_as_void" -> pct + "% of Physical Damage taken as Void Damage";
            case "mount_endurance" -> reduction(value, "Mount Sprint Resource Drain");
            case "sure_footing" -> reduction(value, "Mount Fall Damage taken");
            case "breath_efficiency" -> pct + "% chance to prevent underwater air loss";
            case "travel_efficiency" -> reduction(value, "Hunger Expenditure from movement");
            case "crop_replant_chance" -> pct + "% chance to replant harvested mature Crops";
            case "precision_mining", "repair_chance" -> pct + "% chance to avoid Tool Durability loss";
            case "breaking_range" -> signed(value) + " metres to Block Interaction Reach";
            default -> null;
        };
        if (specific != null) return specific;
        if (id.endsWith("_extra_percent")) return "Gain " + pct + "% of Physical Damage as Extra " + name(id.replace("_extra_percent", "")) + " Damage";
        if (id.startsWith("physical_as_extra_")) return "Gain " + pct + "% of Physical Damage as Extra " + name(id.replace("physical_as_extra_", "").replace("_percent", "")) + " Damage";
        if (id.startsWith("physical_to_") && id.endsWith("_conversion_percent")) return "Convert " + pct + "% of Physical Damage to " + name(id.replace("physical_to_", "").replace("_conversion_percent", "")) + " Damage";
        if (id.startsWith("chance_to_")) return pct + "% chance to " + name(id.replace("chance_to_", ""));
        if (id.endsWith("_resistance")) return points(value, label);
        if (id.endsWith("_penetration_percent") && !id.equals("armor_penetration_percent")) return points(value, label);
        if (id.contains("chance")) return rate(value, label);
        if (id.endsWith("_percent") || id.contains("yield") || id.contains("rarity") || id.contains("fortune")
                || id.endsWith("experience") || RATES.contains(id)) return rate(value, label);
        return signed(value) + " to " + label;
    }
}
