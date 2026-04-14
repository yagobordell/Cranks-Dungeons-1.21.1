package com.cranks.dungeons.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.cranks.dungeons.CranksDungeons;

public class ModParticles {
    
    public static final SimpleParticleType POISON_BUBBLE = FabricParticleTypes.simple();
    public static final SimpleParticleType POISON_DROPLET = FabricParticleTypes.simple();
    
    // Shock particles
    public static final SimpleParticleType SHOCK_SPARK_1 = FabricParticleTypes.simple();
    public static final SimpleParticleType SHOCK_SPARK_2 = FabricParticleTypes.simple();
    public static final SimpleParticleType SHOCK_SPARK_3 = FabricParticleTypes.simple();
    public static final SimpleParticleType SHOCK_SPARK_4 = FabricParticleTypes.simple();
    
    // Shock ambient particles for the ailment (3 variants)
    public static final SimpleParticleType SHOCK_AMBIENT_1 = FabricParticleTypes.simple();
    public static final SimpleParticleType SHOCK_AMBIENT_2 = FabricParticleTypes.simple();
    public static final SimpleParticleType SHOCK_AMBIENT_3 = FabricParticleTypes.simple();

    // Bleed splash particles (4 variants)
    public static final SimpleParticleType BLEED_SPLASH_1 = FabricParticleTypes.simple();
    public static final SimpleParticleType BLEED_SPLASH_2 = FabricParticleTypes.simple();
    public static final SimpleParticleType BLEED_SPLASH_3 = FabricParticleTypes.simple();
    public static final SimpleParticleType BLEED_SPLASH_4 = FabricParticleTypes.simple();
    
    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "poison_bubble"), POISON_BUBBLE);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "poison_droplet"), POISON_DROPLET);
        
        // Register shock particles
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_spark_1"), SHOCK_SPARK_1);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_spark_2"), SHOCK_SPARK_2);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_spark_3"), SHOCK_SPARK_3);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_spark_4"), SHOCK_SPARK_4);
        
        // Register shock ambient particles (3 variants)
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_ambient_1"), SHOCK_AMBIENT_1);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_ambient_2"), SHOCK_AMBIENT_2);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "shock_ambient_3"), SHOCK_AMBIENT_3);

        // Register bleed splash particles (4 variants)
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "bleed_splash_1"), BLEED_SPLASH_1);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "bleed_splash_2"), BLEED_SPLASH_2);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "bleed_splash_3"), BLEED_SPLASH_3);
        Registry.register(Registries.PARTICLE_TYPE, 
            Identifier.of(CranksDungeons.MOD_ID, "bleed_splash_4"), BLEED_SPLASH_4);
    }
}
