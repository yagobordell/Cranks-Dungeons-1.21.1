package com.cranks.dungeons.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class StarvationDamageMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float scaleStarvationDamage(float amount, DamageSource source) {
        if (source.isOf(DamageTypes.STARVE)) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            return player.getMaxHealth() * 0.05f;
        }
        return amount;
    }
}
