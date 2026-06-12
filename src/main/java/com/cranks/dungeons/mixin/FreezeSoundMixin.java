package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class FreezeSoundMixin {

    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    private void cancelSoundWhenFrozen(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living) {
            boolean isStunned;
            if (living.getEntityWorld().isClient()) {
                isStunned = com.cranks.dungeons.util.ClientAilmentHelper.isStunned(living);
            } else {
                isStunned = com.cranks.dungeons.ailment.AilmentManager.hasAilment(living, com.cranks.dungeons.ailment.AilmentType.FREEZE) ||
                            com.cranks.dungeons.ailment.AilmentManager.hasAilment(living, com.cranks.dungeons.ailment.AilmentType.ELECTROCUTE);
            }
            if (isStunned) {
                ci.cancel();
            }
        }
    }
}
