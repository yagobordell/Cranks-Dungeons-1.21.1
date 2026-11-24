package com.cranks.dungeons.mixin;
import com.cranks.dungeons.progression.CombatExperience;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=LivingEntity.class,priority=450)
public abstract class ProgressionActivityMixin {
    @Inject(method="damage",at=@At("HEAD"))
    private void tank(DamageSource source,float amount,CallbackInfoReturnable<Boolean> cir) {
        CombatExperience.attacked((LivingEntity)(Object)this,source,amount);
    }
    @Inject(method="addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",at=@At("RETURN"))
    private void support(StatusEffectInstance effect,Entity source,CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && effect.getEffectType().value().getCategory()==StatusEffectCategory.BENEFICIAL && source instanceof PlayerEntity player)
            CombatExperience.support(player,(LivingEntity)(Object)this);
    }
}
