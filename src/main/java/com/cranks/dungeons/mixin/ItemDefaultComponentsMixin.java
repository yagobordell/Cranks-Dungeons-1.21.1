package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemDefaultComponentsMixin {

    @Inject(method = "getComponents", at = @At("RETURN"), cancellable = true)
    private void modifyDefaultComponents(CallbackInfoReturnable<net.minecraft.component.ComponentMap> cir) {
        Item item = (Item)(Object)this;
        String itemName = item.toString().toLowerCase();

        if (!shouldModify(itemName)) return;

        var originalMap = cir.getReturnValue();
        var attributes = originalMap.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if (attributes == null) return;

        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();

        boolean modified = false;

        for (var entry : attributes.modifiers()) {
            if (isArmor(itemName) && entry.attribute().equals(EntityAttributes.ARMOR)) {
                // Only apply armor multiplier for sets that use traditional armor
                if (usesTraditionalArmor(itemName)) {
                    double multiplier = getArmorMultiplier(itemName);
                    builder.add(
                            entry.attribute(),
                            new EntityAttributeModifier(
                                    entry.modifier().id(),
                                    entry.modifier().value() * multiplier,
                                    entry.modifier().operation()
                            ),
                            entry.slot()
                    );

                    // Add armor toughness for sets that use it
                    double toughness = getToughnessBonus(itemName);
                    if (toughness > 0) {
                        builder.add(
                                EntityAttributes.ARMOR_TOUGHNESS,
                                new EntityAttributeModifier(
                                        Identifier.of("minecraft", "armor.toughness." + getArmorSlot(itemName)),
                                        toughness,
                                        EntityAttributeModifier.Operation.ADD_VALUE
                                ),
                                entry.slot()
                        );
                    }
                } else {
                    // For leather/gold/chainmail: DON'T add the armor attribute at all
                    // This effectively removes their armor value
                    // (we skip the builder.add call)
                }

                // Add unique bonuses based on armor type (this applies to ALL armor types)
                addArmorTypeBonuses(builder, itemName, entry.slot());
                modified = true;  // Mark as modified for all armor

            } else if (isWeapon(itemName) && entry.attribute().equals(EntityAttributes.ATTACK_DAMAGE)) {
                double bonus = getDamageBonus(itemName);
                builder.add(
                        entry.attribute(),
                        new EntityAttributeModifier(
                                entry.modifier().id(),
                                entry.modifier().value() + bonus,
                                entry.modifier().operation()
                        ),
                        entry.slot()
                );
                modified = true;
            } else {
                // Keep other modifiers unchanged
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        if (modified) {
            // Create new component map with modified attributes
            var newMap = net.minecraft.component.ComponentMap.builder()
                    .addAll(originalMap)
                    .add(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build())
                    .build();
            cir.setReturnValue(newMap);
        }
    }

    private void addArmorTypeBonuses(AttributeModifiersComponent.Builder builder, String itemName, net.minecraft.component.type.AttributeModifierSlot slot) {
        String armorSlot = getArmorSlot(itemName);

        // Determine the correct equipment slot based on armor piece
        net.minecraft.component.type.AttributeModifierSlot equipmentSlot;
        if (itemName.contains("helmet")) {
            equipmentSlot = net.minecraft.component.type.AttributeModifierSlot.HEAD;
        } else if (itemName.contains("chestplate")) {
            equipmentSlot = net.minecraft.component.type.AttributeModifierSlot.CHEST;
        } else if (itemName.contains("leggings")) {
            equipmentSlot = net.minecraft.component.type.AttributeModifierSlot.LEGS;
        } else if (itemName.contains("boots")) {
            equipmentSlot = net.minecraft.component.type.AttributeModifierSlot.FEET;
        } else {
            equipmentSlot = slot; // fallback to original slot
        }

        if (itemName.contains("leather")) {
            // Leather: Evasion Rating + Movement Speed
            double evasionBonus = getLeatherEvasionBonus(itemName);
            builder.add(
                    ModAttributes.EVASION_RATING,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.evasion." + armorSlot),
                            evasionBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            double movementSpeed = getLeatherMovementSpeed(itemName);
            builder.add(
                    ModAttributes.MOVEMENT_SPEED,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.movement_speed." + armorSlot),
                            movementSpeed,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

        } else if (itemName.contains("golden")) {
            // Gold: Energy Shield + Luck
            double energyShieldBonus = getGoldEnergyShieldBonus(itemName);
            builder.add(
                    ModAttributes.ENERGY_SHIELD,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.energy_shield." + armorSlot),
                            energyShieldBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            double luck = getGoldLuck(itemName);
            builder.add(
                    ModAttributes.LUCK,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.luck." + armorSlot),
                            luck,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

        } else if (itemName.contains("chainmail")) {
            // Chainmail: Evasion Rating + Energy Shield + Attack Speed
            double evasionBonus = getChainmailEvasionBonus(itemName);
            builder.add(
                    ModAttributes.EVASION_RATING,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.evasion." + armorSlot),
                            evasionBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            double energyShieldBonus = getChainmailEnergyShieldBonus(itemName);
            builder.add(
                    ModAttributes.ENERGY_SHIELD,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.energy_shield." + armorSlot),
                            energyShieldBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            double attackSpeed = getChainmailAttackSpeed(itemName);
            builder.add(
                    EntityAttributes.ATTACK_SPEED,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.attack_speed." + armorSlot),
                            attackSpeed,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

        } else if (itemName.contains("copper")) {
            // Copper: Armor + 10% Elemental Resistances
            double fireRes = getCopperElementalResistance(itemName);
            builder.add(
                    ModAttributes.FIRE_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.fire_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            builder.add(
                    ModAttributes.COLD_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.cold_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            builder.add(
                    ModAttributes.LIGHTNING_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.lightning_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            builder.add(
                    ModAttributes.VOID_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.void_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

        } else if (itemName.contains("iron")) {
            // Iron: Armor + Armor Toughness + Knockback Resistance
            double knockbackRes = getIronKnockbackResistance(itemName);
            builder.add(
                    ModAttributes.KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.knockback_resistance." + armorSlot),
                            knockbackRes,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

        } else if (itemName.contains("diamond")) {
            // Diamond: Armor + Armor Toughness + HP + HP Regen
            double maxHealth = getDiamondMaxHealth(itemName);
            builder.add(
                    EntityAttributes.MAX_HEALTH,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.max_health." + armorSlot),
                            maxHealth,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

            double hpRegen = getDiamondHPRegen(itemName);
            builder.add(
                    ModAttributes.LIFE_REGENERATION,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.hp_regen." + armorSlot),
                            hpRegen,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );

        } else if (itemName.contains("netherite")) {
            // Netherite: Armor + Armor Toughness + Attack Damage
            double attackDamage = getNetheriteAttackDamage(itemName);
            builder.add(
                    ModAttributes.BONUS_ATTACK_DAMAGE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.attack_damage." + armorSlot),
                            attackDamage,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    equipmentSlot
            );
        }
    }

    private String getArmorSlot(String itemName) {
        if (itemName.contains("helmet")) return "helmet";
        if (itemName.contains("chestplate")) return "chestplate";
        if (itemName.contains("leggings")) return "leggings";
        if (itemName.contains("boots")) return "boots";
        return "unknown";
    }

    private boolean shouldModify(String itemName) {
        return isArmor(itemName) || isWeapon(itemName);
    }

    private boolean isArmor(String itemName) {
        return (itemName.contains("helmet") || itemName.contains("chestplate") ||
                itemName.contains("leggings") || itemName.contains("boots")) &&
                (itemName.contains("leather") || itemName.contains("chainmail") ||
                        itemName.contains("copper") || itemName.contains("iron") ||
                        itemName.contains("golden") || itemName.contains("diamond") ||
                        itemName.contains("netherite"));
    }

    private boolean isWeapon(String itemName) {
        // Handle mace and trident separately (they don't have material prefixes)
        if (itemName.contains("mace") || itemName.contains("trident")) {
            return true;
        }

        // Handle regular weapons (swords, axes, spears) with material prefixes
        return (itemName.contains("sword") || itemName.contains("spear")) &&
                (itemName.contains("wood") || itemName.contains("stone") ||
                        itemName.contains("copper") || itemName.contains("iron") ||
                        itemName.contains("golden") || itemName.contains("diamond") ||
                        itemName.contains("netherite"));
    }

    // Returns true for armor sets that use traditional armor/toughness stats
    private boolean usesTraditionalArmor(String itemName) {
        return itemName.contains("copper") || itemName.contains("iron") ||
                itemName.contains("diamond") || itemName.contains("netherite");
    }

    private double getArmorMultiplier(String itemName) {
        if (itemName.contains("copper")) return 2.0;
        if (itemName.contains("iron")) return 2.0;
        if (itemName.contains("diamond")) return 2.0;
        if (itemName.contains("netherite")) return 2.0;
        return 1.0;
    }

    private double getToughnessBonus(String itemName) {
        if (itemName.contains("copper")) return 3.0;
        if (itemName.contains("iron")) return 2.0;
        if (itemName.contains("diamond")) return 3.0;
        if (itemName.contains("netherite")) return 4.0;
        return 0.0;
    }

    // ========== LEATHER: Evasion Rating + Movement Speed ==========
    private double getLeatherEvasionBonus(String itemName) {
        if (itemName.contains("helmet")) return 20.0;
        if (itemName.contains("chestplate")) return 35.0;
        if (itemName.contains("leggings")) return 25.0;
        if (itemName.contains("boots")) return 20.0;
        return 0.0;
    }

    private double getLeatherMovementSpeed(String itemName) {
        if (itemName.contains("helmet")) return 0.05;
        if (itemName.contains("chestplate")) return 0.1;
        if (itemName.contains("leggings")) return 0.1;
        if (itemName.contains("boots")) return 0.2;
        return 0.0;
    }

    // ========== GOLD: Energy Shield + Luck ==========
    private double getGoldEnergyShieldBonus(String itemName) {
        if (itemName.contains("helmet")) return 4.0;
        if (itemName.contains("chestplate")) return 7.0;
        if (itemName.contains("leggings")) return 5.0;
        if (itemName.contains("boots")) return 4.0;
        return 0.0;
    }

    private double getGoldLuck(String itemName) {
        if (itemName.contains("helmet")) return 1.0;
        if (itemName.contains("chestplate")) return 2.0;
        if (itemName.contains("leggings")) return 1.0;
        if (itemName.contains("boots")) return 1.0;
        return 0.0;
    }

    // ========== CHAINMAIL: Evasion Rating + Energy Shield + Attack Speed ==========
    private double getChainmailEvasionBonus(String itemName) {
        if (itemName.contains("helmet")) return 10.0;
        if (itemName.contains("chestplate")) return 20.0;
        if (itemName.contains("leggings")) return 20.0;
        if (itemName.contains("boots")) return 10.0;
        return 0.0;
    }

    private double getChainmailEnergyShieldBonus(String itemName) {
        if (itemName.contains("helmet")) return 2.0;
        if (itemName.contains("chestplate")) return 4.0;
        if (itemName.contains("leggings")) return 3.0;
        if (itemName.contains("boots")) return 1.0;
        return 0.0;
    }

    private double getChainmailAttackSpeed(String itemName) {
        if (itemName.contains("helmet")) return 0.07;
        if (itemName.contains("chestplate")) return 0.1;
        if (itemName.contains("leggings")) return 0.07;
        if (itemName.contains("boots")) return 0.06;
        return 0.0;
    }

    // ========== COPPER: 10% Elemental Resistances ==========
    private double getCopperElementalResistance(String itemName) {
        // Returns 10% per piece (0.10)
        if (itemName.contains("helmet")) return 0.07;
        if (itemName.contains("chestplate")) return 0.10;
        if (itemName.contains("leggings")) return 0.09;
        if (itemName.contains("boots")) return 0.07;
        return 0.0;
    }

    // ========== IRON: Knockback Resistance ==========
    private double getIronKnockbackResistance(String itemName) {
        if (itemName.contains("helmet")) return 0.03;
        if (itemName.contains("chestplate")) return 0.07;
        if (itemName.contains("leggings")) return 0.06;
        if (itemName.contains("boots")) return 0.04;
        return 0.0;
    }

    // ========== DIAMOND: Max Health + HP Regen ==========
    private double getDiamondMaxHealth(String itemName) {
        if (itemName.contains("helmet")) return 2.0;
        if (itemName.contains("chestplate")) return 4.0;
        if (itemName.contains("leggings")) return 3.0;
        if (itemName.contains("boots")) return 1.0;
        return 0.0;
    }

    private double getDiamondHPRegen(String itemName) {
        if (itemName.contains("helmet")) return 0.15;
        if (itemName.contains("chestplate")) return 0.3;
        if (itemName.contains("leggings")) return 0.2;
        if (itemName.contains("boots")) return 0.1;
        return 0.0;
    }

    // ========== NETHERITE: Attack Damage ==========
    private double getNetheriteAttackDamage(String itemName) {
        if (itemName.contains("helmet")) return 1.0;
        if (itemName.contains("chestplate")) return 2.0;
        if (itemName.contains("leggings")) return 2.0;
        if (itemName.contains("boots")) return 1.0;
        return 0.0;
    }

    private double getDamageBonus(String itemName) {
        // Swords and Axes
        if (itemName.contains("sword") || itemName.contains("axe")) {
            if (itemName.contains("wood")) return 2.0;
            if (itemName.contains("stone")) return 2.0;
            if (itemName.contains("copper")) return 2.0;
            if (itemName.contains("iron")) return 2.0;
            if (itemName.contains("golden")) return 2.0;
            if (itemName.contains("diamond")) return 3.0;
            if (itemName.contains("netherite")) return 4.0;
        }

        // Spears - Higher damage than swords
        if (itemName.contains("spear")) {
            if (itemName.contains("wood")) return 3.0;
            if (itemName.contains("stone")) return 3.0;
            if (itemName.contains("copper")) return 3.0;
            if (itemName.contains("iron")) return 3.0;
            if (itemName.contains("golden")) return 3.0;
            if (itemName.contains("diamond")) return 4.0;
            if (itemName.contains("netherite")) return 5.0;
        }

        if (itemName.contains("mace")) {
            return 4.0;
        }

        if (itemName.contains("trident")) {
            return 3.0;
        }

        return 0.0;
    }
}