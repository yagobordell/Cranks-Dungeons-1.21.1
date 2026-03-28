package com.cranks.dungeons.effect;

import com.cranks.dungeons.particle.ModParticles;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;

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
}
