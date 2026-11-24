package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountFoliageCollision;
import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.*;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MountLeafCollisionMixin {
    @Inject(method="getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",at=@At("HEAD"),cancellable=true)
    private void mountedFoliage(BlockView world,BlockPos pos,ShapeContext context,CallbackInfoReturnable<VoxelShape> cir){
        if(((BlockState)(Object)this).isIn(BlockTags.LEAVES)&&context instanceof EntityShapeContext entityContext
                &&MountFoliageCollision.canPass(entityContext.getEntity(),pos))cir.setReturnValue(VoxelShapes.empty());
    }
}
