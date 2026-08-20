package com.cranks.dungeons.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class FireResistanceNeutralizeMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void removeFireResistanceTotalInvulnerability(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living) {
            if (source.isIn(DamageTypeTags.IS_FIRE)) {
                if (living.isInvulnerable() || living.getType().isFireImmune()) {
                    cir.setReturnValue(true);
                } else {
                    // Bypass vanilla's hardcoded FIRE_RESISTANCE status effect invulnerability check
                    // so fire/lava damage proceeds to DamageReductionMixin and is mitigated by ModAttributes.FIRE_RESISTANCE
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
