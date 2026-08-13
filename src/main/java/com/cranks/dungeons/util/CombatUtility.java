package com.cranks.dungeons.util;

import com.cranks.dungeons.util.WeaponProvider;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;

public class CombatUtility {

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
     * Retrieves the value of an enchantment on an item stack, penalized by 50% if the player does not meet the item's attribute requirements.
     */
    public static double getEnchantmentValue(LivingEntity entity, ItemStack stack, String enchantmentId, double baseValue) {
        if (stack.isEmpty()) return 0.0;
        
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) {
                double val = baseValue;
                if (entity instanceof PlayerEntity p && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(p, stack)) {
                    val *= 0.5;
                }
                return val;
            }
        }
        return 0.0;
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
}
