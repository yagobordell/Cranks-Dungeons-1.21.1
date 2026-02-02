package com.cranks.dungeons.registry;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {

    public static final RegistryKey<Enchantment> VEIN_MINING = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(CranksDungeons.MOD_ID, "vein_mining")
    );

    // No initialize method needed - enchantments are loaded from data packs
}