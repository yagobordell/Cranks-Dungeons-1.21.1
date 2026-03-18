package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import com.cranks.dungeons.network.DamageNumberSender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DamageBreakdownProvider {

    @Unique
    private DamageConversionHelper.DamageBreakdown cranks$lastDamageBreakdown;

    @Unique
    private float cranks$accumulatedHeal = 0;
    @Unique
    private int cranks$healTimer = 0;

    @Override
    public DamageConversionHelper.DamageBreakdown cranks$getLastDamageBreakdown() {
        return cranks$lastDamageBreakdown;
    }

    @Override
    public void cranks$setLastDamageBreakdown(DamageConversionHelper.DamageBreakdown breakdown) {
        this.cranks$lastDamageBreakdown = breakdown;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        AilmentManager.onEntityDeath((LivingEntity) (Object) this);
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void onHeal(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient() || amount <= 0) return;

        // Calculate actual health gained
        float actualHeal = Math.min(amount, entity.getMaxHealth() - entity.getHealth());
        if (actualHeal <= 0) return;

        cranks$accumulatedHeal += actualHeal;
        // We no longer broadcast immediately in heal() to avoid multiple numbers per tick
        // Accumulation will be handled in tickLivingEntity()
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"), cancellable = true)
    private static void addCustomAttributesToAllEntities(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();

        builder
                // Defensive
                .add(ModAttributes.FIRE_RESISTANCE)
                .add(ModAttributes.COLD_RESISTANCE)
                .add(ModAttributes.LIGHTNING_RESISTANCE)
                .add(ModAttributes.VOID_RESISTANCE)
                .add(ModAttributes.KNOCKBACK_RESISTANCE)
                .add(ModAttributes.BONUS_ARMOR)
                .add(ModAttributes.BONUS_ARMOR_TOUGHNESS)
                .add(ModAttributes.LIFE_REGENERATION)
                .add(ModAttributes.MAX_HEALTH_BONUS)
                .add(ModAttributes.FEATHER_FALLING)

                // Offensive
                .add(ModAttributes.CRIT_CHANCE)
                .add(ModAttributes.BONUS_ATTACK_DAMAGE)
                .add(ModAttributes.FIRE_DAMAGE)
                .add(ModAttributes.COLD_DAMAGE)
                .add(ModAttributes.LIGHTNING_DAMAGE)
                .add(ModAttributes.VOID_DAMAGE)
                .add(ModAttributes.LIFE_STEAL)
                .add(ModAttributes.CHANCE_TO_BURN)
                .add(ModAttributes.BURN_BASE_DAMAGE)
                .add(ModAttributes.BURN_DAMAGE_PERCENT)
                .add(ModAttributes.ATTACK_RANGE)
                .add(ModAttributes.ATTACK_SPEED)
                .add(ModAttributes.KNOCKBACK)

                // Utility
                .add(ModAttributes.MINING_EFFICIENCY)
                .add(ModAttributes.FORTUNE)
                .add(ModAttributes.DURABILITY_BONUS)
                .add(ModAttributes.EXPERIENCE_BONUS)
                .add(ModAttributes.PRECISION_MINING)
                .add(ModAttributes.BREAKING_RANGE)
                .add(ModAttributes.MOVEMENT_SPEED)
                .add(ModAttributes.LUCK);

        cir.setReturnValue(builder);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickLivingEntity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient()) return;

        AilmentManager.tickAilments(entity);
        spawnAilmentParticles(entity);

        // Show accumulated healing
        if (cranks$accumulatedHeal > 0) {
            cranks$healTimer++;
            
            // If it's a "burst" of healing (like life steal from multiple hits) or enough time passed
            if (cranks$accumulatedHeal >= 1.0f || cranks$healTimer >= 10) {
                DamageNumberSender.broadcastHeal((ServerWorld) entity.getEntityWorld(), entity, (double)Math.round(cranks$accumulatedHeal * 10.0) / 10.0);
                cranks$accumulatedHeal = 0;
                cranks$healTimer = 0;
            }
        }
    }

    @Unique
    private void spawnAilmentParticles(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;
        
        if (AilmentManager.hasAilment(entity, AilmentType.BURN)) {
            // We no longer set fire ticks here to avoid vanilla fire damage and overlay
            
            if (entity.getRandom().nextFloat() > 0.5f) return;
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.7;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.getRandom().nextDouble() - 0.5) * 0.5;
            double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.7;
            world.spawnParticles(ParticleTypes.FLAME, x, y, z, 3, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
