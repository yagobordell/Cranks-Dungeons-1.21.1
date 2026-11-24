package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.loot.RunicTomeLootHandler;
import com.cranks.dungeons.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.*;
import java.util.stream.Collectors;

public final class GuideCreatureArticles {
    private GuideCreatureArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    private static String item(String id){return GuideText.reference(Registries.ITEM.get(Identifier.of(id)).getName().getString(),GuideTarget.item(id));}
    public static String reference(String id){return GuideText.reference(Registries.ENTITY_TYPE.get(Identifier.of(id)).getName().getString(),new GuideTarget(GuideTarget.Kind.MOB,id));}
    public static List<String> tomeSources(net.minecraft.item.Item tome){return GuideKnowledge.catalogue("creatures").keySet().stream().filter(id->{
        var type=Registries.ENTITY_TYPE.get(Identifier.of(id));var theme=RunicTomeLootHandler.thematicTomes(type);
        return RunicTomeLootHandler.guideMobTier(type)>0&&(theme.regular()==tome||theme.greater()==tome);
    }).toList();}
    @SuppressWarnings("unchecked")
    private static void baseStatistics(List<GuideDocument.Block> blocks,EntityType<?> type){
        if(!net.minecraft.entity.attribute.DefaultAttributeRegistry.hasDefinitionFor(type))return;
        var attributes=net.minecraft.entity.attribute.DefaultAttributeRegistry.get((EntityType<? extends net.minecraft.entity.LivingEntity>)type);
        var values=new ArrayList<String>();
        for(var attribute:List.of(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH,
                net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE,net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR,
                com.cranks.dungeons.registry.ModAttributes.EVASION_RATING,com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD,
                com.cranks.dungeons.registry.ModAttributes.FIRE_RESISTANCE,com.cranks.dungeons.registry.ModAttributes.COLD_RESISTANCE,
                com.cranks.dungeons.registry.ModAttributes.LIGHTNING_RESISTANCE,com.cranks.dungeons.registry.ModAttributes.VOID_RESISTANCE)){
            if(!attributes.has(attribute))continue;double value=attributes.getValue(attribute);if(value==0)continue;
            String id=attribute.getIdAsString();String name=GuideBook.statLabel(id);boolean resistance=id.endsWith("_resistance");
            if(id.endsWith("max_health"))name="Maximum Health";if(id.endsWith("attack_damage"))name="Base Melee Attack";
            values.add(GuideText.reference(name,GuideTarget.stat(id))+": "+com.cranks.dungeons.stat.passive.NodeBonusFormat.number(value*(resistance?100:1))+(resistance?"%":""));
        }
        if(values.isEmpty())return;blocks.add(h("Statistics"));var rows=new ArrayList<List<String>>();for(String value:values){int split=value.lastIndexOf(": ");rows.add(List.of(value.substring(0,split),value.substring(split+2)));}blocks.add(GuideTables.table(List.of("Statistic","Base value"),rows,2,1));
        blocks.add(p("Values before equipment, difficulty modifiers, taming and inherited genes. Special attacks can use different damage rules."));
    }
    public static GuideDocument create(String id){
        var facts=GuideKnowledge.entry("creatures",id);var type=Registries.ENTITY_TYPE.get(Identifier.of(id));
        if(facts.isEmpty())return new GuideDocument(type.getName().getString(),List.of());
        var blocks=new ArrayList<GuideDocument.Block>();blocks.add(p(GuideKnowledge.text(facts,"summary")));
        blocks.add(h("Where and when to find it"));blocks.add(p(GuideKnowledge.text(facts,"encounter")));
        var biomes=GuideKnowledge.strings(facts,"biomes");
        if(!biomes.isEmpty()){
            blocks.add(new GuideDocument.Block("subheading","Natural habitats"));
            blocks.add(p(biomes.stream().map(key->{var biome=Identifier.of(key);return Text.translatable("biome."+biome.getNamespace()+"."+biome.getPath()).getString();}).collect(Collectors.joining(", "))));
        }
        if(!GuideKnowledge.text(facts,"advice").isBlank()){blocks.add(h("Behaviour"));blocks.add(p(GuideKnowledge.text(facts,"advice")));}
        baseStatistics(blocks,type);
        var groups=new LinkedHashMap<String,List<String>>();
        for(var value:facts.getAsJsonArray("drops")){
            var drop=value.getAsJsonObject();String key=GuideKnowledge.text(drop,"item");Identifier parsed=Identifier.tryParse(key);
            if(parsed==null||!Registries.ITEM.containsId(parsed))continue;
            var conditions=GuideKnowledge.strings(drop,"conditions");String heading=conditions.contains("carried")?"Carried equipment":conditions.contains("rare")?"Occasional drops":conditions.contains("player")?"Player-kill drops":"Material drops";
            groups.computeIfAbsent(heading,k->new ArrayList<>()).add(item(key));
        }
        groups.forEach((heading,drops)->{blocks.add(h(heading));blocks.add(p(String.join(", ",drops)));});
        if(type==EntityType.SHEEP)blocks.add(p("Wool matches the sheep's colour and is also obtainable by shearing. A recently sheared sheep has no Wool to drop."));
        if(type==EntityType.COW||type==EntityType.PIG||type==EntityType.CHICKEN||type==EntityType.SHEEP||type==EntityType.RABBIT||type==EntityType.HOGLIN)
            blocks.add(p("A creature killed while burning can supply cooked meat instead of the raw version."));
        int reward=RunicTomeLootHandler.guideMobTier(type);
        if(reward>0){
            blocks.add(h("Runic rewards"));blocks.add(p("Can also drop "+GuideText.reference("Runic Tomes",GuideTarget.article("tomes"))+"."));
            var theme=RunicTomeLootHandler.thematicTomes(type);
            if(theme.regular()!=null)blocks.add(p(GuideText.reference(theme.regular().getName().getString(),GuideText.itemTarget(theme.regular().getDefaultStack()))+": 0.8% base drop chance."));
            if(theme.greater()!=null)blocks.add(p(GuideText.reference(theme.greater().getName().getString(),GuideText.itemTarget(theme.greater().getDefaultStack()))+": 0.1% base drop chance. Item Rarity can affect these weighted drops."));
        }
        blocks.add(h("Equipment-upgrade materials"));
        blocks.add(p("Its creature-loot rewards can include "+GuideText.reference(ModItems.ARMOURERS_SCRAP.getName().getString(),GuideText.itemTarget(ModItems.ARMOURERS_SCRAP.getDefaultStack()))+" and "+GuideText.reference(ModItems.BLACKSMITHS_WHETSTONE.getName().getString(),GuideText.itemTarget(ModItems.BLACKSMITHS_WHETSTONE.getDefaultStack()))+" for the "+GuideText.reference("Upgrade Bench",GuideTarget.article("quality"))+"."));
        blocks.add(h("Related articles"));
        var related=new ArrayList<String>();related.add(GuideText.reference("Enemy equipment and threat",GuideTarget.article("enemies")));related.add(GuideText.reference("Loot and gathering",GuideTarget.article("loot")));
        if(type==EntityType.HORSE||type==EntityType.DONKEY||type==EntityType.MULE||type==EntityType.CAMEL||type==EntityType.LLAMA||type==EntityType.PIG||type==EntityType.STRIDER)
            related.add(GuideText.reference("Mounts",GuideTarget.article("mounts")));
        blocks.add(p(String.join(" | ",related)));
        return new GuideDocument(type.getName().getString(),List.copyOf(blocks));
    }
}
