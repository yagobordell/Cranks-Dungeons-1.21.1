package com.cranks.dungeons.stat.passive;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PassiveIconManager {
    private static final Map<String, String> ATTRIBUTE_ICONS = new HashMap<>();
    private static final String DEFAULT_ICON = "passive/generic";

    static {
        // --- Offensive ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_chance", "passive/crit_chance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_multiplier", "passive/crit_multiplier");

        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_damage_percent", "passive/attack_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:melee_damage_percent", "passive/melee_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:ranged_damage_percent", "passive/ranged_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:one_handed_damage_percent", "passive/one_handed_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:dual_wielding_damage_percent", "passive/dual_wielding_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:two_handing_damage_percent", "passive/two_handing_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_speed", "passive/attack_speed");

        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback", "passive/knockback");
        ATTRIBUTE_ICONS.put("cranks-dungeons:life_steal", "passive/life_steal");
        ATTRIBUTE_ICONS.put("cranks-dungeons:reload_speed", "passive/reload_speed");

        // --- Weapon Specialization ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:sword_damage_percent", "passive/sword_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:axe_damage_percent", "passive/axe_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:mace_damage_percent", "passive/mace_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:spear_damage_percent", "passive/spear_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bow_damage_percent", "passive/bow_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:crossbow_damage_percent", "passive/crossbow_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:trident_damage_percent", "passive/trident_damage_percent");

        // --- Elemental Damage ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_damage_percent", "passive/fire_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_damage_percent", "passive/cold_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_damage_percent", "passive/lightning_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_damage_percent", "passive/void_damage_percent");

        // --- Extra Damage (Conversions) ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_extra_percent", "passive/physical_extra_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_extra_percent", "passive/fire_extra_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_extra_percent", "passive/cold_extra_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_extra_percent", "passive/lightning_extra_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_extra_percent", "passive/void_extra_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_as_extra_fire_percent", "passive/physical_as_extra_fire_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_as_extra_cold_percent", "passive/physical_as_extra_cold_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_as_extra_lightning_percent", "passive/physical_as_extra_lightning_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_as_extra_void_percent", "passive/physical_as_extra_void_percent");

        // --- Pure Damage Conversions ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_to_fire_conversion_percent", "passive/physical_to_fire_conversion_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_to_cold_conversion_percent", "passive/physical_to_cold_conversion_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_to_lightning_conversion_percent", "passive/physical_to_lightning_conversion_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_to_void_conversion_percent", "passive/physical_to_void_conversion_percent");

        // --- Penetration Modifiers ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:armor_penetration_percent", "passive/armor_penetration_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_penetration_percent", "passive/fire_penetration_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_penetration_percent", "passive/cold_penetration_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_penetration_percent", "passive/lightning_penetration_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_penetration_percent", "passive/void_penetration_percent");

        // --- Ailment Modifiers ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:damage_against_afflicted_percent", "passive/damage_against_afflicted_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:damage_while_afflicted_percent", "passive/damage_while_afflicted_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_chance_against_afflicted", "passive/crit_chance_against_afflicted");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_burn", "passive/chance_to_burn");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_damage_percent", "passive/burn_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_duration", "passive/burn_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_poison", "passive/chance_to_poison");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_damage_percent", "passive/poison_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_duration", "passive/poison_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_max_stacks", "passive/poison_max_stacks");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_bleed", "passive/chance_to_bleed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_duration", "passive/bleed_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_shock", "passive/chance_to_shock");
        ATTRIBUTE_ICONS.put("cranks-dungeons:shock_duration", "passive/shock_duration");

        // --- Defensive ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:max_health_bonus", "passive/max_health_bonus");
        ATTRIBUTE_ICONS.put("cranks-dungeons:life_regeneration", "passive/life_regeneration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bonus_armor", "passive/bonus_armor");
        ATTRIBUTE_ICONS.put("cranks-dungeons:energy_shield", "passive/energy_shield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:evasion_rating", "passive/evasion_rating");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback_resistance", "passive/knockback_resistance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:leggings_armor_percent", "passive/leggings_armor_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:armor_crit_mitigation", "passive/armor_crit_mitigation");
        ATTRIBUTE_ICONS.put("cranks-dungeons:max_health_percent", "passive/max_health_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:armor_percent", "passive/armor_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:energy_shield_percent", "passive/energy_shield_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:evasion_rating_percent", "passive/evasion_rating_percent");

        // --- Utility ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:movement_speed", "passive/movement_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:armor_speed_penalty_reduction", "passive/armor_speed_penalty_reduction");
        ATTRIBUTE_ICONS.put("cranks-dungeons:low_armor_speed_bonus", "passive/low_armor_speed_bonus");

        // --- Additional Modifiers ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_resistance", "passive/fire_resistance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_resistance", "passive/cold_resistance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_resistance", "passive/lightning_resistance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_resistance", "passive/void_resistance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:armor_applies_to_elemental", "passive/armor_applies_to_elemental");
        ATTRIBUTE_ICONS.put("cranks-dungeons:thorns_damage", "passive/thorns_damage");

        ATTRIBUTE_ICONS.put("cranks-dungeons:projectile_speed", "passive/projectile_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_base_damage", "passive/burn_base_damage");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_base_damage", "passive/poison_base_damage");
        ATTRIBUTE_ICONS.put("cranks-dungeons:freeze_buildup_percent", "passive/freeze_buildup_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_burst_damage_percent", "passive/bleed_burst_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:electrocute_buildup_percent", "passive/electrocute_buildup_percent");


        ATTRIBUTE_ICONS.put("cranks-dungeons:strength", "passive/strength");
        ATTRIBUTE_ICONS.put("cranks-dungeons:dexterity", "passive/dexterity");
        ATTRIBUTE_ICONS.put("cranks-dungeons:intelligence", "passive/intelligence");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_block", "passive/chance_to_block");
        ATTRIBUTE_ICONS.put("cranks-dungeons:block_value", "passive/block_value");
        ATTRIBUTE_ICONS.put("cranks-dungeons:health_on_block", "passive/health_on_block");

    }


    /**
     * Determine the icon for a node based on its modifiers or its ID.
     */
    public static Identifier getIcon(String nodeId, Map<RegistryEntry<EntityAttribute>, Double> modifiers) {
        // Priority 1: Check modifiers
        if (modifiers != null && !modifiers.isEmpty()) {
            for (RegistryEntry<EntityAttribute> entry : modifiers.keySet()) {
                Identifier attrId = Registries.ATTRIBUTE.getId(entry.value());
                if (attrId != null) {
                    String iconPath = ATTRIBUTE_ICONS.get(attrId.toString());
                    if (iconPath != null) {
                        return Identifier.of("cranks-dungeons", iconPath);
                    }
                }
            }
        }

        // Priority 2: Check node ID for class starts
        if (nodeId.startsWith("marauder_")) {
            return Identifier.of("cranks-dungeons", "passive/marauder");
        } else if (nodeId.startsWith("shadow_")) {
            return Identifier.of("cranks-dungeons", "passive/shadow");
        } else if (nodeId.startsWith("scoundrel_")) {
            return Identifier.of("cranks-dungeons", "passive/scoundrel");
        }

        // Final fallback
        return Identifier.of("cranks-dungeons", DEFAULT_ICON);
    }
}
