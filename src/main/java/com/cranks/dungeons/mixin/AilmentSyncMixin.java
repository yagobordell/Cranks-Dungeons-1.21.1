package com.cranks.dungeons.mixin;
import com.cranks.dungeons.ailment.AilmentManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class) public abstract class AilmentSyncMixin {
    @Inject(method="removeStatusEffectInternal",at=@At("RETURN")) private void removeAilment(RegistryEntry<StatusEffect> effect,CallbackInfoReturnable<StatusEffectInstance> cir) {
        if(cir.getReturnValue()!=null)AilmentManager.onEffectRemoved((LivingEntity)(Object)this,effect);
    }
    @Inject(method="clearStatusEffects",at=@At("TAIL")) private void clearAilments(CallbackInfoReturnable<Boolean> cir) {
        AilmentManager.clearAllAilments((LivingEntity)(Object)this);
    }
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL")) private void saveAilments(NbtCompound nbt,CallbackInfo ci) {
        AilmentManager.writeNbt((LivingEntity)(Object)this,nbt);
    }
    @Inject(method="readCustomDataFromNbt",at=@At("TAIL")) private void loadAilments(NbtCompound nbt,CallbackInfo ci) {
        AilmentManager.readNbt((LivingEntity)(Object)this,nbt);
    }
}
