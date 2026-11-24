package com.cranks.dungeons.mixin;
import com.cranks.dungeons.component.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEntity.class)
abstract class EconomyPlayerDataMixin {
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void writeTradeMarker(NbtCompound nbt, CallbackInfo ci) {
        var marker=ModComponents.WALLET.get((PlayerEntity)(Object)this).transaction();
        if(marker!=null)nbt.putUuid("CranksEconomyTransaction",marker);
    }
}
