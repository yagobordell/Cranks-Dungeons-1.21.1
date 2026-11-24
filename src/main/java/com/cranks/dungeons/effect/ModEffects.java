package com.cranks.dungeons.effect;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {

    public static RegistryEntry<StatusEffect> BURN;
    public static RegistryEntry<StatusEffect> POISON;
    public static RegistryEntry<StatusEffect> FREEZE;
    public static RegistryEntry<StatusEffect> BLEED;
    public static RegistryEntry<StatusEffect> SHOCK;
    public static RegistryEntry<StatusEffect> ELECTROCUTE;
    public static RegistryEntry<StatusEffect> WELL_FED;
    public static RegistryEntry<StatusEffect> STARVING;
    public static RegistryEntry<StatusEffect> COLD_RESISTANCE;
    public static RegistryEntry<StatusEffect> LIGHTNING_RESISTANCE;
    public static RegistryEntry<StatusEffect> VOID_RESISTANCE;

    public static void registerEffects() {
        COLD_RESISTANCE = Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(CranksDungeons.MOD_ID, "cold_resistance"),
                new ElementalResistanceEffect(0x9BCBE9, com.cranks.dungeons.registry.ModAttributes.COLD_RESISTANCE, "cold_resistance"));
        LIGHTNING_RESISTANCE = Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(CranksDungeons.MOD_ID, "lightning_resistance"),
                new ElementalResistanceEffect(0xE8C45C, com.cranks.dungeons.registry.ModAttributes.LIGHTNING_RESISTANCE, "lightning_resistance"));
        VOID_RESISTANCE = Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(CranksDungeons.MOD_ID, "void_resistance"),
                new ElementalResistanceEffect(0xA264CC, com.cranks.dungeons.registry.ModAttributes.VOID_RESISTANCE, "void_resistance"));
        Identifier burnId = Identifier.of(CranksDungeons.MOD_ID, "burn");
        Registry.register(Registries.STATUS_EFFECT, burnId, new BurnStatusEffect());
        BURN = Registries.STATUS_EFFECT.getEntry(burnId).orElseThrow();

        Identifier poisonId = Identifier.of(CranksDungeons.MOD_ID, "poison");
        Registry.register(Registries.STATUS_EFFECT, poisonId, new PoisonStatusEffect());
        POISON = Registries.STATUS_EFFECT.getEntry(poisonId).orElseThrow();

        Identifier freezeId = Identifier.of(CranksDungeons.MOD_ID, "freeze");
        Registry.register(Registries.STATUS_EFFECT, freezeId, new FreezeStatusEffect());
        FREEZE = Registries.STATUS_EFFECT.getEntry(freezeId).orElseThrow();

        Identifier bleedId = Identifier.of(CranksDungeons.MOD_ID, "bleed");
        Registry.register(Registries.STATUS_EFFECT, bleedId, new BleedStatusEffect());
        BLEED = Registries.STATUS_EFFECT.getEntry(bleedId).orElseThrow();

        Identifier shockId = Identifier.of(CranksDungeons.MOD_ID, "shock");
        Registry.register(Registries.STATUS_EFFECT, shockId, new ShockStatusEffect());
        SHOCK = Registries.STATUS_EFFECT.getEntry(shockId).orElseThrow();
        
        Identifier electrocuteId = Identifier.of(CranksDungeons.MOD_ID, "electrocute");
        Registry.register(Registries.STATUS_EFFECT, electrocuteId, new ElectrocuteStatusEffect());
        ELECTROCUTE = Registries.STATUS_EFFECT.getEntry(electrocuteId).orElseThrow();

        Identifier wellFedId = Identifier.of(CranksDungeons.MOD_ID, "hunger_high");
        Registry.register(Registries.STATUS_EFFECT, wellFedId, new WellFedStatusEffect());
        WELL_FED = Registries.STATUS_EFFECT.getEntry(wellFedId).orElseThrow();

        Identifier starvingId = Identifier.of(CranksDungeons.MOD_ID, "hunger_low");
        Registry.register(Registries.STATUS_EFFECT, starvingId, new StarvingStatusEffect());
        STARVING = Registries.STATUS_EFFECT.getEntry(starvingId).orElseThrow();

        CranksDungeons.LOGGER.info("Registering effects for " + CranksDungeons.MOD_ID);
    }
}
