package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

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
    private void tickAilments(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AilmentManager.tickAilments(entity);
        spawnAilmentParticles(entity);
    }

    private void spawnAilmentParticles(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;
        if (entity.getRandom().nextFloat() > 0.3f) return;

        ServerWorld world = (ServerWorld) entity.getEntityWorld();
        
        if (AilmentManager.hasAilment(entity, AilmentType.BURN)) {
            double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.5;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.getRandom().nextDouble() - 0.5) * 0.3;
            double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.5;
            world.spawnParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.05, 0, 0.02);
        }
    }
}