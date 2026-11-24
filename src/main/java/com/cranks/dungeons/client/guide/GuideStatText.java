package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.stat.StatDescriptions;

/** Reference prose complements the shared combat/gathering explanations; no gameplay values are changed. */
public final class GuideStatText {
    private GuideStatText(){}
    public static String description(String key){
        String id=key.substring(key.indexOf(':')+1);
        if(id.equals("experience_bonus"))return "Increases experience gained from personal World discoveries. It does not grant combat XP for repeating ordinary gathering or crafting.";
        String shared=StatDescriptions.description(id);if(!shared.isBlank())return shared;
        String exact=switch(id){
            case "strength"->"Each point adds 2 maximum health. Strength also satisfies equipment requirements; it does not directly increase weapon damage.";
            case "dexterity"->"Each point adds 2 evasion rating. Dexterity also satisfies equipment requirements.";
            case "intelligence"->"Each point adds 2 maximum mana. Intelligence also satisfies equipment requirements.";
            case "generic.max_health","max_health_bonus"->"Maximum health determines how much life you can lose before dying. Flat bonuses are combined with your other maximum-health sources before applicable percentage bonuses and keystones.";
            case "max_mana","max_mana_bonus"->"Maximum mana is your mana resource capacity. Intelligence supplies 2 additional maximum mana per point; recovery restores mana up to this capacity.";
            case "generic.armor","bonus_armor"->"Armour reduces physical hit damage using armour / (armour + 10 x incoming physical damage), capped at 90%. Larger hits receive less percentage protection.";
            case "evasion_rating","derived/evasion_rating"->"Automatic evasion uses rating / (rating + 8 x eligible incoming hit damage), capped at 75%. An entropy counter spreads avoided hits out; evasion does not protect against ailment damage over time.";
            case "energy_shield"->"Energy Shield absorbs eligible hit damage before health. Void consumes twice as much shield. Poison, Bleed and other bypassing damage reach health directly. Its recharge starts after a period without an eligible hit.";
            case "energy_shield_percent"->"Increases total maximum Energy Shield. Item-local improvements are already included in the shield value displayed on the equipment.";
            case "energy_shield_recharge_rate_percent"->"Increases delayed Energy Shield recharge above its base 12.5% of maximum shield per second, up to 50% per second. This is separate from regeneration and leech.";
            case "faster_start_of_energy_shield_recharge"->"Shortens the base 4-second recharge delay: delay = 4 / (1 + bonus), with a 1-second minimum. It does not increase recharge speed.";
            case "max_health_percent"->"Increases maximum health after flat health and attribute contributions. Keystone rules can replace or redirect this resource.";
            case "evasion_rating_percent"->"Increases total Evasion Rating. A 20% increase is a rating bonus, not 20 additional percentage points of evade chance.";
            case "generic.attack_damage","bonus_attack_damage"->"Flat physical attack damage before applicable damage increases, critical strikes, conversions and target mitigation. A weapon's displayed physical damage already includes its baked local rune and quality improvements.";
            case "generic.attack_speed"->"Attack speed controls the attack cooldown. Bow drawing and crossbow reloading use the weapon's speed with applicable attack-speed bonuses; the charge indicator follows that same timing.";
            case "crit_chance"->"Base RPG critical chance is 5%. Weapon-local and global critical-chance increases modify it; conditional bonuses apply against eligible targets. Final chance is capped at 100%. Jumping does not guarantee a critical strike.";
            case "global_crit_chance_percent"->"Increases critical-strike chance globally. A 20% increase to a 5% base chance is a relative increase, not a final 25% chance. See the effective Critical Chance in your stats panel.";
            case "crit_chance_against_afflicted"->"Additional critical chance when the target is afflicted by an eligible ailment. It is conditional and is listed separately from the ordinary critical chance.";
            case "crit_multiplier"->"Critical hits start at 150% damage. This multiplier controls critical damage before the target's critical protection and any keystone changes.";
            case "damage_against_afflicted_percent"->"Increases damage against a target currently affected by an eligible ailment. It is conditional on the target, not on whether you are afflicted.";
            case "damage_while_afflicted_percent"->"Increases damage while you are affected by an eligible ailment. It is applied once during damage assembly, separately from the target-afflicted condition.";
            case "armor_speed_penalty_reduction"->"Reduces the movement penalty calculated from your Armour value. The base penalty is 3% per complete 100 Armour; this statistic reduces that penalty rather than increasing movement speed directly.";
            case "low_armor_speed_bonus"->"Grants movement speed for each point your total Armour is below 150: bonus = (150 - Armour) x this value. It gives no bonus at or above 150 Armour.";
            case "feather_falling"->"Reduces fall damage by this fraction before any additional rider-specific mount fall protection. It does not affect ordinary combat hits.";
            case "fortune","crop_fortune","log_fortune","dig_fortune"->"Legacy yield bonus retained for existing equipment and saves. It contributes to the corresponding modern yield statistic; the combined yield limit still applies. New World Tree bonuses should use the named yield statistics.";
            case "experience_bonus"->"Legacy bonus that now contributes to World Discovery Experience. It does not restore repeatable experience for breaking the same resource blocks.";
            case "mining_experience","crop_experience","combat_experience","breeding_experience","fishing_experience"->"Legacy statistic retained for old data. The current progression system uses combat participation and first-time world discoveries; new World Tree nodes should use supported progression statistics instead.";
            case "elemental_damage_percent"->"Increases Fire, Cold and Lightning damage. Void is not included. Applicable element-specific increases still apply to their own damage type.";
            case "freeze_buildup_percent","electrocute_buildup_percent"->"Increases buildup generated toward this control ailment. Buildup is compared with the target's threshold; duration is controlled by magnitude instead.";
            case "chance_to_shock"->"Chance to apply Shock on a qualifying Lightning hit. Positive chance enables the critical-hit guarantee; the hit must still meet magnitude requirements.";
            case "crit_non_damaging_ailment_magnitude_percent"->"Increases the magnitude of non-damaging ailments caused by critical hits, including control duration or Shock amplification as applicable. The ailment's own limits still apply.";
            case "thorns_damage"->"Returns physical retaliation damage to an eligible attacker when you are hit. It is a retaliation effect, not added weapon damage.";
            case "enchantment_effectiveness"->"Increases compatible attribute bonuses supplied by enchantments on this equipment. It does not add enchantment slots or turn utility enchantments into stackable effects.";
            case "knockback_resistance","generic.knockback_resistance"->"Reduces knockback received. Conditional knockback protection can add to the applicable resistance.";
            case "knockback"->"Increases knockback dealt by eligible attacks. The target's knockback resistance still applies.";
            case "generic.movement_speed","movement_speed"->"Changes player walking movement speed. Equipped armour can apply its own movement penalty; mount speed is controlled by riding statistics instead.";
            case "flying_speed"->"Improves gliding movement while fall-flying with an Elytra. It does not change ordinary walking or mount speed.";
            case "durability_bonus"->"Increases the equipment's maximum durability when its item attributes are rebuilt. This does not repair durability already lost.";
            case "trident_return"->"Makes the thrown trident return to its owner. This is an on/off utility effect rather than a chance or a damage bonus.";
            case "mace_wind_bounce"->"Enables the mace's wind-bounce effect on eligible hits. This is an on/off utility effect.";
            case "mace_falling_damage"->"Adds the mace's falling-damage contribution according to fallen distance. Ordinary damage bonuses and target defences still participate in the resolved hit.";
            case "keystone_avatar_of_fire"->"Converts your damage to Fire according to Avatar of Fire's conversion rule. The resulting Fire damage uses Fire scaling and Fire resistance.";
            case "keystone_resolute_technique"->"Your eligible attacks cannot be evaded, but they cannot critically strike.";
            case "keystone_elemental_overload"->"Recent critical hits activate an elemental-damage benefit, while critical strikes lose their extra critical damage. Physical and Void damage are not elemental.";
            case "keystone_iron_reflexes"->"Converts Evasion Rating into Armour. The converted rating no longer gives an evade chance.";
            case "keystone_point_blank"->"Projectile hits gain more damage close to the attacker and lose damage at long range. The distance is evaluated by the projectile combat pipeline.";
            case "horse_endurance"->"A horse's inherited Endurance capacity. Compare it in the mount panel when choosing breeding stock. Rider Mount Endurance is a separate bonus that reduces resource drain; it does not rewrite this inherited gene.";
            case "generic.oxygen_bonus"->"Improves underwater air conservation. With an Oxygen Bonus of 1, each ordinary air-loss check has a 50% chance to preserve your air. This helps breathing time but does not grant permanent Water Breathing.";
            case "player.submerged_mining_speed"->"Controls how quickly you mine while your head is underwater. The ordinary multiplier is 0.2; a flat +1 raises it to 1.2 before other applicable mining modifiers. Being unsupported in the water can still slow mining separately.";
            case "generic.mining_efficiency","player.mining_efficiency"->"Adds to suitable tools' mining effectiveness. Harvest requirements still apply, and using the wrong tool can prevent the resource drop. World Tree gathering speed scales the tool's effective base separately; Haste and Mining Fatigue still affect breaking time.";
            case "player.block_interaction_range"->"Your block interaction distance in metres. Bonus Block Reach can add at most 2 metres. It does not extend entity attack reach or bypass line of sight and protection checks.";
            default->"";
        };
        if(!exact.isBlank())return exact;
        if(id.endsWith("_extra_percent")||id.startsWith("physical_as_extra_"))return "Gains an additional portion of the original physical attack damage as this damage type. This adds damage instead of moving the original portion into another type; the resulting damage uses its own scaling and defences.";
        if(id.startsWith("physical_to_")&&id.endsWith("_conversion_percent"))return "Converts this portion of Physical attack damage into "+id.substring(12,id.length()-19)+" damage. Conversion moves damage instead of adding a second copy. Combined conversions are limited to the available Physical portion, then the resulting damage uses the destination type\'s scaling and defences.";
        if(id.endsWith("_damage_percent"))return "Increases eligible "+id.replace("_damage_percent","").replace('_',' ')+" damage. Applicable increased-damage bonuses combine in the combat pipeline and are applied once.";
        if(id.endsWith("_damage"))return "Adds flat "+id.replace("_damage","").replace('_',' ')+" damage to eligible attacks. The damage is resolved with that type's scaling and the target's matching defences.";
        return "This statistic contributes to the matching character, equipment or survival system. Its displayed value includes the currently applicable sources; conditional effects only apply while their stated condition is satisfied.";
    }
}
