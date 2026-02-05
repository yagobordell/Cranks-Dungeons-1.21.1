package com.cranks.dungeons.mixin;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityAttributeInstance.class)
public class HideArmorAttributeMixin {

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void hideArmorValue(CallbackInfoReturnable<Double> cir) {
        EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;

        if (instance.getAttribute() == EntityAttributes.ARMOR) {
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (element.getClassName().contains("InGameHud")) {
                    cir.setReturnValue(0.0);
                    return;
                }
            }
        }
    }
}