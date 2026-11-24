package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.client.CharacterStats;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.inventory.RecipeDiscovery;
import com.cranks.dungeons.mixin.client.RecipeManagerSnapshotAccessor;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Shared resource-backed articles and live references. Personal recipe data is never globally cached. */
public final class GuideBook {
    public record Entry(GuideTarget target,String title,String category){}
    private static final Map<String,GuideDocument> ARTICLES=new HashMap<>();
    private static List<Entry> staticEntries;
    private static Map<String,CharacterStats.Reference> statEntries;
    private static final Gson GSON=new Gson();
    private static int generation;
    private GuideBook(){}
    public static void reload(){ARTICLES.clear();staticEntries=null;statEntries=null;GuideTomes.reset();GuideKnowledge.reset();GuideSearch.reset();GuideEquipmentArticles.reset();generation++;}
    public static int generation(){return generation;}
    private static GuideDocument.Block p(String text){return new GuideDocument.Block("text",text);}
    private static GuideDocument.Block h(String text){return new GuideDocument.Block("heading",text);}
    private static String ref(String label,GuideTarget target){return GuideText.reference(label,target);}

    public static Map<String,CharacterStats.Reference> statistics(){
        if(statEntries==null){var entries=new LinkedHashMap<String,CharacterStats.Reference>();
            for(var entry:CharacterStats.references())entries.put(entry.id(),new CharacterStats.Reference(entry.id(),friendly(entry.name(),entry.id()),entry.description(),entry.unit()));
            for(var attribute:ModAttributes.ALL_ATTRIBUTES){String id=attribute.getIdAsString();
                String path=Identifier.of(id).getPath();if(com.cranks.dungeons.gathering.GatheringStats.LEGACY.contains(path))continue;
                entries.putIfAbsent(id,new CharacterStats.Reference(id,friendly(PassiveStatHelper.getReadableName(attribute),id),GuideStatText.description(id),""));}
            for(var attribute:List.of(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR,
                    net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED,net.minecraft.entity.attribute.EntityAttributes.GENERIC_OXYGEN_BONUS,
                    net.minecraft.entity.attribute.EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED,
                    net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED,net.minecraft.entity.attribute.EntityAttributes.PLAYER_MINING_EFFICIENCY,
                    net.minecraft.entity.attribute.EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE)){
                String id=attribute.getIdAsString();entries.putIfAbsent(id,new CharacterStats.Reference(id,
                        friendly(Text.translatable(attribute.value().getTranslationKey()).getString(),id),GuideStatText.description(id),""));
            }
            statEntries=Collections.unmodifiableMap(entries);
        }return statEntries;
    }
    private static String friendly(String label,String id){
        if(!label.toLowerCase(Locale.ROOT).contains("attribute."))return label;
        String path=id.substring(id.indexOf(':')+1).replace("generic.","").replace('_',' ');var result=new StringBuilder();
        for(String word:path.split(" "))if(!word.isBlank())result.append(result.isEmpty()?"":" ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));return result.toString();
    }
    public static String statLabel(String id){
        var entry=statistics().get(id);String label=entry!=null?entry.name():friendly("attribute.",id);
        String path=id.substring(id.indexOf(':')+1);
        if(label.equalsIgnoreCase(path.replace('_',' '))){label=com.cranks.dungeons.stat.passive.NodeBonusFormat.name(id);if(path.endsWith("_percent"))label="Increased "+label;}
        return Character.toUpperCase(label.charAt(0))+label.substring(1);
    }
    public static List<Entry> index(){
        var entries=new ArrayList<>(articles());
        for(var stat:statistics().values())entries.add(new Entry(GuideTarget.stat(stat.id()),stat.name(),"Statistics"));
        for(var rune:StatRegistry.getAllStats())if(GuideRuneArticles.visible(rune))entries.add(new Entry(GuideTarget.rune(rune.getId()),GuideTomes.name(rune)+" - "+rune.getEquipmentType().getName(),"Runes"));
        var client=MinecraftClient.getInstance();
        if(client.player!=null){for(var id:ModComponents.ITEM_DISCOVERIES.get(client.player).knownIds())Registries.ITEM.getOrEmpty(id).ifPresent(item->entries.add(new Entry(GuideTarget.item(id.toString()),item.getName().getString(),"Discovered Items")));}
        if(client.player!=null)for(var formula:com.cranks.dungeons.alchemy.AlchemyPotions.FORMULAS)
            if(ModComponents.ALCHEMY_KNOWLEDGE.get(client.player).knows(formula.key()))for(var delivery:com.cranks.dungeons.alchemy.AlchemyItems.Delivery.values()){
                var potion=formula.stack(delivery);entries.add(new Entry(GuideText.itemTarget(potion),potion.getName().getString(),"Learned Potions"));}
        for(String id:GuideKnowledge.catalogue("creatures").keySet()){
            Identifier creature=Identifier.tryParse(id);if(creature!=null&&Registries.ENTITY_TYPE.containsId(creature))
                entries.add(new Entry(new GuideTarget(GuideTarget.Kind.MOB,id),Registries.ENTITY_TYPE.get(creature).getName().getString(),"Creatures"));}
        for(var entry:Registries.STATUS_EFFECT.getEntrySet())entries.add(new Entry(new GuideTarget(GuideTarget.Kind.EFFECT,entry.getKey().getValue().toString()),entry.getValue().getName().getString()+" (effect)","Status Effects"));
        if(client.world!=null)for(var entry:client.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getIndexedEntries())
            if(com.cranks.dungeons.util.EnchantmentFilterUtil.isApproved(entry))entries.add(new Entry(new GuideTarget(GuideTarget.Kind.ENCHANTMENT,entry.getKey().orElseThrow().getValue().toString()),entry.value().description().getString(),"Enchantments"));
        for(var type:EquipmentType.values())if(!GuideEquipmentArticles.variants(type).isEmpty())entries.add(new Entry(GuideTopics.equipment(type),GuideTopics.equipmentTitle(type),"Equipment"));
        for(String id:GuideKnowledge.catalogue("items").keySet()){var stack=GuideIllustrations.stack(id);if(!stack.isEmpty())entries.add(new Entry(GuideTarget.item(id),stack.getName().getString(),"Items and Materials"));}
        var topics=new LinkedHashMap<GuideTarget,Entry>();
        for(var entry:entries){var target=GuideTopics.root(GuideTopics.resolve(entry.target()));String category=entry.category();
            if(target.kind()==GuideTarget.Kind.ARTICLE&&target.key().startsWith("equipment_"))category="Equipment";
            else if(target.kind()==GuideTarget.Kind.STAT)category="Combat and Statistics";
            else if(target.kind()==GuideTarget.Kind.RUNE)category="Runes and Enchantments";
            else if(target.kind()==GuideTarget.Kind.ENCHANTMENT)category="Runes and Enchantments";
            else if(target.kind()==GuideTarget.Kind.EFFECT)category="Status Effects";
            else if(target.kind()==GuideTarget.Kind.ITEM)category="Items and Materials";
            topics.putIfAbsent(target,new Entry(target,GuideTopics.title(target,entry.title()),category));
        }
        return List.copyOf(topics.values());
    }
    public static List<Entry> articles(){
        if(staticEntries==null){var entries=new ArrayList<Entry>();
            try{var resource=MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("cranks-dungeons","compendium/compendium_index.json")).orElseThrow();
                try(var reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)){
                    var root=GSON.fromJson(reader,JsonObject.class);for(var section:root.getAsJsonArray("sections")){var s=section.getAsJsonObject();
                        for(var page:s.getAsJsonArray("pages")){var pageObject=page.getAsJsonObject();entries.add(new Entry(GuideTarget.article(pageObject.get("id").getAsString()),pageObject.get("title").getAsString(),s.get("title").getAsString()));}}
                }
            }catch(Exception e){com.cranks.dungeons.CranksDungeons.LOGGER.warn("Unable to load guide directory",e);}
            staticEntries=List.copyOf(entries);
        }return staticEntries;
    }
    public static GuideDocument load(GuideTarget original){
        GuideTarget target=GuideTopics.resolve(original);String key=GuideTopics.base(target.key());
        try{return switch(target.kind()){
            case ARTICLE->key.startsWith("equipment_")?GuideEquipmentArticles.create(EquipmentType.valueOf(key.substring(10).toUpperCase(Locale.ROOT))):article(key);
            case STAT->GuideStatArticles.create(key);case RUNE->GuideRuneArticles.create(key);case ITEM->GuideItemArticles.create(key);
            case MOB->GuideCreatureArticles.create(key);case EFFECT->effect(key);case ENCHANTMENT->enchantment(key);case RECIPE->recipe(key);
        };}catch(Exception e){com.cranks.dungeons.CranksDungeons.LOGGER.warn("Unable to open guide reference {}",target,e);return new GuideDocument("Reference unavailable",List.of(p("This reference is not available in the current world or resource pack.")));}
    }
    private static GuideDocument article(String id){
        return ARTICLES.computeIfAbsent(id,key->{
            try{var resource=MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("cranks-dungeons","compendium/pages/"+key+".json")).orElseThrow();
                try(var reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)){
                    var json=GSON.fromJson(reader,JsonObject.class);var blocks=new ArrayList<GuideDocument.Block>();
                    for(var element:json.getAsJsonArray("content")){var b=element.getAsJsonObject();blocks.add(new GuideDocument.Block(b.get("type").getAsString(),b.has("value")?b.get("value").getAsString():"",b.has("style")?b.get("style").getAsString():""));}
                    return new GuideDocument(json.get("title").getAsString(),List.copyOf(blocks));
                }
            }catch(Exception e){return new GuideDocument("Reference unavailable",List.of(p("This article is not supplied by the active resources.")));}
        });
    }
    private static GuideDocument recipe(String id){
        if(id.startsWith("alchemy/")||id.startsWith("extraction/"))return alchemyRecipe(id);
        var client=MinecraftClient.getInstance();Identifier parsed=Identifier.tryParse(id);
        if(client.world==null||client.player==null||parsed==null)return hiddenRecipe();
        var entry=((RecipeManagerSnapshotAccessor)client.world.getRecipeManager()).cranksdungeons$recipeSnapshot().get(parsed);
        if(entry==null||!RecipeDiscovery.isDiscovered(entry.value(),ModComponents.ITEM_DISCOVERIES.get(client.player)))return hiddenRecipe();
        var output=entry.value().getResult(client.world.getRegistryManager());var blocks=new ArrayList<GuideDocument.Block>();
        blocks.add(p("Station: "+stationName(entry)));blocks.add(p("Output: "+ref(output.getName().getString(),GuideTarget.item(Registries.ITEM.getId(output.getItem()).toString()))+" x"+output.getCount()));
        blocks.add(h("Materials"));var discoveries=ModComponents.ITEM_DISCOVERIES.get(client.player);
        var groups=new LinkedHashMap<String,String>();var amounts=new LinkedHashMap<String,Integer>();
        for(var ingredient:entry.value().getIngredients()){
            if(ingredient.isEmpty())continue;var alternatives=new ArrayList<String>();
            for(var candidate:ingredient.getMatchingStacks())if(discoveries.contains(candidate.getItem())){
                alternatives.add(ref(candidate.getName().getString(),GuideTarget.item(Registries.ITEM.getId(candidate.getItem()).toString())));if(alternatives.size()==8)break;}
            if(!alternatives.isEmpty()){String group=String.join(" or ",alternatives);groups.putIfAbsent(group,group);amounts.merge(group,1,Integer::sum);}
        }
        for(var group:groups.keySet())blocks.add(p(amounts.get(group)+" x "+group));
        if(blocks.size()==3)blocks.add(p("This recipe uses station-specific choices. Select it at its station to choose the discovered materials."));
        return new GuideDocument(output.getName().getString()+" Recipe",List.copyOf(blocks));
    }
    private static GuideDocument hiddenRecipe(){return new GuideDocument("Recipe unavailable",List.of(p("This recipe is not available in your discovered recipes. Obtain its required components through play first.")));}
    private static GuideDocument alchemyRecipe(String id){
        var client=MinecraftClient.getInstance();if(client.player==null)return hiddenRecipe();
        List<com.cranks.dungeons.alchemy.AlchemyRecipes.Need> needs;ItemStack output;
        if(id.startsWith("extraction/")){
            var extraction=com.cranks.dungeons.alchemy.AlchemyRecipes.extractions().stream().filter(r->r.key().equals(id.substring(11))).findFirst().orElse(null);
            var known=ModComponents.ITEM_DISCOVERIES.get(client.player);
            if(extraction==null||!extraction.needs().stream().allMatch(need->need.alternatives().stream().anyMatch(known::contains)))return hiddenRecipe();
            needs=extraction.needs();output=extraction.output();
        }else{
            String[] parts=id.split("/",3);if(parts.length!=3||!ModComponents.ALCHEMY_KNOWLEDGE.get(client.player).knows(parts[1]))return hiddenRecipe();
            var formula=com.cranks.dungeons.alchemy.AlchemyPotions.get(parts[1]);if(formula==null)return hiddenRecipe();
            var delivery=com.cranks.dungeons.alchemy.AlchemyItems.Delivery.valueOf(parts[2].toUpperCase(Locale.ROOT));
            needs=com.cranks.dungeons.alchemy.AlchemyRecipes.needs(com.cranks.dungeons.alchemy.AlchemyRecipes.forFormula(formula,delivery));output=formula.stack(delivery);
        }
        var blocks=new ArrayList<GuideDocument.Block>();blocks.add(p(ref("Alchemy Stand",GuideTarget.article("alchemy"))));blocks.add(h("Materials"));
        for(var need:needs){var alternatives=new ArrayList<String>();for(var material:need.alternatives()){
            ItemStack example=need.water()?need.example():material.getDefaultStack();
            alternatives.add(ref(example.getName().getString(),GuideText.itemTarget(example)));if(need.water())break;}
            blocks.add(p(need.count()+" x "+String.join(" or ",alternatives)));}
        blocks.add(p("Output: "+ref(output.getName().getString(),GuideText.itemTarget(output))+" x"+output.getCount()));
        return new GuideDocument(output.getName().getString()+" Recipe",List.copyOf(blocks));
    }
    private static GuideDocument effect(String id){return GuideEffectArticles.create(id);}
    private static GuideDocument enchantment(String id){return GuideEnchantmentArticles.create(id);}
    public static String stationName(RecipeEntry<?> recipe){
        var value=recipe.value();if(value instanceof com.cranks.dungeons.inventory.StationAssemblyRecipe assembly)return switch(assembly.station()){case CUTTING_BENCH->"Cutting Bench";case ANVIL->"Anvil";case FLETCHING_TABLE->"Fletching Table";case TAILORING_BENCH->"Tailoring Bench";case CARTOGRAPHY_TABLE->"Cartography Table";};
        String type=Registries.RECIPE_TYPE.getId(value.getType()).getPath();return switch(type){case "crafting"->value instanceof net.minecraft.recipe.CraftingRecipe craft&&craft.fits(2,2)?"Hand Crafting / Crafting Table":"Crafting Table";case "smelting"->"Kiln";case "blasting"->"Foundry";case "smoking"->"Smoker";case "campfire_cooking"->"Campfire";case "stonecutting"->"Cutting Bench";default->type.replace('_',' ');};
    }
}
