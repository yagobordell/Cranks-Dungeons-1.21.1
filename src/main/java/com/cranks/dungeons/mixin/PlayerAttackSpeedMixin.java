package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerAttackSpeedMixin {

    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("RETURN"), cancellable = true)
    private void applyCustomAttackSpeed(CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // ModAttributes.ATTACK_SPEED is a percentage multiplier (e.g. 0.2 = +20% speed)
        double speedMultiplier = 1.0 + player.getAttributeValue(ModAttributes.ATTACK_SPEED);
        
        if (speedMultiplier != 1.0) {
            cir.setReturnValue((float) (cir.getReturnValue() * speedMultiplier));
        }
    }
}
