package com.cranks.dungeons.client.render;

import com.cranks.dungeons.particle.ModParticles;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public class AilmentParticleManager {

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null || client.isPaused()) return;

        // Only visit entities that actually have synchronized ailments.
        for (int entityId : AilmentIconRenderer.getTrackedEntityIds()) {
            Entity entity = client.world.getEntityById(entityId);
            if (entity == null) continue;
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isInvisibleTo(client.player)) continue;

            // Only spawn particles for entities within reasonable distance
            if (living.squaredDistanceTo(client.player) > 64 * 64) continue;

            var activeAilments = AilmentIconRenderer.getAilments(living.getId());
            if (activeAilments.isEmpty()) continue;

            for (var entry : activeAilments) {
                String path = entry.texture().getPath();
                if (path.contains("burn")) {
                    spawnBurnParticles(client, living);
                } else if (path.contains("poison")) {
                    spawnPoisonParticles(client, living);
                } else if (path.contains("freeze")) {
                    spawnFreezeParticles(client, living);
                } else if (path.contains("bleed")) {
                    spawnBleedParticles(client, living);
                } else if (path.contains("shock")) {
                    spawnShockParticles(client, living);
                }
            }
        }
    }

    public static void onEntityHit(LivingEntity entity) {
        var activeAilments = AilmentIconRenderer.getAilments(entity.getId());
        for (var entry : activeAilments) {
            String path = entry.texture().getPath();
            if (path.contains("bleed")) {
                spawnBleedSplash(entity);
                break;
            }
        }
    }

    private static void spawnBleedSplash(LivingEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // Significantly increased count for a much bloodier hit
        int count = 20 + entity.getRandom().nextInt(15);
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getHeight();
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();

            var particleType = switch (entity.getRandom().nextInt(4)) {
                case 0 -> ModParticles.BLEED_SPLASH_1;
                case 1 -> ModParticles.BLEED_SPLASH_2;
                case 2 -> ModParticles.BLEED_SPLASH_3;
                default -> ModParticles.BLEED_SPLASH_4;
            };

            // Burst velocity: slightly outward and upward, but gravity will pull them down fast
            double vx = (entity.getRandom().nextDouble() - 0.5) * 0.45;
            double vy = entity.getRandom().nextDouble() * 0.3;
            double vz = (entity.getRandom().nextDouble() - 0.5) * 0.45;

            client.world.addParticle((net.minecraft.particle.ParticleEffect) particleType, x, y, z, vx, vy, vz);
        }
    }

    private static void spawnBurnParticles(MinecraftClient client, LivingEntity entity) {
        // Fire bursts: random small explosions of fire from the entity
        if (entity.getRandom().nextFloat() < 0.25f) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getHeight();
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();

            // Burst effect: several particles from one point
            int burstCount = 2 + entity.getRandom().nextInt(3);
            for (int i = 0; i < burstCount; i++) {
                client.world.addParticle((net.minecraft.particle.ParticleEffect) ParticleTypes.FLAME, x, y, z, 
                    (entity.getRandom().nextDouble() - 0.5) * 0.1, 
                    entity.getRandom().nextDouble() * 0.1, 
                    (entity.getRandom().nextDouble() - 0.5) * 0.1);
            }
        }
        
        // Occasional smoke
        if (entity.getRandom().nextFloat() < 0.1f) {
            client.world.addParticle((net.minecraft.particle.ParticleEffect) ParticleTypes.LARGE_SMOKE, 
                entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth(),
                entity.getY() + entity.getHeight(),
                entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth(),
                0d, 0.02d, 0d);
        }
    }

    private static void spawnPoisonParticles(MinecraftClient client, LivingEntity entity) {
        // Floating bubbles rising slowly
        if (entity.getRandom().nextFloat() < 0.3f) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getHeight();
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth();
            client.world.addParticle((net.minecraft.particle.ParticleEffect) ModParticles.POISON_BUBBLE, x, y, z, 0d, 0.02d, 0d);
        }

        // Falling droplets
        if (entity.getRandom().nextFloat() < 0.15f) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 0.8;
            double y = entity.getY() + entity.getHeight() * 0.8;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 0.8;
            client.world.addParticle((net.minecraft.particle.ParticleEffect) ModParticles.POISON_DROPLET, x, y, z, 0d, -0.05d, 0d);
        }
    }

    private static void spawnFreezeParticles(MinecraftClient client, LivingEntity entity) {
        // Frost crystals appearing on the surface
        if (entity.getRandom().nextFloat() < 0.4f) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 1.1;
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getHeight();
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 1.1;
            
            // Snowflakes falling very slowly
            client.world.addParticle((net.minecraft.particle.ParticleEffect) ParticleTypes.SNOWFLAKE, x, y, z, 0d, -0.01d, 0d);
            
            // "Frost" vapor
            if (entity.getRandom().nextFloat() < 0.2f) {
                client.world.addParticle((net.minecraft.particle.ParticleEffect) ParticleTypes.CLOUD, x, y, z, 0d, 0d, 0d);
            }
        }
    }

    private static void spawnBleedParticles(MinecraftClient client, LivingEntity entity) {
        // Drip logic - spawn particles that fall downwards
        // Restore RAID_OMEN for idle drips as requested
        float moveAlpha = (float) entity.getVelocity().horizontalLength();
        float frequency = 0.2f + (moveAlpha * 2.0f);
        
        if (entity.getRandom().nextFloat() < frequency) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 0.6;
            double y = entity.getY() + entity.getHeight() * 0.4 + entity.getRandom().nextDouble() * 0.4;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 0.6;
            
            client.world.addParticle((net.minecraft.particle.ParticleEffect) ParticleTypes.RAID_OMEN, x, y, z, 0d, -0.1d, 0d);
        }
    }

    private static void spawnShockParticles(MinecraftClient client, LivingEntity entity) {
        // Rapid electric bursts: quick pops that fade fast
        // Pulse every 30 ticks (1.5s) for a snappier feel
        if (entity.age % 30 == 0) {
            // Play ambient shock sound
            client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), 
                ModSounds.SHOCK_AMBIENT, SoundCategory.HOSTILE, 0.6f, 1.0f, false);

            int sparkCount = 4 + entity.getRandom().nextInt(5);
            for (int i = 0; i < sparkCount; i++) {
                double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 1.1;
                double y = entity.getY() + entity.getRandom().nextDouble() * entity.getHeight();
                double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getWidth() * 1.1;
                
                int variant = entity.getRandom().nextInt(3);
                var particle = switch(variant) {
                    case 0 -> ModParticles.SHOCK_AMBIENT_1;
                    case 1 -> ModParticles.SHOCK_AMBIENT_2;
                    default -> ModParticles.SHOCK_AMBIENT_3;
                };
                
                // Higher velocity makes them "pop out" and disperse
                client.world.addParticle((net.minecraft.particle.ParticleEffect) particle, x, y, z, 
                    (entity.getRandom().nextDouble() - 0.5) * 0.25d, 
                    (entity.getRandom().nextDouble() - 0.5) * 0.25d, 
                    (entity.getRandom().nextDouble() - 0.5) * 0.25d);
            }
        }
    }
}
