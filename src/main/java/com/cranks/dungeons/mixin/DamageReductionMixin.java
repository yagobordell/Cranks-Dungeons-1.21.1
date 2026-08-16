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

@Mixin(value = LivingEntity.class, priority = 800)
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

            float totalDamage = breakdown.getTotal();
            float shockMagnitude = AilmentManager.getShockMagnitude(entity);
            if (shockMagnitude > 0) {
                shockMagnitude = Math.min(shockMagnitude, 0.50f);
                totalDamage *= (1.0f + shockMagnitude);
                breakdown.scale(1.0f + shockMagnitude);
            }

            return totalDamage;
        } finally {
            cranks$processingResistance.set(false);
        }
    }
}
