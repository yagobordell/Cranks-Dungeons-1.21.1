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
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_multiplier", "passive/crit_chance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bonus_attack_damage", "passive/physical_damage");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_extra_percent", "passive/physical_damage");
        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_speed", "passive/attack_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_range", "passive/attack_range");
        ATTRIBUTE_ICONS.put("cranks-dungeons:reload_speed", "passive/reload_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:life_steal", "passive/life_steal");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback", "passive/knockback");

        // --- Elemental (Damage & Resistance) ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_damage", "passive/fire");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_extra_percent", "passive/fire");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_resistance", "passive/fire");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_damage", "passive/cold");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_extra_percent", "passive/cold");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_resistance", "passive/cold");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_damage", "passive/lightning");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_extra_percent", "passive/lightning");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_resistance", "passive/lightning");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_damage", "passive/void");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_extra_percent", "passive/void");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_resistance", "passive/void");
        

        // --- Ailments ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_burn", "passive/burn");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_damage_percent", "passive/burn");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_duration", "passive/burn");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_poison", "passive/poison");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_damage_percent", "passive/poison");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_duration", "passive/poison");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_max_stacks", "passive/poison");
        ATTRIBUTE_ICONS.put("cranks-dungeons:freeze_buildup_percent", "passive/freeze");
        ATTRIBUTE_ICONS.put("cranks-dungeons:freeze_duration", "passive/freeze");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_bleed", "passive/bleed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_burst_damage_percent", "passive/bleed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_duration", "passive/bleed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_shock", "passive/shock");
        ATTRIBUTE_ICONS.put("cranks-dungeons:shock_duration", "passive/shock");
        ATTRIBUTE_ICONS.put("cranks-dungeons:electrocute_buildup_percent", "passive/shock");
        ATTRIBUTE_ICONS.put("cranks-dungeons:electrocute_duration", "passive/shock");

        // --- Defensive ---
        ATTRIBUTE_ICONS.put("minecraft:max_health", "passive/max_health_bonus");
        ATTRIBUTE_ICONS.put("cranks-dungeons:max_health_bonus", "passive/max_health_bonus");
        ATTRIBUTE_ICONS.put("cranks-dungeons:life_regeneration", "passive/life_regeneration");
        ATTRIBUTE_ICONS.put("minecraft:armor", "passive/bonus_armor");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bonus_armor", "passive/bonus_armor");
        ATTRIBUTE_ICONS.put("minecraft:armor_toughness", "passive/armor_toughness");
        ATTRIBUTE_ICONS.put("cranks-dungeons:energy_shield", "passive/energy_shield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:evasion_rating", "passive/evasion");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback_resistance", "passive/knockback");
        ATTRIBUTE_ICONS.put("cranks-dungeons:feather_falling", "passive/feather");

        // --- Utility ---
        ATTRIBUTE_ICONS.put("minecraft:movement_speed", "passive/movement_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:movement_speed", "passive/movement_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:experience_bonus", "passive/experience");
        ATTRIBUTE_ICONS.put("minecraft:luck", "passive/luck");

        // --- Core Stats ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:strength", "passive/strength");
        ATTRIBUTE_ICONS.put("cranks-dungeons:dexterity", "passive/dexterity");
        ATTRIBUTE_ICONS.put("cranks-dungeons:intelligence", "passive/intelligence");
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
