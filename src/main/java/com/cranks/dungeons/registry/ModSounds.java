package com.cranks.dungeons.registry;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent ARTIFICIAL_ALTAR_CLICK = register("artificial_altar_click");
    public static final SoundEvent RUNIC_ALTAR_CLICK = register("runic_altar_click");

    // NEW: energy shield sounds
    public static final SoundEvent ENERGY_SHIELD_HIT = register("energy_shield_hit");
    public static final SoundEvent ENERGY_SHIELD_BREAK = register("energy_shield_break");
    public static final SoundEvent ENERGY_SHIELD_REGEN = register("energy_shield_regen");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        CranksDungeons.LOGGER.info("Registering sounds for {}", CranksDungeons.MOD_ID);
    }
}