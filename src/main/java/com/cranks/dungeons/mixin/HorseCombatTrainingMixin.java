package com.cranks.dungeons.mixin;

import com.cranks.dungeons.horse.HorseBreedingManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class HorseCombatTrainingMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void cranks$awardMountedCombatTraining(DamageSource source, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity player
                && player.getVehicle() instanceof HorseEntity horse) {
            LivingEntity defeated = (LivingEntity) (Object) this;
            if (defeated instanceof net.minecraft.entity.mob.Monster
                    || defeated instanceof net.minecraft.entity.mob.MobEntity mob
                    && !(mob instanceof net.minecraft.entity.passive.PassiveEntity)
                    && mob.getTarget() == player) {
                HorseBreedingManager.addCombatExperience(horse, defeated.getMaxHealth());
            }
        }
    }
}
