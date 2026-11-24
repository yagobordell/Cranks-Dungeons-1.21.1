package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.alchemy.*;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.effect.EffectDescriptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;

public final class GuideEffectArticles {
    private GuideEffectArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    public static GuideDocument create(String id){
        id=GuideTopics.base(GuideTopics.resolve(new GuideTarget(GuideTarget.Kind.EFFECT,id)).key());
        final String topicId=id;
        var effect=Registries.STATUS_EFFECT.getEntry(Identifier.of(id)).orElseThrow();var blocks=new ArrayList<GuideDocument.Block>();
        blocks.add(h("What it does"));var base=EffectDescriptions.descriptions(new StatusEffectInstance(effect,200,0));for(var line:base)blocks.add(p(line.getString()));
        var deliveryEffect=topicId.equals("cranks-dungeons:poison")?net.minecraft.entity.effect.StatusEffects.POISON:effect;
        if(topicId.equals("cranks-dungeons:poison")){
            blocks.add(h("Combat application"));
            blocks.add(p("Physical or Void hits can apply Poison through Chance to Poison; critical hits guarantee it when that chance is positive. Each stack deals a base 20% of the hit's offensive Physical and Void damage per second for 2 seconds. Poison Magnitude increases its damage. The cap is 20 plus Poison Maximum Stacks; stronger new stacks can replace the weakest."));
            blocks.add(h("Potions and coated arrows"));
            blocks.add(p("These sources apply the same Poison ailment with fixed damage instead of scaling from a weapon hit. Each applied stack deals 1 Void damage every 0.5 seconds before Void Resistance. Stacks retain independent timers and bypass Energy Shield."));
            blocks.add(p("Lesser potions apply 1 stack; regular potions and Poison Arrows apply 2. Each source specifies its duration."));
        }
        boolean higher=AlchemyPotions.FORMULAS.stream().flatMap(formula->formula.potion().value().getEffects().stream()).anyMatch(instance->sameTopic(instance,topicId)&&instance.getAmplifier()>0);
        if(higher&&!topicId.equals("cranks-dungeons:poison")){var next=EffectDescriptions.descriptions(new StatusEffectInstance(deliveryEffect,200,1));
            if(!next.stream().map(net.minecraft.text.Text::getString).toList().equals(base.stream().map(net.minecraft.text.Text::getString).toList())){
                blocks.add(h("Higher potency"));for(var line:next)blocks.add(p(line.getString()));
            }}
        blocks.add(h("Duration and delivery"));blocks.add(p("Duration comes from the source applying the effect. A potion article shows that bottle's duration; an active-effect tooltip shows how much remains. Splash distance and lingering delivery can change the result, so do not assume every source lasts as long as a drinking potion."));
        var client=MinecraftClient.getInstance();var formulas=AlchemyPotions.FORMULAS.stream().filter(formula->formula.potion().value().getEffects().stream().anyMatch(instance->sameTopic(instance,topicId))).toList();
        if(!formulas.isEmpty()){
            blocks.add(h("Potion sources"));blocks.add(p("This effect can be prepared through the "+GuideText.reference("Alchemy Stand",GuideTarget.article("alchemy"))+". Successful experiments teach your personal formulas."));
            if(client.player!=null)for(var formula:formulas)if(ModComponents.ALCHEMY_KNOWLEDGE.get(client.player).knows(formula.key())){
                var stack=formula.stack(AlchemyItems.Delivery.DRINK);blocks.add(p(GuideText.reference(stack.getName().getString(),GuideText.itemTarget(stack))));}
        }
        var creatures=switch(Identifier.of(id).getPath()){
            case "poison"->List.of("cave_spider","bogged");case "slowness"->List.of("stray");case "darkness"->List.of("warden");
            case "mining_fatigue"->List.of("elder_guardian");case "hunger"->List.of("husk");case "wither"->List.of("wither_skeleton","wither");case "levitation"->List.of("shulker");default->List.<String>of();};
        if(!creatures.isEmpty()){blocks.add(h("Creature encounters"));blocks.add(p(creatures.stream().map(name->GuideCreatureArticles.reference("minecraft:"+name)).collect(java.util.stream.Collectors.joining(", "))));}
        blocks.add(h("Related articles"));blocks.add(p(GuideText.reference("Food and active effects",GuideTarget.article("food_effects"))+" | "+GuideText.reference("Ailments",GuideTarget.article("ailments"))+" | "+GuideText.reference("Recovery",GuideTarget.article("recovery"))));
        return new GuideDocument(GuideTopics.effectName(id)+" (effect)",List.copyOf(blocks));
    }
    private static boolean sameTopic(StatusEffectInstance instance,String id){
        return GuideTopics.resolve(new GuideTarget(GuideTarget.Kind.EFFECT,instance.getEffectType().getIdAsString())).key().equals(id);
    }
}
