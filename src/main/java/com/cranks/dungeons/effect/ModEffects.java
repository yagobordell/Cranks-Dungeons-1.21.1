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
    public static RegistryEntry<StatusEffect> ELEMENTAL_OVERLOAD;
    public static RegistryEntry<StatusEffect> WELL_FED;
    public static RegistryEntry<StatusEffect> STARVING;

    public static void registerEffects() {
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

        Identifier overloadId = Identifier.of(CranksDungeons.MOD_ID, "elemental_overload");
        Registry.register(Registries.STATUS_EFFECT, overloadId, new ElementalOverloadEffect());
        ELEMENTAL_OVERLOAD = Registries.STATUS_EFFECT.getEntry(overloadId).orElseThrow();

        Identifier wellFedId = Identifier.of(CranksDungeons.MOD_ID, "hunger_high");
        Registry.register(Registries.STATUS_EFFECT, wellFedId, new WellFedStatusEffect());
        WELL_FED = Registries.STATUS_EFFECT.getEntry(wellFedId).orElseThrow();

        Identifier starvingId = Identifier.of(CranksDungeons.MOD_ID, "hunger_low");
        Registry.register(Registries.STATUS_EFFECT, starvingId, new StarvingStatusEffect());
        STARVING = Registries.STATUS_EFFECT.getEntry(starvingId).orElseThrow();

        CranksDungeons.LOGGER.info("Registering effects for " + CranksDungeons.MOD_ID);
    }
}
