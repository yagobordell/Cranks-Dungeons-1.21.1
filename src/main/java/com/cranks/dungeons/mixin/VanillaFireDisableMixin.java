package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class VanillaFireDisableMixin {

    // We no longer intercept setOnFireFor or setFireTicks here.
    // This allows vanilla fire mechanics (like Zombies in sun) to trigger damage(ON_FIRE) calls.
    // The damage reduction and numbers are handled in DamageReductionMixin.

    // Fire overlay follows vanilla behavior.
}
