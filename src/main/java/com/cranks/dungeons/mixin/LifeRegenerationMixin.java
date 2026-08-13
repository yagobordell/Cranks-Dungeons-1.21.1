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

        if (!entity.isAlive()) {
            return;
        }

        // Vaal Pact prevents Life Regeneration
        if (entity.getAttributeValue(ModAttributes.KEYSTONE_VAAL_PACT) > 0) {
            return;
        }

        double flatLifeRegen = entity.getAttributeValue(ModAttributes.LIFE_REGENERATION);
        double percentLifeRegen = entity.getAttributeValue(ModAttributes.MAX_HEALTH_REGENERATION_PERCENT);
        
        double lifeRegen = flatLifeRegen + (entity.getMaxHealth() * percentLifeRegen);
        
        if (lifeRegen > 0) {
            float healAmount = (float) (lifeRegen / 20.0);

            // Zealot's Oath applies Life Regeneration to Energy Shield instead
            if (entity.getAttributeValue(ModAttributes.KEYSTONE_ZEALOTS_OATH) > 0) {
                if (entity instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
                    float currentES = provider.cranksdungeons$getCurrentEnergyShield();
                    float maxES = (float) entity.getAttributeValue(ModAttributes.ENERGY_SHIELD);
                    if (currentES < maxES) {
                        float newES = Math.min(maxES, currentES + healAmount);
                        provider.cranksdungeons$setCurrentEnergyShield(newES);
                        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player && player.networkHandler != null) {
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                                    new com.cranks.dungeons.network.EnergyShieldSyncPacket(newES, maxES));
                        }
                    }
                }
            } else {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.heal(healAmount);
                    if (entity.age % 20 == 0) {
                        if (entity.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                            com.cranks.dungeons.network.DamageNumberSender.broadcastHeal(serverWorld, entity, lifeRegen);
                        }
                    }
                }
            }
        }
    }
}