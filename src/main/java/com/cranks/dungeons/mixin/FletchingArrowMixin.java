package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.FletchingArrows;
import com.cranks.dungeons.inventory.ArrowCoatingAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

@Mixin(ArrowEntity.class)
abstract class FletchingArrowMixin implements ArrowCoatingAccess {
    @Shadow protected abstract void setStack(ItemStack stack);
    @Unique private UUID cranks$coatingTarget;
    @Unique private boolean cranks$coatingApplies;
    @Override public void cranks$recordCoatingHit(UUID target, boolean applies) {
        cranks$coatingTarget = target; cranks$coatingApplies = applies;
    }
    @ModifyVariable(method = "setStack", at = @At("HEAD"), argsOnly = true)
    private ItemStack normalizeAmmunition(ItemStack stack) { return FletchingArrows.canonicalize(stack); }
    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    private void normalizeStrayArrow(StatusEffectInstance effect, CallbackInfo ci) {
        var arrow = (ArrowEntity) (Object) this;
        if (arrow.getItemStack().isOf(net.minecraft.item.Items.ARROW)
                && effect.getEffectType().equals(net.minecraft.entity.effect.StatusEffects.SLOWNESS)) {
            setStack(FletchingArrows.Kind.SLOWING.stack(arrow.getItemStack().getCount()));
            ci.cancel();
        }
    }
    @WrapOperation(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
    private boolean applyCoating(LivingEntity target, StatusEffectInstance effect, Entity source, Operation<Boolean> original) {
        if (FletchingArrows.kind(((ArrowEntity) (Object) this).getItemStack()) != null
                && (!cranks$coatingApplies || !target.getUuid().equals(cranks$coatingTarget))) return false;
        return original.call(target, effect, source);
    }
}
