package com.cranks.dungeons.gathering;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.*;
import net.minecraft.world.World;
import java.util.function.Consumer;

public final class GatheringLoot {
    private GatheringLoot() {}
    public static net.minecraft.loot.LootChoice weighted(net.minecraft.loot.LootChoice choice,double bonus,LootContext context){
        // Keep fractional low weights useful, with one stable rounding decision for the entire selection.
        double roll=context.getRandom().nextDouble();
        return new net.minecraft.loot.LootChoice(){
            public int getWeight(float luck){return GatheringRules.weight(choice.getWeight(luck),bonus,roll);}
            public void generateLoot(Consumer<ItemStack> output,LootContext context){choice.generateLoot(output,context);}
        };
    }
    public static PlayerEntity player(LootContext context){
        Object actor=context.hasParameter(LootContextParameters.ATTACKING_ENTITY)?context.get(LootContextParameters.ATTACKING_ENTITY):
                context.hasParameter(LootContextParameters.THIS_ENTITY)?context.get(LootContextParameters.THIS_ENTITY):null;
        if(actor instanceof net.minecraft.entity.projectile.FishingBobberEntity bobber)actor=bobber.getPlayerOwner();
        return actor instanceof PlayerEntity player&&!player.isCreative()&&!player.isSpectator()&&player.isAlive()?player:null;
    }
    public static double rarity(PlayerEntity player,World world){
        return player.getAttributeValue(ModAttributes.ITEM_RARITY_PERCENT)+player.getAttributeValue(world.getRegistryKey()==World.NETHER
                ?ModAttributes.ITEM_RARITY_IN_NETHER:world.getRegistryKey()==World.END?ModAttributes.ITEM_RARITY_IN_END:ModAttributes.ITEM_RARITY_IN_OVERWORLD);
    }
    public static Consumer<ItemStack> output(LootContext context,Consumer<ItemStack> original){
        var player=player(context);
        var victim=context.hasParameter(LootContextParameters.THIS_ENTITY)?context.get(LootContextParameters.THIS_ENTITY):null;
        if(player==null||!(victim instanceof LivingEntity)||victim==player)return original;
        boolean monster=victim instanceof Monster,animal=victim instanceof AnimalEntity||victim instanceof FishEntity||victim instanceof SquidEntity;
        return stack->{
            double bonus=0;
            if(!stack.isIn(GatheringTags.RARE_LOOT)){
                if(monster&&stack.isIn(GatheringTags.MONSTER_DROPS))bonus+=player.getAttributeValue(ModAttributes.MONSTER_DROP_YIELD);
                if(animal&&stack.isIn(GatheringTags.ANIMAL_DROPS))bonus+=player.getAttributeValue(ModAttributes.ANIMAL_DROP_YIELD);
                if(animal&&stack.isIn(GatheringTags.MEAT))bonus+=player.getAttributeValue(ModAttributes.MEAT_YIELD);
            }
            GatheringDrops.emit(stack,bonus,context.getRandom(),original);
        };
    }
    public static double findBonus(LootContext context,boolean rare,int quality){
        var player=player(context);if(player==null)return 0;
        double bonus=rarity(player,context.getWorld())*Math.clamp(quality/8.0,0,1);
        var victim=context.hasParameter(LootContextParameters.THIS_ENTITY)?context.get(LootContextParameters.THIS_ENTITY):null;
        if(rare&&victim instanceof Monster){
            bonus+=player.getAttributeValue(ModAttributes.RARE_MOB_DROP_CHANCE);
            if(context.getWorld().getRegistryKey()==World.NETHER)bonus+=player.getAttributeValue(ModAttributes.RARE_MOB_DROP_CHANCE_IN_NETHER);
        }
        return bonus;
    }
}
