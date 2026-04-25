package com.cranks.dungeons.client;

import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class ReactionVisualHelper {
    private static final Random RANDOM = new Random();

    public static void playReaction(MinecraftClient client, BlockPos pos, String type) {
        ClientWorld world = client.world;
        if (world == null) return;

        switch (type) {
            case "shatter" -> {
                spawnShatterParticles(world, pos);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.RUNIC_ALTAR_CLICK, SoundCategory.PLAYERS, 1.0f, 1.5f);
            }
            case "cauterize" -> {
                spawnCauterizeParticles(world, pos);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.ELEMENTAL_FIRE_HIT, SoundCategory.PLAYERS, 1.2f, 0.8f);
            }
            case "thermal_shock" -> {
                spawnThermalShockParticles(world, pos);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.ELEMENTAL_COLD_HIT, SoundCategory.PLAYERS, 1.2f, 1.2f);
            }
            case "supercharge" -> {
                spawnSuperchargeParticles(world, pos);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.ELEMENTAL_LIGHTNING_HIT, SoundCategory.PLAYERS, 1.5f, 0.7f);
            }
            case "miasma" -> {
                spawnMiasmaParticles(world, pos);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.ELEMENTAL_VOID_HIT, SoundCategory.PLAYERS, 1.0f, 0.5f);
            }
        }
    }

    private static void spawnShatterParticles(ClientWorld world, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 2;
            double y = pos.getY() + 0.5 + (RANDOM.nextDouble() - 0.5) * 2;
            double z = pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 2;
            world.addParticleClient((ParticleEffect) ParticleTypes.SNOWFLAKE, x, y, z, 0.0, 0.1, 0.0);
            world.addParticleClient((ParticleEffect) ParticleTypes.CLOUD, x, y, z, 0.0, 0.05, 0.0);
        }
    }

    private static void spawnCauterizeParticles(ClientWorld world, BlockPos pos) {
        for (int i = 0; i < 15; i++) {
            double x = pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5);
            double y = pos.getY() + 1.0 + (RANDOM.nextDouble() - 0.5);
            double z = pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5);
            world.addParticleClient((ParticleEffect) ParticleTypes.FLAME, x, y, z, 0.0, 0.2, 0.0);
            world.addParticleClient((ParticleEffect) ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.1, 0.0);
        }
    }

    private static void spawnThermalShockParticles(ClientWorld world, BlockPos pos) {
        for (int i = 0; i < 15; i++) {
            double x = pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5);
            double y = pos.getY() + 0.5 + (RANDOM.nextDouble() - 0.5);
            double z = pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5);
            world.addParticleClient((ParticleEffect) ParticleTypes.SNOWFLAKE, x, y, z, 0.0, 0.1, 0.0);
            world.addParticleClient((ParticleEffect) ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0, 0.1, 0.0);
        }
    }

    private static void spawnSuperchargeParticles(ClientWorld world, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            double x = pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 1.5;
            double y = pos.getY() + 1.0 + (RANDOM.nextDouble() - 0.5) * 1.5;
            double z = pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 1.5;
            world.addParticleClient((ParticleEffect) ParticleTypes.INSTANT_EFFECT, x, y, z, 0.0, 0.0, 0.0);
            world.addParticleClient((ParticleEffect) ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0, 0.5, 0.0);
        }
    }

    private static void spawnMiasmaParticles(ClientWorld world, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 3;
            double y = pos.getY() + 0.5 + (RANDOM.nextDouble() - 0.5);
            double z = pos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 3;
            world.addParticleClient((ParticleEffect) ParticleTypes.WITCH, x, y, z, 0.0, 0.05, 0.0);
            world.addParticleClient((ParticleEffect) ParticleTypes.SQUID_INK, x, y, z, 0.0, -0.05, 0.0);
        }
    }
}
