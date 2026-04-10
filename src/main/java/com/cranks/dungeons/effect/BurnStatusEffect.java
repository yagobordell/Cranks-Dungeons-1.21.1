package com.cranks.dungeons.effect;

import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

public class BurnStatusEffect extends StatusEffect {
    public BurnStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFF4500);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        // Play looping burn sound every 15 ticks (approx 0.75 seconds)
        // A slightly faster loop than shock for a more intense "burning" feel
        if (entity.age % 15 == 0) {
            world.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                ModSounds.BURN_LOOP,
                SoundCategory.NEUTRAL,
                0.8f, // Standardized volume to match other ailments
                0.9f + (world.random.nextFloat() * 0.2f) // Pitch variation
            );
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
