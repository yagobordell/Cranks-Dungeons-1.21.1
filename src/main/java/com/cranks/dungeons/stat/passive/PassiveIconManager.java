package com.cranks.dungeons.stat.passive;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PassiveIconManager {
    private static final Map<String, String> ATTRIBUTE_ICONS = new HashMap<>();
    private static final String DEFAULT_ICON = "textures/gui/passive/generic.png";

    static {
        // --- Offensive ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_chance", "textures/gui/passive/crit_chance.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_multiplier", "textures/gui/passive/crit_chance.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bonus_attack_damage", "textures/gui/passive/physical_damage.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_extra_percent", "textures/gui/passive/physical_damage.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_speed", "textures/gui/passive/attack_speed.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_range", "textures/gui/passive/attack_range.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:reload_speed", "textures/gui/passive/reload_speed.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:life_steal", "textures/gui/passive/life_steal.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback", "textures/gui/passive/knockback.png");

        // --- Elemental (Damage & Resistance) ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_damage", "textures/gui/passive/fire.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_extra_percent", "textures/gui/passive/fire.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fire_resistance", "textures/gui/passive/fire.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_damage", "textures/gui/passive/cold.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_extra_percent", "textures/gui/passive/cold.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:cold_resistance", "textures/gui/passive/cold.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_damage", "textures/gui/passive/lightning.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_extra_percent", "textures/gui/passive/lightning.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:lightning_resistance", "textures/gui/passive/lightning.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_damage", "textures/gui/passive/void.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_extra_percent", "textures/gui/passive/void.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:void_resistance", "textures/gui/passive/void.png");

        // --- Ailments ---
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_burn", "textures/gui/passive/burn.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_damage_percent", "textures/gui/passive/burn.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_duration", "textures/gui/passive/burn.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_poison", "textures/gui/passive/poison.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_damage_percent", "textures/gui/passive/poison.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_duration", "textures/gui/passive/poison.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_max_stacks", "textures/gui/passive/poison.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_freeze", "textures/gui/passive/freeze.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:freeze_duration", "textures/gui/passive/freeze.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_bleed", "textures/gui/passive/bleed.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_duration", "textures/gui/passive/bleed.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_shock", "textures/gui/passive/shock.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:shock_duration", "textures/gui/passive/shock.png");

        // --- Defensive ---
        ATTRIBUTE_ICONS.put("minecraft:max_health", "textures/gui/passive/max_health_bonus.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:max_health_bonus", "textures/gui/passive/max_health_bonus.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:life_regeneration", "textures/gui/passive/life_regeneration.png");
        ATTRIBUTE_ICONS.put("minecraft:armor", "textures/gui/passive/bonus_armor.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bonus_armor", "textures/gui/passive/bonus_armor.png");
        ATTRIBUTE_ICONS.put("minecraft:armor_toughness", "textures/gui/passive/armor_toughness.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bonus_armor_toughness", "textures/gui/passive/armor_toughness.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:energy_shield", "textures/gui/passive/energy_shield.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:evasion_rating", "textures/gui/passive/evasion.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback_resistance", "textures/gui/passive/knockback.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:feather_falling", "textures/gui/passive/feather.png");

        // --- Utility ---
        ATTRIBUTE_ICONS.put("minecraft:movement_speed", "textures/gui/passive/movement_speed.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:movement_speed", "textures/gui/passive/movement_speed.png");
        ATTRIBUTE_ICONS.put("cranks-dungeons:experience_bonus", "textures/gui/passive/experience.png");
        ATTRIBUTE_ICONS.put("minecraft:luck", "textures/gui/passive/luck.png");
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
        if (nodeId.startsWith("warrior_")) {
            return Identifier.of("cranks-dungeons", "textures/gui/passive/warrior.png");
        } else if (nodeId.startsWith("ranger_")) {
            return Identifier.of("cranks-dungeons", "textures/gui/passive/ranger.png");
        } else if (nodeId.startsWith("mercenary_")) {
            return Identifier.of("cranks-dungeons", "textures/gui/passive/mercenary.png");
        }

        // Final fallback
        return Identifier.of("cranks-dungeons", DEFAULT_ICON);
    }
}
