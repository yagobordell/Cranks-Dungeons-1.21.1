package com.cranks.dungeons.stat;

/** Shared explanations; old IDs and custom tree layouts remain compatible. */
public final class StatDescriptions {
    private StatDescriptions() {}

    public static String description(String id) {
        return switch(id) {
            case "bonus_armor" -> "Flat armour added to your total before global increases.";
            case "armor_percent" -> "Global increase to total armour. Item-local increases are already included in that item's armour.";
            case "leggings_armor_percent" -> "Increases armour supplied by equipped leggings, including local item improvements.";
            case "armor_crit_mitigation" -> "Legacy specialist protection per point of armour. Included in Critical Protection.";
            case "reduced_crit_damage_taken_percent" -> "Reduces only the extra damage from critical hits, not their ordinary damage.";
            case "block_value", "block_value_percent" -> "Active frontal shield blocks subtract block value, up to 85% of the incoming hit, before armour and resistances.";
            case "armor_applies_to_elemental" -> "This fraction of armour also protects against Fire, Cold and Lightning hits, not Void. Resistances apply afterwards.";
            case "physical_taken_as_void" -> "Converts incoming physical hit damage to Void before mitigation. Void consumes twice as much energy shield.";
            case "life_regeneration" -> "Health recovered per second. Total regeneration is capped at 10% of maximum health per second.";
            case "max_health_regeneration_percent" -> "Maximum health regenerated each second; this contribution is capped at 5% per second.";
            case "regeneration_rate_while_moving_percent" -> "Increases health regeneration while moving. Does not increase mana regeneration.";
            case "physical_damage_leeched_as_life" -> "Physical damage actually dealt is recovered over time. Up to 10% of maximum life per second; up to 20% can await recovery.";
            case "physical_damage_leeched_as_mana" -> "Physical damage actually dealt is recovered as mana over time, up to 10% of maximum mana per second.";
            case "increased_life_leech_percent", "increased_mana_leech_percent" -> "Increases the amount recovered by leech, without raising its recovery speed or pool limit.";
            case "health_on_kill", "health_on_kill_percent", "mana_on_kill" -> "Immediate recovery when you kill a living target. Animals count.";
            case "mana_regeneration" -> "Flat mana recovered each second, added before regeneration and recovery multipliers.";
            case "mana_regeneration_percent" -> "Fraction of maximum mana regenerated each second, in addition to flat regeneration.";
            case "mana_regeneration_rate" -> "Increases mana regeneration only.";
            case "mana_recovery_rate" -> "Increases regeneration and the amount gained from mana leech and mana on kill. Leech limits still apply.";
            case "projectile_ricochet_chance" -> "Chance for arrows and thrown tridents to redirect from terrain toward a visible enemy within 12 blocks. No target means an ordinary impact; at most 4 redirects.";
            case "projectile_pierce_chance" -> "Chance for arrows to pierce one additional enemy, stacking with existing piercing. Does not apply to tridents.";
            case "projectile_speed" -> "Increases launch speed for bows, crossbows and thrown projectiles.";
            case "attack_speed" -> "Increases melee attack speed and bow/crossbow draw speed.";
            case "damage_after_crit_percent" -> "Increased damage for 8 seconds after a critical hit actually damages life or energy shield.";
            case "increased_ailment_chance_percent" -> "Increases chance-based Poison, Bleed and Shock. Does not increase Freeze or Electrocute buildup.";
            case "armor_penetration_percent" -> "Increases your flat armour penetration. With no flat penetration, this gives no benefit.";
            case "flat_armor_penetration" -> "Armour ignored on the target before physical hit mitigation.";
            case "elemental_penetration_percent" -> "Ignores this fraction of positive Fire, Cold and Lightning resistance. Does not affect Void; combined penetration is capped at 100%.";
            case "fire_penetration_percent", "cold_penetration_percent", "lightning_penetration_percent", "void_penetration_percent" -> "Ignores this fraction of the target's positive resistance to this damage type. Combined penetration is capped at 100%; negative resistance is not lowered further.";
            case "burn_magnitude_percent" -> "Increases Burn damage per second, not duration. A hit-derived Burn starts at 50% of the offensive Fire hit per second for 4 seconds; Fire Resistance applies once to each tick.";
            case "flammability_magnitude_percent" -> "Increases Flammability buildup. Without modifiers, Fire hit damage equal to 25% of the target's ailment threshold ignites it.";
            case "fire_resistance" -> "Reduces Fire hit and Burn damage, and indirectly reduces Flammability buildup. Effective resistance is capped at 75% before penetration.";
            case "cold_resistance" -> "Reduces Cold hit damage and the resulting Chill/Freeze buildup. It does not shorten an existing Freeze.";
            case "lightning_resistance" -> "Reduces Lightning hits and arcs, plus the Shock magnitude and Electrocute buildup generated by those hits.";
            case "void_resistance" -> "Reduces Void hit damage and Poison damage. Poison bypasses Energy Shield, making this useful against Poison builds.";
            case "poison_magnitude_percent" -> "Increases Poison damage per second, not duration. Each hit-derived stack starts at 20% of offensive Physical + Void damage per second for 2 seconds. Stacks expire independently.";
            case "poison_max_stacks" -> "Additional independently timed Poison stacks above the base cap of 20. A stronger new stack can replace the weakest at the cap.";
            case "bleed_magnitude_percent" -> "Increases the fraction of later hit damage stored by Bleed: base 40%, capped at 100%. Does not extend the 5-second fuse or reset an existing burst.";
            case "shock_magnitude_percent" -> "Increases Shock damage-taken amplification, up to 50%. Does not extend its 10-second base duration.";
            case "chance_to_poison", "chance_to_bleed" -> "Application chance on an eligible hit. Any positive chance enables guaranteed application on critical hits; magnitude requirements still apply.";
            case "freeze_magnitude_percent", "electrocute_magnitude_percent" -> "Increases control duration. Freeze and Electrocute share a 1-second recovery window after either ends and cannot overlap.";
            case "knockback_resistance_when_full_life" -> "Additional knockback resistance if your health was full before the hit.";
            case "knockback_resistance_per_250_energy_shield" -> "Additional knockback resistance for every 250 maximum energy shield.";
            case "close_range_knockback_percent" -> "Increases knockback against targets within 4 blocks.";
            case "elemental_ailment_threshold_percent", "freeze_threshold_percent" -> "Increases the damage/buildup needed to inflict the affected ailments. Does not directly shorten existing ailments.";
            case "ailment_threshold_percent_of_max_health" -> "Adds this fraction of maximum health to your ailment threshold.";
            case "ailment_threshold_percent_of_max_es" -> "Adds this fraction of maximum energy shield to your ailment threshold.";
            case "elemental_ailment_threshold_per_dexterity" -> "Additional ailment threshold per point of Dexterity.";
            case "ailment_threshold_from_boots_lowest_armor_evasion" -> "Adds the lower of your boots' armour and evasion to ailment threshold. Both must be present.";
            case "keystone_vaal_pact" -> "Life leech is immediate, with a replenishing 10%-of-maximum-resource budget per second. No health regeneration; on-kill recovery is halved.";
            case "keystone_ghost_reaver" -> "Life leech restores energy shield instead. Energy shield recharge is disabled.";
            case "keystone_zealots_oath" -> "Health regeneration restores energy shield instead of health.";
            case "keystone_crimson_pact" -> "Energy shield contributes twice its value to maximum health, but cannot absorb damage or recover. Incompatible with Void Inoculation.";
            case "keystone_void_inoculation" -> "Maximum health becomes exactly 1 and Void damage is prevented. Takes precedence over other maximum-health bonuses.";
            case "mining_efficiency" -> "Effective tool gathering-speed multiplier. World-tree increases scale the tool base rather than adding flat speed. Wrong tools retain vanilla restrictions; Haste and fatigue apply once.";
            case "mining_speed_percent" -> "Increased breaking speed for pickaxe-mineable blocks. 0.1 means 10%; maximum 300% extra. Tool harvest requirements still apply.";
            case "woodcutting_speed_percent" -> "Increased log-breaking speed. 0.1 means 10%; maximum 300% extra.";
            case "digging_speed_percent" -> "Increased speed for shovel-mineable blocks. Maximum 300% extra.";
            case "farming_speed_percent" -> "Legacy harvesting-speed bonus for crop blocks. New farming investment uses Crop Replant Chance instead.";
            case "ore_yield" -> "Extra ore resources from manual harvesting, including applicable dimension bonuses and legacy Fortune. Bonus sources add before rounding; combined yield is capped at 300%. No extra Silk Touch blocks or placed-block yield.";
            case "gem_yield" -> "Additional yield for diamond, emerald and lapis resources, added to ore yield before its 300% cap.";
            case "log_yield" -> "Extra logs from manually chopping grown trees with an axe. Includes legacy Log Fortune; capped at 300%. Placing, moving and breaking logs does not create extra wood.";
            case "sapling_yield" -> "Extra saplings and apples when manually harvesting naturally grown leaves. Placed leaves receive no bonus.";
            case "dig_yield" -> "Extra clay balls and naturally dropped flint when digging. Includes legacy Dig Fortune. Does not duplicate gravel, clay blocks or Silk Touch blocks.";
            case "flint_yield" -> "Additional yield for flint already rolled from natural gravel, added to Dig Yield. Does not turn a gravel drop into free flint.";
            case "crop_yield" -> "Extra produce from mature crops harvested with a hoe. Includes legacy Crop Fortune. Cocoa and Nether Wart are supported; immature crops do not benefit.";
            case "seed_yield" -> "Extra seed drops from mature crops harvested with a hoe. Carrots and potatoes use Crop Yield because the harvested item is also their seed.";
            case "crop_replant_chance" -> "Chance to replant a mature crop after harvesting with a hoe. Consumes one matching seed or planting item from the hotbar, regular inventory or an unlocked bag slot. Requires valid soil and an available seed. Sneak to harvest without replanting.";
            case "world_discovery_experience" -> "Increases World XP from first resource, recipe, biome, structure, animal, fish and alchemy discoveries. Capped at 50% from this stat. Discoveries stay individual and cannot be claimed repeatedly.";
            case "monster_drop_yield" -> "Extra tagged ordinary monster materials on player-attributed kills. Equipment and rare loot are excluded; shared yield is capped at 300%.";
            case "animal_drop_yield" -> "Extra tagged animal materials and meat on player-attributed kills, including fish. Equipment and rare loot are excluded.";
            case "meat_yield" -> "Additional yield for tagged raw and cooked meat or fish, combined with animal yield before the 300% cap.";
            case "rare_mob_drop_chance" -> "Increases selection weight of tagged rare loot in weighted monster loot pools. +25% makes an eligible weight of 100 become 125; it is not +25 percentage points or a guaranteed drop. Conditions and zero weights are preserved.";
            case "item_rarity_percent" -> "Favors higher-quality entries in weighted loot pools. At quality 8, +25% makes weight 100 become 125; lower positive quality receives proportionally less. Does not upgrade equipment rarity or alter unweighted loot.";
            case "precision_mining" -> "Chance to avoid tool durability loss. Combines additively with Preservation; combined savings are capped at 80%, so tools still need repairs.";
            case "repair_chance" -> "Chance to avoid durability loss, not a chance to restore durability. Combines with tool preservation, up to 80% total.";
            case "breaking_range" -> "Additional block interaction reach, capped at 2 metres. Does not increase entity attack reach or bypass line-of-sight and permission checks.";
            case "riding_speed" -> "Increases your mount's controlled movement speed while you ride it. 0.1 means 10%. Uses only the controlling rider's bonus and does not change genes or unmounted speed.";
            case "mount_endurance" -> "Reduces your ridden mount's sprint stamina or ability-resource drain. 0.2 means 20% less drain; maximum reduction 100%. Does not reduce jump or camel dash costs.";
            case "mount_recovery" -> "Increases your ridden mount's stamina or ability-resource regeneration after its normal recovery delay. 0.2 means 20% faster regeneration. Does not shorten the delay or reduce environmental heat loss.";
            case "riding_agility" -> "Increases your mount's acceleration and braking responsiveness while you ride it. 0.15 means 15% faster response. Does not change maximum speed or turning rate.";
            case "sure_footing" -> "Reduces fall damage taken by your mount while you control it. 0.2 means 20% less damage; maximum reduction 100%. Does not reduce passengers' fall damage or protect an unmounted animal.";
            case "swimming_speed_percent" -> "Increases player acceleration while moving in water, up to 50%. Does not affect land movement or mounts.";
            case "breath_efficiency" -> "Chance to prevent an underwater air-loss tick, up to 75%. Works alongside existing breathing effects; does not grant permanent Water Breathing.";
            case "travel_efficiency" -> "Reduces movement-related hunger expenditure from sprinting and swimming, up to 50%. Does not reduce combat, mining or mount stamina costs.";
            case "ore_yield_in_overworld" -> "Additional ore yield in the Overworld, combined with other ore yield before the 300% cap.";
            case "ore_yield_in_nether" -> "Additional ore yield in the Nether, combined with other ore yield before the 300% cap.";
            case "rare_mob_drop_chance_in_nether" -> "Additional rare-loot selection weight in the Nether; uses the same conditions and limits as Rare Loot Find.";
            case "item_rarity_in_overworld" -> "Additional higher-quality loot weighting in the Overworld. Applies the same quality-scaled calculation as Item Rarity.";
            case "item_rarity_in_nether" -> "Additional higher-quality loot weighting in the Nether. Applies the same quality-scaled calculation as Item Rarity.";
            case "item_rarity_in_end" -> "Additional higher-quality loot weighting in the End. Applies the same quality-scaled calculation as Item Rarity.";
            default -> id.startsWith("reduced_")&&id.endsWith("_duration_on_you")
                    ? "Shortens this ailment on you; Chill and Shock buildup also clear faster." : "";
        };
    }

    public static String reviseNodeDescription(String description) {
        if(description==null)return "";
        return description.replace("Projectiles have a 10% chance to Chain an additional time from terrain",
                        "Arrows and thrown tridents have a 10% chance to redirect from terrain toward a visible enemy within 12 blocks")
                .replace("15% chance to Pierce an Enemy","Arrows have a 15% chance to pierce 1 additional enemy")
                .replace("10% increased chance to inflict Ailments","10% increased chance to inflict Poison, Bleed and Shock")
                .replace("20% increased Critical Damage Bonus","+0.2x Critical Multiplier")
                .replace("8% increased Armor Penetration","8% increased flat Armour Penetration (requires flat penetration)")
                .replace("10% increased Armor Penetration","10% increased flat Armour Penetration")
                .replaceAll("([0-9.]+)% increased Knockback Resistance","+$1% Knockback Resistance");
    }
}
