package com.cranks.dungeons.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneNerfMixin {

    @Inject(method = "grind", at = @At("HEAD"), cancellable = true)
    private void blockDamagedDisenchant(ItemStack item, CallbackInfoReturnable<ItemStack> cir) {
        if (!item.isEmpty() && item.isDamageable()) {
            int maxDamage = item.getMaxDamage();
            int currentDamage = item.getDamage();

            if (currentDamage > (maxDamage / 2)) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "grind", at = @At("RETURN"))
    private void reduceDurabilityAfterGrind(ItemStack item, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();

        if (!result.isEmpty() && result.isDamageable()) {
            int maxDamage = result.getMaxDamage();
            int currentDamage = result.getDamage();

            int damageToAdd = maxDamage / 2;
            int newDamage = Math.min(currentDamage + damageToAdd, maxDamage - 1);
            result.setDamage(newDamage);
        }
    }
}