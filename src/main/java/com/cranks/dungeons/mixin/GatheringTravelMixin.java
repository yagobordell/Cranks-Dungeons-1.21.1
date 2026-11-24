package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayerEntity.class)
public abstract class GatheringTravelMixin {
    @ModifyArg(method="increaseTravelMotionStats",at=@At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"),index=0)
    private float efficientTravel(float amount){return (float)(amount*(1-((ServerPlayerEntity)(Object)this).getAttributeValue(ModAttributes.TRAVEL_EFFICIENCY)));}
}
