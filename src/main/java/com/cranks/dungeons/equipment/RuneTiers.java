package com.cranks.dungeons.equipment;

import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.stream.IntStream;

/** T1 is strongest. Unknown equipment starts conservatively; tags support future bases. */
public final class RuneTiers {
    private RuneTiers() {}
    private static TagKey<Item> tag(int tier) { return TagKey.of(RegistryKeys.ITEM,Identifier.of("cranks-dungeons","runes/tier_"+tier)); }
    private static final List<TagKey<Item>> TAGS=List.of(tag(10),tag(9),tag(8),tag(7));
    public static int best(ItemStack gear) {
        for(int i=0;i<TAGS.size();i++)if(gear.isIn(TAGS.get(i)))return 10-i;
        Item item=gear.getItem();
        if(item instanceof CrossbowItem)return 9;
        if(item instanceof BowItem||item instanceof ShieldItem)return 10;
        if(item instanceof ToolItem tool){
            var material=tool.getMaterial();
            if(material==ToolMaterials.NETHERITE)return 7;
            if(material==ToolMaterials.DIAMOND)return 8;
            if(material==ToolMaterials.IRON||material==ToolMaterials.GOLD)return 9;
        }
        if(item instanceof ArmorItem armor){
            var material=armor.getMaterial().value();
            if(material==ArmorMaterials.NETHERITE.value())return 7;
            if(material==ArmorMaterials.DIAMOND.value())return 8;
            if(material==ArmorMaterials.IRON.value()||material==ArmorMaterials.CHAIN.value()||material==ArmorMaterials.GOLD.value())return 9;
        }
        return 10;
    }
    public static int clamp(ItemStack gear,int tier){return Math.clamp(tier,best(gear),10);}
    /** Stronger tomes remove the weakest outcomes when the base has room to improve. */
    public static List<Integer> random(ItemStack gear,int strength){
        return RuneRollRules.tiers(best(gear),strength);
    }
    public static List<Integer> artificial(ItemStack gear){return IntStream.rangeClosed(best(gear),Math.min(10,best(gear)+2)).boxed().toList();}
}
