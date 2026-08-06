package com.cranks.dungeons.compat.punchy;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;

@Mixin(value = PunchyAnimationManager.class, remap = false)
public class PunchyAnimationManagerMixin {

    @Inject(method = "clipSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private static void cranks$adjustBowAndCrossbowSpeed(AnimationClip clip, CallbackInfoReturnable<Float> cir) {
        if (clip == null) return;
        boolean isBow = PunchyAnimationManager.isClipNamed(clip, "use_bow");
        boolean isCrossbow = PunchyAnimationManager.isClipNamed(clip, "use_crossbow");
        
        if (isBow || isCrossbow) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                double genericAttackSpeedMultiplier = client.player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED) / 4.0;
                double customAttackSpeedBonus = client.player.getAttributeValue(ModAttributes.ATTACK_SPEED);
                double speed = genericAttackSpeedMultiplier * (1.0 + customAttackSpeedBonus);
                
                ItemStack stack = client.player.getActiveItem();
                if (stack.isEmpty()) {
                    stack = client.player.getMainHandStack();
                }
                
                if (client.player.getActiveHand() == net.minecraft.util.Hand.OFF_HAND) {
                    speed += com.cranks.dungeons.equipment.ItemStatManager.getTotalStatValue(stack, "attack_speed");
                }
                
                if (speed > 0 && speed != 1.0) {
                    float original = cir.getReturnValue();
                    // Original is usually 1.0. If speed is 0.5 (slower), the multiplier should be original * 0.5
                    // which means the animation plays at half speed and takes twice as long.
                    cir.setReturnValue((float) (original * speed));
                }
            }
        }
    }
}
