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
        ATTRIBUTE_ICONS.put("cranks-dungeons:local_crit_chance_percent", "passive/crit_chance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:global_crit_chance_percent", "passive/crit_chance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_multiplier", "passive/crit_multiplier");

        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_damage_percent", "passive/attack_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:melee_damage_percent", "passive/melee_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:ranged_damage_percent", "passive/ranged_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:projectile_damage_percent", "passive/projectile_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:one_handed_damage_percent", "passive/one_handed_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:dual_wielding_damage_percent", "passive/dual_wielding_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:two_handing_damage_percent", "passive/two_handing_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:attack_speed", "passive/attack_speed");

        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback", "passive/knockback");
        ATTRIBUTE_ICONS.put("cranks-dungeons:physical_damage_leeched_as_life", "passive/life_steal");

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
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_damage_percent", "passive/burn_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:burn_duration", "passive/burn_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_poison", "passive/chance_to_poison");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_damage_percent", "passive/poison_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_duration", "passive/poison_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:poison_max_stacks", "passive/poison_max_stacks");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_bleed", "passive/chance_to_bleed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_damage_percent", "passive/bleed_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:bleed_duration", "passive/bleed_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_shock", "passive/chance_to_shock");
        ATTRIBUTE_ICONS.put("cranks-dungeons:shock_magnitude_percent", "passive/shock_magnitude_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:shock_duration", "passive/shock_duration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:chance_to_freeze", "passive/freeze_buildup_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:freeze_magnitude_percent", "passive/freeze_buildup_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:electrocute_magnitude_percent", "passive/electrocute_buildup_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:elemental_ailment_threshold_percent", "passive/ailment_threshold_reduction");
        ATTRIBUTE_ICONS.put("cranks-dungeons:freeze_threshold_percent", "passive/freeze_buildup_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:ailment_threshold_percent_of_max_health", "passive/max_health_bonus");
        ATTRIBUTE_ICONS.put("cranks-dungeons:ailment_threshold_percent_of_max_es", "passive/energy_shield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:elemental_ailment_threshold_per_dexterity", "passive/dexterity");
        ATTRIBUTE_ICONS.put("cranks-dungeons:ailment_threshold_from_boots_lowest_armor_evasion", "passive/bonus_armor");

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
        ATTRIBUTE_ICONS.put("cranks-dungeons:energy_shield_recharge_rate_percent", "passive/energy_shield_percent");
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

        // New Passive Tree Modifiers
        ATTRIBUTE_ICONS.put("cranks-dungeons:reduced_crit_damage_taken_percent", "passive/armor_crit_mitigation");
        ATTRIBUTE_ICONS.put("cranks-dungeons:close_range_knockback_percent", "passive/knockback");
        ATTRIBUTE_ICONS.put("cranks-dungeons:increased_ailment_chance_percent", "passive/chance_to_shock");
        ATTRIBUTE_ICONS.put("cranks-dungeons:projectile_ricochet_chance", "passive/projectile_speed");
        ATTRIBUTE_ICONS.put("cranks-dungeons:regeneration_rate_while_moving_percent", "passive/life_regeneration");
        ATTRIBUTE_ICONS.put("cranks-dungeons:projectile_pierce_chance", "passive/projectile_damage_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:damage_after_crit_percent", "passive/crit_multiplier");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback_resistance_when_full_life", "passive/knockback_resistance");
        ATTRIBUTE_ICONS.put("cranks-dungeons:crit_non_damaging_ailment_magnitude_percent", "passive/shock_magnitude_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:knockback_resistance_per_250_energy_shield", "passive/knockback_resistance");


        ATTRIBUTE_ICONS.put("cranks-dungeons:strength", "passive/strength");
        ATTRIBUTE_ICONS.put("cranks-dungeons:dexterity", "passive/dexterity");
        ATTRIBUTE_ICONS.put("cranks-dungeons:intelligence", "passive/intelligence");

        // === WORLD TREE GATHERING & EXPERIENCE ===
        ATTRIBUTE_ICONS.put("cranks-dungeons:log_yield", "world/log_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:sapling_yield", "world/sapling_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:woodcutting_speed_percent", "world/woodcutting_speed_percent");
        
        ATTRIBUTE_ICONS.put("cranks-dungeons:ore_yield", "world/ore_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:gem_yield", "world/gem_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:mining_speed_percent", "world/mining_speed_percent");
        ATTRIBUTE_ICONS.put("cranks-dungeons:flint_yield", "world/flint_yield");
        
        ATTRIBUTE_ICONS.put("cranks-dungeons:crop_yield", "world/crop_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:seed_yield", "world/seed_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:farming_speed_percent", "world/farming_speed_percent");
        
        ATTRIBUTE_ICONS.put("cranks-dungeons:monster_drop_yield", "world/monster_drop_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:animal_drop_yield", "world/animal_drop_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:meat_yield", "world/meat_yield");
        ATTRIBUTE_ICONS.put("cranks-dungeons:rare_mob_drop_chance", "world/rare_mob_drop_chance");
        
        ATTRIBUTE_ICONS.put("cranks-dungeons:mining_experience", "world/mining_experience");
        ATTRIBUTE_ICONS.put("cranks-dungeons:combat_experience", "world/combat_experience");
        ATTRIBUTE_ICONS.put("cranks-dungeons:breeding_experience", "world/breeding_experience");
        ATTRIBUTE_ICONS.put("cranks-dungeons:fishing_experience", "world/fishing_experience");
        
        ATTRIBUTE_ICONS.put("cranks-dungeons:ore_yield_in_overworld", "world/ore_yield_in_overworld");
        ATTRIBUTE_ICONS.put("cranks-dungeons:ore_yield_in_nether", "world/ore_yield_in_nether");
        ATTRIBUTE_ICONS.put("cranks-dungeons:rare_mob_drop_chance_in_nether", "world/rare_mob_drop_chance_in_nether");
        ATTRIBUTE_ICONS.put("cranks-dungeons:item_rarity_in_overworld", "world/item_rarity_in_overworld");
        ATTRIBUTE_ICONS.put("cranks-dungeons:item_rarity_in_nether", "world/item_rarity_in_nether");
        ATTRIBUTE_ICONS.put("cranks-dungeons:item_rarity_in_end", "world/item_rarity_in_end");

        ATTRIBUTE_ICONS.put("cranks-dungeons:block_value", "passive/block_value");
        ATTRIBUTE_ICONS.put("cranks-dungeons:health_on_block", "passive/health_on_block");

        // Keystones
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_void_inoculation", "passive/keystone_void_inoculation");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_avatar_of_fire", "passive/keystone_avatar_of_fire");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_resolute_technique", "passive/keystone_resolute_technique");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_elemental_overload", "passive/keystone_elemental_overload");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_crimson_pact", "passive/keystone_crimson_pact");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_iron_reflexes", "passive/keystone_iron_reflexes");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_zealots_oath", "passive/keystone_zealots_oath");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_ghost_reaver", "passive/keystone_ghost_reaver");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_vaal_pact", "passive/keystone_vaal_pact");
        ATTRIBUTE_ICONS.put("cranks-dungeons:keystone_point_blank", "passive/keystone_point_blank");

    }


    private static final Map<String, String> TITLE_ICONS = new HashMap<>();
    public static final Identifier PLACEHOLDER_ICON = Identifier.of("cranks-dungeons", "passive/placeholder");

    static {
        registerTitleIcon("Path of the Warrior", "passive/strength");
        registerTitleIcon("Path of the Ranger", "passive/dexterity");
        registerTitleIcon("Path of the Mercenary", "passive/attack_speed");
        registerTitleIcon("Physical Conditioning", "passive/attack_damage_percent");
        registerTitleIcon("Vitality", "passive/life_steal");
        registerTitleIcon("Warrior's Might", "passive/strength");
        registerTitleIcon("Swiftness", "passive/movement_speed");
        registerTitleIcon("Agility", "passive/dexterity");
        registerTitleIcon("Deadly Aim", "passive/crit_chance");
        registerTitleIcon("Opportunist", "passive/knockback");
        registerTitleIcon("Dirty Tactics", "passive/life_steal");
        registerTitleIcon("Ruthless", "passive/crit_multiplier");
        registerTitleIcon("Roots of the World", "passive/world_root");
        registerTitleIcon("Swift Hands", "passive/mining");
        registerTitleIcon("Prospector", "passive/prospector");
    }

    /**
     * Registers a title to icon mapping (e.g. "Iron Skin", "passive/armor_percent").
     */
    public static void registerTitleIcon(String title, String iconPath) {
        if (title != null && iconPath != null) {
            TITLE_ICONS.put(title.trim().toLowerCase(), iconPath);
        }
    }

    /**
     * Determines the icon for a node based on its title / name.
     * - Attribute selection nodes return passive/no.
     * - Nodes with no title (or blank/empty) return null (nothing drawn).
     * - Nodes with a title mapped in TITLE_ICONS return that icon.
     * - Nodes with an unmapped title use the generic emblem.
     */
    public static Identifier getIcon(String nodeId, String nodeTitle, boolean isAttributeChoice) {
        // Priority 1: Check attribute choice
        if (isAttributeChoice) {
            return Identifier.of("cranks-dungeons", "passive/no");
        }

        // Priority 2: Check if node has no title
        if (nodeTitle == null || nodeTitle.trim().isEmpty()) {
            return null;
        }

        String stripped = net.minecraft.util.Formatting.strip(nodeTitle);
        String lookupKey = (stripped != null ? stripped : nodeTitle).trim().toLowerCase();

        if (lookupKey.isEmpty()) {
            return null;
        }

        // Priority 3: Check registered title icons
        String iconPath = TITLE_ICONS.get(lookupKey);
        if (iconPath != null) {
            return Identifier.of("cranks-dungeons", iconPath);
        }

        // Priority 4: Fallback for nodes with a title but no mapped icon
        return Identifier.of("cranks-dungeons", DEFAULT_ICON);
    }

    /**
     * Legacy modifier-based overload for backwards compatibility.
     */
    public static Identifier getIcon(String nodeId, Map<RegistryEntry<EntityAttribute>, Double> modifiers, boolean isAttributeChoice) {
        return getIcon(nodeId, "", isAttributeChoice);
    }

    public static Identifier getIconForModifier(String modifierId) {
        String fullId = modifierId;
        if (!fullId.contains(":")) {
            fullId = "cranks-dungeons:" + fullId;
        }
        String iconPath = ATTRIBUTE_ICONS.get(fullId);
        if (iconPath != null) {
            return Identifier.of("cranks-dungeons", iconPath);
        }
        return null;
    }

    /**
     * Scans the ResourceManager for all existing sprite textures in textures/gui/sprites/ (passive/ and world/ subdirectories).
     * Only returns icons that actually exist in the resource directory or in loaded resource packs.
     */
    public static java.util.List<String> getAllAvailableIcons(net.minecraft.resource.ResourceManager resourceManager) {
        java.util.Set<String> discovered = new java.util.TreeSet<>();
        if (resourceManager != null) {
            try {
                var resources = resourceManager.findResources("textures/gui/sprites", path -> path.getPath().endsWith(".png"));
                for (var entry : resources.entrySet()) {
                    Identifier resId = entry.getKey();
                    String path = resId.getPath();
                    if (path.startsWith("textures/gui/sprites/") && path.endsWith(".png")) {
                        String spritePath = path.substring("textures/gui/sprites/".length(), path.length() - 4);
                        if (spritePath.startsWith("passive/") || spritePath.startsWith("world/")) {
                            if (resId.getNamespace().equals("cranks-dungeons")) {
                                discovered.add(spritePath);
                            } else {
                                discovered.add(resId.getNamespace() + ":" + spritePath);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                com.cranks.dungeons.CranksDungeons.LOGGER.error("[PassiveIconManager] Failed to scan sprites", e);
            }
        }
        return new java.util.ArrayList<>(discovered);
    }
}
