package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Block.class)
public class VeinMiningMixin {

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        // Check if tool has vein mining enchantment
        int veinLevel = EnchantmentHelper.getLevel(
                world.getRegistryManager()
                        .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                        .getOrThrow(ModEnchantments.VEIN_MINING),
                tool
        );

        if (veinLevel <= 0) return;

        // Only work on ores
        if (!isOre(state)) return;

        // Vein mine adjacent ores
        veinMine(serverWorld, pos, state, serverPlayer, tool);
    }

    @Unique
    private boolean isOre(BlockState state) {
        // Check if block is an ore
        return state.isIn(BlockTags.COAL_ORES) ||
                state.isIn(BlockTags.IRON_ORES) ||
                state.isIn(BlockTags.GOLD_ORES) ||
                state.isIn(BlockTags.DIAMOND_ORES) ||
                state.isIn(BlockTags.EMERALD_ORES) ||
                state.isIn(BlockTags.COPPER_ORES) ||
                state.isIn(BlockTags.LAPIS_ORES) ||
                state.isIn(BlockTags.REDSTONE_ORES);
    }

    @Unique
    private void veinMine(ServerWorld world, BlockPos origin, BlockState originState, ServerPlayerEntity player, ItemStack tool) {
        Block targetBlock = originState.getBlock();

        // Max 32 blocks for vein mining (excluding the origin)
        int maxBlocks = 31;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        List<BlockPos> toBreak = new ArrayList<>();

        // Start checking from origin, but don't break it (it's already broken)
        toCheck.add(origin);
        visited.add(origin);

        // BFS to find connected ore blocks
        while (!toCheck.isEmpty() && toBreak.size() < maxBlocks) {
            BlockPos current = toCheck.poll();

            // Check all 26 surrounding blocks (3x3x3 cube)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos neighbor = current.add(dx, dy, dz);

                        if (visited.contains(neighbor)) continue;
                        visited.add(neighbor);

                        BlockState neighborState = world.getBlockState(neighbor);

                        // Check if it's the same ore type
                        if (neighborState.getBlock() == targetBlock) {
                            toBreak.add(neighbor);
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }

        // Get fortune chance once
        double fortuneChance = ToolStatHelper.getFortune(tool);
        fortuneChance += player.getAttributeValue(ModAttributes.FORTUNE);

        // Break all found blocks
        for (BlockPos pos : toBreak) {
            BlockState state = world.getBlockState(pos);

            // Check if block still exists
            if (state.isAir() || state.getBlock() != targetBlock) continue;

            // Drop items as if the player broke it with their tool
            Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);

            // Apply fortune chance to each ore independently
            if (fortuneChance > 0 && player.getRandom().nextDouble() < fortuneChance) {
                Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);

                // Play fortune sound
                world.playSound(null, pos,
                        net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.5f);
            }

            // Remove the block
            world.removeBlock(pos, false);

            // Play break sound and particles
            world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));

            // Damage the tool for each block
            tool.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);

            // Stop if tool is about to break
            if (tool.getDamage() >= tool.getMaxDamage() - 1) {
                break;
            }
        }
    }
}