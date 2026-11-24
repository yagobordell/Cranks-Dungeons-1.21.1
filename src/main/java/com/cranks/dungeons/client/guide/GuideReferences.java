package com.cranks.dungeons.client.guide;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class GuideReferences {
    private GuideReferences(){}
    // Explicit integration with the existing equipment formatter's field keys.
    private static final Map<String,String> FIELDS=Map.ofEntries(
            Map.entry("Physical Damage","minecraft:generic.attack_damage"),Map.entry("Attack Damage","minecraft:generic.attack_damage"),
            Map.entry("Attacks Per Second","minecraft:generic.attack_speed"),Map.entry("Attack Speed","cranks-dungeons:attack_speed"),
            Map.entry("Critical Hit Chance","cranks-dungeons:crit_chance"),Map.entry("Armor","minecraft:generic.armor"),
            Map.entry("Evasion Rating","cranks-dungeons:evasion_rating"),Map.entry("Energy Shield","cranks-dungeons:energy_shield"),
            Map.entry("Fire Damage","cranks-dungeons:fire_damage"),Map.entry("Cold Damage","cranks-dungeons:cold_damage"),
            Map.entry("Lightning Damage","cranks-dungeons:lightning_damage"),Map.entry("Void Damage","cranks-dungeons:void_damage"),
            Map.entry("Movement Speed","minecraft:generic.movement_speed"),Map.entry("Max Health","minecraft:generic.max_health"),
            Map.entry("Health Regen","cranks-dungeons:life_regeneration"),Map.entry("Block Value","cranks-dungeons:block_value"),
            Map.entry("Gathering Speed","cranks-dungeons:mining_efficiency"),Map.entry("Knockback Resistance","cranks-dungeons:knockback_resistance"),
            Map.entry("Fire Resistance","cranks-dungeons:fire_resistance"),Map.entry("Cold Resistance","cranks-dungeons:cold_resistance"),
            Map.entry("Lightning Resistance","cranks-dungeons:lightning_resistance"),Map.entry("Void Resistance","cranks-dungeons:void_resistance"),
            Map.entry("Rarity of Items Found","cranks-dungeons:item_rarity_percent"));
    public static GuideTarget equipmentField(String key){
        if(FIELDS.containsKey(key))return GuideTarget.stat(FIELDS.get(key));
        return switch(key){
            case "Quality"->GuideTarget.article("quality");
            case "Requires"->GuideTarget.article("rarity_requirements");
            case "Draw Time","Reload Time"->GuideTarget.article("ranged_weapons");
            case "Durability"->GuideTarget.article("repairing");
            default->null;
        };
    }
    public static GuideTarget station(ItemStack item){
        return station(Registries.ITEM.getId(item.getItem()).getPath());
    }
    public static GuideTarget station(String id){
        String page=switch(id){
            case "hand_crafting"->"hand_crafting";case "crafting_table"->"crafting_table";
            case "furnace"->"kiln";case "blast_furnace"->"foundry";case "smoker"->"smoker";
            case "campfire","soul_campfire"->"campfire";case "stonecutter"->"cutting_bench";
            case "anvil","chipped_anvil","damaged_anvil"->"anvil";case "grindstone"->"repairing";
            case "smithing_table","quality_station"->"quality";case "enchanting_table"->"enchantments";
            case "brewing_stand"->"alchemy";case "fletching_table"->"fletching";case "loom"->"tailoring";
            case "cartography_table"->"cartography";case "crafter"->"automation";
            case "runic_altar","rune_altar","runic_enhancement_altar"->"runic_altar";
            case "artificial_runic_altar","artificial_altar","artificial_rune_altar","artificial_enhancement_altar"->"artificial_tome";
            default->null;
        };
        return page==null?null:GuideTarget.article(page);
    }
    public static String chapterForStat(String id){
        String path=id.substring(id.indexOf(':')+1).replace("armour","armor");
        if(path.startsWith("keystone_"))return "keystones";
        if(path.startsWith("horse_")||path.contains("riding")||path.contains("mount_")||path.equals("sure_footing"))return "horse_training";
        if(path.contains("yield")||path.contains("fortune")||path.contains("mining")||path.contains("gather")||path.contains("discovery"))return "gathering_stats";
        if(path.contains("poison")||path.contains("bleed")||path.contains("burn")||path.contains("shock")||path.contains("freeze")||path.contains("electrocute")||path.contains("ailment")||path.contains("flammability"))return "ailments";
        if(path.contains("armor")||path.contains("evasion")||path.contains("resistance")||path.contains("block_"))return "defences";
        if(path.contains("health")||path.contains("mana")||path.contains("shield")||path.contains("regener")||path.contains("leech"))return "recovery";
        return "damage";
    }
    public static String slug(String name){return name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+","_").replaceAll("^_|_$","");}
    public static GuideTarget attribute(String id){return Identifier.tryParse(id)==null?null:GuideTarget.stat(id);}
}
