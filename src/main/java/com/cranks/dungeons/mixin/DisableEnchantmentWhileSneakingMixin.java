package com.cranks.dungeons.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public class DisableEnchantmentWhileSneakingMixin {

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"), cancellable = true)
    private static void disableEnchantmentsWhileSneaking(BlockState state, ServerWorld world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (entity instanceof ServerPlayerEntity player && player.isSneaking() && !stack.isEmpty()) {
            var enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (enchants != null) {
                boolean hasSilk = enchants.getEnchantments().stream().anyMatch(e -> e.getKey().get().getValue().toString().equals("minecraft:silk_touch"));
                boolean hasVein = enchants.getEnchantments().stream().anyMatch(e -> e.getKey().get().getValue().toString().equals("cranks-dungeons:vein_mining"));

                if (hasSilk || hasVein) {
                    ItemStack fakeStack = stack.copy();
                    fakeStack.remove(DataComponentTypes.ENCHANTMENTS);
                    cir.setReturnValue(Block.getDroppedStacks(state, world, pos, blockEntity, entity, fakeStack));
                }
            }
        }
    }
}