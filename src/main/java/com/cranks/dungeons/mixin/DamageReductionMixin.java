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
    private float cranks$applyElementalResistances(float amount, ServerWorld world, DamageSource source) {
        if (cranks$processingResistance.get()) {
            return amount;
        }

        try {
            cranks$processingResistance.set(true);
            LivingEntity entity = (LivingEntity) (Object) this;

            // If the entity already has our custom BURN ailment, we disable the vanilla "On Fire" DOT
            // to avoid double-ticking damage. 
            // However, we let it through if there's no BURN ailment (e.g. Zombies in the sun)
            // so they still take fire damage and show damage numbers.
            if (source.isOf(DamageTypes.ON_FIRE) && AilmentManager.hasAilment(entity, AilmentType.BURN)) {
                return 0;
            }

            DamageConversionHelper.DamageBreakdown breakdown = DamageConversionHelper.getBreakdown(source, amount, entity);
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

            return breakdown.getTotal();
        } finally {
            cranks$processingResistance.set(false);
        }
    }
}
