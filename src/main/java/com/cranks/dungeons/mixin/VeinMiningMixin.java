package com.cranks.dungeons.mixin;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.registry.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

@Mixin(Block.class)
public class VeinMiningMixin {
    @Unique private static final int CRANKS_MAX_EXTRA_BLOCKS = 31;
    @Unique private static final ThreadLocal<Boolean> CRANKS_MINING_VEIN = ThreadLocal.withInitial(() -> false);

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
                            BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity miner) || !(world instanceof ServerWorld serverWorld)) return;

        if (CRANKS_MINING_VEIN.get() || !state.isIn(ModBlockTags.ORE_FORTUNE_ORES)
                || EnchantmentDisableManager.isDisabled(player.getUuid(), ModEnchantments.VEIN_MINING.getValue())) return;
        var enchantment = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(ModEnchantments.VEIN_MINING);
        ItemStack heldTool = miner.getMainHandStack();
        if (heldTool.isEmpty() || enchantment.isEmpty() || EnchantmentHelper.getLevel(enchantment.get(), tool) <= 0) return;

        var oreGroups = state.streamTags().filter(ModBlockTags::isOreFamily).toList();
        var queue = new ArrayDeque<BlockPos>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(pos);
        visited.add(pos);
        int broken = 0;
        CRANKS_MINING_VEIN.set(true);
        try {
            while (!queue.isEmpty() && broken < CRANKS_MAX_EXTRA_BLOCKS && !heldTool.isEmpty()
                    && miner.getMainHandStack() == heldTool) {
                BlockPos current = queue.removeFirst();
                for (Direction direction : Direction.values()) {
                    BlockPos next = current.offset(direction);
                    if (!visited.add(next) || !serverWorld.isChunkLoaded(ChunkPos.toLong(next))
                            || !world.getWorldBorder().contains(next) || !world.canPlayerModifyAt(miner, next)) continue;
                    BlockState neighbor = world.getBlockState(next);
                    if (!neighbor.isIn(ModBlockTags.ORE_FORTUNE_ORES) || !miner.canHarvest(neighbor)
                            || !(neighbor.isOf(state.getBlock()) || oreGroups.stream().anyMatch(neighbor::isIn))) continue;
                    if (miner.interactionManager.tryBreakBlock(next) && !world.getBlockState(next).isOf(neighbor.getBlock())) {
                        queue.addLast(next);
                        broken++;
                        if (broken >= CRANKS_MAX_EXTRA_BLOCKS || heldTool.isEmpty()
                                || miner.getMainHandStack() != heldTool) break;
                    }
                }
            }
        } finally {
            CRANKS_MINING_VEIN.remove();
        }
    }
}
