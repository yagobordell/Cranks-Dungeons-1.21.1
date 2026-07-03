package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

public class PassiveStatHelper {

    public static final Set<String> WHITELISTED_PASSIVE_ATTRIBUTES = Set.of(
            // Core Base Stats
            "strength", "dexterity", "intelligence",

            // Percentage Damage Scaling
            "attack_damage_percent", "elemental_damage_percent",
            "fire_damage_percent", "cold_damage_percent", "lightning_damage_percent", "void_damage_percent",
            "melee_damage_percent", "ranged_damage_percent",
            "one_handed_damage_percent", "dual_wielding_damage_percent", "two_handing_damage_percent",
            "sword_damage_percent", "axe_damage_percent", "mace_damage_percent", "spear_damage_percent",
            "bow_damage_percent", "crossbow_damage_percent", "trident_damage_percent",
            "damage_against_afflicted_percent", "damage_while_afflicted_percent",

            // Percentage Defense Scaling
            "max_health_percent", "armor_percent", "evasion_rating_percent",
            "energy_shield_percent", "leggings_armor_percent", "armor_crit_mitigation",

            // Resistances & Penetration
            "fire_resistance", "cold_resistance", "lightning_resistance", "void_resistance",
            "fire_penetration_percent", "cold_penetration_percent", "lightning_penetration_percent", "void_penetration_percent",
            "elemental_penetration_percent", "armor_penetration_percent",

            // Critical Strikes
            "global_crit_chance_percent", "crit_multiplier", "crit_chance_against_afflicted",

            // Ailment Magnitudes & Durations
            "shock_magnitude_percent", "shock_duration",
            "burn_magnitude_percent", "burn_duration",
            "poison_magnitude_percent", "poison_duration", "poison_max_stacks",
            "bleed_magnitude_percent", "bleed_duration",
            "freeze_magnitude_percent", "freeze_duration",
            "electrocute_magnitude_percent", "electrocute_duration",
            "shock_threshold_reduction", "freeze_threshold_reduction", "electrocute_threshold_reduction",

            // Conversions & Extra Damage
            "physical_extra_percent",
            "physical_to_fire_conversion_percent", "physical_to_cold_conversion_percent",
            "physical_to_lightning_conversion_percent", "physical_to_void_conversion_percent",
            "physical_as_extra_fire_percent", "physical_as_extra_cold_percent",
            "physical_as_extra_lightning_percent", "physical_as_extra_void_percent",
            "fire_extra_percent", "cold_extra_percent", "lightning_extra_percent", "void_extra_percent",

            // Combat Utility
            "movement_speed", "attack_speed", "projectile_speed", "reload_speed",
            "attack_range", "physical_damage_leeched_as_life", "life_regeneration",
            "chance_to_block", "health_on_block", "knockback", "knockback_resistance",
            "armor_speed_penalty_reduction", "low_armor_speed_bonus",
            
            // Keystones
            "keystone_void_inoculation", "keystone_avatar_of_fire", "keystone_resolute_technique",
            "keystone_elemental_overload", "keystone_crimson_pact"
    );

    /**
     * Aggregates all modifiers from unlocked nodes in the passive tree for a given
     * player.
     * This works on both client and server as long as the component is synced.
     */
    public static Map<RegistryEntry<EntityAttribute>, Double> getPassiveTreeSummary(PlayerEntity player) {
        Map<RegistryEntry<EntityAttribute>, Double> summary = new HashMap<>();

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        if (data == null)
            return summary;

        for (String nodeId : data.getUnlockedNodes()) {
            PassiveNode node = PassiveNodeRegistry.getNode(nodeId);
            if (node != null) {
                if (node.isAttributeSelection()) {
                    String choice = data.getNodeChoice(nodeId);
                    RegistryEntry<EntityAttribute> attr = null;
                    if ("strength".equalsIgnoreCase(choice)) attr = com.cranks.dungeons.registry.ModAttributes.STRENGTH;
                    else if ("dexterity".equalsIgnoreCase(choice)) attr = com.cranks.dungeons.registry.ModAttributes.DEXTERITY;
                    else if ("intelligence".equalsIgnoreCase(choice)) attr = com.cranks.dungeons.registry.ModAttributes.INTELLIGENCE;
                    
                    if (attr != null) {
                        summary.merge(attr, node.attributeSelectionAmount(), Double::sum);
                    }
                }
                
                if (node.modifiers() != null) {
                    for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                        String idStr = entry.getKey().getIdAsString();
                        if (!idStr.startsWith("cranks-dungeons:")) continue;
                        
                        String id = idStr.substring(16);
                        if (!WHITELISTED_PASSIVE_ATTRIBUTES.contains(id)) continue;
                        
                        summary.merge(entry.getKey(), entry.getValue(), Double::sum);
                    }
                }
            }
        }

        return summary;
    }

    /**
     * Returns a sorted map of attribute names to their total passive values for
     * display purposes.
     * Optionally includes a set of drafted node IDs to provide a "preview" summary.
     */
    public static Map<String, Double> getDisplayableSummary(PlayerEntity player, java.util.Map<String, String> draftChoices) {
        Map<RegistryEntry<EntityAttribute>, Double> raw = getPassiveTreeSummary(player);

        // Add draft nodes to the summary calculation
        if (draftChoices != null && !draftChoices.isEmpty()) {
            for (Map.Entry<String, String> entryDraft : draftChoices.entrySet()) {
                String nodeId = entryDraft.getKey();
                String choice = entryDraft.getValue();
                PassiveNode node = PassiveNodeRegistry.getNode(nodeId);
                
                if (node != null) {
                    if (node.isAttributeSelection()) {
                        RegistryEntry<EntityAttribute> attr = null;
                        if ("strength".equalsIgnoreCase(choice)) attr = com.cranks.dungeons.registry.ModAttributes.STRENGTH;
                        else if ("dexterity".equalsIgnoreCase(choice)) attr = com.cranks.dungeons.registry.ModAttributes.DEXTERITY;
                        else if ("intelligence".equalsIgnoreCase(choice)) attr = com.cranks.dungeons.registry.ModAttributes.INTELLIGENCE;
                        
                        if (attr != null) {
                            raw.merge(attr, node.attributeSelectionAmount(), Double::sum);
                        }
                    }
                    
                    if (node.modifiers() != null) {
                        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                            String idStr = entry.getKey().getIdAsString();
                            if (!idStr.startsWith("cranks-dungeons:")) continue;
                            
                            String id = idStr.substring(16);
                            if (!WHITELISTED_PASSIVE_ATTRIBUTES.contains(id)) continue;
                            
                            raw.merge(entry.getKey(), entry.getValue(), Double::sum);
                        }
                    }
                }
            }
        }

        Map<String, Double> display = new TreeMap<>(); // Sorted by name
        double totalArmor = 0;

        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : raw.entrySet()) {
            RegistryEntry<EntityAttribute> attr = entry.getKey();
            double value = entry.getValue();

            // Special case for Leggings Armor Multiplier
            if (attr.equals(com.cranks.dungeons.registry.ModAttributes.LEGGINGS_ARMOR_PERCENT)) {
                double leggingsBaseArmor = 0;
                net.minecraft.item.ItemStack leggings = player
                        .getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
                if (!leggings.isEmpty()) {
                    var comp = leggings.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    if (comp != null) {
                        for (var modEntry : comp.modifiers()) {
                            if (modEntry.attribute().equals(net.minecraft.entity.attribute.EntityAttributes.ARMOR)) {
                                leggingsBaseArmor += modEntry.modifier().value();
                            }
                        }
                    }
                }
                totalArmor += (leggingsBaseArmor * value);
                continue; // Don't add as a separate entry
            }

            // Special case for Flat Bonus Armor
            if (attr.equals(com.cranks.dungeons.registry.ModAttributes.BONUS_ARMOR)) {
                totalArmor += value;
                continue; // Don't add as a separate entry
            }

            String readableName = getReadableName(attr);
            display.put(readableName, value);
        }

        if (totalArmor != 0) {
            display.put("Armor", totalArmor);
        }

        return display;
    }

    /**
     * Unified method to get a player-friendly name for an attribute.
     */
    public static String getReadableName(RegistryEntry<EntityAttribute> attribute) {
        String translationKey = attribute.value().getTranslationKey().toLowerCase();

        // Targeted mappings for consistency with the rest of the mod
        if (translationKey.contains("attack_damage")) {
            String name = translationKey.replace("attribute.name.minecraft.", "")
                    .replace("attribute.name.cranks-dungeons.", "")
                    .replace("generic.attack_damage", "physical_damage")
                    .replace("attack_damage", "physical_damage")
                    .replace("_", " ");
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        if (translationKey.contains("life_regeneration")) {
            return "Health Regen";
        }

        // Default processing for other stats with abbreviations
        String name = translationKey.replace("attribute.name.minecraft.", "")
                .replace("attribute.name.cranks-dungeons.", "")
                .replace("resistance", "res")
                .replace("experience", "exp")
                .replace("regeneration", "regen")
                .replace("armor crit mitigation", "crit defense per armor")
                .replace("damage against afflicted percent", "dmg vs afflicted")
                .replace("elemental damage percent", "elemental damage")
                .replace("melee damage percent", "melee damage")
                .replace("ranged damage percent", "ranged damage")
                .replace("one handed damage percent", "one handed damage")
                .replace("dual wielding damage percent", "dual wield damage")
                .replace("two handing damage percent", "two hand damage")
                .replace("damage while afflicted percent", "dmg while afflicted")
                .replace("physical as extra fire percent", "phys as fire")
                .replace("physical as extra cold percent", "phys as cold")
                .replace("physical as extra lightning percent", "phys as light.")
                .replace("physical as extra void percent", "phys as void")
                .replace("physical to fire conversion percent", "phys to fire conversion")
                .replace("physical to cold conversion percent", "phys to cold conversion")
                .replace("physical to lightning conversion percent", "phys to light. conversion")
                .replace("physical to void conversion percent", "phys to void conversion")
                .replace("crit chance against afflicted", "crit chance vs afflicted")
                .replace("armor penetration percent", "armor penetration")
                .replace("fire penetration percent", "fire penetration")
                .replace("cold penetration percent", "cold penetration")
                .replace("lightning penetration percent", "lightning penetration")
                .replace("void penetration percent", "void penetration")
                .replace("elemental penetration percent", "elemental penetration")
                .replace("max health percent", "increased maximum health")
                .replace("armor percent", "increased armor")
                .replace("armor toughness percent", "increased armor toughness")
                .replace("energy shield percent", "increased energy shield")
                .replace("evasion rating percent", "increased evasion rating")
                .replace("_", " ");

        if (name.isEmpty())
            return "Unknown Stat";

        // Capitalize first letter
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Returns the appropriate unit suffix for a stat name.
     */
    public static String getStatSuffix(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("duration"))
            return "s";
        if (lower.contains("range"))
            return " blocks";
        if (lower.contains("stacks"))
            return " max";
        if (lower.contains("max health"))
            return " HP";
        if (lower.contains("health regen"))
            return " HP/s";
        if (lower.contains("xp"))
            return " xp";
        return "";
    }
}
