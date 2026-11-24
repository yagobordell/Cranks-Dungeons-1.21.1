package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.util.DamageHit;
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
        if (amount <= 0 || DamageHit.current() == null || cranks$processingResistance.get()) {
            return amount;
        }

        try {
            cranks$processingResistance.set(true);
            LivingEntity entity = (LivingEntity) (Object) this;
            net.minecraft.world.World world = entity.getWorld();

            var secondary = AilmentManager.secondaryHit(entity);
            if (secondary != null) {
                var resolved = new DamageConversionHelper.DamageBreakdown();
                resolved.set(secondary.element(), amount);
                if (!secondary.ailment()) {
                    resolved.applyResistancesInPlace(entity, source.getAttacker());
                    resolved.scale(1 + AilmentManager.getShockMagnitude(entity));
                }
                DamageHit.forTarget(entity).setBreakdown(resolved);
                return resolved.getTotal();
            }
            // All fire damage is unified under AilmentType.BURN.
            // When environmental fire hits (lava, fire, campfire, on_fire),
            // apply or refresh the Burn ailment and suppress vanilla duplicate damage.
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.LAVA)) {
                if (!world.isClient()) {
                    com.cranks.dungeons.ailment.AilmentManager.applyAilment(entity, com.cranks.dungeons.ailment.AilmentType.BURN, 8.0f, 4.0, null, null, null, 0);
                }
                return 0;
            } else if (source.isOf(net.minecraft.entity.damage.DamageTypes.CAMPFIRE) || source.isOf(net.minecraft.entity.damage.DamageTypes.IN_FIRE)) {
                if (!world.isClient()) {
                    com.cranks.dungeons.ailment.AilmentManager.applyAilment(entity, com.cranks.dungeons.ailment.AilmentType.BURN, 3.0f, 1.0, null, null, null, 0);
                }
                return 0;
            } else if (source.isOf(net.minecraft.entity.damage.DamageTypes.ON_FIRE)) {
                return 0;
            }

            // Magma blocks deal 3 direct fire damage without applying the Burn ailment
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.HOT_FLOOR)) {
                amount = 3.0f;
            }

            if (source.isOf(DamageTypes.DROWN) && entity instanceof net.minecraft.entity.player.PlayerEntity) {
                amount = Math.max(entity.getMaxHealth() * 0.12f, 2.0f);
            }

            DamageConversionHelper.DamageBreakdown breakdown = DamageConversionHelper.getBreakdown(source, amount, entity);
            if (source.getSource() instanceof com.cranks.dungeons.ranged.RangedProjectile projectile && projectile.cranks$rangedDamage() > 0) {
                breakdown.scale(projectile.cranks$rangedPower());
            }
            if (entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.KEYSTONE_VOID_INOCULATION) > 0) {
                breakdown.set(DamageElement.VOID, 0f);
            }
            DamageHit.forTarget(entity).beforeMitigation(breakdown);
            breakdown.applyResistancesInPlace(entity, com.cranks.dungeons.effect.PotionRules.isPotionDamage(source)
                    ? null : source.getAttacker());
            DamageHit hit = DamageHit.forTarget(entity);
            if (hit != null) hit.setBreakdown(breakdown);

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
