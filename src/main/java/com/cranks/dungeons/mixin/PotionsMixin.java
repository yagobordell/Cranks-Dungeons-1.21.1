package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.PotionRules;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Potions.class)
abstract class PotionsMixin {
    @ModifyVariable(method = "register", at = @At("HEAD"), argsOnly = true)
    private static Potion balancePotion(Potion original, String id, Potion ignored) {
        return PotionRules.balance(id, original);
    }
}
