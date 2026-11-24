package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringPistonData;
import com.cranks.dungeons.util.PlacedBlockTracker;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public abstract class GatheringPistonBlockEntityMixin implements GatheringPistonData {
    @Unique private boolean cranks$placed;
    @Override public boolean cranks$placedResource(){return cranks$placed;}
    @Override public void cranks$placedResource(boolean value){cranks$placed=value;}
    @WrapMethod(method="tick")
    private static void settle(World world,BlockPos pos,BlockState state,PistonBlockEntity entity,Operation<Void> original){
        boolean placed=((GatheringPistonData)entity).cranks$placedResource();
        original.call(world,pos,state,entity);
        if(placed&&world instanceof ServerWorld server&&PlacedBlockTracker.tracked(world.getBlockState(pos)))PlacedBlockTracker.mark(server,pos);
    }
    @WrapMethod(method="finish")
    private void finish(Operation<Void> original){
        var entity=(PistonBlockEntity)(Object)this;var world=entity.getWorld();var pos=entity.getPos();
        original.call();
        if(cranks$placed&&world instanceof ServerWorld server&&PlacedBlockTracker.tracked(world.getBlockState(pos)))PlacedBlockTracker.mark(server,pos);
    }
    @Inject(method="writeNbt",at=@At("TAIL"))
    private void save(NbtCompound nbt,RegistryWrapper.WrapperLookup registries,CallbackInfo ci){if(cranks$placed)nbt.putBoolean("CranksPlacedResource",true);}
    @Inject(method="readNbt",at=@At("TAIL"))
    private void load(NbtCompound nbt,RegistryWrapper.WrapperLookup registries,CallbackInfo ci){cranks$placed=nbt.getBoolean("CranksPlacedResource");}
}
