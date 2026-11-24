package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringPistonMove;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBlock.class)
public abstract class GatheringPistonMixin {
    @WrapMethod(method="move")
    private boolean carry(World world,BlockPos pos,Direction direction,boolean extending,Operation<Boolean> original){
        if(!(world instanceof ServerWorld server))return original.call(world,pos,direction,extending);
        try(var movement=GatheringPistonMove.begin(server)){
            boolean moved=original.call(world,pos,direction,extending);
            if(moved)movement.finish(extending?direction:direction.getOpposite());
            return moved;
        }
    }
    @WrapOperation(method="move",at=@At(value="INVOKE",target="Lnet/minecraft/block/piston/PistonHandler;calculatePush()Z"))
    private boolean capture(PistonHandler handler,Operation<Boolean> original){
        boolean valid=original.call(handler);
        if(valid)GatheringPistonMove.capture(handler);
        return valid;
    }
}
