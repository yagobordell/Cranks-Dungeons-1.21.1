package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.particle.ModParticles;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import com.cranks.dungeons.network.DamageNumberSender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DamageBreakdownProvider, AilmentHistoryProvider {

    @Unique
    private DamageConversionHelper.DamageBreakdown cranks$lastDamageBreakdown;

    @Unique
    private float cranks$accumulatedHeal = 0;
    @Unique
    private int cranks$healTimer = 0;

    @Unique
    private final Set<UUID> cranks$recentBurnChains = new HashSet<>();

    @Override
    public DamageConversionHelper.DamageBreakdown cranks$getLastDamageBreakdown() {
        return cranks$lastDamageBreakdown;
    }

    @Override
    public void cranks$setLastDamageBreakdown(DamageConversionHelper.DamageBreakdown breakdown) {
        this.cranks$lastDamageBreakdown = breakdown;
    }

    @Override
    public void cranks$recordBurnChain(UUID chainUuid) {
        if (chainUuid != null) {
            cranks$recentBurnChains.add(chainUuid);
        }
    }

    @Override
    public boolean cranks$wasHitByChain(UUID chainUuid) {
        return chainUuid != null && cranks$recentBurnChains.contains(chainUuid);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        AilmentManager.onEntityDeath((LivingEntity) (Object) this);
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void onHeal(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient() || amount <= 0) return;

        float actualHeal = Math.min(amount, entity.getMaxHealth() - entity.getHealth());
        if (actualHeal <= 0) return;

        cranks$accumulatedHeal += actualHeal;
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
                .add(ModAttributes.ENERGY_SHIELD)
                .add(ModAttributes.EVASION_RATING)

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
                .add(ModAttributes.CHANCE_TO_POISON)
                .add(ModAttributes.POISON_BASE_DAMAGE)
                .add(ModAttributes.POISON_DAMAGE_PERCENT)
                .add(ModAttributes.CHANCE_TO_FREEZE)
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
                .add(ModAttributes.FLYING_SPEED)
                .add(ModAttributes.LUCK)
                .add(ModAttributes.CROP_FORTUNE)
                .add(ModAttributes.LOG_FORTUNE)
                .add(ModAttributes.DIG_FORTUNE)
                .add(ModAttributes.CROP_EXPERIENCE)

                // Percentage damage
                .add(ModAttributes.ATTACK_DAMAGE_PERCENT)
                .add(ModAttributes.FIRE_DAMAGE_PERCENT)
                .add(ModAttributes.COLD_DAMAGE_PERCENT)
                .add(ModAttributes.LIGHTNING_DAMAGE_PERCENT)
                .add(ModAttributes.VOID_DAMAGE_PERCENT)
                .add(ModAttributes.RELOAD_SPEED);

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
            if (entity.getRandom().nextFloat() > 0.5f) return;
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.7;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.getRandom().nextDouble() - 0.5) * 0.5;
            double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.7;
            world.spawnParticles(ParticleTypes.FLAME, x, y, z, 3, 0.1, 0.1, 0.1, 0.05);
        }

        if (AilmentManager.hasAilment(entity, AilmentType.POISON)) {
            if (entity.getRandom().nextFloat() > 0.4f) return;
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.6;
            double y = entity.getY() + entity.getHeight() * 0.75 + (world.getRandom().nextDouble() - 0.5) * 0.4;
            double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.6;
            world.spawnParticles(ModParticles.POISON_BUBBLE, x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
        }

        if (AilmentManager.hasAilment(entity, AilmentType.FREEZE)) {
            if (entity.getRandom().nextFloat() > 0.3f) return;
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.8;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.getRandom().nextDouble() - 0.5) * 0.6;
            double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.8;
            
            // Breaking ice crystals around the frozen entity
            world.spawnParticles(ParticleTypes.ITEM_SNOWBALL, x, y, z, 2, 0.15, 0.15, 0.15, 0.05);
            
            // Falling snow effect near feet occasionally
            if (entity.getRandom().nextFloat() < 0.3f) {
                double snowX = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.5;
                double snowY = entity.getY() + 0.1;
                double snowZ = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.5;
                world.spawnParticles(ParticleTypes.SNOWFLAKE, snowX, snowY, snowZ, 1, 0.05, 0.02, 0.05, 0.01);
            }
        }

        if (AilmentManager.hasAilment(entity, AilmentType.BLEED)) {
            if (entity.getRandom().nextFloat() > 0.4f) return;
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * 0.7;
            double y = entity.getY() + entity.getHeight() * 0.5 + (world.getRandom().nextDouble() - 0.5) * 0.5;
            double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.7;
            world.spawnParticles(ParticleTypes.RAID_OMEN, x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
        }

        if (AilmentManager.hasAilment(entity, AilmentType.SHOCK)) {
            // Electric pulse effect: spawn multiple particles every 3 seconds (60 ticks)
            // This gives particles time to disappear before next spawn
            if (entity.age % 60 == 0) {
                ServerWorld world = (ServerWorld) entity.getEntityWorld();
                // Play ambient shock sound
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                    ModSounds.SHOCK_AMBIENT, SoundCategory.HOSTILE, 0.6f, 1.0f);
                // Spawn 4-6 particles in a burst around the entity
                int particleCount = 4 + entity.getRandom().nextInt(3);
                for (int i = 0; i < particleCount; i++) {
                    double x = entity.getX() + (world.getRandom().nextDouble() - 0.5) * entity.getWidth() * 0.5;
                    double y = entity.getY() + entity.getHeight() * 0.4 + world.getRandom().nextDouble() * entity.getHeight() * 0.4;
                    double z = entity.getZ() + (world.getRandom().nextDouble() - 0.5) * entity.getWidth() * 0.5;
                    // Randomly choose one of 3 shock ambient particles
                    int particleVariant = entity.getRandom().nextInt(3);
                    ParticleEffect particle = switch (particleVariant) {
                        case 0 -> ModParticles.SHOCK_AMBIENT_1;
                        case 1 -> ModParticles.SHOCK_AMBIENT_2;
                        case 2 -> ModParticles.SHOCK_AMBIENT_3;
                        default -> ModParticles.SHOCK_AMBIENT_1;
                    };
                    // Lower velocity spread keeps particles closer to entity
                    // Speed 0.05 for slower movement
                    world.spawnParticles(particle, x, y, z, 1, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }
    }
}
