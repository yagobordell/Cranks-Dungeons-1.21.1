package com.cranks.dungeons.client;

import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Tooltip order, preserving the original index used by server crafting requests. */
final class RuneDisplayOrder {
    record Entry(int index, ItemStatManager.ItemStat rune, CustomStat definition) {}
    private RuneDisplayOrder() {}
    static List<Entry> entries(ItemStack gear){
        var runes=ItemStatManager.getStats(gear);var result=new ArrayList<Entry>();
        for(var side:CustomStat.AffixType.values())for(int i=0;i<runes.size();i++){
            var definition=StatRegistry.getStat(runes.get(i).statId);
            if(definition!=null&&definition.getAffixType()==side)result.add(new Entry(i,runes.get(i),definition));
        }
        return List.copyOf(result);
    }
}
