package com.cranks.dungeons.client.guide;

import java.util.*;

/** Worked examples and useful relationships, without storage/authoring prose. */
public final class GuideWriting {
    private GuideWriting() {}
    private static String path(String id){return id.substring(id.indexOf(':')+1);}
    /** Enchantment definitions use floats; don't expose their binary rounding noise as player values. */
    public static double displayValue(double value){return java.math.BigDecimal.valueOf(value).setScale(4,java.math.RoundingMode.HALF_UP).doubleValue();}
    public static String playerText(String text){
        return text.replaceAll("(?i)0\\.\\d+ means \\d+%[^.;]*[.;]\\s*", "")
                .replaceAll("(?i)A bonus of \\d+% is stored as 0\\.\\d+\\.?", "")
                .replace("when its item attributes are rebuilt", "")
                .replace("baked local rune and quality improvements", "local runes and quality")
                .replace("when its item attributes are rebuilt", "")
                .replace("combat pipeline", "damage calculation")
                .replace("projectile combat pipeline", "projectile's travel")
                .replaceAll("New World Tree bonuses should[^.]*\\.", "")
                .replace("A supported vanilla attribute used by the mod's gathering and interaction rules. ", "")
                .replaceAll("\\s+", " ").strip();
    }
    public static List<String> calculations(String key){
        String id=path(key);
        return switch(id){
            case "generic.armor","bonus_armor","armor_percent"->List.of("Physical damage reduction = Armour ÷ (Armour + 10 × incoming Physical hit), capped at 90%.","100 Armour against a 50-damage Physical hit reduces it by about 16.7%. Against a 10-damage hit, the same Armour reduces it by 50%.");
            case "evasion_rating","evasion_rating_percent"->List.of("Evade chance = Evasion Rating ÷ (Evasion Rating + max(1, 8 × eligible incoming hit)), capped at 75%.","400 Evasion against a 50-damage eligible hit gives a 50% evade chance. The same rating is less reliable against a larger hit; it does not avoid damage-over-time ticks.");
            case "crit_chance","global_crit_chance_percent"->List.of("Final chance = base chance × (1 + local increase ÷ 100) × (1 + global increase ÷ 100) + applicable conditional chance, capped at 100%.","With a 5% base, 40% local increase and 20% global increase, the result is 8.4% before conditional chance. Resolute Technique prevents critical strikes.");
            case "crit_multiplier"->List.of("A 150% critical multiplier makes a 100-damage ordinary hit deal 150 damage before target defences. Adding 15 percentage points makes that 165 damage. Critical Protection reduces the extra portion, not the ordinary hit.");
            case "faster_start_of_energy_shield_recharge"->List.of("Recharge delay = 4 seconds ÷ (1 + Faster Start ÷ 100), with a 1-second minimum. 100% Faster Start shortens a 4-second delay to 2 seconds.");
            case "energy_shield_recharge_rate_percent"->List.of("Recharge starts at 12.5% of maximum Shield per second and scales with Recharge Rate, up to 50% of maximum Shield per second. A 20% increase gives 15% per second before the cap.");
            case "bleed_magnitude_percent"->List.of("Stored share = 40% × (1 + Bleed Magnitude ÷ 100), capped at 100%. With 50% increased Magnitude, Bleed stores 60% of later qualifying hit damage for its burst.");
            case "mana_regeneration_rate","mana_recovery_rate","mana_regeneration_percent"->List.of("Mana per second = (flat regeneration + maximum Mana × percentage regeneration ÷ 100) × (1 + Regeneration Rate ÷ 100) × (1 + Recovery Rate ÷ 100).", "Regeneration Rate affects regeneration; Recovery Rate also improves eligible mana leech and mana-on-kill gains.");
            default->List.of();
        };
    }
    public static List<String> relatedStats(String key){
        String id=path(key);
        if(id.contains("crit"))return List.of("crit_chance","global_crit_chance_percent","crit_multiplier","reduced_crit_damage_taken_percent");
        if(id.contains("armor")||id.contains("armour"))return List.of("bonus_armor","armor_percent","flat_armor_penetration","max_health_bonus");
        if(id.contains("energy_shield"))return List.of("energy_shield","energy_shield_percent","energy_shield_recharge_rate_percent","faster_start_of_energy_shield_recharge","void_resistance");
        if(id.contains("poison"))return List.of("chance_to_poison","poison_magnitude_percent","poison_max_stacks","void_resistance");
        if(id.contains("bleed"))return List.of("chance_to_bleed","bleed_magnitude_percent","physical_damage_percent");
        if(id.contains("freeze")||id.contains("cold"))return List.of("cold_damage_percent","freeze_buildup_percent","freeze_magnitude_percent","cold_resistance");
        if(id.contains("shock")||id.contains("electrocute")||id.contains("lightning"))return List.of("lightning_damage_percent","chance_to_shock","shock_magnitude_percent","electrocute_buildup_percent","electrocute_magnitude_percent");
        if(id.contains("fire")||id.contains("burn")||id.contains("flammability"))return List.of("fire_damage_percent","flammability_magnitude_percent","burn_magnitude_percent","fire_resistance");
        if(id.contains("leech")||id.contains("leeched")||id.contains("health"))return List.of("max_health_bonus","life_regeneration","physical_damage_leeched_as_life","health_on_kill");
        if(id.contains("mana"))return List.of("max_mana","mana_regeneration_rate","mana_recovery_rate","physical_damage_leeched_as_mana");
        if(id.contains("projectile"))return List.of("projectile_damage_percent","projectile_speed","projectile_pierce_chance","projectile_ricochet_chance");
        return List.of();
    }
}
