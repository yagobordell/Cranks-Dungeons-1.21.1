package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.PotionCloudAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionEntity.class)
abstract class PotionEntityMixin {
    @WrapOperation(method = "applySplashPotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
    private boolean failedMixtureProtection(net.minecraft.entity.LivingEntity target, net.minecraft.entity.effect.StatusEffectInstance effect,
                                            Entity source, Operation<Boolean> original) {
        var projectile = (PotionEntity) (Object) this;
        if (com.cranks.dungeons.alchemy.AlchemyPotions.isFailure(projectile.getStack())
                && !com.cranks.dungeons.alchemy.AlchemyPotions.canApplyFailure(projectile.getOwner(), target)) return false;
        return original.call(target, effect, source);
    }
    @Inject(method = "isLingering", at = @At("HEAD"), cancellable = true)
    private void alchemyDelivery(CallbackInfoReturnable<Boolean> result) {
        if (((PotionEntity) (Object) this).getStack().getItem() instanceof com.cranks.dungeons.alchemy.AlchemyItems.Brew brew) {
            result.setReturnValue(brew.delivery == com.cranks.dungeons.alchemy.AlchemyItems.Delivery.LINGERING);
        }
    }
    @WrapOperation(method = "applyLingeringPotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean configurePotionCloud(World world, Entity cloud, Operation<Boolean> original) {
        ((PotionCloudAccess) cloud).cranks$configurePotionCloud();
        return original.call(world, cloud);
    }
}
