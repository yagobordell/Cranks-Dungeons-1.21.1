package com.cranks.dungeons.mixin;
import com.cranks.dungeons.inventory.AutomaticCrafting;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CrafterBlock.class)
public abstract class CrafterCraftingMixin {
    @Inject(method="craft",at=@At("HEAD"),cancellable=true)
    private void craftSelected(BlockState state,ServerWorld world,BlockPos pos,CallbackInfo ci) {
        ci.cancel();AutomaticCrafting.pulse((CrafterBlock)(Object)this,state,world,pos);
    }
}
