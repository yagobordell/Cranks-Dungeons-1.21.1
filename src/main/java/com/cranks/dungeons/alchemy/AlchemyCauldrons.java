package com.cranks.dungeons.alchemy;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/** Cauldrons dispatch by registered item, so new bottle types need their own behaviors. */
final class AlchemyCauldrons {
    private AlchemyCauldrons() {}

    static void register() {
        for (boolean lesser : new boolean[]{true, false}) {
            for (var delivery : AlchemyItems.Delivery.values()) {
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(AlchemyItems.empty(lesser, delivery), AlchemyCauldrons::fillBottle);
            }
            var drinking = AlchemyItems.filled(lesser, AlchemyItems.Delivery.DRINK);
            CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.map().put(drinking, AlchemyCauldrons::pourWater);
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(drinking, AlchemyCauldrons::pourWater);
        }
    }

    private static ItemActionResult fillBottle(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        var item = stack.getItem();
        if (!(item instanceof AlchemyItems.Bottle) && item != Items.GLASS_BOTTLE) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        boolean lesser = item instanceof AlchemyItems.Bottle bottle && bottle.lesser;
        var delivery = item instanceof AlchemyItems.Bottle bottle ? bottle.delivery : AlchemyItems.Delivery.DRINK;
        if (!world.isClient()) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, AlchemyItems.water(lesser, delivery)));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1, 1);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ItemActionResult.success(world.isClient());
    }

    private static ItemActionResult pourWater(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        var contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        if (!(stack.getItem() instanceof AlchemyItems.Brew bottle) || !contents.matches(Potions.WATER)
                || !contents.customEffects().isEmpty() || state.isOf(Blocks.WATER_CAULDRON) && state.get(LeveledCauldronBlock.LEVEL) == 3)
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!world.isClient()) {
            var next = state.isOf(Blocks.CAULDRON) ? Blocks.WATER_CAULDRON.getDefaultState() : state.cycle(LeveledCauldronBlock.LEVEL);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, AlchemyItems.empty(bottle.lesser, bottle.delivery).getDefaultStack()));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(bottle));
            world.setBlockState(pos, next);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ItemActionResult.success(world.isClient());
    }
}
