package com.cranks.dungeons.mixin;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Vanilla orbs do not carry validated combat or discovery provenance. */
@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntitySpawnMixin {
    @Inject(method="spawn",at=@At("HEAD"),cancellable=true)
    private static void suppress(ServerWorld world,Vec3d pos,int amount,CallbackInfo ci) { ci.cancel(); }
    @Inject(method="onPlayerCollision",at=@At("HEAD"),cancellable=true)
    private void rejectPickup(net.minecraft.entity.player.PlayerEntity player,CallbackInfo ci) {
        if(!player.getWorld().isClient())((ExperienceOrbEntity)(Object)this).discard();
        ci.cancel();
    }
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void discardUntypedOrbs(CallbackInfo ci) {
        var orb=(ExperienceOrbEntity)(Object)this;
        if(!orb.getWorld().isClient()){orb.discard();ci.cancel();}
    }
}
