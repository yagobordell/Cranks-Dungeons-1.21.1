package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public class WorldTreeBlockDropsMixin {

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void modifyBlockDrops(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.block.entity.BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(entity instanceof PlayerEntity player)) return;
        
        List<ItemStack> originalDrops = cir.getReturnValue();
        if (originalDrops == null || originalDrops.isEmpty()) return;

        List<ItemStack> modifiedDrops = new ArrayList<>();
        boolean isNether = world.getRegistryKey() == World.NETHER;
        boolean isOverworld = world.getRegistryKey() == World.OVERWORLD;

        for (ItemStack stack : originalDrops) {
            ItemStack newStack = stack.copy();

            // 1. Woodcutting
            if (state.isIn(BlockTags.LOGS)) {
                double yield = getAttr(player, ModAttributes.LOG_YIELD);
                applyYield(newStack, yield, world.random);
            } else if (state.isIn(BlockTags.LEAVES) && (newStack.isOf(Items.APPLE) || isSapling(newStack.getItem()))) {
                double yield = getAttr(player, ModAttributes.SAPLING_YIELD);
                applyYield(newStack, yield, world.random);
            }

            // 2. Mining (Ores & Gems)
            if (state.isIn(BlockTags.COAL_ORES) || state.isIn(BlockTags.COPPER_ORES) || state.isIn(BlockTags.IRON_ORES) ||
                state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.REDSTONE_ORES) || state.isIn(BlockTags.LAPIS_ORES) ||
                state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.EMERALD_ORES) || state.isOf(net.minecraft.block.Blocks.NETHER_QUARTZ_ORE)) {
                
                double yield = getAttr(player, ModAttributes.ORE_YIELD);
                if (isNether) yield += getAttr(player, ModAttributes.ORE_YIELD_IN_NETHER);
                if (isOverworld) yield += getAttr(player, ModAttributes.ORE_YIELD_IN_OVERWORLD);
                
                // Extra yield for gems specifically
                if (state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.EMERALD_ORES) || state.isIn(BlockTags.LAPIS_ORES)) {
                    yield += getAttr(player, ModAttributes.GEM_YIELD);
                }
                
                applyYield(newStack, yield, world.random);
            }
            
            // Flint
            if (state.isOf(net.minecraft.block.Blocks.GRAVEL) && newStack.isOf(Items.FLINT)) {
                double yield = getAttr(player, ModAttributes.FLINT_YIELD);
                applyYield(newStack, yield, world.random);
            }

            // 3. Farming
            if (state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
                if (newStack.isOf(Items.WHEAT_SEEDS) || newStack.isOf(Items.BEETROOT_SEEDS) || newStack.isOf(Items.MELON_SEEDS) || newStack.isOf(Items.PUMPKIN_SEEDS)) {
                    double yield = getAttr(player, ModAttributes.SEED_YIELD);
                    applyYield(newStack, yield, world.random);
                } else {
                    // Actual crop food
                    double yield = getAttr(player, ModAttributes.CROP_YIELD);
                    applyYield(newStack, yield, world.random);
                }
            }

            modifiedDrops.add(newStack);
        }

        cir.setReturnValue(modifiedDrops);
    }

    private static double getAttr(PlayerEntity player, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        var instance = player.getAttributeInstance(attr);
        return instance != null ? instance.getValue() : 0.0;
    }

    private static void applyYield(ItemStack stack, double yieldAmount, net.minecraft.util.math.random.Random random) {
        if (yieldAmount <= 0) return;
        
        int extraItems = (int) yieldAmount; // e.g. 2.5 -> 2 guaranteed
        double fractional = yieldAmount - extraItems; // 0.5 chance
        
        if (random.nextDouble() < fractional) {
            extraItems++;
        }
        
        if (extraItems > 0) {
            int max = stack.getMaxCount();
            int newCount = Math.min(max, stack.getCount() + (stack.getCount() * extraItems));
            stack.setCount(newCount);
        }
    }
    
    private static boolean isSapling(Item item) {
        return item == Items.OAK_SAPLING || item == Items.SPRUCE_SAPLING || item == Items.BIRCH_SAPLING ||
               item == Items.JUNGLE_SAPLING || item == Items.ACACIA_SAPLING || item == Items.DARK_OAK_SAPLING ||
               item == Items.CHERRY_SAPLING || item == Items.MANGROVE_PROPAGULE;
    }
}
