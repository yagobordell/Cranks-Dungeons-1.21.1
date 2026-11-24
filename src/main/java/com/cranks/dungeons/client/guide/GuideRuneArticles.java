package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.stat.*;
import java.util.*;
import java.util.stream.Collectors;

/** One rune family, with equipment differences expressed as rows rather than duplicate articles. */
public final class GuideRuneArticles {
    private GuideRuneArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    public static Map<String,List<Integer>> fixed(CustomStat stat){
        var result=new LinkedHashMap<String,List<Integer>>();
        for(var item:GuideKnowledge.catalogue("fixed_runes").entrySet())for(var value:item.getValue().getAsJsonArray()){
            var row=value.getAsJsonObject();if(GuideKnowledge.text(row,"rune").equals(stat.getId()))
                result.put(item.getKey(),row.getAsJsonArray("tiers").asList().stream().map(com.google.gson.JsonElement::getAsInt).toList());
        }return result;
    }
    public static boolean visible(CustomStat stat){return !GuideTomes.bases(stat).isEmpty()||!fixed(stat).isEmpty();}
    public static List<CustomStat> family(CustomStat stat){return StatRegistry.getAllStats().stream().filter(other->other.getGroupId().equals(stat.getGroupId()))
            .filter(GuideRuneArticles::visible).sorted(Comparator.comparing(CustomStat::getEquipmentType).thenComparing(CustomStat::getId)).toList();}
    public static List<GuideDocument.Block> catalogue(EquipmentType type){return GuideEquipmentArticles.runes(type);}
    public static GuideDocument create(String id){
        var stat=StatRegistry.getStat(GuideTopics.base(id));if(stat==null)return new GuideDocument("Rune unavailable",List.of());
        var blocks=new ArrayList<GuideDocument.Block>();
        blocks.add(p(GuideTomes.name(stat)+" is a rune family. "+(stat.getComponents().size()>1?"Its linked bonuses occupy one affix slot together.":"")));
        blocks.add(h("Effect"));
        for(var component:stat.getComponents()){
            if(component.getAttributes().isEmpty())blocks.add(p("Increases the weapon's base critical chance before global increases: base chance × (1 + local increase) × (1 + global increase)."));
            else for(var attribute:component.getAttributes())blocks.add(p(GuideText.reference(GuideBook.statLabel(attribute.getIdAsString()),GuideTarget.stat(attribute.getIdAsString()))+": "+componentDescription(attribute)));
        }
        blocks.addAll(reference(family(stat)));
        return new GuideDocument(GuideTomes.name(stat),List.copyOf(blocks));
    }
    private static String componentDescription(net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute){
        if(com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,com.cranks.dungeons.registry.ModAttributes.PHYSICAL_DAMAGE_PERCENT))
            return "On a weapon, increases that weapon's physical damage after flat additions. For example, 20 physical damage with 50% local increase becomes 30 before character-wide bonuses.";
        return GuideWriting.playerText(GuideStatText.description(attribute.getIdAsString()));
    }
    private static String tierLabel(List<Integer> tiers){
        var parts=new ArrayList<String>();int start=tiers.getFirst(),previous=start;
        for(int index=1;index<=tiers.size();index++){
            if(index<tiers.size()&&tiers.get(index)==previous-1){previous=tiers.get(index);continue;}
            parts.add(start==previous?"T"+start:"T"+start+"–T"+previous);
            if(index<tiers.size())start=previous=tiers.get(index);
        }return String.join(" / ",parts);
    }
    /** Also embedded into the matching statistic article, avoiding a second article for the same effect. */
    public static List<GuideDocument.Block> reference(Collection<CustomStat> input){
        var blocks=new ArrayList<GuideDocument.Block>();var families=new LinkedHashMap<String,List<CustomStat>>();
        input.stream().filter(GuideRuneArticles::visible).sorted(Comparator.comparing(CustomStat::getGroupId)).forEach(stat->families.computeIfAbsent(stat.getGroupId(),unused->new ArrayList<>()).add(stat));
        if(families.isEmpty())return blocks;
        blocks.add(h("Rune affixes"));var compatible=new ArrayList<List<String>>();
        for(var values:families.values()){
            var first=values.getFirst();String sides=values.stream().map(v->v.getAffixType().name().toLowerCase(Locale.ROOT)).distinct().collect(Collectors.joining(" / "));
            compatible.add(List.of(GuideTomes.name(first),sides,String.join("; ",GuideEquipmentGroups.describe(values))));
        }
        blocks.add(GuideTables.table(List.of("Rune","Affix","Equipment"),compatible,1.5,.7,2));
        blocks.add(h("Obtaining"));
        var random=families.values().stream().flatMap(Collection::stream).anyMatch(StatRegistry::isRandomlyRollable);
        if(random)blocks.add(p("Roll compatible equipment with random "+GuideText.reference("Runic Tomes",GuideTarget.article("tomes"))+" at the "+GuideText.reference("Runic Altar",GuideTarget.article("runic_altar"))+", or find the rune on dropped equipment. "+GuideText.reference("Artificial Tomes",GuideTarget.article("artificial_tome"))+" offer a choice of compatible results."));
        var tomes=new LinkedHashSet<com.cranks.dungeons.item.AbstractTomeItem>();input.forEach(stat->tomes.addAll(GuideTomes.forRune(stat)));
        if(!tomes.isEmpty()){
            blocks.add(new GuideDocument.Block("heading","Specialized tomes","anchor=specialized_tomes"));
            var rows=new ArrayList<List<String>>();for(var tome:tomes)rows.add(List.of(GuideText.reference(tome.getName().getString(),GuideText.itemTarget(tome.getDefaultStack())),"Equipment material tier"));
            blocks.add(GuideTables.table(List.of("Tome","Rune tier"),rows,2,1));
        }
        var unique=new LinkedHashSet<String>();for(var stat:input)fixed(stat).forEach((item,tiers)->unique.add(GuideText.reference(GuideIllustrations.stack(item).getName().getString(),GuideTarget.item(item))+" ("+GuideTomes.tiers(tiers)+")"));
        if(!unique.isEmpty())blocks.add(p("Fixed Unique sources: "+String.join(", ",unique)+"."));
        for(var values:families.values()){
            blocks.add(h(families.size()==1?"Tier values":GuideTomes.name(values.getFirst())+" — tier values"));
            var grouped=new LinkedHashMap<List<String>,List<Integer>>();
            for(int tier=10;tier>=1;tier--){
                final int t=tier;var groups=new LinkedHashMap<String,List<CustomStat>>();
                for(var stat:values){boolean ordinary=!GuideTomes.bases(stat).isEmpty();if(!ordinary&&!fixed(stat).values().stream().anyMatch(ts->ts.contains(t)))continue;
                    String range=stat.getComponents().stream().map(c->c.formatTierRange(t,stat.getEquipmentType())).collect(Collectors.joining("; "));groups.computeIfAbsent(range,unused->new ArrayList<>()).add(stat);}
                for(var entry:groups.entrySet())grouped.computeIfAbsent(List.of(String.join("; ",GuideEquipmentGroups.describe(entry.getValue())),entry.getKey()),unused->new ArrayList<>()).add(tier);
            }
            var rows=new ArrayList<List<String>>();grouped.forEach((key,tiers)->rows.add(List.of(tierLabel(tiers),key.getFirst(),key.getLast())));
            if(!rows.isEmpty())blocks.add(GuideTables.table(List.of("Tier","Equipment","Values"),rows,.7,1.8,2));
        }
        blocks.add(p(GuideText.reference("Material tier caps",GuideTarget.article("rune_tiers"))));
        return List.copyOf(blocks);
    }
}
