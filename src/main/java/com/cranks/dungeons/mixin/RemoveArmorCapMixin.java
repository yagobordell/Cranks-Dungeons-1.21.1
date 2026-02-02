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
        // Force modify maxValue for armor attributes
        if (max == 30.0 || max == 20.0) {  // 30 = armor cap, 20 = toughness cap
            try {
                Field field = ClampedEntityAttribute.class.getDeclaredField("maxValue");
                field.setAccessible(true);
                field.set(this, 1024.0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}