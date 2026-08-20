package com.cranks.dungeons.util;

import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.util.WeaponProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;

public class CombatUtility {

    private static final java.util.Map<java.util.UUID, Long> ELEMENTAL_OVERLOAD_LAST_CRIT = new java.util.concurrent.ConcurrentHashMap<>();

    public static void triggerElementalOverload(LivingEntity entity) {
        if (entity != null && entity.getWorld() != null) {
            ELEMENTAL_OVERLOAD_LAST_CRIT.put(entity.getUuid(), entity.getWorld().getTime());
        }
    }

    public static boolean isElementalOverloadActive(LivingEntity entity) {
        if (entity == null || entity.getWorld() == null) return false;
        Long lastCrit = ELEMENTAL_OVERLOAD_LAST_CRIT.get(entity.getUuid());
        if (lastCrit == null) return false;
        long current = entity.getWorld().getTime();
        if (current - lastCrit <= 160L && current >= lastCrit) { // 8 seconds (160 ticks)
            return true;
        }
        ELEMENTAL_OVERLOAD_LAST_CRIT.remove(entity.getUuid());
        return false;
    }

    /**
     * Determines the active weapon stack for a damage event.
     * If the damage was caused by a thrown trident, returns the trident's item
     * stack.
     * Otherwise, returns the attacker's main hand stack.
     */
    public static ItemStack getCombatWeapon(DamageSource source, LivingEntity attacker) {
        if (source.getSource() instanceof WeaponProvider provider) {
            ItemStack weapon = provider.cranks$getWeapon();
            if (weapon != null && !weapon.isEmpty()) {
                return weapon;
            }
        }
        return attacker.getMainHandStack();
    }

    /**
     * Checks if the damage source is a thrown trident.
     */
    public static boolean isTridentThrow(DamageSource source) {
        return source != null && source.getSource() instanceof TridentEntity;
    }

    /**
     * Safely compares two attribute RegistryEntry objects.
     * Fabric's registry can return different RegistryEntry instances for the same attribute,
     * so .equals() may return false. This method checks identity, equals, registry key, and translation key.
     */
    public static boolean attributesMatch(net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> e1,
                                          net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> e2) {
        if (e1 == e2) return true;
        if (e1 == null || e2 == null) return false;
        if (e1.equals(e2)) return true;
        if (e1.getKey().isPresent() && e2.getKey().isPresent() && e1.getKey().get().equals(e2.getKey().get())) {
            return true;
        }
        if (e1.value() != null && e2.value() != null) {
            String k1 = e1.value().getTranslationKey();
            String k2 = e2.value().getTranslationKey();
            if (k1 != null && k1.equals(k2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the attribute value for an entity, accounting for trident throws and
     * potential doubling.
     */
    public static double getAttributeValue(LivingEntity attacker,
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
            ItemStack weapon, String statId, DamageSource source) {
        
        double currentTotal = 0.0;

        if (attacker.getAttributes().hasAttribute(attribute)) {
            currentTotal = attacker.getAttributeValue(attribute);
            
            // 1. Get the exact flat contribution of the currently held mainhand weapon
            ItemStack held = attacker.getMainHandStack();
            double heldContribution = 0.0;
            if (!held.isEmpty()) {
                var modifiers = held.getOrDefault(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, net.minecraft.component.type.AttributeModifiersComponent.DEFAULT);
                for (var entry : modifiers.modifiers()) {
                    if (attributesMatch(entry.attribute(), attribute) && entry.slot() == net.minecraft.component.type.AttributeModifierSlot.MAINHAND) {
                        if (entry.modifier().operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE) {
                            heldContribution += entry.modifier().value();
                        }
                    }
                }
                if (attacker instanceof PlayerEntity p && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(p, held)) {
                    heldContribution *= 0.5; // Requirement penalty
                }
            }
            
            // 2. Get the exact flat contribution of the weapon used for the attack
            double weaponContribution = 0.0;
            if (!weapon.isEmpty()) {
                var modifiers = weapon.getOrDefault(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, net.minecraft.component.type.AttributeModifiersComponent.DEFAULT);
                for (var entry : modifiers.modifiers()) {
                    if (attributesMatch(entry.attribute(), attribute) && entry.slot() == net.minecraft.component.type.AttributeModifierSlot.MAINHAND) {
                        if (entry.modifier().operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE) {
                            weaponContribution += entry.modifier().value();
                        }
                    }
                }
                if (attacker instanceof PlayerEntity p && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(p, weapon)) {
                    weaponContribution *= 0.5; // Requirement penalty
                }
            }
            
            currentTotal = currentTotal - heldContribution + weaponContribution;
        }

        // 3. Apply the global trident throw nerf (8/12 = 0.66x) to the WHOLE value
        if (isTridentThrow(source)) {
            currentTotal *= 0.6667;
        }

        return currentTotal;
    }

    /**
     * Returns the weapon-specific damage percentage bonus for the entity based on
     * the held weapon.
     */
    public static double getWeaponSpecificDamageBonus(LivingEntity attacker, ItemStack weapon) {
        EquipmentType type = EquipmentType.getTypeForItem(weapon).orElse(null);
        if (type == null)
            return 0.0;

        return switch (type) {
            case SWORD -> attacker.getAttributes().hasAttribute(ModAttributes.SWORD_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.SWORD_DAMAGE_PERCENT) : 0.0;
            case AXE -> attacker.getAttributes().hasAttribute(ModAttributes.AXE_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.AXE_DAMAGE_PERCENT) : 0.0;
            case MACE -> attacker.getAttributes().hasAttribute(ModAttributes.MACE_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.MACE_DAMAGE_PERCENT) : 0.0;
            case SPEAR -> attacker.getAttributes().hasAttribute(ModAttributes.SPEAR_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.SPEAR_DAMAGE_PERCENT) : 0.0;
            case BOW -> attacker.getAttributes().hasAttribute(ModAttributes.BOW_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.BOW_DAMAGE_PERCENT) : 0.0;
            case CROSSBOW -> attacker.getAttributes().hasAttribute(ModAttributes.CROSSBOW_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.CROSSBOW_DAMAGE_PERCENT) : 0.0;
            case TRIDENT -> attacker.getAttributes().hasAttribute(ModAttributes.SPEAR_DAMAGE_PERCENT) ? attacker.getAttributeValue(ModAttributes.SPEAR_DAMAGE_PERCENT) : 0.0;
            default -> 0.0;
        };
    }

    public static boolean isMelee(ItemStack weapon, DamageSource source) {
        if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE) || source.isOf(net.minecraft.entity.damage.DamageTypes.THORNS) || source.isOf(net.minecraft.entity.damage.DamageTypes.MAGIC))
            return false;
        EquipmentType type = EquipmentType.getTypeForItem(weapon).orElse(null);
        if (type == EquipmentType.BOW || type == EquipmentType.CROSSBOW)
            return false;
        return source.isOf(net.minecraft.entity.damage.DamageTypes.PLAYER_ATTACK) || source.isOf(net.minecraft.entity.damage.DamageTypes.MOB_ATTACK);
    }

    public static boolean isProjectile(DamageSource source) {
        return source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE);
    }

    public static boolean isRanged(ItemStack weapon, DamageSource source) {
        // In some UI contexts (like InventoryScreenStatsMixin), a dummy melee source is passed.
        // We shouldn't strictly require IS_PROJECTILE if it's a UI check, but we need to know it's a ranged weapon.
        EquipmentType type = EquipmentType.getTypeForItem(weapon).orElse(null);
        return type == EquipmentType.BOW || type == EquipmentType.CROSSBOW;
    }

    public static boolean isTwoHandedWeapon(ItemStack stack) {
        EquipmentType type = EquipmentType.getTypeForItem(stack).orElse(null);
        return type == EquipmentType.SPEAR || type == EquipmentType.BOW || type == EquipmentType.CROSSBOW || type == EquipmentType.TRIDENT;
    }

    public static boolean isTwoHanding(LivingEntity entity, ItemStack mainWeapon) {
        return isTwoHandedWeapon(mainWeapon);
    }

    public static boolean isDualWielding(LivingEntity entity, ItemStack mainWeapon) {
        if (isTwoHandedWeapon(mainWeapon)) return false;
        ItemStack offhand = entity.getOffHandStack();
        if (offhand.isEmpty())
            return false;

        EquipmentType mainType = EquipmentType.getTypeForItem(mainWeapon).orElse(null);
        EquipmentType offType = EquipmentType.getTypeForItem(offhand).orElse(null);

        return mainType != null && mainType == offType;
    }

    public static boolean isOneHanding(LivingEntity entity, ItemStack mainWeapon) {
        return !isTwoHandedWeapon(mainWeapon) && !mainWeapon.isEmpty();
    }

    private static final java.util.Map<java.util.UUID, Long> LAST_CRIT_TIME = new java.util.concurrent.ConcurrentHashMap<>();
    private static long lastCritEvictionTick = 0;

    /**
     * Records that an entity performed a critical hit at the specified game time.
     * Periodically evicts stale entries to prevent unbounded memory growth.
     */
    public static void recordRecentCrit(LivingEntity entity, long gameTime) {
        if (entity != null) {
            LAST_CRIT_TIME.put(entity.getUuid(), gameTime);
            // Evict stale entries every ~60 seconds (1200 ticks)
            if (gameTime - lastCritEvictionTick > 1200L) {
                lastCritEvictionTick = gameTime;
                LAST_CRIT_TIME.entrySet().removeIf(e -> (gameTime - e.getValue()) > 160L);
            }
        }
    }

    /**
     * Checks whether the entity has performed a critical hit within the last 8 seconds (160 ticks).
     */
    public static boolean hasCritInLast8Seconds(LivingEntity entity, long currentGameTime) {
        if (entity == null) return false;
        Long lastTime = LAST_CRIT_TIME.get(entity.getUuid());
        if (lastTime == null) return false;
        return (currentGameTime - lastTime) <= 160L;
    }

    /**
     * Calculates the lowest of Armor and Evasion Rating on the entity's equipped boots.
     * Returns 0.0 if either Armor or Evasion is missing or <= 0.
     */
    public static double getBootsLowestArmorEvasion(LivingEntity entity) {
        if (entity == null) return 0.0;
        ItemStack boots = entity.getEquippedStack(EquipmentSlot.FEET);
        if (boots == null || boots.isEmpty()) return 0.0;

        AttributeModifiersComponent comp = boots.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        double armor = 0.0;
        double evasion = 0.0;
        boolean hasArmor = false;
        boolean hasEvasion = false;

        for (var entry : comp.modifiers()) {
            if (entry.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                if (attributesMatch(entry.attribute(), EntityAttributes.GENERIC_ARMOR)) {
                    armor += entry.modifier().value();
                    hasArmor = true;
                } else if (attributesMatch(entry.attribute(), ModAttributes.EVASION_RATING)) {
                    evasion += entry.modifier().value();
                    hasEvasion = true;
                }
            }
        }

        if (!hasArmor || !hasEvasion || armor <= 0.0 || evasion <= 0.0) {
            return 0.0;
        }

        return Math.min(armor, evasion);
    }

    /**
     * Calculates the effective Ailment Threshold for a target entity against a specific ailment type.
     */
    public static float getEffectiveAilmentThreshold(LivingEntity target, AilmentType type) {
        if (target == null) return 1.0f;

        double maxHealth = target.getMaxHealth();
        double maxES = target.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        double dexterity = target.getAttributeValue(ModAttributes.DEXTERITY);

        // 1. Base Health + Additional Pool Bonuses
        double healthBonus = maxHealth * target.getAttributeValue(ModAttributes.AILMENT_THRESHOLD_PERCENT_OF_MAX_HEALTH);
        double esBonus = maxES * target.getAttributeValue(ModAttributes.AILMENT_THRESHOLD_PERCENT_OF_MAX_ES);
        double dexBonus = dexterity * target.getAttributeValue(ModAttributes.ELEMENTAL_AILMENT_THRESHOLD_PER_DEXTERITY);

        // 2. Boots Lowest Armor & Evasion
        double bootsBonus = 0.0;
        if (target.getAttributeValue(ModAttributes.AILMENT_THRESHOLD_FROM_BOOTS_LOWEST_ARMOR_EVASION) > 0) {
            bootsBonus = getBootsLowestArmorEvasion(target);
        }

        double flatThreshold = maxHealth + healthBonus + esBonus + dexBonus + bootsBonus;

        // 3. Percentage Multipliers
        double percentMultiplier = 1.0 + target.getAttributeValue(ModAttributes.ELEMENTAL_AILMENT_THRESHOLD_PERCENT);
        if (type == AilmentType.FREEZE) {
            percentMultiplier += target.getAttributeValue(ModAttributes.FREEZE_THRESHOLD_PERCENT);
        }

        return Math.max(1.0f, (float) (flatThreshold * percentMultiplier));
    }

    public static boolean isOverflowingBarsLoaded() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("overflowingbars")
                || net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("overflowing-bars");
    }
}
