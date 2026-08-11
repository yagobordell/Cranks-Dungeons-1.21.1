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

        public void set(DamageElement element, float amount) {
            damages.put(element, amount);
        }

        public float get(DamageElement element) {
            return damages.getOrDefault(element, 0f);
        }

        public Map<DamageElement, Float> getDamages() {
            return damages;
        }

        public void scale(float factor) {
            for (Map.Entry<DamageElement, Float> entry : damages.entrySet()) {
                entry.setValue(entry.getValue() * factor);
            }
        }

        public float getTotal() {
            float total = 0;
            for (float f : damages.values()) total += f;
            return total;
        }

        /**
         * Reduces each damage component in the breakdown by the target's resistances and armor,
         * accounting for attacker's penetration.
         */
        public void applyResistancesInPlace(LivingEntity target, Entity attacker) {
            for (Map.Entry<DamageElement, Float> entry : new java.util.HashMap<>(damages).entrySet()) {
                DamageElement element = entry.getKey();
                float amount = entry.getValue();

                if (element == DamageElement.PHYSICAL) {
                    damages.put(element, calculatePhysicalReduction(target, amount, attacker));
                } else if (element != DamageElement.DROWN) {
                    double res = switch (element) {
                        case FIRE -> target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                        case COLD -> target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                        case LIGHTNING -> target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                        case VOID -> target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                        default -> 0;
                    };

                    double armorAppliesPercent = getAttributeValueSafe(target, ModAttributes.ARMOR_APPLIES_TO_ELEMENTAL, 0.0);
                    if (armorAppliesPercent > 0) {
                        amount = calculateArmorReduction(target, amount, 0.0, armorAppliesPercent);
                    }

                    double penetration = 0;
                    if (attacker instanceof LivingEntity le) {
                        penetration = getAttributeValueSafe(le, ModAttributes.ELEMENTAL_PENETRATION_PERCENT, 0.0);
                        penetration += switch (element) {
                            case FIRE -> getAttributeValueSafe(le, ModAttributes.FIRE_PENETRATION_PERCENT, 0.0);
                            case COLD -> getAttributeValueSafe(le, ModAttributes.COLD_PENETRATION_PERCENT, 0.0);
                            case LIGHTNING -> getAttributeValueSafe(le, ModAttributes.LIGHTNING_PENETRATION_PERCENT, 0.0);
                            case VOID -> getAttributeValueSafe(le, ModAttributes.VOID_PENETRATION_PERCENT, 0.0);
                            default -> 0;
                        };
                    }

                    double finalRes = Math.max(0, res * (1.0 - penetration));
                    damages.put(element, (float) (amount * (1.0 - finalRes)));
                }
            }
        }
    }

    public static DamageBreakdown getBreakdown(DamageSource source, float amount, LivingEntity target) {
        DamageBreakdown breakdown = new DamageBreakdown();
        Entity attacker = source.getAttacker();
        Entity directSource = source.getSource();

        // Point Blank Keystone
        if (attacker instanceof LivingEntity livingAttacker && livingAttacker.getAttributeValue(ModAttributes.KEYSTONE_POINT_BLANK) > 0) {
            if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE) || directSource instanceof net.minecraft.entity.projectile.ProjectileEntity) {
                double distance = Math.sqrt(attacker.squaredDistanceTo(target));
                if (distance <= 5.0) {
                    amount *= 1.3f;
                } else if (distance >= 30.0) {
                    amount *= 0.5f;
                } else {
                    amount *= (float) (1.3 - ((distance - 5.0) / 25.0 * 0.8));
                }
            }
        }

        // Specific handling for quiet 'generic' damage from AilmentManager
        if (source.isOf(DamageTypes.GENERIC) && com.cranks.dungeons.ailment.AilmentManager.hasAilment(target, com.cranks.dungeons.ailment.AilmentType.BURN)) {
            breakdown.add(DamageElement.FIRE, amount);
            return breakdown;
        }

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

        // Default: 100% Physical OR calculated from LivingEntity stats
        if (attacker instanceof LivingEntity livingAttacker) {
            calculateLivingAttackerBreakdown(livingAttacker, target, source, amount, breakdown);
        } else {
            breakdown.add(DamageElement.PHYSICAL, amount);
        }

        float currentPhysical = breakdown.get(DamageElement.PHYSICAL);
        if (currentPhysical > 0) {
            double takenAsVoid = target.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.PHYSICAL_TAKEN_AS_VOID);
            if (takenAsVoid > 0) {
                float convertAmount = (float) (currentPhysical * takenAsVoid);
                breakdown.add(DamageElement.PHYSICAL, -convertAmount);
                breakdown.add(DamageElement.VOID, convertAmount);
            }
        }

        return breakdown;
    }

    public static float applyResistances(LivingEntity target, DamageBreakdown breakdown, Entity attacker) {
        float finalTotal = 0;
        for (Map.Entry<DamageElement, Float> entry : breakdown.getDamages().entrySet()) {
            float amount = entry.getValue();
            double res = 0;
            DamageElement element = entry.getKey();
            switch (element) {
                case FIRE -> res = target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                case COLD -> res = target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                case LIGHTNING -> res = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                case VOID -> res = target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                case PHYSICAL -> {
                    finalTotal += calculatePhysicalReduction(target, amount, attacker);
                    continue;
                }
            }

            double armorAppliesPercent = getAttributeValueSafe(target, ModAttributes.ARMOR_APPLIES_TO_ELEMENTAL, 0.0);
            if (armorAppliesPercent > 0) {
                amount = calculateArmorReduction(target, amount, 0.0, armorAppliesPercent);
            }

            double penetration = 0;
            if (attacker instanceof LivingEntity le) {
                penetration = le.getAttributeValue(ModAttributes.ELEMENTAL_PENETRATION_PERCENT);
                penetration += switch (element) {
                    case FIRE -> le.getAttributeValue(ModAttributes.FIRE_PENETRATION_PERCENT);
                    case COLD -> le.getAttributeValue(ModAttributes.COLD_PENETRATION_PERCENT);
                    case LIGHTNING -> le.getAttributeValue(ModAttributes.LIGHTNING_PENETRATION_PERCENT);
                    case VOID -> le.getAttributeValue(ModAttributes.VOID_PENETRATION_PERCENT);
                    default -> 0;
                };
            }

            double finalRes = Math.max(0, res * (1.0 - penetration));
            finalTotal += (float) (amount * (1.0 - finalRes));
        }
        return finalTotal;
    }

    public static float calculateArmorReduction(LivingEntity target, float amount, double penetration, double armorMultiplier) {
        double ar = target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR);
        if (target.getAttributeValue(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0) {
            ar += target.getAttributeValue(ModAttributes.EVASION_RATING);
        }
        ar *= armorMultiplier;

        if (penetration > 0) {
            ar *= (1.0 - penetration);
        }

        if (ar > 0 && amount > 0) {
            // Pure PoE 2 style formula: PDR = Armor / (Armor + (10 * Damage))
            // This makes armor significantly more dynamic against different hit sizes.
            double x = 10.0 * amount;
            double pdr = ar / (ar + x);
            
            // Cap PDR at 90%
            pdr = Math.min(0.9, pdr);
            
            return (float) (amount * (1.0 - pdr));
        }
        return amount;
    }

    /**
     * Calculates the remaining damage after applying the custom Path of Exile 2 inspired armor reduction.
     */
    public static float calculatePhysicalReduction(LivingEntity target, float amount, Entity attacker) {
        double penetration = 0.0;
        if (attacker instanceof LivingEntity le) {
            penetration = getAttributeValueSafe(le, ModAttributes.ARMOR_PENETRATION_PERCENT, 0.0);
        }
        return calculateArmorReduction(target, amount, penetration, 1.0);
    }

    private static double getAttributeValueSafe(LivingEntity entity, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, double defaultValue) {
        if (entity.getAttributes().hasAttribute(attribute)) {
            return entity.getAttributeValue(attribute);
        }
        return defaultValue;
    }

    private static void calculateLivingAttackerBreakdown(LivingEntity attacker, LivingEntity target, DamageSource source, float amount, DamageBreakdown breakdown) {
        net.minecraft.item.ItemStack weapon = com.cranks.dungeons.util.CombatUtility.getCombatWeapon(source, attacker);

        if (com.cranks.dungeons.util.CombatUtility.isRanged(weapon, source)) {
            double baseDamage = attacker.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double totalPercent = 1.0 + getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.ATTACK_DAMAGE_PERCENT, 0.0)
                    + getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.RANGED_DAMAGE_PERCENT, 0.0);
            double drawMultiplier = 1.0;
            if (weapon.getItem() instanceof net.minecraft.item.BowItem) {
                drawMultiplier = Math.min(1.0, amount / 6.0);
            }
            amount = (float) (baseDamage * totalPercent * drawMultiplier);
        }

        boolean isPvP = attacker instanceof net.minecraft.entity.player.PlayerEntity && target instanceof net.minecraft.entity.player.PlayerEntity;
        if (isPvP) {
            amount *= 0.25f;
        }

        double pvpMultiplier = isPvP ? 0.25 : 1.0;
        boolean isCrit = com.cranks.dungeons.util.CriticalHitState.isCriticalHit();
        float critMultiplier = isCrit ? (float) getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.CRIT_MULTIPLIER, 1.0) : 1.0f;
        if (isCrit && isPvP) {
            critMultiplier = 1.0f + (critMultiplier - 1.0f) * 0.5f;
        }

        float unreducedPhysicalDamage = (float) (amount / pvpMultiplier);
        float physicalDamage = amount;

        double weaponSpecificPercent = com.cranks.dungeons.util.CombatUtility.getWeaponSpecificDamageBonus(attacker, weapon);

        double afflictedBonus = 0;
        if (com.cranks.dungeons.ailment.AilmentManager.isAfflicted(target)) {
            afflictedBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.DAMAGE_AGAINST_AFFLICTED_PERCENT, 0.0);
        }

        double selfAfflictedBonus = 0;
        if (com.cranks.dungeons.ailment.AilmentManager.isAfflicted(attacker)) {
            selfAfflictedBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.DAMAGE_WHILE_AFFLICTED_PERCENT, 0.0);
        }

        double elementalBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.ELEMENTAL_DAMAGE_PERCENT, 0.0);
        if (attacker.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.ELEMENTAL_OVERLOAD)) {
            elementalBonus += 0.40;
        }

        double typeBonus = 0;
        if (com.cranks.dungeons.util.CombatUtility.isMelee(weapon, source)) {
            typeBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.MELEE_DAMAGE_PERCENT, 0.0);
        } else if (com.cranks.dungeons.util.CombatUtility.isRanged(weapon, source)) {
            typeBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.RANGED_DAMAGE_PERCENT, 0.0);
        }

        double wieldingBonus = 0;
        if (com.cranks.dungeons.util.CombatUtility.isTwoHanding(attacker, weapon)) {
            wieldingBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.TWO_HANDING_DAMAGE_PERCENT, 0.0);
        } else if (com.cranks.dungeons.util.CombatUtility.isDualWielding(attacker, weapon)) {
            wieldingBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT, 0.0);
        } else if (com.cranks.dungeons.util.CombatUtility.isOneHanding(attacker, weapon)) {
            wieldingBonus = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.ONE_HANDED_DAMAGE_PERCENT, 0.0);
        }

        double fireBaseDamage = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.FIRE_DAMAGE, weapon, "local_fire_damage", source);
        double coldBaseDamage = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.COLD_DAMAGE, weapon, "local_cold_damage", source);
        double lightningBaseDamage = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.LIGHTNING_DAMAGE, weapon, "local_lightning_damage", source);
        double voidBaseDamage = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.VOID_DAMAGE, weapon, "local_void_damage", source);

        double originalUnreducedPhysical = unreducedPhysicalDamage;
        
        double toFire = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_TO_FIRE_CONVERSION_PERCENT, 0.0);
        double toCold = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_TO_COLD_CONVERSION_PERCENT, 0.0);
        double toLightning = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_TO_LIGHTNING_CONVERSION_PERCENT, 0.0);
        double toVoid = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 0.0);
        
        double totalConversion = toFire + toCold + toLightning + toVoid;
        if (totalConversion > 1.0) {
            toFire /= totalConversion;
            toCold /= totalConversion;
            toLightning /= totalConversion;
            toVoid /= totalConversion;
        }

        double convertedFire = originalUnreducedPhysical * toFire;
        double convertedCold = originalUnreducedPhysical * toCold;
        double convertedLightning = originalUnreducedPhysical * toLightning;
        double convertedVoid = originalUnreducedPhysical * toVoid;
        
        double totalConverted = convertedFire + convertedCold + convertedLightning + convertedVoid;
        unreducedPhysicalDamage -= totalConverted;
        physicalDamage -= totalConverted * pvpMultiplier;

        fireBaseDamage += convertedFire;
        coldBaseDamage += convertedCold;
        lightningBaseDamage += convertedLightning;
        voidBaseDamage += convertedVoid;

        double totalBasicDamage = unreducedPhysicalDamage + fireBaseDamage + coldBaseDamage + lightningBaseDamage + voidBaseDamage;

        double extraPhysicalPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_EXTRA_PERCENT, weapon, "attack_extra_damage", source);
        double finalPhysicalExtraDamage = totalBasicDamage * extraPhysicalPercent + unreducedPhysicalDamage * selfAfflictedBonus;

        double extraFirePercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.FIRE_EXTRA_PERCENT, weapon, "fire_extra_damage", source);
        fireBaseDamage += totalBasicDamage * extraFirePercent;

        double extraColdPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.COLD_EXTRA_PERCENT, weapon, "cold_extra_damage", source);
        coldBaseDamage += totalBasicDamage * extraColdPercent;

        double extraLightningPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.LIGHTNING_EXTRA_PERCENT, weapon, "lightning_extra_damage", source);
        lightningBaseDamage += totalBasicDamage * extraLightningPercent;

        double extraVoidPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.VOID_EXTRA_PERCENT, weapon, "void_extra_damage", source);
        voidBaseDamage += totalBasicDamage * extraVoidPercent;

        double physAsFire = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_AS_EXTRA_FIRE_PERCENT, 0.0);
        fireBaseDamage += originalUnreducedPhysical * physAsFire;

        double physAsCold = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_AS_EXTRA_COLD_PERCENT, 0.0);
        coldBaseDamage += originalUnreducedPhysical * physAsCold;

        double physAsLightning = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_AS_EXTRA_LIGHTNING_PERCENT, 0.0);
        lightningBaseDamage += originalUnreducedPhysical * physAsLightning;

        double physAsVoid = getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.PHYSICAL_AS_EXTRA_VOID_PERCENT, 0.0);
        voidBaseDamage += originalUnreducedPhysical * physAsVoid;

        double baseMultiplier = weaponSpecificPercent + afflictedBonus + selfAfflictedBonus + elementalBonus + typeBonus + wieldingBonus;

        double fireTotalPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.FIRE_DAMAGE_PERCENT, weapon, "fire_damage_percent", source);
        double fireDamage = fireBaseDamage * (1.0 + fireTotalPercent + baseMultiplier) * critMultiplier;

        double coldTotalPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.COLD_DAMAGE_PERCENT, weapon, "cold_damage_percent", source);
        double coldDamage = coldBaseDamage * (1.0 + coldTotalPercent + baseMultiplier) * critMultiplier;

        double lightningTotalPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.LIGHTNING_DAMAGE_PERCENT, weapon, "lightning_damage_percent", source);
        double lightningDamage = lightningBaseDamage * (1.0 + lightningTotalPercent + baseMultiplier) * critMultiplier;

        double voidTotalPercent = com.cranks.dungeons.util.CombatUtility.getAttributeValue(attacker, com.cranks.dungeons.registry.ModAttributes.VOID_DAMAGE_PERCENT, weapon, "void_damage_percent", source);
        double voidDamage = voidBaseDamage * (1.0 + voidTotalPercent + baseMultiplier) * critMultiplier;

        fireDamage *= pvpMultiplier;
        coldDamage *= pvpMultiplier;
        lightningDamage *= pvpMultiplier;
        voidDamage *= pvpMultiplier;
        finalPhysicalExtraDamage *= pvpMultiplier;

        if (getAttributeValueSafe(attacker, com.cranks.dungeons.registry.ModAttributes.KEYSTONE_AVATAR_OF_FIRE, 0.0) > 0) {
            fireDamage += (physicalDamage + finalPhysicalExtraDamage) * 0.5;
            fireDamage += coldDamage * 0.5;
            fireDamage += lightningDamage * 0.5;
            
            physicalDamage = 0;
            finalPhysicalExtraDamage = 0;
            coldDamage = 0;
            lightningDamage = 0;
            voidDamage = 0;
        }

        if (physicalDamage + finalPhysicalExtraDamage > 0) breakdown.add(DamageElement.PHYSICAL, (float) (physicalDamage + finalPhysicalExtraDamage));
        if (fireDamage > 0) breakdown.add(DamageElement.FIRE, (float) fireDamage);
        if (coldDamage > 0) breakdown.add(DamageElement.COLD, (float) coldDamage);
        if (lightningDamage > 0) breakdown.add(DamageElement.LIGHTNING, (float) lightningDamage);
        if (voidDamage > 0) breakdown.add(DamageElement.VOID, (float) voidDamage);
    }

}
