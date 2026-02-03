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
import net.minecraft.registry.tag.TagKey;
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

        int veinLevel = EnchantmentHelper.getLevel(
                world.getRegistryManager()
                        .getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                        .getOrThrow(ModEnchantments.VEIN_MINING),
                tool
        );

        if (veinLevel <= 0) return;

        TagKey<Block> oreTag = getOreTag(state);
        if (oreTag == null) return;

        veinMine(serverWorld, pos, state, serverPlayer, tool, oreTag);
    }

    @Unique
    private TagKey<Block> getOreTag(BlockState state) {
        // Return the ore tag for this block, or null if it's not an ore
        if (state.isIn(BlockTags.COAL_ORES)) return BlockTags.COAL_ORES;
        if (state.isIn(BlockTags.IRON_ORES)) return BlockTags.IRON_ORES;
        if (state.isIn(BlockTags.GOLD_ORES)) return BlockTags.GOLD_ORES;
        if (state.isIn(BlockTags.DIAMOND_ORES)) return BlockTags.DIAMOND_ORES;
        if (state.isIn(BlockTags.EMERALD_ORES)) return BlockTags.EMERALD_ORES;
        if (state.isIn(BlockTags.COPPER_ORES)) return BlockTags.COPPER_ORES;
        if (state.isIn(BlockTags.LAPIS_ORES)) return BlockTags.LAPIS_ORES;
        if (state.isIn(BlockTags.REDSTONE_ORES)) return BlockTags.REDSTONE_ORES;
        return null;
    }

    @Unique
    private void veinMine(ServerWorld world, BlockPos origin, BlockState originState, ServerPlayerEntity player, ItemStack tool, TagKey<Block> oreTag) {
        int maxBlocks = 31;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        List<BlockPos> toBreak = new ArrayList<>();

        toCheck.add(origin);
        visited.add(origin);

        while (!toCheck.isEmpty() && toBreak.size() < maxBlocks) {
            BlockPos current = toCheck.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos neighbor = current.add(dx, dy, dz);

                        if (visited.contains(neighbor)) continue;
                        visited.add(neighbor);

                        BlockState neighborState = world.getBlockState(neighbor);

                        // Check if it's the same ore TYPE (using tags)
                        if (neighborState.isIn(oreTag)) {
                            toBreak.add(neighbor);
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }

        double fortuneChance = ToolStatHelper.getFortune(tool);
        fortuneChance += player.getAttributeValue(ModAttributes.FORTUNE);

        for (BlockPos pos : toBreak) {
            BlockState state = world.getBlockState(pos);

            if (state.isAir() || !state.isIn(oreTag)) continue;

            Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);

            if (fortuneChance > 0 && player.getRandom().nextDouble() < fortuneChance) {
                Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);

                world.playSound(null, pos,
                        net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.5f);
            }

            world.removeBlock(pos, false);

            world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));

            tool.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);

            if (tool.getDamage() >= tool.getMaxDamage() - 1) {
                break;
            }
        }
    }
}