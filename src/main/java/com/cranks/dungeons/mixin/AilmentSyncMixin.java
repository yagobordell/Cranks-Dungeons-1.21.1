package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(LivingEntity.class)
public abstract class AilmentSyncMixin {

    @Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
    private void onAilmentApplied(StatusEffectInstance effect, net.minecraft.entity.Entity source, CallbackInfo ci) {
        if (isAilmentEffect(effect.getEffectType())) {
            AilmentManager.syncAilments((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "removeStatusEffectInternal", at = @At("RETURN"))
    private void onAilmentRemoved(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (isAilmentEffect(effect)) {
            AilmentManager.syncAilments((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "clearStatusEffects", at = @At("TAIL"))
    private void onAilmentsCleared(CallbackInfoReturnable<Boolean> cir) {
        // Just always sync when all effects are removed (e.g. milk)
        AilmentManager.syncAilments((LivingEntity) (Object) this);
    }

    private boolean isAilmentEffect(RegistryEntry<StatusEffect> effect) {
        return effect.equals(ModEffects.BURN) ||
                effect.equals(ModEffects.POISON) ||
                effect.equals(ModEffects.FREEZE) ||
                effect.equals(ModEffects.SHOCK) ||
                effect.equals(ModEffects.BLEED);
    }
}
