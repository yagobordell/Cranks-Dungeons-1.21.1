package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Collection of Mixins to slow down various combat goals when a mob is frozen.
 * Handles Melee, Bow, Crossbow, and Generic Projectile attacks.
 */
public abstract class MobAttackSlowdownMixin {

    @Mixin(MeleeAttackGoal.class)
    public static abstract class Melee {
        @Unique private MobEntity bonus$mob;
        @Shadow protected int cooldown;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void captureMob(net.minecraft.entity.mob.PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, CallbackInfo ci) {
            this.bonus$mob = mob;
        }

        @Inject(method = "tick", at = @At("TAIL"))
        private void slowMeleeAttack(CallbackInfo ci) {
            if (this.bonus$mob != null && this.cooldown > 0) {
                if (this.bonus$mob.hasStatusEffect(ModEffects.FREEZE) || this.bonus$mob.hasStatusEffect(ModEffects.ELECTROCUTE)) {
                    this.cooldown++; // Full freeze = cooldown doesn't tick down
                } else {
                    float buildup = com.cranks.dungeons.ailment.AilmentManager.getFreezeBuildup(this.bonus$mob.getUuid());
                    if (buildup > 0 && Math.random() < buildup * 0.35) {
                        this.cooldown++; // Fractional slowdown
                    }
                }
            }
        }
    }

    @Mixin(BowAttackGoal.class)
    public static abstract class Bow {
        @Unique private MobEntity bonus$mob;
        @Shadow private int cooldown; // BowAttackGoal uses cooldown (private)

        @Inject(method = "<init>", at = @At("RETURN"))
        private void captureMob(net.minecraft.entity.mob.HostileEntity mob, double speed, int attackInterval, float range, CallbackInfo ci) {
            this.bonus$mob = mob;
        }

        @Inject(method = "tick", at = @At("TAIL"))
        private void slowBowAttack(CallbackInfo ci) {
            if (this.bonus$mob != null && this.cooldown > 0) {
                if (this.bonus$mob.hasStatusEffect(ModEffects.FREEZE) || this.bonus$mob.hasStatusEffect(ModEffects.ELECTROCUTE)) {
                    this.cooldown++; // Full freeze = cooldown doesn't tick down
                } else {
                    float buildup = com.cranks.dungeons.ailment.AilmentManager.getFreezeBuildup(this.bonus$mob.getUuid());
                    if (buildup > 0 && Math.random() < buildup * 0.35) {
                        this.cooldown++;
                    }
                }
            }
        }
    }

    @Mixin(CrossbowAttackGoal.class)
    public static abstract class Crossbow {
        @Unique private MobEntity bonus$mob;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void captureMob(net.minecraft.entity.mob.HostileEntity mob, double speed, float range, CallbackInfo ci) {
            this.bonus$mob = mob;
        }
    }

    @Mixin(ProjectileAttackGoal.class)
    public static abstract class Projectile {
        @Unique private MobEntity bonus$mob;
        @Shadow private int updateCountdownTicks; // ProjectileAttackGoal uses updateCountdownTicks (private)

        @Inject(method = "<init>", at = @At("RETURN"))
        private void captureMob(net.minecraft.entity.ai.RangedAttackMob mob, double mobSpeed, int intervalTicks, float maxShootRange, CallbackInfo ci) {
            if (mob instanceof MobEntity me) this.bonus$mob = me;
        }

        @Inject(method = "tick", at = @At("TAIL"))
        private void slowProjectileAttack(CallbackInfo ci) {
            if (this.bonus$mob != null && this.updateCountdownTicks > 0) {
                if (this.bonus$mob.hasStatusEffect(ModEffects.FREEZE) || this.bonus$mob.hasStatusEffect(ModEffects.ELECTROCUTE)) {
                    this.updateCountdownTicks++;
                } else {
                    float buildup = com.cranks.dungeons.ailment.AilmentManager.getFreezeBuildup(this.bonus$mob.getUuid());
                    if (buildup > 0 && Math.random() < buildup * 0.35) {
                        this.updateCountdownTicks++;
                    }
                }
            }
        }
    }
}
