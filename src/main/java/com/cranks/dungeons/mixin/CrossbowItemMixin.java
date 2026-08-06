package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(method = "getPullTime", at = @At("RETURN"), cancellable = true)
    private static void cranks$applyReloadSpeedToCrossbow(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (user != null) {
            double genericAttackSpeedMultiplier = 1.0;
            if (user.getAttributes().hasAttribute(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED)) {
                genericAttackSpeedMultiplier = user.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED) / 4.0;
            }
            
            double customAttackSpeedBonus = 0.0;
            if (user.getAttributes().hasAttribute(ModAttributes.ATTACK_SPEED)) {
                customAttackSpeedBonus = user.getAttributeValue(ModAttributes.ATTACK_SPEED);
            }
            
            double speed = genericAttackSpeedMultiplier * (1.0 + customAttackSpeedBonus);
            
            if (user.getActiveHand() == net.minecraft.util.Hand.OFF_HAND) {
                speed += com.cranks.dungeons.equipment.ItemStatManager.getTotalStatValue(stack, "attack_speed");
            }

            if (speed > 0 && speed != 1.0) {
                int originalTime = cir.getReturnValue();
                // If speed is 0.5, it takes twice as long (originalTime / 0.5)
                cir.setReturnValue((int) (originalTime / speed));
            }
        }
    }
}
