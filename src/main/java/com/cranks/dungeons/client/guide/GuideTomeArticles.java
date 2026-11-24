package com.cranks.dungeons.client.guide;
import com.cranks.dungeons.item.AbstractTomeItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;
import java.util.stream.Collectors;

/** Normal and Greater variants share one factual article. */
public final class GuideTomeArticles {
    private GuideTomeArticles(){}
    public static GuideDocument create(AbstractTomeItem normal){
        var blocks=new ArrayList<GuideDocument.Block>();var id=Registries.ITEM.getId(normal);var variants=new ArrayList<AbstractTomeItem>();variants.add(normal);
        var greater=Registries.ITEM.get(Identifier.of(id.getNamespace(),"greater_"+id.getPath()));if(greater instanceof AbstractTomeItem tome)variants.add(tome);
        blocks.add(new GuideDocument.Block("item_image",id.toString(),"caption="+GuideTopics.title(GuideTarget.item(id.toString()),normal.getName().getString())));
        blocks.add(new GuideDocument.Block("text","A specialized tome adds or replaces a specific rune on Magic or Rare equipment at the "+GuideText.reference("Runic Altar",GuideTarget.article("runic_altar"))+"."));
        for(int i=0;i<variants.size();i++){
            var tome=variants.get(i);blocks.add(new GuideDocument.Block("heading",tome.getName().getString(),"anchor="+(i==0?"normal":"greater")));
            var rows=new ArrayList<List<String>>();for(var summary:tome.summaries())rows.add(List.of(summary.label(),GuideText.reference(GuideTomes.name(summary.stat()),GuideTarget.rune(summary.stat().getId()))));
            blocks.add(GuideTables.table(List.of("Equipment","Guaranteed rune"),rows,1,1.7));
        }
        blocks.add(new GuideDocument.Block("heading","Obtaining"));var sources=new ArrayList<List<String>>();
        for(var tome:variants)sources.add(List.of(tome.getName().getString(),GuideCreatureArticles.tomeSources(tome).stream().map(GuideCreatureArticles::reference).collect(Collectors.joining(", "))));
        blocks.add(GuideTables.table(List.of("Tome","Drops from"),sources,1.3,2));
        blocks.add(new GuideDocument.Block("heading","Usage"));
        blocks.add(new GuideDocument.Block("text","Choose a free slot on the rune's affix side or select an existing rune to replace. The result uses the equipment's material tier. Magic equipment becomes Rare. One tome is consumed per attempt; replacements let you keep the old rune or accept the new roll. The same rune family cannot appear twice on an item."));
        blocks.add(new GuideDocument.Block("text",GuideText.reference("Equipment tier limits",GuideTarget.article("rune_tiers"))+" | "+GuideText.reference("Runic Altar",GuideTarget.article("runic_altar"))));
        return new GuideDocument(GuideTopics.title(GuideTarget.item(id.toString()),normal.getName().getString()),List.copyOf(blocks));
    }
}
