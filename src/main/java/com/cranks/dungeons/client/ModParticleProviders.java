package com.cranks.dungeons.client;

import com.cranks.dungeons.particle.ModParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.EndRodParticle;

public class ModParticleProviders {
    
    public static void register() {
        // Use EndRodParticle which floats upward
        ParticleFactoryRegistry.getInstance().register(ModParticles.POISON_BUBBLE, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.POISON_DROPLET, EndRodParticle.Factory::new);
        
        // Register shock particles with end rod effect (electric spark-like)
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_1, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_2, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_3, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_4, EndRodParticle.Factory::new);
        
        // Register shock ambient particles (3 variants) with a QuickFade factory
        // Duration set to 8 ticks (~0.4s) for a snappier look
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_AMBIENT_1, createQuickFadeFactory(EndRodParticle.Factory::new, 8));
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_AMBIENT_2, createQuickFadeFactory(EndRodParticle.Factory::new, 8));
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_AMBIENT_3, createQuickFadeFactory(EndRodParticle.Factory::new, 8));

        // Register bleed splash particles (4 variants) with a BloodSplash factory
        ParticleFactoryRegistry.getInstance().register(ModParticles.BLEED_SPLASH_1, createBloodSplashFactory(EndRodParticle.Factory::new));
        ParticleFactoryRegistry.getInstance().register(ModParticles.BLEED_SPLASH_2, createBloodSplashFactory(EndRodParticle.Factory::new));
        ParticleFactoryRegistry.getInstance().register(ModParticles.BLEED_SPLASH_3, createBloodSplashFactory(EndRodParticle.Factory::new));
        ParticleFactoryRegistry.getInstance().register(ModParticles.BLEED_SPLASH_4, createBloodSplashFactory(EndRodParticle.Factory::new));
    }

    private static <T extends net.minecraft.particle.ParticleEffect> net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory<T> createQuickFadeFactory(net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory<T> baseFactory, int duration) {
        return (spriteProvider) -> {
            net.minecraft.client.particle.ParticleFactory<T> factory = baseFactory.create(spriteProvider);
            return (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> {
                net.minecraft.client.particle.Particle particle = factory.createParticle(parameters, world, x, y, z, velocityX, velocityY, velocityZ);
                if (particle != null) {
                    particle.setMaxAge(duration);
                }
                return particle;
            };
        };
    }

    private static <T extends net.minecraft.particle.ParticleEffect> net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory<T> createBloodSplashFactory(net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory<T> baseFactory) {
        return (spriteProvider) -> {
            net.minecraft.client.particle.ParticleFactory<T> factory = baseFactory.create(spriteProvider);
            return (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> {
                net.minecraft.client.particle.Particle particle = factory.createParticle(parameters, world, x, y, z, velocityX, velocityY, velocityZ);
                if (particle != null) {
                    // Access wideners allow direct field access
                    particle.gravityStrength = 1.0f; // High gravity for blood
                    particle.maxAge = 15 + world.getRandom().nextInt(10); // Slightly longer life
                    particle.scale(1.5f); // Make them more visible
                }
                return particle;
            };
        };
    }
}
