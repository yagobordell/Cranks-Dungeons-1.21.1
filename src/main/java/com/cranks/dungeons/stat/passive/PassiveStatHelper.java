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
            "physical_damage_percent", "elemental_damage_percent",
            "fire_damage_percent", "cold_damage_percent", "lightning_damage_percent", "void_damage_percent",
            "melee_damage_percent", "ranged_damage_percent", "projectile_damage_percent",
            "one_handed_damage_percent", "dual_wielding_damage_percent", "two_handing_damage_percent",
            "sword_damage_percent", "axe_damage_percent", "mace_damage_percent", "spear_damage_percent",
            "bow_damage_percent", "crossbow_damage_percent",
            "damage_against_afflicted_percent", "damage_while_afflicted_percent",

            // Percentage Defense Scaling
            "max_health_bonus", "max_health_percent", "armor_percent", "evasion_rating_percent",
            "energy_shield_percent", "leggings_armor_percent", "armor_crit_mitigation",
            "bonus_armor", "evasion_rating", "energy_shield",

            // Resistances & Penetration
            "fire_resistance", "cold_resistance", "lightning_resistance", "void_resistance",
            "fire_penetration_percent", "cold_penetration_percent", "lightning_penetration_percent", "void_penetration_percent",
            "elemental_penetration_percent", "armor_penetration_percent", "flat_armor_penetration",

            // Critical Strikes
            "crit_chance", "global_crit_chance_percent", "crit_multiplier", "crit_chance_against_afflicted",

            // Ailment Magnitudes & Durations
            "chance_to_shock", "shock_magnitude_percent",
            "burn_magnitude_percent", "flammability_magnitude_percent",
            "chance_to_poison", "poison_magnitude_percent", "poison_max_stacks",
            "chance_to_bleed", "bleed_magnitude_percent",
            "freeze_buildup_percent", "freeze_magnitude_percent",
            "electrocute_buildup_percent", "electrocute_magnitude_percent",

            // Conversions & Extra Damage
            "physical_extra_percent",
            "physical_to_fire_conversion_percent", "physical_to_cold_conversion_percent",
            "physical_to_lightning_conversion_percent", "physical_to_void_conversion_percent",
            "physical_as_extra_fire_percent", "physical_as_extra_cold_percent",
            "physical_as_extra_lightning_percent", "physical_as_extra_void_percent",
            "fire_extra_percent", "cold_extra_percent", "lightning_extra_percent", "void_extra_percent",

            // Combat Utility
            "movement_speed", "attack_speed", "projectile_speed", "experience_bonus",
            "physical_damage_leeched_as_life", "physical_damage_leeched_as_mana", 
            "health_on_kill", "health_on_kill_percent", "faster_start_of_energy_shield_recharge",
            "max_mana",
            "increased_life_leech_percent", "increased_mana_leech_percent", 
            "life_regeneration", "max_health_regeneration_percent",
            "mana_regeneration_percent", "mana_regeneration_rate", "mana_recovery_rate",
            "block_value", "block_value_percent", "knockback", "knockback_resistance",
            "armor_speed_penalty_reduction", "low_armor_speed_bonus", "armor_applies_to_elemental",
            "energy_shield_recharge_rate_percent",

            // New Passive Tree Modifiers
            "reduced_crit_damage_taken_percent", "close_range_knockback_percent",
            "increased_ailment_chance_percent", "projectile_ricochet_chance",
            "regeneration_rate_while_moving_percent", "projectile_pierce_chance",
            "damage_after_crit_percent", "knockback_resistance_when_full_life",
            "crit_non_damaging_ailment_magnitude_percent",
            "knockback_resistance_per_250_energy_shield",
            "ailment_threshold_percent_of_max_health",
            "ailment_threshold_percent_of_max_es",
            "elemental_ailment_threshold_percent",
            "freeze_threshold_percent",
            "elemental_ailment_threshold_per_dexterity",
            "ailment_threshold_from_boots_lowest_armor_evasion",
            
            // Keystones
            "keystone_void_inoculation", "keystone_avatar_of_fire", "keystone_resolute_technique",
            "keystone_elemental_overload", "keystone_crimson_pact",
            "keystone_iron_reflexes", "keystone_zealots_oath", "keystone_ghost_reaver",
            "keystone_vaal_pact", "keystone_point_blank"
    );

    public static final Set<String> WHITELISTED_WORLD_ATTRIBUTES = Set.of(
            // Woodcutting
            "log_yield", "sapling_yield", "woodcutting_speed_percent",
            
            // Mining
            "ore_yield", "gem_yield", "mining_speed_percent", "flint_yield",
            
            // Farming
            "crop_yield", "seed_yield", "farming_speed_percent",
            
            // Mob Drops
            "monster_drop_yield", "animal_drop_yield", "meat_yield", "rare_mob_drop_chance",
            
            // Experience
            "mining_experience", "combat_experience", "breeding_experience", "fishing_experience",
            
            // Dimensions & Biomes
            "ore_yield_in_nether", "rare_mob_drop_chance_in_nether", "ore_yield_in_overworld",
            "item_rarity_in_overworld", "item_rarity_in_nether", "item_rarity_in_end",
            
            // Existing ones from ModAttributes that apply to the world
            "mining_efficiency", "fortune", "item_rarity_percent", "crop_fortune", "log_fortune", "dig_fortune", "crop_experience",
            "digging_speed_percent", "dig_yield", "crop_replant_chance", "world_discovery_experience",
            "swimming_speed_percent", "breath_efficiency", "travel_efficiency", "breaking_range", "precision_mining",
            "riding_speed", "mount_endurance", "mount_recovery", "riding_agility", "sure_footing"
    );

    /** Old IDs remain loadable; new editing uses useful current stats only. */
    public static final Set<String> WORLD_EDITOR_ATTRIBUTES=WHITELISTED_WORLD_ATTRIBUTES.stream()
            .filter(id->!com.cranks.dungeons.gathering.GatheringStats.LEGACY.contains(id))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    /**
     * Aggregates all modifiers from unlocked nodes in the passive tree for a given
     * player.
     * This works on both client and server as long as the component is synced.
     */
    public static Map<RegistryEntry<EntityAttribute>, Double> getPassiveTreeSummary(PlayerEntity player) {
        Map<RegistryEntry<EntityAttribute>, Double> summary = new HashMap<>();

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        com.cranks.dungeons.component.WorldTreeDataComponent worldData = ModComponents.WORLD_TREE_DATA.get(player);

        if (data != null) {
            // Apply Class Base Attributes First
            String playerClass = data.getPlayerClass();
            if (playerClass != null && !playerClass.isEmpty()) {
                com.cranks.dungeons.config.PassiveTreeConfig.ClassDefinition def = com.cranks.dungeons.config.PassiveTreeConfig.getClassDef(playerClass);
                if (def != null && def.baseAttributes() != null) {
                    for (Map.Entry<String, Double> entry : def.baseAttributes().entrySet()) {
                        String attrId = entry.getKey();
                        Double value = entry.getValue();
                        
                        net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(attrId);
                        if (id != null) {
                            RegistryEntry<EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(id).orElse(null);
                            if (attr != null) {
                                summary.merge(attr, value, Double::sum);
                            }
                        }
                    }
                }
            }

            mergeTreeStats(summary, data.getUnlockedNodes(), data::getNodeChoice, false);
        }
        if (worldData != null) {
            mergeTreeStats(summary, worldData.getUnlockedNodes(), worldData::getNodeChoice, true);
        }
        return summary;
    }

    private static void mergeTreeStats(Map<RegistryEntry<EntityAttribute>, Double> summary, Set<String> unlockedNodes, java.util.function.Function<String, String> choiceProvider, boolean isWorldTree) {
        for (String nodeId : unlockedNodes) {
            PassiveNode node = isWorldTree ? com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNode(nodeId) : PassiveNodeRegistry.getNode(nodeId);
            if (node != null) {
                if (node.isAttributeSelection()) {
                    String choice = choiceProvider.apply(nodeId);
                    if (choice != null && !choice.isEmpty()) {
                        Set<String> whitelist = isWorldTree ? WHITELISTED_WORLD_ATTRIBUTES : WHITELISTED_PASSIVE_ATTRIBUTES;
                        if (whitelist.contains(choice)) {
                            net.minecraft.util.Identifier choiceId = net.minecraft.util.Identifier.tryParse("cranks-dungeons:" + choice);
                            if (choiceId != null) {
                                RegistryEntry<EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(choiceId).orElse(null);
                                if (attr != null) {
                                    double amount = node.attributeSelectionAmount();
                                    if (node.modifiers() != null && node.modifiers().containsKey(attr)) {
                                        amount = node.modifiers().get(attr);
                                    }
                                    summary.merge(attr, amount, Double::sum);
                                }
                            }
                        }
                    }
                } else if (node.modifiers() != null) {
                    for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                        String idStr = entry.getKey().getIdAsString();
                        if (!idStr.startsWith("cranks-dungeons:")) continue;
                        
                        String id = idStr.substring("cranks-dungeons:".length());
                        Set<String> whitelist = isWorldTree ? WHITELISTED_WORLD_ATTRIBUTES : WHITELISTED_PASSIVE_ATTRIBUTES;
                        if (!whitelist.contains(id)) continue;
                        
                        summary.merge(entry.getKey(), entry.getValue(), Double::sum);
                    }
                }
            }
        }
    }

    public static Map<String, Double> getDraftBonuses(PlayerEntity player, Map<String, String> draft, boolean world) {
        if (draft.isEmpty()) return Map.of();
        Set<String> candidates = new java.util.HashSet<>(draft.keySet());
        candidates.removeAll(world ? ModComponents.WORLD_TREE_DATA.get(player).getUnlockedNodes()
                : ModComponents.PASSIVE_DATA.get(player).getUnlockedNodes());
        Map<RegistryEntry<EntityAttribute>, Double> modifiers = new HashMap<>();
        mergeTreeStats(modifiers, candidates, draft::get, world);
        Map<String, Double> bonuses = new HashMap<>();
        modifiers.forEach((attribute, amount) -> bonuses.merge(attribute.getIdAsString(), amount, Double::sum));
        return bonuses;
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
                if (node == null) {
                    node = com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNode(nodeId);
                }
                
                if (node != null) {
                    if (node.isAttributeSelection()) {
                        if (choice != null && !choice.isEmpty()) {
                            if (WHITELISTED_PASSIVE_ATTRIBUTES.contains(choice) || WHITELISTED_WORLD_ATTRIBUTES.contains(choice)) {
                                net.minecraft.util.Identifier choiceId = net.minecraft.util.Identifier.tryParse("cranks-dungeons:" + choice);
                                if (choiceId != null) {
                                    RegistryEntry<EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(choiceId).orElse(null);
                                    if (attr != null) {
                                        double amount = node.attributeSelectionAmount();
                                        if (node.modifiers() != null && node.modifiers().containsKey(attr)) {
                                            amount = node.modifiers().get(attr);
                                        }
                                        raw.merge(attr, amount, Double::sum);
                                    }
                                }
                            }
                        }
                    } else if (node.modifiers() != null) {
                        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                            String idStr = entry.getKey().getIdAsString();
                            if (!idStr.startsWith("cranks-dungeons:")) continue;
                            
                            String id = idStr.substring("cranks-dungeons:".length());
                            if (!WHITELISTED_PASSIVE_ATTRIBUTES.contains(id) && !WHITELISTED_WORLD_ATTRIBUTES.contains(id)) continue;
                            
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
            if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attr, com.cranks.dungeons.registry.ModAttributes.LEGGINGS_ARMOR_PERCENT)) {
                double leggingsBaseArmor = 0;
                net.minecraft.item.ItemStack leggings = player
                        .getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
                if (!leggings.isEmpty()) {
                    var comp = leggings.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    if (comp != null) {
                        for (var modEntry : comp.modifiers()) {
                            if (com.cranks.dungeons.util.CombatUtility.attributesMatch(modEntry.attribute(), net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR)) {
                                leggingsBaseArmor += modEntry.modifier().value();
                            }
                        }
                    }
                }
                totalArmor += (leggingsBaseArmor * value);
                continue; // Don't add as a separate entry
            }

            // Special case for Flat Bonus Armor
            if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attr, com.cranks.dungeons.registry.ModAttributes.BONUS_ARMOR)) {
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
        if (translationKey.contains("physical_damage") || translationKey.contains("attack_damage")) {
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
                .replace("armor penetration percent", "increased armor penetration")
                .replace("flat armor penetration", "armor penetration")
                .replace("fire penetration percent", "fire penetration")
                .replace("cold penetration percent", "cold penetration")
                .replace("lightning penetration percent", "lightning penetration")
                .replace("void penetration percent", "void penetration")
                .replace("elemental penetration percent", "elemental penetration")
                .replace("max health percent", "increased maximum health")
                .replace("armor percent", "increased armor")
                .replace("armor toughness percent", "increased armor toughness")
                .replace("energy shield recharge rate percent", "increased energy shield recharge rate")
                .replace("energy shield percent", "increased energy shield")
                .replace("evasion rating percent", "increased evasion rating")
                .replace("increased life leech percent", "increased life leech")
                .replace("increased mana leech percent", "increased mana leech")
                .replace("burn magnitude percent", "burn magnitude")
                .replace("flammability magnitude percent", "flammability magnitude")
                .replace("poison magnitude percent", "poison magnitude")
                .replace("bleed magnitude percent", "bleed magnitude")
                .replace("shock magnitude percent", "shock magnitude")
                .replace("freeze buildup percent", "freeze buildup")
                .replace("freeze magnitude percent", "freeze magnitude")
                .replace("electrocute buildup percent", "electrocute buildup")
                .replace("electrocute magnitude percent", "electrocute magnitude")
                .replace("reduced crit damage taken percent", "reduced crit dmg taken")
                .replace("close range knockback percent", "close range knockback")
                .replace("increased ailment chance percent", "increased ailment chance")
                .replace("projectile ricochet chance", "terrain ricochet chance")
                .replace("regeneration rate while moving percent", "moving regen rate")
                .replace("projectile pierce chance", "pierce chance")
                .replace("damage after crit percent", "damage after crit")
                .replace("knockback resistance when full life", "knockback res full life")
                .replace("crit non damaging ailment magnitude percent", "crit non-damaging ailment magnitude")
                .replace("knockback resistance per 250 energy shield", "knockback res per 250 ES")
                .replace("ailment threshold percent of max health", "ailment threshold % of health")
                .replace("ailment threshold percent of max es", "ailment threshold % of ES")
                .replace("elemental ailment threshold percent", "elemental ailment threshold")
                .replace("freeze threshold percent", "freeze threshold")
                .replace("elemental ailment threshold per dexterity", "elemental ailment threshold per Dex")
                .replace("ailment threshold from boots lowest armor evasion", "ailment threshold from boots")
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
        if (lower.contains("max mana"))
            return " Mana";
        if (lower.contains("health regen"))
            return " HP/s";
        if (lower.contains("xp"))
            return " xp";
        return "";
    }
}
