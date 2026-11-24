package com.cranks.dungeons.client.guide;
import com.cranks.dungeons.alchemy.*;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.effect.EffectDescriptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import java.util.*;
import java.util.stream.Collectors;

/** One potion family with tier/delivery comparisons; formula links remain personal. */
public final class GuidePotionArticles {
    private GuidePotionArticles(){}
    public static GuideDocument create(ItemStack stack){
        var contents=stack.get(DataComponentTypes.POTION_CONTENTS);if(contents==null||stack.getItem() instanceof ArrowItem)return null;
        var selected=AlchemyPotions.FORMULAS.stream().filter(f->contents.matches(f.potion())).findFirst().orElse(null);if(selected==null)return null;
        var formulas=AlchemyPotions.FORMULAS.stream().filter(f->f.family().equals(selected.family())).sorted(Comparator.comparing(AlchemyPotions.Formula::lesser).reversed()).toList();
        var regular=formulas.stream().filter(f->!f.lesser()).findFirst().orElse(selected);String title=regular.stack(AlchemyItems.Delivery.DRINK).getName().getString();
        var blocks=new ArrayList<GuideDocument.Block>();blocks.add(new GuideDocument.Block("item_image",GuideText.itemTarget(regular.stack(AlchemyItems.Delivery.DRINK)).key()));
        blocks.add(new GuideDocument.Block("text",title+" is an alchemical consumable. It is available in Lesser and regular tiers, in drinking, throwable and lingering bottles. Matching potions stack to 16."));
        for(var formula:formulas){
            blocks.add(new GuideDocument.Block("heading",formula.lesser()?"Lesser":"Regular","anchor="+(formula.lesser()?"lesser":"regular")));
            var rows=new ArrayList<List<String>>();for(var delivery:AlchemyItems.Delivery.values()){
                var descriptions=new ArrayList<String>();for(var effect:formula.potion().value().getEffects())descriptions.add(EffectDescriptions.tooltip(effect,delivery==AlchemyItems.Delivery.LINGERING?.25f:1,20,delivery==AlchemyItems.Delivery.LINGERING?.5f:1).stream().map(net.minecraft.text.Text::getString).collect(Collectors.joining("; ")));
                rows.add(List.of(switch(delivery){case DRINK->"Drinking";case SPLASH->"Throwable";case LINGERING->"Lingering";},String.join("; ",descriptions)));
            }blocks.add(GuideTables.table(List.of("Delivery","Effect and duration"),rows,1,3));
        }
        blocks.add(new GuideDocument.Block("heading","Obtaining"));blocks.add(new GuideDocument.Block("text","Prepared at the "+GuideText.reference("Alchemy Stand",GuideTarget.article("alchemy"))+". A successful experiment records that tier's formula in your personal recipe list."));
        var client=MinecraftClient.getInstance();var recipes=new ArrayList<List<String>>();if(client.player!=null)for(var formula:formulas)if(ModComponents.ALCHEMY_KNOWLEDGE.get(client.player).knows(formula.key()))for(var delivery:AlchemyItems.Delivery.values())recipes.add(List.of(formula.lesser()?"Lesser":"Regular",GuideText.reference(formula.stack(delivery).getName().getString(),new GuideTarget(GuideTarget.Kind.RECIPE,"alchemy/"+formula.key()+"/"+delivery.name()))));
        if(!recipes.isEmpty()){blocks.add(new GuideDocument.Block("heading","Learned recipes"));blocks.add(GuideTables.table(List.of("Tier","Recipe"),recipes,1,3));}
        blocks.add(new GuideDocument.Block("heading","Usage"));blocks.add(new GuideDocument.Block("text","Drinking applies the effect to the user and returns the matching empty bottle. Throwable bottles break on impact and their effective duration depends on distance from the splash. Lingering bottles leave an effect cloud and are consumed."));
        return new GuideDocument(title,List.copyOf(blocks));
    }
}
