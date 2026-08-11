package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class DamageReductionMixin {

    @Unique
    private static final ThreadLocal<Boolean> cranks$processingResistance = ThreadLocal.withInitial(() -> false);

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float cranks$applyElementalResistances(float amount, DamageSource source) {
        if (cranks$processingResistance.get()) {
            return amount;
        }

        try {
            cranks$processingResistance.set(true);
            LivingEntity entity = (LivingEntity) (Object) this;
            net.minecraft.world.World world = entity.getWorld();

            // If the entity already has our custom BURN ailment, we disable the vanilla "On Fire" DOT
            // to avoid double-ticking damage. 
            // However, we let it through if there's no BURN ailment (e.g. Zombies in the sun)
            // so they still take fire damage and show damage numbers.
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.ON_FIRE) && 
                com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BURN)) {
                return 0;
            }

            DamageConversionHelper.DamageBreakdown breakdown = DamageConversionHelper.getBreakdown(source, amount, entity);
            if (entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.KEYSTONE_VOID_INOCULATION) > 0) {
                breakdown.set(DamageElement.VOID, 0f);
            }
            breakdown.applyResistancesInPlace(entity, source.getAttacker());
            if (entity instanceof DamageBreakdownProvider provider) {
                provider.cranks$setLastDamageBreakdown(breakdown);
            }

            // Chance to apply custom BURN from fire sources (except direct DOT which we handled above)
            float fireDamage = breakdown.get(DamageElement.FIRE);
            if (fireDamage > 0 && !world.isClient()) {
                // Remove chance to burn from environmental sources per user request
                boolean isEnvironmental = source.isOf(DamageTypes.IN_FIRE) || 
                                         source.isOf(DamageTypes.LAVA) || 
                                         source.isOf(DamageTypes.HOT_FLOOR) ||
                                         source.isOf(DamageTypes.ON_FIRE); // Sun/Vanilla fire DOT doesn't apply the status effect
                
                if (!isEnvironmental && Math.random() < 0.20) {
                    AilmentManager.applyAilment(entity, AilmentType.BURN, fireDamage * 0.4f, AilmentType.BURN.getBaseDuration(), source.getAttacker());
                }
            }

            float totalDamage = breakdown.getTotal();
            float shockMagnitude = AilmentManager.getShockMagnitude(entity);
            if (shockMagnitude > 0) {
                totalDamage *= (1.0f + shockMagnitude);
                breakdown.scale(1.0f + shockMagnitude);
            }

            // Energy Shield Mitigation
            if (entity instanceof com.cranks.dungeons.EnergyShieldProvider esProvider) {
                float currentShield = esProvider.cranksdungeons$getCurrentEnergyShield();
                long currentTime = world.getTime();
                long lastDamageTime = esProvider.cranksdungeons$getLastDamageTime();
                boolean isSameHit = (currentTime == lastDamageTime);
                boolean onCooldown = (currentTime - lastDamageTime < 10L);

                if (currentShield > 0 && (isSameHit || !onCooldown)) {
                    float weightedDamage = totalDamage + breakdown.get(DamageElement.VOID); // Void deals 2x to shields
                    
                    if (currentShield >= weightedDamage) {
                        float newShield = currentShield - weightedDamage;
                        esProvider.cranksdungeons$setCurrentEnergyShield(newShield);
                        esProvider.cranksdungeons$setLastDamageTime(currentTime);
                        
                        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                            float maxShield = (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                    serverPlayer,
                                    new com.cranks.dungeons.network.EnergyShieldSyncPacket(newShield, maxShield));
                        }
                        return 0.001f; // Damage fully absorbed by shield
                    } else {
                        float remainingWeighted = weightedDamage - currentShield;
                        esProvider.cranksdungeons$setCurrentEnergyShield(0);
                        esProvider.cranksdungeons$setLastDamageTime(currentTime);
                        
                        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                            float maxShield = (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                    serverPlayer,
                                    new com.cranks.dungeons.network.EnergyShieldSyncPacket(0, maxShield));
                        }
                        
                        float mitigationRatio = remainingWeighted / weightedDamage;
                        breakdown.scale(mitigationRatio);
                        totalDamage = breakdown.getTotal();
                    }
                }
            }

            return totalDamage;
        } finally {
            cranks$processingResistance.set(false);
        }
    }
}
