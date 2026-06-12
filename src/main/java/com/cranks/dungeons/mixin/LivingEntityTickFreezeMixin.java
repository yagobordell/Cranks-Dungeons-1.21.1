package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTickFreezeMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void freezeTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        boolean isStunned;
        if (entity.getEntityWorld().isClient()) {
            isStunned = com.cranks.dungeons.util.ClientAilmentHelper.isStunned(entity);
        } else {
            isStunned = com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.FREEZE) ||
                        com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.ELECTROCUTE);
        }
        if (isStunned) {
            // Undo the World.tickEntity() age increment
            entity.age--;
            // Manually tick ailments so the freeze duration decays
            com.cranks.dungeons.ailment.AilmentManager.tickAilments(entity);
            ci.cancel();
        }
    }
}
