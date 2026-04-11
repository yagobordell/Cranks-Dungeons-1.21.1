package com.cranks.dungeons.util;

import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;

import java.util.EnumMap;
import java.util.Map;

public class DamageConversionHelper {

    public static class DamageBreakdown {
        private final Map<DamageElement, Float> damages = new EnumMap<>(DamageElement.class);

        public void add(DamageElement element, float amount) {
            damages.put(element, damages.getOrDefault(element, 0f) + amount);
        }

        public float get(DamageElement element) {
            return damages.getOrDefault(element, 0f);
        }

        public Map<DamageElement, Float> getDamages() {
            return damages;
        }

        public float getTotal() {
            float total = 0;
            for (float f : damages.values()) total += f;
            return total;
        }

        /**
         * Reduces each damage component in the breakdown by the target's resistances and armor.
         * Used to ensure the display matches the actual health loss.
         */
        public void applyResistancesInPlace(LivingEntity target) {
            for (Map.Entry<DamageElement, Float> entry : new java.util.HashMap<>(damages).entrySet()) {
                DamageElement element = entry.getKey();
                float amount = entry.getValue();

                if (element == DamageElement.PHYSICAL) {
                    damages.put(element, calculatePhysicalReduction(target, amount));
                } else if (element != DamageElement.DROWN) {
                    double res = switch (element) {
                        case FIRE -> target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                        case COLD -> target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                        case LIGHTNING -> target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                        case VOID -> target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                        default -> 0;
                    };
                    damages.put(element, (float) (amount * (1.0 - res)));
                }
            }
        }
    }

    public static DamageBreakdown getBreakdown(DamageSource source, float amount) {
        DamageBreakdown breakdown = new DamageBreakdown();
        Entity attacker = source.getAttacker();
        Entity directSource = source.getSource();

        // Environmental Damage
        if (source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE) ||
                source.isOf(DamageTypes.LAVA) || source.isOf(DamageTypes.HOT_FLOOR)) {
            breakdown.add(DamageElement.FIRE, amount);
            return breakdown;
        }

        if (source.isOf(DamageTypes.LIGHTNING_BOLT)) {
            breakdown.add(DamageElement.LIGHTNING, amount);
            return breakdown;
        }

        // Drowning damage - unique cyan-blue color
        if (source.isOf(DamageTypes.DROWN)) {
            breakdown.add(DamageElement.DROWN, amount);
            return breakdown;
        }

        if (source.isOf(DamageTypes.OUT_OF_WORLD)) {
            breakdown.add(DamageElement.VOID, amount);
            return breakdown;
        }

        if (source.isOf(DamageTypes.FREEZE)) {
            breakdown.add(DamageElement.COLD, amount);
            return breakdown;
        }

        // --- Elemental Mob Conversions ---

        // FIRE
        if (attacker instanceof BlazeEntity || attacker instanceof MagmaCubeEntity || 
            attacker instanceof GhastEntity || directSource instanceof SmallFireballEntity || 
            directSource instanceof FireballEntity || source.isOf(DamageTypes.FIREBALL)) {
            breakdown.add(DamageElement.FIRE, amount);
            return breakdown;
        }

        // COLD
        if (directSource instanceof SnowballEntity) {
            breakdown.add(DamageElement.COLD, amount);
            return breakdown;
        }
        if (attacker instanceof StrayEntity) {
            float coldPart = amount * 0.5f;
            breakdown.add(DamageElement.PHYSICAL, amount - coldPart);
            breakdown.add(DamageElement.COLD, coldPart);
            return breakdown;
        }
        if (attacker instanceof PolarBearEntity) {
            float coldPart = amount * 0.2f;
            breakdown.add(DamageElement.PHYSICAL, amount - coldPart);
            breakdown.add(DamageElement.COLD, coldPart);
            return breakdown;
        }

        // LIGHTNING
        if (attacker instanceof GuardianEntity || attacker instanceof BreezeEntity) {
            if (!source.isOf(DamageTypes.THORNS)) {
                breakdown.add(DamageElement.LIGHTNING, amount);
                return breakdown;
            }
        }

        // VOID
        if (attacker instanceof EndermanEntity || attacker instanceof EndermiteEntity) {
            float voidPart = amount * 0.4f;
            breakdown.add(DamageElement.PHYSICAL, amount - voidPart);
            breakdown.add(DamageElement.VOID, voidPart);
            return breakdown;
        }
        if (attacker instanceof ShulkerEntity || attacker instanceof EvokerEntity || source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.INDIRECT_MAGIC)) {
            breakdown.add(DamageElement.VOID, amount);
            return breakdown;
        }

        // Standard Wither handling (kept from previous implementation)
        if (attacker instanceof WitherEntity || attacker instanceof WitherSkeletonEntity || directSource instanceof WitherSkullEntity) {
            float voidPart = amount * 0.5f;
            breakdown.add(DamageElement.PHYSICAL, amount - voidPart);
            breakdown.add(DamageElement.VOID, voidPart);
            return breakdown;
        }

        // Default: 100% Physical
        breakdown.add(DamageElement.PHYSICAL, amount);
        return breakdown;
    }

    public static float applyResistances(LivingEntity target, DamageBreakdown breakdown) {
        float finalTotal = 0;
        for (Map.Entry<DamageElement, Float> entry : breakdown.getDamages().entrySet()) {
            float amount = entry.getValue();
            double res = 0;
            switch (entry.getKey()) {
                case FIRE -> res = target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                case COLD -> res = target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                case LIGHTNING -> res = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                case VOID -> res = target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                case PHYSICAL -> {
                    finalTotal += calculatePhysicalReduction(target, amount);
                    continue;
                }
            }
            finalTotal += (float) (amount * (1.0 - res));
        }
        return finalTotal;
    }

    /**
     * Calculates the remaining damage after applying the custom Path of Exile 2 inspired armor reduction.
     */
    public static float calculatePhysicalReduction(LivingEntity target, float amount) {
        double ar = target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ARMOR);
        double toughness = target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ARMOR_TOUGHNESS);

        if (ar > 0 && amount > 0) {
            // Stabilized formula: X = (400 + 10 * Damage) / (1 + Toughness / 20)
            // PDR = Armor / (Armor + X)
            // This is less sensitive to hit size than the pure PoE 2 formula, making damage more consistent.
            double x = (400.0 + 10.0 * amount) / (1.0 + (toughness / 20.0));
            double pdr = ar / (ar + x);
            return (float) (amount * (1.0 - pdr));
        }
        return amount;
    }
}
