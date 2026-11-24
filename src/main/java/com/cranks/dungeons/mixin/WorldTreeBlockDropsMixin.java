package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringDrops;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;

@Mixin(Block.class)
public abstract class WorldTreeBlockDropsMixin {
    @WrapOperation(method="dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/block/Block;getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private static List<ItemStack> yield(BlockState state,ServerWorld world,BlockPos pos,BlockEntity blockEntity,Entity player,ItemStack tool,Operation<List<ItemStack>> original){
        return GatheringDrops.block(original.call(state,world,pos,blockEntity,player,tool),state,world,pos,player,tool);
    }
}
