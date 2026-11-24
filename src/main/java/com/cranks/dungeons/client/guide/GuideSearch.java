package com.cranks.dungeons.client.guide;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;

/** Searches authored facts and effect descriptions without generating tier tables or unknown recipes. */
public final class GuideSearch {
    private static final Map<GuideTarget,String> TEXT=new HashMap<>();
    private static Object world;
    private GuideSearch() {}
    public static void reset(){TEXT.clear();world=null;}
    public static String text(GuideTarget target){
        target=GuideTopics.root(GuideTopics.resolve(target));
        Object current=net.minecraft.client.MinecraftClient.getInstance().world;if(current!=world){TEXT.clear();world=current;}
        return TEXT.computeIfAbsent(target,key->{
        var result=new StringBuilder(key.key());
        switch(key.kind()){
            case ARTICLE->{if(key.key().startsWith("equipment_")){
                var type=com.cranks.dungeons.equipment.EquipmentType.valueOf(key.key().substring(10).toUpperCase(Locale.ROOT));
                for(var item:GuideEquipmentArticles.variants(type))result.append(' ').append(item.getName().getString());
                for(var stat:com.cranks.dungeons.stat.StatRegistry.getStatsByEquipmentType(type))result.append(' ').append(GuideTomes.name(stat));
            }else for(var block:GuideBook.load(key).blocks())if(!block.type().equals("item_image"))result.append(' ').append(block.type().equals("table")?GuideTables.searchable(block.value()):GuideText.plain(block.value()));}
            case STAT->{for(var alias:GuideBook.statistics().values())if(GuideTopics.resolve(GuideTarget.stat(alias.id())).key().equals(key.key()))result.append(' ').append(alias.name());var stat=GuideBook.statistics().get(key.key());if(stat!=null)result.append(' ').append(stat.name()).append(' ').append(GuideWriting.playerText(stat.description().isBlank()?GuideStatText.description(key.key()):stat.description()));}
            case RUNE->{var rune=com.cranks.dungeons.stat.StatRegistry.getStat(key.key().split("#",2)[0]);if(rune!=null){
                result.append(' ').append(GuideTomes.name(rune)).append(' ').append(rune.getEquipmentType().getName());
                for(var component:rune.getComponents())for(var attribute:component.getAttributes())result.append(' ').append(GuideStatText.description(attribute.getIdAsString()));
                for(var tome:GuideTomes.forRune(rune))result.append(' ').append(tome.getName().getString());}}
            case ITEM->{var item=GuideIllustrations.stack(key.key());if(!item.isEmpty()){
                result.append(' ').append(item.getName().getString());
                if(item.getItem() instanceof com.cranks.dungeons.item.AbstractTomeItem tome){for(var summary:tome.summaries())result.append(' ').append(summary.label()).append(' ').append(GuideTomes.name(summary.stat()));
                    var greater=Registries.ITEM.get(Identifier.of(Registries.ITEM.getId(item.getItem()).getNamespace(),"greater_"+Registries.ITEM.getId(item.getItem()).getPath()));if(greater instanceof com.cranks.dungeons.item.AbstractTomeItem t)result.append(' ').append(t.getName().getString());}
                var facts=GuideKnowledge.entry("items",Registries.ITEM.getId(item.getItem()).toString());
                for(String field:List.of("summary","obtaining","uses"))result.append(' ').append(GuideText.plain(GuideKnowledge.text(facts,field)));
                var contents=item.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);if(contents!=null)for(var effect:contents.getEffects())result.append(' ').append(effect.getEffectType().value().getName().getString());}}
            case MOB->{var facts=GuideKnowledge.entry("creatures",key.key());for(String field:List.of("summary","encounter","advice"))result.append(' ').append(GuideText.plain(GuideKnowledge.text(facts,field)));
                for(String biome:GuideKnowledge.strings(facts,"biomes"))result.append(' ').append(biome.replace('_',' '));
                for(var drop:facts.getAsJsonArray("drops")){Identifier id=Identifier.tryParse(GuideKnowledge.text(drop.getAsJsonObject(),"item"));if(id!=null&&Registries.ITEM.containsId(id))result.append(' ').append(Registries.ITEM.get(id).getName().getString());}}
            case EFFECT->{var effect=Registries.STATUS_EFFECT.getEntry(Identifier.of(key.key()));if(effect.isPresent()){
                result.append(' ').append(effect.get().value().getName().getString());
                for(var line:com.cranks.dungeons.effect.EffectDescriptions.descriptions(new net.minecraft.entity.effect.StatusEffectInstance(effect.get(),200,0)))result.append(' ').append(line.getString());}}
            case ENCHANTMENT->{var client=net.minecraft.client.MinecraftClient.getInstance();if(client.world!=null){
                var entry=client.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(key.key()));
                var parsed=Identifier.of(key.key());client.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(parsed.getNamespace(),"greater_"+parsed.getPath())).ifPresent(other->result.append(' ').append(other.value().description().getString()));
                entry.ifPresent(value->{result.append(' ').append(value.value().description().getString());var attributes=value.value().effects().get(net.minecraft.component.EnchantmentEffectComponentTypes.ATTRIBUTES);
                    if(attributes!=null)for(var attribute:attributes)result.append(' ').append(GuideBook.statLabel(attribute.attribute().getIdAsString()));});}}
            case RECIPE->{} // Personal recipe visibility is evaluated only when opening the actual recipe.
        }
        return result.toString().toLowerCase(Locale.ROOT).replace('_',' ');
    });}
    public static int rank(GuideBook.Entry entry,String query){
        String title=entry.title().toLowerCase(Locale.ROOT),q=query.toLowerCase(Locale.ROOT).strip();
        return title.equals(q)?0:title.startsWith(q)?1:title.contains(q)?2:3;
    }
}
