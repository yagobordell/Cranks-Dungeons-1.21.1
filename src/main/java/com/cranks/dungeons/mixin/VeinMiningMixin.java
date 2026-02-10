package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
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
        if (world.isClient() || player.isSneaking()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) return;

        int veinLevel = EnchantmentHelper.getLevel(
                world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.VEIN_MINING),
                tool
        );

        if (veinLevel <= 0) return;
        veinMine(serverWorld, pos, state, serverPlayer, tool);
    }

    @Unique
    private void veinMine(ServerWorld world, BlockPos origin, BlockState originState, ServerPlayerEntity player, ItemStack tool) {
        Optional<TagKey<Block>> oreTag = originState.streamTags()
                .filter(tag -> tag.id().getPath().contains("ores"))
                .findFirst();

        if (oreTag.isEmpty()) return;
        TagKey<Block> targetTag = oreTag.get();

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

                        if (world.getBlockState(neighbor).isIn(targetTag)) {
                            toBreak.add(neighbor);
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }

        boolean hasSilk = EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(net.minecraft.enchantment.Enchantments.SILK_TOUCH), tool) > 0;
        double fortuneChance = ToolStatHelper.getFortune(tool) + player.getAttributeValue(ModAttributes.FORTUNE);

        for (BlockPos pos : toBreak) {
            if (pos.equals(origin)) continue; // CRITICAL: Skip first block to prevent 7/4 duplicate issue

            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.isIn(targetTag)) continue;

            Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);

            // Only Fortune if NOT Silk Touching
            if (!hasSilk && fortuneChance > 0 && player.getRandom().nextDouble() < fortuneChance) {
                Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.5f);
            }

            world.removeBlock(pos, false);
            world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
            tool.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
            if (tool.getDamage() >= tool.getMaxDamage() - 1) break;
        }
    }
}