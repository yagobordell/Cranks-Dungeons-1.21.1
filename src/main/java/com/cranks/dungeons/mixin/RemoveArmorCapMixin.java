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

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private static double uncapArmor(double max) {
        if (max == 30.0 || max == 20.0) {
            return 1000000.0;
        }
        return max;
    }
}