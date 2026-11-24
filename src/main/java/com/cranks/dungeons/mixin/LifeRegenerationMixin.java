package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LifeRegenerationMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyLifeRegeneration(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getWorld().isClient() || !entity.isAlive()) {
            return;
        }

        // Vaal Pact prevents Life Regeneration
        if (entity.getAttributeValue(ModAttributes.KEYSTONE_VAAL_PACT) > 0) {
            return;
        }

        double flatLifeRegen = entity.getAttributeValue(ModAttributes.LIFE_REGENERATION);
        double percentLifeRegen = entity.getAttributeValue(ModAttributes.MAX_HEALTH_REGENERATION_PERCENT);
        

        double movingRegenBonus = entity.getAttributeValue(ModAttributes.REGENERATION_RATE_WHILE_MOVING_PERCENT);
        boolean moving = entity.getVelocity().horizontalLengthSquared() > 0.0001
                || Math.abs(entity.getX() - entity.prevX) > 0.001 || Math.abs(entity.getZ() - entity.prevZ) > 0.001;
        double lifeRegen = com.cranks.dungeons.stat.CombatStatRules.healthRegeneration(entity.getMaxHealth(),
                flatLifeRegen, percentLifeRegen, moving ? movingRegenBonus : 0);
        
        if (lifeRegen > 0) {
            float healAmount = (float) (lifeRegen / 20.0);

            // Zealot's Oath applies Life Regeneration to Energy Shield instead
            if (entity.getAttributeValue(ModAttributes.KEYSTONE_ZEALOTS_OATH) > 0) {
                com.cranks.dungeons.util.LeechManager.addShield(entity, healAmount);
            } else {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    com.cranks.dungeons.damage.HealingNumberContext.gradual(() -> entity.heal(healAmount));
                }
            }
        }
    }
}
