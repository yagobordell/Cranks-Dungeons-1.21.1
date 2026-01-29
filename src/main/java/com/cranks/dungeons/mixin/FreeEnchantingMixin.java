package com.cranks.dungeons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public class FreeEnchantingMixin {

    @Unique
    private int storedXPLevel = 0;

    @Unique
    private int storedTotalXP = 0;

    @Inject(method = "onButtonClick", at = @At("HEAD"))
    private void storeXP(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        storedXPLevel = player.experienceLevel;
        storedTotalXP = player.totalExperience;
    }

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void restoreXP(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            player.experienceLevel = storedXPLevel;
            player.totalExperience = storedTotalXP;

            int vanillaTook = id + 1;
            int totalTarget = 16;
            int extraToTake = totalTarget - vanillaTook;

            if (extraToTake > 0 && !player.getAbilities().creativeMode) {
                EnchantmentScreenHandler handler = (EnchantmentScreenHandler) (Object) this;
                handler.getSlot(1).getStack().decrement(extraToTake);
            }
        }
    }
}