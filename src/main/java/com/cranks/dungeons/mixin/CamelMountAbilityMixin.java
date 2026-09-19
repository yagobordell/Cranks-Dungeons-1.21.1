package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountDataProvider;
import net.minecraft.entity.passive.CamelEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CamelEntity.class)
public abstract class CamelMountAbilityMixin {
    @ModifyVariable(method = "jump", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float cranks$applyDashPowerGene(float strength) {
        MountData data = ((MountDataProvider) this).cranksdungeons$getMountData();
        if (!data.initialized) return strength;
        double quality = Math.max(0.0, Math.min(1.0, (data.power - 0.45) / 0.45));
        double traitBonus = "Powerful".equals(data.trait) ? 0.10 : 0.0;
        return (float) (strength * (0.90 + quality * 0.35 + traitBonus));
    }
}
