package com.cranks.dungeons.mixin;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ClampedEntityAttribute.class)
public class RemoveArmorCapMixin {

    @Shadow
    private double maxValue;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void uncapArmor(String translationKey, double fallback, double min, double max, CallbackInfo ci) {
        // Force modify maxValue for armor and toughness attributes
        // This used reflection before, which fails in production due to obfuscation.
        // Using the Shadow field directly allows Mixin to handle remapping.
        if (max == 30.0 || max == 20.0) {
            this.maxValue = 1024.0;
        }
    }
}