package com.cranks.dungeons.effect;

import com.cranks.dungeons.particle.ModParticles;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

public class ShockStatusEffect extends StatusEffect {
    public ShockStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFD700);
    }
    
    @Override
    public ParticleEffect createParticle(StatusEffectInstance effect) {
        // Randomly return one of 3 shock ambient particles
        int variant = effect.getAmplifier() % 3; // Use amplifier to vary particles
        return switch (variant) {
            case 0 -> ModParticles.SHOCK_AMBIENT_1;
            case 1 -> ModParticles.SHOCK_AMBIENT_2;
            case 2 -> ModParticles.SHOCK_AMBIENT_3;
            default -> ModParticles.SHOCK_AMBIENT_1;
        };
    }
    
    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        // Play looping shock sound every second (20 ticks), alternating between two sounds
        if (entity.age % 20 == 0) {
            // Alternate between SHOCK_LOOP and SHOCK_LOOP_2 every second
            boolean useSecondSound = (entity.age / 20) % 2 == 1;
            var sound = useSecondSound ? ModSounds.SHOCK_LOOP_2 : ModSounds.SHOCK_LOOP;
            
            world.playSound(
                null, // player - null means all players can hear it
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                sound,
                SoundCategory.NEUTRAL,
                0.3f, // volume
                1.0f + (world.random.nextFloat() * 0.2f - 0.1f) // pitch with slight variation
            );
        }
        return true; // Return true to indicate the effect should continue
    }
    
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Always apply the update effect
    }
}
