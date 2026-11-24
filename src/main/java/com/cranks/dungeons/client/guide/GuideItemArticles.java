package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.alchemy.*;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.inventory.*;
import com.cranks.dungeons.item.*;
import com.cranks.dungeons.mixin.client.RecipeManagerSnapshotAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import java.util.*;
import java.util.stream.Collectors;

/** Practical item roles and acquisition routes, not a wall of unlocked recipes. */
public final class GuideItemArticles {
    private GuideItemArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    private static String ref(String label,GuideTarget target){return GuideText.reference(label,target);}
    public static GuideDocument create(String key){
        var stack=GuideIllustrations.stack(key);if(stack.isEmpty())return new GuideDocument("Item unavailable",List.of());
        if(stack.getItem() instanceof AbstractTomeItem tome)return GuideTomeArticles.create(tome);
        var potionArticle=GuidePotionArticles.create(stack);if(potionArticle!=null)return potionArticle;
        var item=stack.getItem();String id=Registries.ITEM.getId(item).toString(),path=Registries.ITEM.getId(item).getPath();
        var client=MinecraftClient.getInstance();var blocks=new ArrayList<GuideDocument.Block>();
        blocks.add(new GuideDocument.Block("item_image",key));
        var facts=GuideKnowledge.entry("items",id);var related=new LinkedHashSet<String>();
        if(item instanceof RunicTomeItem tome){
            blocks.add(p(runic(tome)));blocks.add(h("How to obtain"));blocks.add(p("Earn Runic Tomes from eligible hostile-creature combat and generated treasure. More dangerous creatures and structures use richer reward pools. Search Mineshaft chest carts for an early Transmutation tome."));
            blocks.add(h("Choosing a base"));blocks.add(p("The equipment's material caps the strongest rune tier. A stronger tome can improve the chance of the better available tiers; it cannot turn an early base into a late-game base."));
            related.add(ref("Tome operations",GuideTarget.article("tomes")));related.add(ref("Tier chances",GuideTarget.article("rune_tiers")));related.add(ref("Runic Altar",GuideTarget.article("runic_altar")));
        }else if(item instanceof ArtificialTomeItem){
            blocks.add(p("Reserves an artificial rune on Rare equipment so you can choose between revealed results at a second altar."));blocks.add(h("How to obtain"));blocks.add(p("A rare reward from the mod's richer creature and treasure pools."));
            blocks.add(h("How to use"));blocks.add(p("Use it first at the "+ref("Runic Altar",GuideTarget.article("runic_altar"))+", then reveal the marked equipment at the "+ref("Artificial Runic Altar",GuideTarget.article("artificial_tome"))+". One tome pays for the reservation; revealing does not require a second tome."));related.add(ref("Artificial rune choices",GuideTarget.article("artificial_tome")));
        }else{
            String summary=GuideKnowledge.text(facts,"summary");
            if(!summary.isBlank())blocks.add(p(summary));
            var equipment=EquipmentType.getTypeForItem(stack);
            if(equipment.isPresent())equipment(blocks,related,stack,equipment.get());
            var contents=stack.get(DataComponentTypes.POTION_CONTENTS);
            if(contents!=null){
                blocks.add(h(item instanceof ArrowItem?"Effect on hit":"Effects"));
                var arrowKind=FletchingArrows.kind(stack);
                if(arrowKind==FletchingArrows.Kind.VOID)blocks.add(p("Converts 30% of this projectile's Physical hit into Void before the target's defences. This is not an Instant Damage potion and does not add a second hit."));
                for(var original:contents.getEffects()){
                    var effect=arrowKind==null?original:new StatusEffectInstance(original.getEffectType(),Math.max(1,original.getDuration()/8),original.getAmplifier());
                    String effectId=Registries.STATUS_EFFECT.getId(effect.getEffectType().value()).toString();
                    blocks.add(p(ref(effect.getEffectType().value().getName().getString(),new GuideTarget(GuideTarget.Kind.EFFECT,effectId))));
                    boolean lingering=item instanceof LingeringPotionItem||item instanceof AlchemyItems.Brew brew&&brew.delivery==AlchemyItems.Delivery.LINGERING;
                    var details=com.cranks.dungeons.effect.EffectDescriptions.tooltip(effect,lingering?0.25f:1f,20,lingering?0.5f:1f);
                    for(int line=1;line<details.size();line++)blocks.add(p(details.get(line).getString()));
                }
                if(contents.potion().isPresent()&&contents.matches(net.minecraft.potion.Potions.WATER))blocks.add(p("Water-filled bottle. It is ready for mixing at the Alchemy Stand."));
                else if(item instanceof ArrowItem)blocks.add(p("A coated arrow delivers its supported effect to the target it hits. Craft coated ammunition at the Fletching Table using prepared alchemical components."));
                else blocks.add(p("Drinking applies the effects to you and returns the matching bottle. Throwable bottles break on use; lingering bottles leave a temporary effect cloud. Matching potions stack to 16."));
                related.add(ref("Alchemy and bottles",GuideTarget.article("alchemy")));related.add(ref("Effects",GuideTarget.article("food_effects")));
                learnedPotion(blocks,stack);
            }
            if(item instanceof AlchemyItems.Bottle bottle){
                blocks.add(p("An empty "+(bottle.lesser?"Lesser":"regular")+" "+bottle.delivery.name().toLowerCase(Locale.ROOT)+" bottle. Fill it from a water source or water cauldron, then choose it for an experiment at the Alchemy Stand."));
                related.add(ref("Bottles and brewing",GuideTarget.article("alchemy")));
            }
            var food=stack.get(DataComponentTypes.FOOD);
            if(food!=null){blocks.add(h("Eating it"));blocks.add(p("Restores "+food.nutrition()+" Hunger. Food supports Well Fed and helps avoid Starving; Hunger does not directly regenerate Health in this mod."));related.add(ref("Food and recovery",GuideTarget.article("food_effects")));
                for(var benefit:food.effects()){
                    var effect=benefit.effect();blocks.add(p(com.cranks.dungeons.stat.passive.NodeBonusFormat.number(benefit.probability()*100)+"% chance: "+ref(effect.getEffectType().value().getName().getString(),new GuideTarget(GuideTarget.Kind.EFFECT,effect.getEffectType().getIdAsString()))));
                    var details=com.cranks.dungeons.effect.EffectDescriptions.tooltip(effect,1,20,1);for(int line=1;line<details.size();line++)blocks.add(p(details.get(line).getString()));
                }
            }
            String obtaining=GuideKnowledge.text(facts,"obtaining"),uses=GuideKnowledge.text(facts,"uses");
            if(!obtaining.isBlank()){blocks.add(h("How to obtain"));blocks.add(p(obtaining));}
            if(!uses.isBlank()){blocks.add(h("What it is used for"));blocks.add(p(uses));}
            var station=GuideReferences.station(stack);if(station!=null){
                blocks.add(h("Station use"));blocks.add(p(ref("Open the station's instructions",station)));related.add(ref("Station instructions",station));
            }
            boolean prepared=false;
            for(var extraction:AlchemyRecipes.extractions())if(extraction.output().isOf(item)){
                if(!prepared){blocks.add(h("Alchemical preparation"));prepared=true;}
                blocks.add(p("Prepare it at the "+ref("Alchemy Stand",GuideTarget.article("alchemy"))+" from "+extraction.needs().stream().map(need->need.alternatives().stream().map(material->ref(material.getName().getString(),GuideText.itemTarget(material.getDefaultStack()))).collect(Collectors.joining(" or "))).collect(Collectors.joining(" and "))+"."));
                String hint="alchemy.cranks-dungeons.hint."+path;var text=net.minecraft.text.Text.translatable(hint);if(!text.getString().equals(hint))blocks.add(p(text.getString()));
                blocks.add(p("Use prepared components in Experiment to learn a mixture, then repeat a successful formula from your personal recipe list."));related.add(ref("Experiments and formula knowledge",GuideTarget.article("alchemy")));
            }
            var creatures=GuideKnowledge.creaturesFor(id);
            if(!creatures.isEmpty()){blocks.add(h("Creature sources"));blocks.add(p(creatures.stream().map(GuideCreatureArticles::reference).collect(Collectors.joining(", "))));}
            if(summary.isBlank()&&equipment.isEmpty()&&contents==null&&!prepared){
                if(item instanceof BlockItem&&station==null&&food==null&&isBuildingMaterial(path)){blocks.add(h("Building use"));blocks.add(p(building(path)));related.add(ref("Cutting Bench",GuideTarget.article("cutting_bench")));}
                if(item instanceof SpawnEggItem){String creature="minecraft:"+path.replace("_spawn_egg","");if(GuideKnowledge.catalogue("creatures").has(creature))blocks.add(p("Creates a "+GuideCreatureArticles.reference(creature)+" when used. This is a Creative utility rather than an ordinary survival drop."));}
                sources(blocks,item);
                uses(blocks,item);
            }
        }
        if(!related.isEmpty()){blocks.add(h("Related articles"));blocks.add(p(String.join(" | ",related)));}
        return new GuideDocument(stack.getName().getString(),List.copyOf(blocks));
    }
    private static void equipment(List<GuideDocument.Block> blocks,Set<String> related,ItemStack stack,EquipmentType type){
        boolean tool=stack.getItem() instanceof MiningToolItem;
        blocks.add(h("Equipment role"));blocks.add(p(tool?"A gathering tool. Use the tool suited to the block and its required material tier; combat weapon improvements do not replace harvest requirements.":switch(type){
            case SWORD->"A one-handed melee weapon. It can support a Shield or a matching one-handed dual-wield setup.";
            case SPEAR->"A two-handed melee weapon.";
            case BOW->"A draw-based ranged weapon. Hold Use to draw, press Attack to fire, and release Use to cancel. A held draw can begin the next arrow after firing.";
            case CROSSBOW->"A ranged weapon that stores a loaded shot. Hold Use to finish loading, then press Attack to fire. Release after loading and hold Use again to aim with zoom.";
            case SHIELD->"An active frontal defence. An eligible block subtracts Block Value, up to 85% of the incoming hit, before Armour and resistances. It does not protect against every attack direction.";
            case TRIDENT->"A fixed Unique weapon usable in melee or as a thrown projectile. Thrown attacks preserve the item's compatible statistics with the throw's damage scaling; its returning property supports repeated use.";
            case MACE->"A fixed Unique melee weapon that rewards suitable falling attacks.";
            case ELYTRA->"A fixed Unique chest-slot item for gliding. Its Energy Shield and flight properties offer a different travel and defensive plan from an ordinary chestplate.";
            default->"Equipment for the "+type.getName()+" slot. Its native defensive affinity determines which compatible rune families it can accept.";
        }));
        if(RunePool.supports(stack)){
            blocks.add(p("Strongest rune tier on this base: T"+RuneTiers.best(stack)+". Rarity controls how many affixes it can hold; upgrading rarity does not raise this material cap."));
            String catalogue="rune_"+type.name().toLowerCase(Locale.ROOT);related.add(ref("Compatible rune families",GuideTarget.article(catalogue)));
        }else if(CustomRarity.getRarity(stack)==CustomRarity.UNIQUE)blocks.add(p("A fixed Unique base. Its special properties cannot be rewritten with Runic Tomes."));
        else blocks.add(p("This equipment does not accept ordinary rune affixes."));
        blocks.add(p("Maximum enchantment slots: "+EnchantmentSlotHelper.getMaxEnchantmentSlots(stack)+". Use the "+ref("Upgrade Bench",GuideTarget.article("quality"))+" for supported quality and slot improvements, the "+ref("Enchanting Table",GuideTarget.article("enchantments"))+" for compatible enchantments, and the "+ref("Repair Bench",GuideTarget.article("repairing"))+" for durability."));
        var fixed=GuideKnowledge.catalogue("fixed_runes").getAsJsonArray(Registries.ITEM.getId(stack.getItem()).toString());
        if(fixed!=null){blocks.add(h("Fixed rune properties"));for(var row:fixed){var value=row.getAsJsonObject();var rune=com.cranks.dungeons.stat.StatRegistry.getStat(GuideKnowledge.text(value,"rune"));
            if(rune!=null)blocks.add(p(ref(GuideTomes.name(rune),GuideTarget.rune(rune.getId()))));}related.add(ref("Unique properties",GuideTarget.article("unique_equipment")));}
        sources(blocks,stack.getItem());related.add(ref("Requirements",GuideTarget.article("rarity_requirements")));related.add(ref("Equipment bases",GuideTarget.article("equipment")));
    }
    private static void sources(List<GuideDocument.Block> blocks,Item item){
        var client=MinecraftClient.getInstance();if(client.world==null)return;
        var stations=((RecipeManagerSnapshotAccessor)client.world.getRecipeManager()).cranksdungeons$recipeSnapshot().values().stream()
                .filter(recipe->recipe.value().getResult(client.world.getRegistryManager()).isOf(item)).map(GuideBook::stationName).distinct().sorted().toList();
        if(!stations.isEmpty()){blocks.add(h("Production"));blocks.add(p("Produced at: "+String.join(", ",stations)+". "));}
    }
    private static void uses(List<GuideDocument.Block> blocks,Item item){
        var client=MinecraftClient.getInstance();if(client.world==null)return;var roles=new LinkedHashSet<String>();
        for(var recipe:((RecipeManagerSnapshotAccessor)client.world.getRecipeManager()).cranksdungeons$recipeSnapshot().values()){
            if(!RecipeDiscovery.materialTest(recipe.value()).test(item))continue;
            var output=recipe.value().getResult(client.world.getRegistryManager());if(output.isEmpty())continue;
            var type=EquipmentType.getTypeForItem(output);
            if(type.isPresent())roles.add(type.get().getName()+" equipment");
            else if(output.getItem() instanceof BlockItem)roles.add("construction and station assembly");
            else if(output.contains(DataComponentTypes.FOOD))roles.add("food preparation");
            else if(output.getItem() instanceof ArrowItem)roles.add("ammunition");
            else roles.add("utility components");
        }
        if(!roles.isEmpty()){blocks.add(h("Material role"));blocks.add(p("Used in "+String.join(", ",roles)+" recipes."));}
    }
    private static boolean isBuildingMaterial(String path){
        return path.matches(".*_(planks|log|wood|stem|hyphae|ore|sapling|propagule|stairs|slab|wall|bricks|tiles|concrete|terracotta|glass|wool)$")
                ||Set.of("stone","cobblestone","deepslate","cobbled_deepslate","granite","diorite","andesite","tuff","calcite","dirt","sand","gravel","clay","glass","obsidian","netherrack","end_stone","quartz_block").contains(path);
    }
    private static String building(String path){
        if(path.endsWith("_log")||path.endsWith("_wood")||path.endsWith("_stem")||path.endsWith("_hyphae"))return "Raw wood from the matching tree or fungus. It can supply Planks for equipment, stations and building parts, or be used directly in construction. Grown trees are a renewable source.";
        if(path.endsWith("_planks"))return "A processed wood material for assembly and construction. Use Planks for ordinary utility crafts, or take them to the Cutting Bench for the matching supported stairs, slabs, doors and other building parts.";
        if(path.endsWith("_sapling")||path.endsWith("_propagule"))return "Planting material for the matching tree. Give it suitable ground and growing space to establish a renewable wood supply.";
        if(path.endsWith("_ore"))return "An ore block. Use a suitable pickaxe to obtain its resource; Silk Touch preserves the block instead. The resource's article explains its processing and uses.";
        return "A placeable construction block. Supported shaped parts and building conversions belong at the Cutting Bench; use the material filter to keep related building recipes together.";
    }
    private static void learnedPotion(List<GuideDocument.Block> blocks,ItemStack stack){
        var client=MinecraftClient.getInstance();if(client.player==null)return;var contents=stack.get(DataComponentTypes.POTION_CONTENTS);if(contents==null)return;
        for(var formula:AlchemyPotions.FORMULAS)if(contents.matches(formula.potion())&&ModComponents.ALCHEMY_KNOWLEDGE.get(client.player).knows(formula.key())){
            var delivery=stack.getItem() instanceof AlchemyItems.Brew brew?brew.delivery:stack.getItem() instanceof SplashPotionItem?AlchemyItems.Delivery.SPLASH:stack.getItem() instanceof LingeringPotionItem?AlchemyItems.Delivery.LINGERING:AlchemyItems.Delivery.DRINK;
            blocks.add(p(ref("Your learned formula",new GuideTarget(GuideTarget.Kind.RECIPE,"alchemy/"+formula.key()+"/"+delivery.name()))));}
    }
    private static String runic(RunicTomeItem tome){return switch(tome.getTomeType()){
        case TRANSMUTATION,GREATER_TRANSMUTATION,PERFECT_TRANSMUTATION->"Turns Normal equipment into Magic equipment and adds one random compatible rune.";
        case AUGMENTATION,GREATER_AUGMENTATION,PERFECT_AUGMENTATION->"Adds a compatible rune to Magic equipment when an eligible affix slot is open.";
        case REGAL,GREATER_REGAL,PERFECT_REGAL->"Turns Magic equipment into Rare equipment and adds one compatible rune.";
        case EXALTED,GREATER_EXALTED,PERFECT_EXALTED->"Adds one compatible rune to Rare equipment without replacing the other runes.";
        case CHAOS,GREATER_CHAOS,PERFECT_CHAOS->"Replaces a selected rune with a random compatible rune on the same affix side. The paid comparison lets you keep the old result if the proposal is worse.";
        case ALCHEMY->"Reforges editable runes into a Rare item with up to two prefixes and two suffixes. Existing protected properties are retained.";
        case DIVINE->"Rerolls the selected rune's numeric values within its tier. Choose it when you like the rune family but want a better roll.";
        case ANNULMENT->"Removes a selected editable rune, freeing its affix slot for another attempt.";
    };}
}
