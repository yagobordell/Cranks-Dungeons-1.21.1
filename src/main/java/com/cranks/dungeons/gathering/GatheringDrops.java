package com.cranks.dungeons.gathering;

import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.CropHarvest;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;
import java.util.function.Consumer;

/** Modifies one native loot result. Never rerolls a table or duplicates a container's contents. */
public final class GatheringDrops {
    private GatheringDrops() {}
    public static double attr(PlayerEntity player,RegistryEntry<EntityAttribute> attribute){return player.getAttributeValue(attribute);}
    public static void emit(ItemStack stack,double bonus,net.minecraft.util.math.random.Random random,Consumer<ItemStack> output){
        if(stack.isEmpty())return;
        output.accept(stack);
        if(stack.getMaxCount()<=1||bonus<=0)return;
        int extra=GatheringRules.extra(stack.getCount(),bonus,random.nextDouble());
        while(extra>0){int count=Math.min(extra,stack.getMaxCount());output.accept(stack.copyWithCount(count));extra-=count;}
    }
    public static List<ItemStack> block(List<ItemStack> original,BlockState state,ServerWorld world,BlockPos pos,Entity entity,ItemStack tool){
        if(!(entity instanceof ServerPlayerEntity player)||!player.isAlive()||player.isCreative()||player.isSpectator()||original.isEmpty())return original;
        boolean ore=state.isIn(ModBlockTags.ORE_FORTUNE_ORES),logs=state.isIn(BlockTags.LOGS);
        boolean placed=GatheringBreakContext.placed(world,pos);
        var result=new ArrayList<ItemStack>();
        for(var stack:original){
            double bonus=0;
            // A silk-touched block never yields extra blocks. Grown logs are renewable; placed logs are not.
            if(!placed&&logs&&tool.isIn(ItemTags.AXES)&&stack.isOf(state.getBlock().asItem()))
                bonus=attr(player,ModAttributes.LOG_YIELD)+attr(player,ModAttributes.LOG_FORTUNE);
            else if(!placed&&ore&&tool.isSuitableFor(state)&&!stack.isOf(state.getBlock().asItem())){
                bonus=attr(player,ModAttributes.ORE_YIELD)+attr(player,ModAttributes.FORTUNE);
                bonus+=attr(player,world.getRegistryKey()==World.NETHER?ModAttributes.ORE_YIELD_IN_NETHER:ModAttributes.ORE_YIELD_IN_OVERWORLD)
                        *(world.getRegistryKey()==World.NETHER||world.getRegistryKey()==World.OVERWORLD?1:0);
                if(state.isIn(BlockTags.DIAMOND_ORES)||state.isIn(BlockTags.EMERALD_ORES)||state.isIn(BlockTags.LAPIS_ORES))bonus+=attr(player,ModAttributes.GEM_YIELD);
            }else if(!placed&&state.isOf(Blocks.GRAVEL)&&stack.isOf(Items.FLINT))
                bonus=attr(player,ModAttributes.FLINT_YIELD)+attr(player,ModAttributes.DIG_YIELD)+attr(player,ModAttributes.DIG_FORTUNE);
            else if(!placed&&state.isOf(Blocks.CLAY)&&stack.isOf(Items.CLAY_BALL))bonus=attr(player,ModAttributes.DIG_YIELD)+attr(player,ModAttributes.DIG_FORTUNE);
            else if(!placed&&state.isIn(BlockTags.LEAVES)&&(stack.isIn(ItemTags.SAPLINGS)||stack.isOf(Items.APPLE)))bonus=attr(player,ModAttributes.SAPLING_YIELD);
            else if(CropHarvest.isMature(state)&&tool.isIn(ItemTags.HOES)){
                boolean seed=stack.isOf(Items.WHEAT_SEEDS)||stack.isOf(Items.BEETROOT_SEEDS)||stack.isOf(Items.MELON_SEEDS)||stack.isOf(Items.PUMPKIN_SEEDS);
                bonus=attr(player,seed?ModAttributes.SEED_YIELD:ModAttributes.CROP_YIELD)+attr(player,ModAttributes.CROP_FORTUNE);
            }
            emit(stack,bonus,world.random,result::add);
        }
        return result;
    }
    public static void replant(ServerPlayerEntity player,ServerWorld world,BlockPos pos,BlockState oldState,ItemStack tool){
        if(com.cranks.dungeons.economy.TradeManager.active(player)||player.isSneaking()||player.isCreative()||player.isSpectator()||!player.isAlive()||!CropHarvest.isMature(oldState)
                ||!tool.isIn(ItemTags.HOES)||!world.getBlockState(pos).isAir()
                ||!world.canPlayerModifyAt(player,pos)||player.getRandom().nextDouble()>=attr(player,ModAttributes.CROP_REPLANT_CHANCE))return;
        BlockState fresh=oldState.getBlock() instanceof CropBlock crop?crop.withAge(0):oldState.getBlock() instanceof CocoaBlock
                ?oldState.with(CocoaBlock.AGE,0):oldState.with(NetherWartBlock.AGE,0);
        if(!fresh.canPlaceAt(world,pos))return;
        var seed=oldState.getBlock().getPickStack(world,pos,oldState);
        var inventory=player.getInventory();
        for(int slot=0;slot<PlayerLoadout.EQUIPPED_BAGS;slot++){
            if(slot>=36&&slot!=PlayerLoadout.EXTRA_HOTBAR&&slot<PlayerLoadout.BAG_STORAGE)continue;
            if(slot>=PlayerLoadout.BAG_STORAGE){int offset=slot-PlayerLoadout.BAG_STORAGE;if(offset%PlayerLoadout.BAG_WIDTH>=PlayerLoadout.of(inventory).capacity(offset/PlayerLoadout.BAG_WIDTH))continue;}
            var stack=inventory.getStack(slot);
            if(!stack.isEmpty()&&ItemStack.areItemsAndComponentsEqual(stack,seed)){
                if(world.setBlockState(pos,fresh,Block.NOTIFY_ALL)){stack.decrement(1);inventory.markDirty();}
                return;
            }
        }
    }
}
