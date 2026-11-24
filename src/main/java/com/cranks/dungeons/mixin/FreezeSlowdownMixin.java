package com.cranks.dungeons.mixin;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.util.ClientAilmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Stop locomotion/AI, not base ticks: effects, hit immunity, death, and networking keep advancing. */
@Mixin(LivingEntity.class) public class FreezeSlowdownMixin {
    @Unique private boolean cranks$wasStunned;
    @Unique private float cranks$bodyYaw,cranks$headYaw,cranks$yaw,cranks$pitch;
    @Unique private boolean cranks$stunned(LivingEntity entity) {
        return entity.getWorld().isClient()?ClientAilmentHelper.isStunned(entity):AilmentManager.isStunned(entity);
    }
    @Inject(method="tickMovement",at=@At("HEAD"),cancellable=true) private void stopStunnedMovement(CallbackInfo ci) {
        LivingEntity entity=(LivingEntity)(Object)this;
        if(!cranks$stunned(entity))return;
        entity.setVelocity(Vec3d.ZERO);
        entity.sidewaysSpeed=entity.forwardSpeed=entity.upwardSpeed=0;
        entity.clearActiveItem();
        ci.cancel();
    }
    @Inject(method="tick",at=@At("TAIL")) private void holdStunnedPose(CallbackInfo ci) {
        LivingEntity entity=(LivingEntity)(Object)this;
        if(cranks$stunned(entity)) {
            if(!cranks$wasStunned) {
                cranks$bodyYaw=entity.getBodyYaw();
                cranks$headYaw=entity.getHeadYaw();
                cranks$yaw=entity.getYaw();
                cranks$pitch=entity.getPitch();
            }
            entity.setBodyYaw(cranks$bodyYaw);
            entity.setHeadYaw(cranks$headYaw);
            entity.setYaw(cranks$yaw);
            entity.setPitch(cranks$pitch);
            entity.setVelocity(Vec3d.ZERO);
            cranks$wasStunned=true;
        } else cranks$wasStunned=false;
    }
    @Inject(method="setCurrentHand",at=@At("HEAD"),cancellable=true) private void preventStunnedUse(net.minecraft.util.Hand hand,CallbackInfo ci) {
        if(cranks$stunned((LivingEntity)(Object)this))ci.cancel();
    }
}
