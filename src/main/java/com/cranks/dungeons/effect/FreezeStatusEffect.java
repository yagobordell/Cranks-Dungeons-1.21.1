package com.cranks.dungeons.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public class FreezeStatusEffect extends StatusEffect {
    public FreezeStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x00FFFF);
    }
    
    @Override
    public ParticleEffect createParticle(StatusEffectInstance effect) {
        // Return item snowball particle
        return ParticleTypes.ITEM_SNOWBALL;
    }
}