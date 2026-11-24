package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.registry.RegistryKeys;
import java.util.*;
import java.util.stream.Collectors;

public final class GuideStatArticles {
    private GuideStatArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    private static String ref(String title,GuideTarget target){return GuideText.reference(title,target);}
    public static GuideDocument create(String id){
        var entry=GuideBook.statistics().get(id);String title=GuideBook.statLabel(id),path=id.substring(id.indexOf(':')+1);
        String description=id.startsWith("derived/")&&entry!=null&&!entry.description().isBlank()?entry.description():GuideStatText.description(id);
        if(description.startsWith("This statistic contributes")&&entry!=null&&!entry.description().isBlank())description=entry.description();
        var blocks=new ArrayList<GuideDocument.Block>();blocks.add(p(GuideWriting.playerText(description)));
        if(Set.of("physical_damage_percent","armor_percent","evasion_rating_percent","energy_shield_percent").contains(path))
            blocks.add(p("On equipment, this kind of rune improves that item's own corresponding value. Bonuses from the Passive Tree improve the matching character total. The equipment tooltip already includes its local improvements."));
        var variants=GuideBook.statistics().values().stream().filter(ref->!ref.id().equals(id)&&GuideTopics.resolve(GuideTarget.stat(ref.id())).key().equals(id)).toList();
        var descriptions=new LinkedHashMap<String,String>();for(var variant:variants){String detail=GuideWriting.playerText(variant.description().isBlank()?GuideStatText.description(variant.id()):variant.description());if(!detail.isBlank()&&!detail.startsWith("This statistic contributes")&&!detail.equals(GuideWriting.playerText(description)))descriptions.putIfAbsent(detail,GuideBook.statLabel(variant.id()));}
        if(!descriptions.isEmpty()){blocks.add(h("Modifiers"));var rows=new ArrayList<List<String>>();descriptions.forEach((detail,label)->rows.add(List.of(label,detail)));blocks.add(GuideTables.table(List.of("Modifier","Effect"),rows,1,2.5));}
        var math=GuideWriting.calculations(id);if(!math.isEmpty()){blocks.add(h("Calculation and examples"));for(String line:math)blocks.add(p(line));}
        var family=new LinkedHashMap<String,List<CustomStat>>();
        for(var rune:StatRegistry.getAllStats())if(GuideRuneArticles.visible(rune)&&(rune.getComponents().stream().flatMap(component->component.getAttributes().stream()).anyMatch(attribute->GuideTopics.resolve(GuideTarget.stat(attribute.getIdAsString())).key().equals(id))
                ||id.equals("cranks-dungeons:crit_chance")&&rune.getGroupId().equals("local_crit_chance")))family.computeIfAbsent(rune.getGroupId(),unused->new ArrayList<>()).add(rune);
        if(!family.isEmpty())blocks.addAll(GuideRuneArticles.reference(family.values().stream().flatMap(Collection::stream).toList()));
        var sources=new ArrayList<String>();
        if(PassiveStatHelper.WHITELISTED_PASSIVE_ATTRIBUTES.contains(path)||variants.stream().anyMatch(v->PassiveStatHelper.WHITELISTED_PASSIVE_ATTRIBUTES.contains(v.id().substring(v.id().indexOf(':')+1))))sources.add(ref("Passive Tree",GuideTarget.article("passive_tree")));
        if(PassiveStatHelper.WHITELISTED_WORLD_ATTRIBUTES.contains(path)||variants.stream().anyMatch(v->PassiveStatHelper.WHITELISTED_WORLD_ATTRIBUTES.contains(v.id().substring(v.id().indexOf(':')+1))))sources.add(ref("World Tree",GuideTarget.article("world_tree")));
        var client=MinecraftClient.getInstance();
        if(client.world!=null)for(var enchantment:client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getIndexedEntries()){
            if(!com.cranks.dungeons.util.EnchantmentFilterUtil.isApproved(enchantment))continue;
            var effects=enchantment.value().effects().get(EnchantmentEffectComponentTypes.ATTRIBUTES);
            if(effects!=null&&effects.stream().anyMatch(effect->effect.attribute().getIdAsString().equals(id)))
                sources.add(ref(enchantment.value().description().getString(),new GuideTarget(GuideTarget.Kind.ENCHANTMENT,enchantment.getKey().orElseThrow().getValue().toString())));
        }
        String effect=switch(path){case "fire_resistance"->"fire_resistance";case "bonus_armor","generic.armor"->"resistance";case "life_regeneration"->"regeneration";case "movement_speed"->"speed";case "physical_damage_percent"->"strength";case "max_health_bonus"->"health_boost";case "mining_efficiency","mining_speed_percent"->"haste";default->null;};
        if(effect!=null)sources.add(ref("Effect: "+com.cranks.dungeons.stat.passive.NodeBonusFormat.name(effect),new GuideTarget(GuideTarget.Kind.EFFECT,"minecraft:"+effect)));
        if(!sources.isEmpty()){blocks.add(h("Other sources"));blocks.add(p(String.join(" | ",sources)));}
        blocks.add(h("Related articles"));var related=new LinkedHashSet<String>();
        related.add(ref("Full mechanics",GuideTarget.article(GuideReferences.chapterForStat(id))));
        for(String stat:GuideWriting.relatedStats(id))if(!stat.equals(path)&&GuideBook.statistics().containsKey("cranks-dungeons:"+stat))related.add(ref(GuideBook.statLabel("cranks-dungeons:"+stat),GuideTarget.stat("cranks-dungeons:"+stat)));
        related.add(ref("Reading your character",GuideTarget.article("stats")));blocks.add(p(String.join(" | ",related)));
        return new GuideDocument(title,List.copyOf(blocks));
    }
}
