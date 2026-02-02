package com.cranks.dungeons.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

// Enchantments in modern Minecraft are data-driven and defined via JSON
// This class is only needed if you want custom effect logic
public class VeinMiningEnchantment {
    // Modern Minecraft enchantments don't extend Enchantment class
    // They are defined via data pack JSON files

    // If you need custom behavior, you can create an enchantment effect
    public static final MapCodec<VeinMiningEffect> CODEC = MapCodec.unit(new VeinMiningEffect());

    public record VeinMiningEffect() {
        // Custom effect logic goes here if needed
    }
}