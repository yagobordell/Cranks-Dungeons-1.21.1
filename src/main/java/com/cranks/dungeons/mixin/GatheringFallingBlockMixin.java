package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.PlacedBlockTracker;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class GatheringFallingBlockMixin {
    @Unique private boolean cranks$placedResource;
    @WrapMethod(method="spawnFromBlock")
    private static FallingBlockEntity capture(World world,BlockPos pos,BlockState state,Operation<FallingBlockEntity> original){
        boolean placed=world instanceof ServerWorld server&&PlacedBlockTracker.isPlaced(server,pos);
        var entity=original.call(world,pos,state);
        ((GatheringFallingBlockMixin)(Object)entity).cranks$placedResource=placed;
        return entity;
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean land(World world,BlockPos pos,BlockState state,int flags,Operation<Boolean> original){
        boolean result=original.call(world,pos,state,flags);
        if(result&&cranks$placedResource&&world instanceof ServerWorld server)PlacedBlockTracker.mark(server,pos);
        return result;
    }
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void save(NbtCompound nbt,CallbackInfo ci){if(cranks$placedResource)nbt.putBoolean("CranksPlacedResource",true);}
    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void load(NbtCompound nbt,CallbackInfo ci){cranks$placedResource=nbt.getBoolean("CranksPlacedResource");}
}
