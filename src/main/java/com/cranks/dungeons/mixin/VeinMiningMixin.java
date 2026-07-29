package com.cranks.dungeons.mixin;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
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
import net.minecraft.util.Identifier;
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

    @Unique
    private static final Identifier VEIN_MINING_ID = Identifier.of("cranks-dungeons", "vein_mining");

    @Unique
    private static final Identifier SILK_TOUCH_ID = Identifier.of("minecraft", "silk_touch");

    @Unique
    private static final int VEIN_MINE_MAX_BLOCKS = 31;

    @Unique
    private static final int VEIN_MINE_MAX_ITERATIONS = 64;

    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) return;

        // Check if vein mining is toggled disabled
        if (EnchantmentDisableManager.isDisabled(player.getUuid(), VEIN_MINING_ID)) return;

        int veinLevel = EnchantmentHelper.getLevel(
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(ModEnchantments.VEIN_MINING).get(),
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

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        List<BlockPos> toBreak = new ArrayList<>();

        toCheck.add(origin);
        visited.add(origin);

        // Limit iterations to prevent excessive computation while still finding enough blocks
        int iterations = 0;

        while (!toCheck.isEmpty() && toBreak.size() < VEIN_MINE_MAX_BLOCKS && iterations < VEIN_MINE_MAX_ITERATIONS) {
            BlockPos current = toCheck.poll();
            iterations++;

            // Check 6 direct neighbors (cardinal directions) first for better performance
            BlockPos[] neighbors = {
                current.up(), current.down(),
                current.north(), current.south(),
                current.east(), current.west()
            };

            for (BlockPos neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                if (world.getBlockState(neighbor).isIn(targetTag)) {
                    toBreak.add(neighbor);
                    toCheck.add(neighbor);
                    if (toBreak.size() >= VEIN_MINE_MAX_BLOCKS) break;
                }
            }
        }

        boolean hasSilk = EnchantmentHelper.getLevel(
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(net.minecraft.enchantment.Enchantments.SILK_TOUCH).get(), tool) > 0;

        // If silk touch is toggled disabled, ignore it
        if (EnchantmentDisableManager.isDisabled(player.getUuid(), SILK_TOUCH_ID)) {
            hasSilk = false;
        }

        double fortuneChance = ToolStatHelper.getFortune(tool) + player.getAttributeValue(ModAttributes.FORTUNE);

        for (BlockPos pos : toBreak) {
            if (pos.equals(origin)) continue;

            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.isIn(targetTag)) continue;

            Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, tool);

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