package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class FreezeMobSoundMixin {

    @Inject(method = "playAmbientSound", at = @At("HEAD"), cancellable = true)
    private void cancelAmbientSoundWhenFrozen(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        boolean isStunned;
        if (mob.getEntityWorld().isClient()) {
            isStunned = com.cranks.dungeons.util.ClientAilmentHelper.isStunned(mob);
        } else {
            isStunned = com.cranks.dungeons.ailment.AilmentManager.hasAilment(mob, com.cranks.dungeons.ailment.AilmentType.FREEZE) ||
                        com.cranks.dungeons.ailment.AilmentManager.hasAilment(mob, com.cranks.dungeons.ailment.AilmentType.ELECTROCUTE);
        }
        if (isStunned) {
            ci.cancel();
        }
    }
}
