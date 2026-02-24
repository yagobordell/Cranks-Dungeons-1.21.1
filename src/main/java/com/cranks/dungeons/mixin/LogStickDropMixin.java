package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class LogStickDropMixin {

    @Unique
    private static double cranks$getStickChance(PlayerEntity player, ItemStack tool) {
        // Try attribute first
        EntityAttributeInstance instance = player.getAttributeInstance(ModAttributes.STICK_DROP_CHANCE);
        if (instance != null && instance.getValue() > 0.0) {
            return instance.getValue();
        }

        // Fallback: read directly from the item's stored stat data
        return EquipmentStatApplier.getItemStatValue(tool, "stick_drop_chance");
    }

    @Inject(
            method = "afterBreak",
            at = @At("TAIL")
    )
    private void cranks$afterLogBreak(
            World world,
            PlayerEntity player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool,
            CallbackInfo ci
    ) {
        if (world.isClient()) {
            return;
        }

        if (player == null || player.isSpectator()) {
            return;
        }

        if (!state.isIn(BlockTags.LOGS)) {
            return;
        }

        if (tool.isEmpty()) {
            return;
        }

        double stickChance = cranks$getStickChance(player, tool);
        if (stickChance <= 0.0) {
            return;
        }

        if (world.getRandom().nextDouble() < stickChance) {
            Block.dropStack(world, pos, new ItemStack(Items.STICK, 2));
        }
    }
}