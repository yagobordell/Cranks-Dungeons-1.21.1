package com.cranks.dungeons.util;

import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;

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

        if (source.isOf(DamageTypes.OUT_OF_WORLD)) {
            breakdown.add(DamageElement.VOID, amount);
            return breakdown;
        }

        if (source.isOf(DamageTypes.FREEZE)) {
            breakdown.add(DamageElement.COLD, amount);
            return breakdown;
        }

        // Mob specific conversions
        if (attacker instanceof BlazeEntity || attacker instanceof MagmaCubeEntity || directSource instanceof SmallFireballEntity) {
            breakdown.add(DamageElement.FIRE, amount);
            return breakdown;
        }

        if (attacker instanceof EndermanEntity) {
            float voidPart = amount * 0.4f; // 40% void damage
            breakdown.add(DamageElement.PHYSICAL, amount - voidPart);
            breakdown.add(DamageElement.VOID, voidPart);
            return breakdown;
        }

        if (attacker instanceof WitherEntity || attacker instanceof WitherSkeletonEntity || directSource instanceof WitherSkullEntity) {
            float voidPart = amount * 0.5f; // 50% void damage
            breakdown.add(DamageElement.PHYSICAL, amount - voidPart);
            breakdown.add(DamageElement.VOID, voidPart);
            return breakdown;
        }

        if (attacker instanceof StrayEntity) {
            float coldPart = amount * 0.3f; // 30% cold damage
            breakdown.add(DamageElement.PHYSICAL, amount - coldPart);
            breakdown.add(DamageElement.COLD, coldPart);
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
                    finalTotal += amount;
                    continue;
                }
            }
            finalTotal += (float) (amount * (1.0 - res));
        }
        return finalTotal;
    }
}
