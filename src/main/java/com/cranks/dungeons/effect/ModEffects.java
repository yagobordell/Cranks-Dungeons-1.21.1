package com.cranks.dungeons.effect;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {

    public static RegistryEntry<StatusEffect> BURN;

    public static void registerEffects() {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, "burn");
        Registry.register(Registries.STATUS_EFFECT, id, new BurnStatusEffect());
        BURN = Registries.STATUS_EFFECT.getEntry(id).orElseThrow();
        CranksDungeons.LOGGER.info("Registering effects for " + CranksDungeons.MOD_ID);
    }
}
