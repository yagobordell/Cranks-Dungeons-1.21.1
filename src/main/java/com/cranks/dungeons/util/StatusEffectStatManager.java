package com.cranks.dungeons.util;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;

public class StatusEffectStatManager {

    // Attribute Modifier IDs for neutralized status effects
    public static final Identifier FIRE_RES_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_fire_resistance");
    public static final Identifier RESISTANCE_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_bonus_armor");
    public static final Identifier REGEN_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_life_regeneration");
    public static final Identifier STRENGTH_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_physical_damage");
    public static final Identifier WEAKNESS_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_weakness_damage");
    public static final Identifier SPEED_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_movement_speed");
    public static final Identifier SLOWNESS_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_slowness");
    public static final Identifier HASTE_MINING_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_haste_mining");
    public static final Identifier FATIGUE_MINING_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_fatigue_mining");
    public static final Identifier ABSORPTION_MAX_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_absorption_max");
    public static final Identifier LUCK_RARITY_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_luck_rarity");
    public static final Identifier UNLUCK_RARITY_MOD_ID = Identifier.of(CranksDungeons.MOD_ID, "effect_unluck_rarity");

    /**
     * Updates all attribute modifiers on an entity derived from its active status effects.
     */
    public static void updateEntityStatusEffectModifiers(LivingEntity entity) {
        if (entity == null || entity.getWorld().isClient()) return;

        // 1. Fire Resistance: +15% per tier (or exact % if amp >= 10)
        StatusEffectInstance fireRes = entity.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
        if (fireRes != null) {
            int amp = fireRes.getAmplifier();
            double val = (amp >= 10) ? (amp / 100.0) : ((amp + 1) * 0.15);
            applyModifier(entity, ModAttributes.FIRE_RESISTANCE, FIRE_RES_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.FIRE_RESISTANCE, FIRE_RES_MOD_ID);
        }

        // 2. Resistance: +150 Armor per tier (or exact flat armor if amp >= 10)
        StatusEffectInstance resistance = entity.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) {
            int amp = resistance.getAmplifier();
            double val = (amp >= 10) ? (double) amp : ((amp + 1) * 150.0);
            applyModifier(entity, ModAttributes.BONUS_ARMOR, RESISTANCE_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.BONUS_ARMOR, RESISTANCE_MOD_ID);
        }

        // 3. Regeneration: +3.0 HP/s per tier (or exact HP/s if amp >= 10)
        StatusEffectInstance regen = entity.getStatusEffect(StatusEffects.REGENERATION);
        if (regen != null) {
            int amp = regen.getAmplifier();
            double val = (amp >= 10) ? (double) amp : ((amp + 1) * 3.0);
            applyModifier(entity, ModAttributes.LIFE_REGENERATION, REGEN_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.LIFE_REGENERATION, REGEN_MOD_ID);
        }

        // 4. Strength: +20% Physical Damage per tier (or exact % if amp >= 10)
        StatusEffectInstance strength = entity.getStatusEffect(StatusEffects.STRENGTH);
        if (strength != null) {
            int amp = strength.getAmplifier();
            double val = (amp >= 10) ? (amp / 100.0) : ((amp + 1) * 0.20);
            applyModifier(entity, ModAttributes.PHYSICAL_DAMAGE_PERCENT, STRENGTH_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.PHYSICAL_DAMAGE_PERCENT, STRENGTH_MOD_ID);
        }

        // 5. Weakness: -20% Physical Damage per tier (or exact % if amp >= 10)
        StatusEffectInstance weakness = entity.getStatusEffect(StatusEffects.WEAKNESS);
        if (weakness != null) {
            int amp = weakness.getAmplifier();
            double val = (amp >= 10) ? (-amp / 100.0) : (- (amp + 1) * 0.20);
            applyModifier(entity, ModAttributes.PHYSICAL_DAMAGE_PERCENT, WEAKNESS_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.PHYSICAL_DAMAGE_PERCENT, WEAKNESS_MOD_ID);
        }

        // 6. Speed: +15% Movement Speed per tier (or exact % if amp >= 10)
        StatusEffectInstance speed = entity.getStatusEffect(StatusEffects.SPEED);
        if (speed != null) {
            int amp = speed.getAmplifier();
            double val = (amp >= 10) ? (amp / 100.0) : ((amp + 1) * 0.15);
            applyModifier(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_MOD_ID, val, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            removeModifier(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_MOD_ID);
        }

        // 7. Slowness: -15% Movement Speed per tier (or exact % if amp >= 10)
        StatusEffectInstance slowness = entity.getStatusEffect(StatusEffects.SLOWNESS);
        if (slowness != null) {
            int amp = slowness.getAmplifier();
            double val = (amp >= 10) ? (-amp / 100.0) : (- (amp + 1) * 0.15);
            applyModifier(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, SLOWNESS_MOD_ID, val, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            removeModifier(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, SLOWNESS_MOD_ID);
        }

        // 8. Haste: +20% Mining Speed per tier (NO attack speed modification)
        StatusEffectInstance haste = entity.getStatusEffect(StatusEffects.HASTE);
        if (haste != null) {
            int amp = haste.getAmplifier();
            double miningVal = (amp >= 10) ? (amp / 100.0) : ((amp + 1) * 0.20);
            applyModifier(entity, ModAttributes.MINING_SPEED_PERCENT, HASTE_MINING_MOD_ID, miningVal, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.MINING_SPEED_PERCENT, HASTE_MINING_MOD_ID);
        }

        // 9. Mining Fatigue: -30% Mining Speed per tier (NO attack speed modification)
        StatusEffectInstance fatigue = entity.getStatusEffect(StatusEffects.MINING_FATIGUE);
        if (fatigue != null) {
            int amp = fatigue.getAmplifier();
            double miningVal = (amp >= 10) ? (-amp / 100.0) : (- (amp + 1) * 0.30);
            applyModifier(entity, ModAttributes.MINING_SPEED_PERCENT, FATIGUE_MINING_MOD_ID, miningVal, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.MINING_SPEED_PERCENT, FATIGUE_MINING_MOD_ID);
        }

        // 10. Absorption: Max absorption attribute scaling (+20 / +40 absorption health)
        StatusEffectInstance absorption = entity.getStatusEffect(StatusEffects.ABSORPTION);
        if (absorption != null) {
            int amp = absorption.getAmplifier();
            double val = (amp >= 10) ? (double) amp : ((amp + 1) * 20.0);
            applyModifier(entity, EntityAttributes.GENERIC_MAX_ABSORPTION, ABSORPTION_MAX_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, EntityAttributes.GENERIC_MAX_ABSORPTION, ABSORPTION_MAX_MOD_ID);
        }

        // 11. Luck: +15% Rarity of Items Found per tier
        StatusEffectInstance luck = entity.getStatusEffect(StatusEffects.LUCK);
        if (luck != null) {
            int amp = luck.getAmplifier();
            double val = (amp >= 10) ? (amp / 100.0) : ((amp + 1) * 0.15);
            applyModifier(entity, ModAttributes.ITEM_RARITY_PERCENT, LUCK_RARITY_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.ITEM_RARITY_PERCENT, LUCK_RARITY_MOD_ID);
        }

        // 12. Bad Luck (Unluck): -15% Rarity of Items Found per tier
        StatusEffectInstance unluck = entity.getStatusEffect(StatusEffects.UNLUCK);
        if (unluck != null) {
            int amp = unluck.getAmplifier();
            double val = (amp >= 10) ? (-amp / 100.0) : (- (amp + 1) * 0.15);
            applyModifier(entity, ModAttributes.ITEM_RARITY_PERCENT, UNLUCK_RARITY_MOD_ID, val, EntityAttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, ModAttributes.ITEM_RARITY_PERCENT, UNLUCK_RARITY_MOD_ID);
        }
    }

    private static void applyModifier(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, Identifier id, double value, EntityAttributeModifier.Operation operation) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            EntityAttributeModifier existing = instance.getModifier(id);
            if (existing != null && existing.value() == value && existing.operation() == operation) {
                return;
            }
            instance.removeModifier(id);
            if (value != 0) {
                instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
            }
        }
    }

    private static void removeModifier(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null && instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }

    /**
     * Handles Instant Health application with flat healing and damage conversion.
     */
    public static void applyInstantHealth(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (target == null) return;

        double effectiveProximity = (attacker == target || source == target || proximity >= 0.85) ? 1.0 : proximity;

        if (target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
            // Damages undead with Magic/Void damage
            float baseDamage = (amplifier >= 10) ? (float) amplifier : (float) ((amplifier + 1) * 25);
            float actualDamage = (float) Math.max(1.0, Math.ceil(baseDamage * effectiveProximity));
            DamageSource dmgSource = attacker != null ? target.getDamageSources().indirectMagic(source != null ? source : attacker, attacker) : target.getDamageSources().magic();
            target.damage(dmgSource, actualDamage);
        } else {
            // Heals living entity with flat HP
            float baseHeal = (amplifier >= 10) ? (float) amplifier : (float) ((amplifier + 1) * 25);
            float actualHeal = (float) Math.max(1.0, Math.ceil(baseHeal * effectiveProximity));
            target.heal(actualHeal);
        }
    }

    /**
     * Handles Instant Damage (Harming) application with ~15 flat Void damage.
     */
    public static void applyInstantDamage(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (target == null) return;

        double effectiveProximity = (attacker == target || source == target || proximity >= 0.85) ? 1.0 : proximity;

        if (target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
            // Heals undead
            float baseHeal = (amplifier >= 5) ? (float) amplifier : (float) ((amplifier + 1) * 15);
            float actualHeal = (float) Math.max(1.0, Math.ceil(baseHeal * effectiveProximity));
            target.heal(actualHeal);
        } else {
            // Deals flat Void Damage to living entities
            float baseDamage = (amplifier >= 5) ? (float) amplifier : (float) ((amplifier + 1) * 15);
            float actualDamage = (float) Math.max(1.0, Math.ceil(baseDamage * effectiveProximity));
            DamageSource dmgSource = attacker != null ? target.getDamageSources().indirectMagic(source != null ? source : attacker, attacker) : target.getDamageSources().magic();
            target.damage(dmgSource, actualDamage);
        }
    }

    /**
     * Formats a StatusEffectInstance into a descriptive stat-driven display text.
     */
    public static Text getFormattedEffectName(StatusEffectInstance instance) {
        if (instance == null) return Text.empty();
        Text custom = getFormattedEffectName(instance.getEffectType(), instance.getAmplifier());
        if (custom != null) {
            return custom;
        }
        return Text.translatable(instance.getTranslationKey());
    }

    /**
     * Formats a status effect and amplifier into its explicit stat name.
     */
    public static Text getFormattedEffectName(RegistryEntry<StatusEffect> effect, int amplifier) {
        if (effect == null) return null;

        // Fire Resistance
        if (isMatch(effect, StatusEffects.FIRE_RESISTANCE)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 15);
            return Text.literal("+" + percent + "% Fire Resistance").formatted(Formatting.GOLD);
        }
        // Resistance
        if (isMatch(effect, StatusEffects.RESISTANCE)) {
            int armor = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 150);
            return Text.literal("+" + armor + " Armor").formatted(Formatting.BLUE);
        }
        // Regeneration
        if (isMatch(effect, StatusEffects.REGENERATION)) {
            double regen = (amplifier >= 10) ? (double) amplifier : ((amplifier + 1) * 3.0);
            return Text.literal("+" + String.format(Locale.ROOT, "%.1f", regen) + " Health Regen/s").formatted(Formatting.GREEN);
        }
        // Strength
        if (isMatch(effect, StatusEffects.STRENGTH)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 20);
            return Text.literal("+" + percent + "% Physical Damage").formatted(Formatting.RED);
        }
        // Weakness
        if (isMatch(effect, StatusEffects.WEAKNESS)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 20);
            return Text.literal("-" + percent + "% Damage Penalty").formatted(Formatting.GRAY);
        }
        // Speed
        if (isMatch(effect, StatusEffects.SPEED)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 15);
            return Text.literal("+" + percent + "% Movement Speed").formatted(Formatting.AQUA);
        }
        // Slowness
        if (isMatch(effect, StatusEffects.SLOWNESS)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 15);
            return Text.literal("-" + percent + "% Movement Speed Penalty").formatted(Formatting.DARK_AQUA);
        }
        // Haste (Mining speed only)
        if (isMatch(effect, StatusEffects.HASTE)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 20);
            return Text.literal("+" + percent + "% Mining Speed").formatted(Formatting.YELLOW);
        }
        // Mining Fatigue (Mining speed only)
        if (isMatch(effect, StatusEffects.MINING_FATIGUE)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 30);
            return Text.literal("-" + percent + "% Mining Speed Penalty").formatted(Formatting.DARK_GRAY);
        }
        // Absorption (Extra one-time health shield: +20 / +40)
        if (isMatch(effect, StatusEffects.ABSORPTION)) {
            int hp = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 20);
            return Text.literal("+" + hp + " Absorption Health").formatted(Formatting.GOLD);
        }
        // Luck (Item Rarity)
        if (isMatch(effect, StatusEffects.LUCK)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 15);
            return Text.literal("+" + percent + "% Rarity of Items Found").formatted(Formatting.GREEN);
        }
        // Bad Luck (Unluck)
        if (isMatch(effect, StatusEffects.UNLUCK)) {
            int percent = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 15);
            return Text.literal("-" + percent + "% Item Rarity Penalty").formatted(Formatting.RED);
        }
        // Instant Health
        if (isMatch(effect, StatusEffects.INSTANT_HEALTH)) {
            int hp = (amplifier >= 10) ? amplifier : ((amplifier + 1) * 25);
            return Text.literal("+" + hp + " Instant Health").formatted(Formatting.GREEN);
        }
        // Instant Damage
        if (isMatch(effect, StatusEffects.INSTANT_DAMAGE)) {
            int voidDmg = (amplifier >= 5) ? amplifier : ((amplifier + 1) * 15);
            return Text.literal("+" + voidDmg + " Instant Void Damage").formatted(Formatting.DARK_PURPLE);
        }
        // Dolphin's Grace: Display name remains Dolphin's Grace
        if (isMatch(effect, StatusEffects.DOLPHINS_GRACE)) {
            return Text.literal("Dolphin's Grace").formatted(Formatting.AQUA);
        }

        // Mod effects
        if (effect == ModEffects.WELL_FED) {
            return Text.literal("Well Fed").formatted(Formatting.GOLD);
        }
        if (effect == ModEffects.STARVING) {
            return Text.literal("Starving").formatted(Formatting.DARK_GREEN);
        }
        if (effect == ModEffects.POISON) {
            int stacks = amplifier + 1;
            return Text.literal("Poison +" + stacks).formatted(Formatting.DARK_GREEN);
        }
        if (effect == ModEffects.BURN) {
            return Text.literal("Burning").formatted(Formatting.GOLD);
        }
        if (effect == ModEffects.FREEZE) {
            return Text.literal("Frozen").formatted(Formatting.AQUA);
        }
        if (effect == ModEffects.BLEED) {
            return Text.literal("Bleeding").formatted(Formatting.RED);
        }
        if (effect == ModEffects.SHOCK) {
            return Text.literal("Shocked").formatted(Formatting.YELLOW);
        }
        if (effect == ModEffects.ELECTROCUTE) {
            return Text.literal("Electrocuted").formatted(Formatting.BLUE);
        }

        return null;
    }

    /**
     * Returns an informative hover explanation text for status effects (e.g. Bad Omen, Dolphin's Grace, etc.).
     */
    public static Text getEffectDescription(StatusEffectInstance instance) {
        if (instance == null) return null;
        return getEffectDescription(instance.getEffectType());
    }

    /**
     * Returns an informative hover explanation text for a given status effect.
     */
    public static Text getEffectDescription(RegistryEntry<StatusEffect> effect) {
        List<Text> list = getDescriptionsByTitle(effect != null ? effect.value().getName().getString() : "");
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Finds the description lines for a status effect title string. Multi-line descriptions are split into cleanly formatted rows.
     */
    public static List<Text> getDescriptionsByTitle(String title) {
        if (title == null || title.isEmpty()) return null;
        String clean = title.trim().toLowerCase(Locale.ROOT);

        if (clean.contains("well fed")) {
            return List.of(
                Text.literal("Active while Hunger is 16 or above:").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("10% increased Movement & Attack Speed").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("starving")) {
            return List.of(
                Text.literal("Active while Hunger is 6 or below:").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("10% reduced Movement & Attack Speed").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("dolphin")) {
            return List.of(
                Text.literal("Increases swimming speed by 30%").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("while swimming near dolphins").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("raid omen")) {
            return List.of(Text.literal("A Village Raid will begin after the countdown expires").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("trial omen")) {
            return List.of(Text.literal("Converts nearby Spawners into Ominous Trial Spawners").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("bad omen") || clean.contains("omen")) {
            return List.of(Text.literal("Triggers a Village Raid when entering a village").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("conduit")) {
            return List.of(
                Text.literal("Grants underwater breathing,").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("night vision, and mining speed").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("water breathing") || clean.contains("breathing")) {
            return List.of(Text.literal("Prevents drowning and oxygen depletion underwater").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("hero of the village")) {
            return List.of(
                Text.literal("Grants large price discounts").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("on all Villager trades").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("invisibility") || clean.contains("invisible")) {
            return List.of(Text.literal("Hides entity visually and reduces mob detection range").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("glowing")) {
            return List.of(Text.literal("Outlines the entity, making it visible through solid blocks").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("levitation")) {
            return List.of(Text.literal("Forces the entity to float continuously upward").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("slow falling")) {
            return List.of(Text.literal("Slows falling descent speed and negates all fall damage").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("darkness")) {
            return List.of(Text.literal("Periodically dims surrounding light and severely restricts vision").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("blindness")) {
            return List.of(Text.literal("Dramatically reduces vision radius and disables sprinting").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("fire resistance") || clean.contains("fire")) {
            return List.of(Text.literal("Reduces fire damage taken").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("armor") || clean.contains("resistance")) {
            return List.of(Text.literal("Reduces incoming physical damage").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("health regen") || clean.contains("regeneration")) {
            return List.of(Text.literal("Restores health continuously over time").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("physical damage") || clean.contains("strength")) {
            return List.of(Text.literal("Increases outgoing physical attack damage").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("damage penalty") || clean.contains("weakness")) {
            return List.of(Text.literal("Reduces outgoing damage").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("movement speed penalty") || clean.contains("slowness")) {
            return List.of(Text.literal("Reduces ground movement speed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("movement speed") || clean.contains("speed")) {
            return List.of(Text.literal("Increases ground movement speed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("mining speed penalty") || clean.contains("mining fatigue") || clean.contains("fatigue")) {
            return List.of(Text.literal("Reduces block mining speed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("mining speed") || clean.contains("haste")) {
            return List.of(Text.literal("Increases block mining speed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("absorption")) {
            return List.of(Text.literal("Grants temporary one-time bonus health shield").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("item rarity penalty") || clean.contains("bad luck") || clean.contains("unluck")) {
            return List.of(Text.literal("Decreases the rarity of items found").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("rarity of items") || clean.contains("item rarity") || clean.contains("luck")) {
            return List.of(Text.literal("Increases the rarity of items found").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("instant health") || clean.contains("healing")) {
            return List.of(Text.literal("Instantly restores a flat amount of health").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("instant void") || clean.contains("harming")) {
            return List.of(Text.literal("Deals instant Void damage mitigated by Void Resistance").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("poison")) {
            return List.of(Text.literal("Chaos damage over time that stacks and bypasses Energy Shield").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("burning") || clean.contains("burn")) {
            return List.of(Text.literal("Fire damage over time that can spread to nearby enemies").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("frozen") || clean.contains("freeze")) {
            return List.of(Text.literal("Completely immobilizes and incapacitates the entity").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("bleeding") || clean.contains("bleed")) {
            return List.of(
                Text.literal("Accumulates 40% of damage taken").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("and bursts when it expires").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("shocked") || clean.contains("shock")) {
            return List.of(Text.literal("Increases damage taken and arcs lightning to nearby enemies").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("electrocuted") || clean.contains("electrocute")) {
            return List.of(
                Text.literal("Stuns the entity, preventing").formatted(Formatting.GRAY, Formatting.ITALIC),
                Text.literal("movement and attacks").formatted(Formatting.GRAY, Formatting.ITALIC)
            );
        }
        if (clean.contains("wind charged")) {
            return List.of(Text.literal("Emits a burst of wind upon death").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("weaving")) {
            return List.of(Text.literal("Spawns cobwebs and allows moving through webs unimpeded").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("oozing")) {
            return List.of(Text.literal("Spawns slimes upon death").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
        if (clean.contains("infested")) {
            return List.of(Text.literal("Has a chance to spawn silverfish when taking damage").formatted(Formatting.GRAY, Formatting.ITALIC));
        }

        return null;
    }

    private static boolean isMatch(RegistryEntry<StatusEffect> a, RegistryEntry<StatusEffect> b) {
        if (a == null || b == null) return false;
        if (a.equals(b) || a.value() == b.value()) return true;
        if (a.getKey().isPresent() && b.getKey().isPresent()) {
            return a.getKey().get().equals(b.getKey().get());
        }
        return a.value().getTranslationKey().equals(b.value().getTranslationKey());
    }
}
