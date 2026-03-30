package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to block all healing when drowning.
 * This ensures drowning is a real threat regardless of regen gear.
 */
@Mixin(LivingEntity.class)
public class DrowningHealBlockMixin {

    /**
     * Block all healing when player is drowning (air supply depleted).
     * This makes drowning a consistent threat regardless of regeneration gear.
     */
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void blockHealingWhileDrowning(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Only affect players
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }
        
        // Check if player is drowning (air depleted and underwater)
        if (player.getAir() <= 0 && player.isSubmergedInWater()) {
            // Cancel healing
            ci.cancel();
        }
    }
}
