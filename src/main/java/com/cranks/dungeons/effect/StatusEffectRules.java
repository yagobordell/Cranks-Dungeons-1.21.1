package com.cranks.dungeons.effect;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/** Values used by both gameplay modifiers and effect descriptions. */
public enum StatusEffectRules {
    FIRE_RESISTANCE(StatusEffects.FIRE_RESISTANCE, ModAttributes.FIRE_RESISTANCE, "fire_resistance", 15, true, false),
    RESISTANCE(StatusEffects.RESISTANCE, ModAttributes.BONUS_ARMOR, "bonus_armor", 150, false, false),
    REGENERATION(StatusEffects.REGENERATION, ModAttributes.LIFE_REGENERATION, "life_regeneration", 3, false, false),
    STRENGTH(StatusEffects.STRENGTH, ModAttributes.PHYSICAL_DAMAGE_PERCENT, "physical_damage", 20, true, false),
    WEAKNESS(StatusEffects.WEAKNESS, ModAttributes.PHYSICAL_DAMAGE_PERCENT, "weakness_damage", -20, true, false),
    SPEED(StatusEffects.SPEED, EntityAttributes.GENERIC_MOVEMENT_SPEED, "movement_speed", 15, true, true),
    SLOWNESS(StatusEffects.SLOWNESS, EntityAttributes.GENERIC_MOVEMENT_SPEED, "slowness", -15, true, true),
    HASTE(StatusEffects.HASTE, ModAttributes.MINING_EFFICIENCY, "haste_mining", 20, true, true),
    MINING_FATIGUE(StatusEffects.MINING_FATIGUE, ModAttributes.MINING_EFFICIENCY, "fatigue_mining", -30, true, true),
    HEALTH_BOOST(StatusEffects.HEALTH_BOOST, EntityAttributes.GENERIC_MAX_HEALTH, "maximum_health", 25, false, false),
    ABSORPTION(StatusEffects.ABSORPTION, EntityAttributes.GENERIC_MAX_ABSORPTION, "absorption_max", 20, false, false),
    LUCK(StatusEffects.LUCK, ModAttributes.ITEM_RARITY_PERCENT, "luck_rarity", 15, true, false),
    UNLUCK(StatusEffects.UNLUCK, ModAttributes.ITEM_RARITY_PERCENT, "unluck_rarity", -15, true, false);

    public final RegistryEntry<StatusEffect> effect;
    public final RegistryEntry<EntityAttribute> attribute;
    public final Identifier modifierId;
    public final boolean percent;
    public final EntityAttributeModifier.Operation operation;
    private final double perTier;

    StatusEffectRules(RegistryEntry<StatusEffect> effect, RegistryEntry<EntityAttribute> attribute, String id,
                      double perTier, boolean percent, boolean multiplicative) {
        this.effect = effect;
        this.attribute = attribute;
        this.modifierId = Identifier.of(CranksDungeons.MOD_ID, "effect_" + id);
        this.perTier = perTier;
        this.percent = percent;
        this.operation = multiplicative ? EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                : EntityAttributeModifier.Operation.ADD_VALUE;
    }

    public double amount(int amplifier) {
        double value = amplifier >= 10 ? Math.copySign(amplifier, perTier) : perTier * (amplifier + 1);
        if (perTier < 0) return Math.max(this == WEAKNESS || this == UNLUCK ? -100 : -90, value);
        return value;
    }

    public double modifier(int amplifier) {
        double amount = amount(amplifier);
        return percent ? amount / 100 : amount;
    }

    public static StatusEffectRules of(RegistryEntry<StatusEffect> effect) {
        for (var rule : values()) if (rule.effect.equals(effect)) return rule;
        return null;
    }

    public RegistryEntry<EntityAttribute> nativeAttribute() {
        return switch (this) {
            case SPEED, SLOWNESS -> EntityAttributes.GENERIC_MOVEMENT_SPEED;
            case HASTE, MINING_FATIGUE -> EntityAttributes.GENERIC_ATTACK_SPEED;
            case STRENGTH, WEAKNESS -> EntityAttributes.GENERIC_ATTACK_DAMAGE;
            case LUCK, UNLUCK -> EntityAttributes.GENERIC_LUCK;
            case HEALTH_BOOST -> EntityAttributes.GENERIC_MAX_HEALTH;
            case ABSORPTION -> EntityAttributes.GENERIC_MAX_ABSORPTION;
            default -> null;
        };
    }

    public Identifier nativeModifierId() {
        return Identifier.ofVanilla("effect." + name().toLowerCase(java.util.Locale.ROOT));
    }

    public static float instantHealth(int amplifier) {
        return amplifier >= 10 ? amplifier : (amplifier + 1) * 25;
    }

    public static float instantDamage(int amplifier) {
        return amplifier >= 5 ? amplifier : (amplifier + 1) * 15;
    }

    public static int poisonStacks(int amplifier) {
        return Math.min(20, amplifier + 1);
    }

    public static int witherReduction(int amplifier) {
        return amplifier == 0 ? 35 : 50;
    }
}
