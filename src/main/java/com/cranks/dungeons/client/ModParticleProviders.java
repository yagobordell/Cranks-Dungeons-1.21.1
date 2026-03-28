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
        
        // Register shock ambient particles (3 variants) with EndRodParticle
        // Note: To make them last shorter, we spawn more frequently with lower velocity in LivingEntityMixin
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_AMBIENT_1, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_AMBIENT_2, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_AMBIENT_3, EndRodParticle.Factory::new);
    }
}
