package com.cranks.dungeons.gathering;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class GatheringTags {
    private GatheringTags() {}
    private static TagKey<Item> item(String id){return TagKey.of(RegistryKeys.ITEM,Identifier.of("cranks-dungeons","gathering/"+id));}
    public static final TagKey<Item> RARE_LOOT=item("rare_loot");
    public static final TagKey<Item> MONSTER_DROPS=item("monster_drops");
    public static final TagKey<Item> ANIMAL_DROPS=item("animal_drops");
    public static final TagKey<Item> MEAT=item("meat");
}
