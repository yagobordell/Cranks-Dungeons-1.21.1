package com.cranks.dungeons.item;

import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ItemComponentModifier {

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(item -> true, (builder, item) -> {
                String itemName = Registries.ITEM.getId(item).toString().toLowerCase();

                if (!shouldModify(itemName)) {
                    return;
                }

                AttributeModifiersComponent attributes = item.getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                if (attributes == null) {
                    attributes = AttributeModifiersComponent.DEFAULT;
                }

                AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder();
                boolean modified = false;

                if (isArmor(itemName)) {
                    // For armors in 1.21.1, the AttributeModifiersComponent might be empty natively. 
                    // So we manually rebuild it using the vanilla base values + our multipliers.
                    
                    if (usesTraditionalArmor(itemName)) {
                        double vanillaArmor = getVanillaArmor(itemName);
                        if (vanillaArmor > 0) {
                            double multiplier = getArmorMultiplier(itemName);
                            attrBuilder.add(
                                    EntityAttributes.GENERIC_ARMOR,
                                    new EntityAttributeModifier(
                                            Identifier.of("minecraft", "armor." + getArmorSlot(itemName)),
                                            vanillaArmor * multiplier,
                                            EntityAttributeModifier.Operation.ADD_VALUE),
                                    getEquipmentSlot(itemName));
                            modified = true;
                        }

                        double vanillaToughness = getVanillaToughness(itemName);
                        if (usesTraditionalArmorToughness(itemName)) {
                            vanillaToughness += getToughnessBonus(itemName); // Custom mod toughness
                        }
                        if (vanillaToughness > 0) {
                            attrBuilder.add(
                                    EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                                    new EntityAttributeModifier(
                                            Identifier.of("minecraft", "armor.toughness." + getArmorSlot(itemName)),
                                            vanillaToughness,
                                            EntityAttributeModifier.Operation.ADD_VALUE),
                                    getEquipmentSlot(itemName));
                            modified = true;
                        }

                        double vanillaKbRes = getVanillaKnockbackRes(itemName);
                        if (vanillaKbRes > 0) {
                            attrBuilder.add(
                                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                                    new EntityAttributeModifier(
                                            Identifier.of("minecraft", "armor.knockback_resistance." + getArmorSlot(itemName)),
                                            vanillaKbRes,
                                            EntityAttributeModifier.Operation.ADD_VALUE),
                                    getEquipmentSlot(itemName));
                            modified = true;
                        }
                    }

                    addArmorTypeBonuses(attrBuilder, itemName, getEquipmentSlot(itemName));
                    modified = true;

                } else if (isWeapon(itemName)) {
                    for (var entry : attributes.modifiers()) {
                        if (attributesMatch(entry.attribute(), EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
                            double value = entry.modifier().value();
                            if (isTool(itemName)) {
                                value = 0.0; // Total 1.0 (player base)
                            } else {
                                value += getDamageBonus(itemName);
                            }

                            attrBuilder.add(
                                    entry.attribute(),
                                    new EntityAttributeModifier(
                                            entry.modifier().id(),
                                            value,
                                            entry.modifier().operation()),
                                    entry.slot());
                            modified = true;
                        } else if (attributesMatch(entry.attribute(), EntityAttributes.GENERIC_ATTACK_SPEED)) {
                            double value = entry.modifier().value();
                            if (isTool(itemName)) {
                                value = -3.0; // Total 1.0 (4.0 base - 3.0)
                            } else {
                                value += getWeaponSpeedBonus(itemName);
                            }

                            attrBuilder.add(
                                    entry.attribute(),
                                    new EntityAttributeModifier(
                                            entry.modifier().id(),
                                            value,
                                            entry.modifier().operation()),
                                    entry.slot());
                            modified = true;
                        } else {
                            attrBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
                        }
                    }

                    if (isWeapon(itemName)) {
                        addWeaponNativeAttributes(attrBuilder, itemName);
                        modified = true;
                    }
                } else {
                    for (var entry : attributes.modifiers()) {
                        attrBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
                    }
                }

                if (itemName.contains("shield")) {
                    attrBuilder.add(ModAttributes.CHANCE_TO_BLOCK, new EntityAttributeModifier(Identifier.of("cranks-dungeons", "shield.chance_to_block"), 0.3, EntityAttributeModifier.Operation.ADD_VALUE), net.minecraft.component.type.AttributeModifierSlot.ANY);
                    attrBuilder.add(ModAttributes.BLOCK_VALUE, new EntityAttributeModifier(Identifier.of("cranks-dungeons", "shield.block_value"), 3.0, EntityAttributeModifier.Operation.ADD_VALUE), net.minecraft.component.type.AttributeModifierSlot.ANY);
                    modified = true;
                }

                if (modified) {
                    builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
                }
            });
        });
    }

    private static boolean attributesMatch(RegistryEntry<EntityAttribute> e1, RegistryEntry<EntityAttribute> e2) {
        if (e1 == e2) return true;
        if (e1 == null) {
            System.out.println("[DEBUG] e1 is null!");
            return false;
        }
        if (e2 == null) {
            System.out.println("[DEBUG] e2 is null! (e1 key=" + e1.value().getTranslationKey() + ")");
            return false;
        }
        if (e1.equals(e2)) return true;
        
        String key1 = e1.value().getTranslationKey();
        String key2 = e2.value().getTranslationKey();
        boolean match = key1.equals(key2);
        
        if (!match && key1.contains("armor")) {
            System.out.println("[DEBUG] attributesMatch failed! key1=" + key1 + ", key2=" + key2);
        }
        
        return match;
    }

    private static void addArmorTypeBonuses(AttributeModifiersComponent.Builder builder, String itemName,
            net.minecraft.component.type.AttributeModifierSlot slot) {
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
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            double movementSpeed = getLeatherMovementSpeed(itemName);
            builder.add(
                    ModAttributes.MOVEMENT_SPEED,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.movement_speed." + armorSlot),
                            movementSpeed,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

        } else if (itemName.contains("golden")) {
            // Gold: Energy Shield + Luck
            double energyShieldBonus = getGoldEnergyShieldBonus(itemName);
            builder.add(
                    ModAttributes.ENERGY_SHIELD,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.energy_shield." + armorSlot),
                            energyShieldBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            double luck = getGoldLuck(itemName);
            builder.add(
                    ModAttributes.ITEM_RARITY_PERCENT,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.luck." + armorSlot),
                            luck,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

        } else if (itemName.contains("chainmail")) {
            // Chainmail: Evasion Rating + Armor (from traditional) + Attack Speed
            double evasionBonus = getChainmailEvasionBonus(itemName);
            builder.add(
                    ModAttributes.EVASION_RATING,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.evasion." + armorSlot),
                            evasionBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            double attackSpeed = getChainmailAttackSpeed(itemName);
            builder.add(
                    EntityAttributes.GENERIC_ATTACK_SPEED,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.attack_speed." + armorSlot),
                            attackSpeed,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

        } else if (itemName.contains("copper")) {
            // Copper: Armor + 10% Elemental Resistances
            double fireRes = getCopperElementalResistance(itemName);
            builder.add(
                    ModAttributes.FIRE_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.fire_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            builder.add(
                    ModAttributes.COLD_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.cold_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            builder.add(
                    ModAttributes.LIGHTNING_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.lightning_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            builder.add(
                    ModAttributes.VOID_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.void_resistance." + armorSlot),
                            fireRes,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

        } else if (itemName.contains("iron")) {
            // Iron: Armor + Armor Toughness + Knockback Resistance
            double knockbackRes = getIronKnockbackResistance(itemName);
            builder.add(
                    ModAttributes.KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.knockback_resistance." + armorSlot),
                            knockbackRes,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

        } else if (itemName.contains("diamond")) {
            // Diamond: Armor + Armor Toughness + HP + HP Regen
            double maxHealth = getDiamondMaxHealth(itemName);
            builder.add(
                    EntityAttributes.GENERIC_MAX_HEALTH,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.max_health." + armorSlot),
                            maxHealth,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

            double hpRegen = getDiamondHPRegen(itemName);
            builder.add(
                    ModAttributes.LIFE_REGENERATION,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.hp_regen." + armorSlot),
                            hpRegen,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);

        } else if (itemName.contains("turtle")) {
            builder.add(
                    EntityAttributes.GENERIC_ARMOR,
                    new EntityAttributeModifier(
                            Identifier.of("minecraft", "armor." + armorSlot),
                            45.0,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);
        } else if (itemName.contains("elytra")) {
            builder.add(
                    ModAttributes.ENERGY_SHIELD,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "armor.energy_shield." + armorSlot),
                            90.0,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    equipmentSlot);
        }
    }

    private static String getArmorSlot(String itemName) {
        if (itemName.contains("helmet"))
            return "helmet";
        if (itemName.contains("chestplate") || itemName.contains("elytra"))
            return "chestplate";
        if (itemName.contains("leggings"))
            return "leggings";
        if (itemName.contains("boots"))
            return "boots";
        return "unknown";
    }

    private static boolean shouldModify(String itemName) {
        return isArmor(itemName) || isWeapon(itemName) || itemName.contains("shield");
    }

    private static boolean isArmor(String itemName) {
        if (itemName.contains("elytra")) return true;
        return (itemName.contains("helmet") || itemName.contains("chestplate") ||
                itemName.contains("leggings") || itemName.contains("boots")) &&
                (itemName.contains("leather") || itemName.contains("chainmail") ||
                        itemName.contains("copper") || itemName.contains("iron") ||
                        itemName.contains("golden") || itemName.contains("diamond") ||
                        itemName.contains("netherite") || itemName.contains("turtle"));
    }

    private static boolean isWeapon(String itemName) {
        // Handle names that clearly indicate weapon type
        if (itemName.contains("sword") || itemName.contains("spear") ||
                itemName.contains("mace") || itemName.contains("trident") ||
                itemName.contains("axe") || itemName.contains("dagger") ||
                itemName.contains("longsword") || itemName.contains("pike") ||
                itemName.contains("halberd") || itemName.contains("glaive") ||
                itemName.contains("bow") || itemName.contains("crossbow") ||
                itemName.contains("pickaxe") || itemName.contains("shovel") ||
                itemName.contains("hoe")) {
            return true;
        }

        return false;
    }

    private static boolean isTool(String itemName) {
        return itemName.contains("pickaxe") || itemName.contains("shovel") ||
                itemName.contains("hoe") || itemName.contains("axe");
    }

    private static boolean usesTraditionalArmor(String itemName) {
        return itemName.contains("copper") || itemName.contains("iron") ||
                itemName.contains("diamond") || itemName.contains("netherite") ||
                itemName.contains("chainmail") || itemName.contains("golden");
    }

    private static boolean usesTraditionalArmorToughness(String itemName) {
        return itemName.contains("iron") || itemName.contains("diamond") || itemName.contains("netherite");
    }

    private static double getArmorMultiplier(String itemName) {
        if (itemName.contains("copper"))
            return 5.0;
        if (itemName.contains("chainmail"))
            return 5.0;
        if (itemName.contains("golden"))
            return 5.0;
        if (itemName.contains("iron"))
            return 8.0;
        if (itemName.contains("diamond"))
            return 15.0;
        if (itemName.contains("netherite"))
            return 25.0;
        return 1.0;
    }

    private static double getToughnessBonus(String itemName) {
        if (itemName.contains("copper"))
            return 5.0;
        if (itemName.contains("iron"))
            return 10.0;
        if (itemName.contains("diamond"))
            return 20.0;
        if (itemName.contains("netherite"))
            return 35.0;
        return 0.0;
    }

    private static double getLeatherEvasionBonus(String itemName) {
        if (itemName.contains("helmet"))
            return 20.0;
        if (itemName.contains("chestplate"))
            return 35.0;
        if (itemName.contains("leggings"))
            return 25.0;
        if (itemName.contains("boots"))
            return 20.0;
        return 0.0;
    }

    private static double getLeatherMovementSpeed(String itemName) {
        if (itemName.contains("helmet"))
            return 0.05;
        if (itemName.contains("chestplate"))
            return 0.1;
        if (itemName.contains("leggings"))
            return 0.1;
        if (itemName.contains("boots"))
            return 0.2;
        return 0.0;
    }

    private static double getGoldEnergyShieldBonus(String itemName) {
        if (itemName.contains("helmet"))
            return 4.0;
        if (itemName.contains("chestplate"))
            return 7.0;
        if (itemName.contains("leggings"))
            return 5.0;
        if (itemName.contains("boots"))
            return 4.0;
        return 0.0;
    }

    private static double getGoldLuck(String itemName) {
        if (itemName.contains("helmet"))
            return 0.01;
        if (itemName.contains("chestplate"))
            return 0.03;
        if (itemName.contains("leggings"))
            return 0.02;
        if (itemName.contains("boots"))
            return 0.01;
        return 0.0;
    }

    private static double getChainmailEvasionBonus(String itemName) {
        if (itemName.contains("helmet"))
            return 10.0;
        if (itemName.contains("chestplate"))
            return 20.0;
        if (itemName.contains("leggings"))
            return 20.0;
        if (itemName.contains("boots"))
            return 10.0;
        return 0.0;
    }



    private static double getChainmailAttackSpeed(String itemName) {
        if (itemName.contains("helmet"))
            return 0.07;
        if (itemName.contains("chestplate"))
            return 0.1;
        if (itemName.contains("leggings"))
            return 0.07;
        if (itemName.contains("boots"))
            return 0.06;
        return 0.0;
    }

    private static double getCopperElementalResistance(String itemName) {
        if (itemName.contains("helmet"))
            return 0.07;
        if (itemName.contains("chestplate"))
            return 0.10;
        if (itemName.contains("leggings"))
            return 0.09;
        if (itemName.contains("boots"))
            return 0.07;
        return 0.0;
    }

    private static double getIronKnockbackResistance(String itemName) {
        if (itemName.contains("helmet"))
            return 0.03;
        if (itemName.contains("chestplate"))
            return 0.07;
        if (itemName.contains("leggings"))
            return 0.06;
        if (itemName.contains("boots"))
            return 0.04;
        return 0.0;
    }

    private static double getDiamondMaxHealth(String itemName) {
        if (itemName.contains("helmet"))
            return 2.0;
        if (itemName.contains("chestplate"))
            return 4.0;
        if (itemName.contains("leggings"))
            return 3.0;
        if (itemName.contains("boots"))
            return 1.0;
        return 0.0;
    }

    private static double getDiamondHPRegen(String itemName) {
        if (itemName.contains("helmet"))
            return 0.15;
        if (itemName.contains("chestplate"))
            return 0.3;
        if (itemName.contains("leggings"))
            return 0.2;
        if (itemName.contains("boots"))
            return 0.1;
        return 0.0;
    }

    private static double getWeaponSpeedBonus(String itemName) {
        if (itemName.contains("sword")) {
            if (itemName.contains("wood"))
                return -0.2;
            if (itemName.contains("stone"))
                return -0.1;
            if (itemName.contains("iron"))
                return 0.0;
            if (itemName.contains("golden"))
                return 0.1;
            if (itemName.contains("diamond"))
                return 0.1;
            if (itemName.contains("netherite"))
                return 0.2;
            return 0.0;
        }

        if (itemName.contains("spear")) {
            if (itemName.contains("wood"))
                return -0.8;
            if (itemName.contains("stone"))
                return -0.6;
            if (itemName.contains("iron"))
                return -0.4;
            if (itemName.contains("golden"))
                return -0.2;
            if (itemName.contains("diamond"))
                return 0.0;
            if (itemName.contains("netherite"))
                return 0.2;
            return -0.4;
        }

        if (itemName.contains("mace")) {
            return 0.2;
        }

        if (itemName.contains("trident")) {
            return -0.2;
        }

        return 0.0;
    }

    private static void addWeaponNativeAttributes(AttributeModifiersComponent.Builder builder, String itemName) {
        double rangeBonus = getWeaponRangeBonus(itemName);
        if (rangeBonus != 0) {
            builder.add(
                    ModAttributes.ATTACK_RANGE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "weapon.range"),
                            rangeBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
        }

        double knockbackBonus = getWeaponKnockbackBonus(itemName);
        if (knockbackBonus != 0) {
            builder.add(
                    ModAttributes.KNOCKBACK,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "weapon.knockback"),
                            knockbackBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);

            builder.add(
                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "weapon.vanilla_knockback"),
                            knockbackBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
        }

        if (itemName.contains("bow") || itemName.contains("crossbow")) {
            double baseDamage = getDamageBonus(itemName);
            builder.add(
                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "weapon.base_damage"),
                            baseDamage,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
            
            double baseReload = itemName.contains("crossbow") ? 0.4 : 0.7;
            builder.add(
                    EntityAttributes.GENERIC_ATTACK_SPEED,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "weapon.base_reload"),
                            (baseReload * 4.0) - 4.0,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
        }

        if (isTool(itemName)) {
            double miningSpeed = getMiningSpeedBonus(itemName);
            builder.add(
                    ModAttributes.MINING_EFFICIENCY,
                    new EntityAttributeModifier(
                            Identifier.of("cranks-dungeons", "tool.mining_speed"),
                            miningSpeed - 1.0,
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
        }
    }

    private static double getMiningSpeedBonus(String itemName) {
        if (itemName.contains("wood"))
            return 2.0;
        if (itemName.contains("stone"))
            return 4.0;
        if (itemName.contains("iron"))
            return 6.0;
        if (itemName.contains("diamond"))
            return 8.0;
        if (itemName.contains("netherite") || itemName.contains("gold"))
            return 12.0;
        return 2.0;
    }

    private static double getWeaponRangeBonus(String itemName) {
        if (itemName.contains("spear")) {
            if (itemName.contains("wood"))
                return 1.0;
            if (itemName.contains("stone"))
                return 1.25;
            if (itemName.contains("iron"))
                return 1.5;
            if (itemName.contains("diamond"))
                return 2.0;
            if (itemName.contains("netherite"))
                return 3.0;
            return 1.5;
        }

        if (itemName.contains("trident")) {
            return 1.5;
        }

        if (itemName.contains("mace")) {
            return 0.5;
        }

        if (itemName.contains("sword")) {
            if (itemName.contains("diamond"))
                return 0.1;
            if (itemName.contains("netherite"))
                return 0.2;
        }

        return 0.0;
    }

    private static double getWeaponKnockbackBonus(String itemName) {
        if (itemName.contains("mace")) {
            return 1.2;
        }

        if (itemName.contains("spear")) {
            if (itemName.contains("wood"))
                return 0.3;
            if (itemName.contains("stone"))
                return 0.4;
            if (itemName.contains("iron"))
                return 0.5;
            if (itemName.contains("diamond"))
                return 0.7;
            if (itemName.contains("netherite"))
                return 1.0;
            return 0.5;
        }

        return 0.0;
    }

    private static double getDamageBonus(String itemName) {
        if (itemName.contains("sword")) {
            if (itemName.contains("wood"))
                return 2.0;
            if (itemName.contains("stone"))
                return 2.0;
            if (itemName.contains("iron"))
                return 2.0;
            if (itemName.contains("golden"))
                return 3.0;
            if (itemName.contains("diamond"))
                return 3.0;
            if (itemName.contains("netherite"))
                return 4.0;
        }

        if (itemName.contains("axe")) {
            return 0.0;
        }

        if (itemName.contains("spear")) {
            if (itemName.contains("wood"))
                return 3.0;
            if (itemName.contains("stone"))
                return 3.5;
            if (itemName.contains("iron"))
                return 4.0;
            if (itemName.contains("golden"))
                return 5.0;
            if (itemName.contains("diamond"))
                return 6.0;
            if (itemName.contains("netherite"))
                return 8.0;
        }

        if (itemName.contains("mace")) {
            return 8.0;
        }

        if (itemName.contains("trident")) {
            return 4.0;
        }

        if (itemName.contains("crossbow")) {
            return 7.0;
        }

        if (itemName.contains("bow")) {
            return 5.0;
        }

        return 0.0;
    }

    private static double getVanillaArmor(String itemName) {
        if (itemName.contains("leather")) {
            if (itemName.contains("helmet")) return 1.0;
            if (itemName.contains("chestplate")) return 3.0;
            if (itemName.contains("leggings")) return 2.0;
            if (itemName.contains("boots")) return 1.0;
        } else if (itemName.contains("chainmail")) {
            if (itemName.contains("helmet")) return 2.0;
            if (itemName.contains("chestplate")) return 5.0;
            if (itemName.contains("leggings")) return 4.0;
            if (itemName.contains("boots")) return 1.0;
        } else if (itemName.contains("iron")) {
            if (itemName.contains("helmet")) return 2.0;
            if (itemName.contains("chestplate")) return 6.0;
            if (itemName.contains("leggings")) return 5.0;
            if (itemName.contains("boots")) return 2.0;
        } else if (itemName.contains("golden")) {
            if (itemName.contains("helmet")) return 2.0;
            if (itemName.contains("chestplate")) return 5.0;
            if (itemName.contains("leggings")) return 3.0;
            if (itemName.contains("boots")) return 1.0;
        } else if (itemName.contains("diamond") || itemName.contains("netherite")) {
            if (itemName.contains("helmet")) return 3.0;
            if (itemName.contains("chestplate")) return 8.0;
            if (itemName.contains("leggings")) return 6.0;
            if (itemName.contains("boots")) return 3.0;
        }
        return 0.0;
    }

    private static double getVanillaToughness(String itemName) {
        if (itemName.contains("diamond")) return 2.0;
        if (itemName.contains("netherite")) return 3.0;
        return 0.0;
    }

    private static double getVanillaKnockbackRes(String itemName) {
        if (itemName.contains("netherite")) return 0.1;
        return 0.0;
    }

    private static net.minecraft.component.type.AttributeModifierSlot getEquipmentSlot(String itemName) {
        if (itemName.contains("helmet")) return net.minecraft.component.type.AttributeModifierSlot.HEAD;
        if (itemName.contains("chestplate") || itemName.contains("elytra")) return net.minecraft.component.type.AttributeModifierSlot.CHEST;
        if (itemName.contains("leggings")) return net.minecraft.component.type.AttributeModifierSlot.LEGS;
        if (itemName.contains("boots")) return net.minecraft.component.type.AttributeModifierSlot.FEET;
        return net.minecraft.component.type.AttributeModifierSlot.MAINHAND;
    }
}
