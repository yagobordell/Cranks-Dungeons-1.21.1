package com.cranks.dungeons.equipment;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.*;

public class EquipmentStatApplier {

    private static final Map<UUID, Map<Identifier, RegistryEntry<EntityAttribute>>> appliedModifiers = new HashMap<>();

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    /**
     * Updates player stats from equipped armor.
     * FIXED: Now ignores items that already have these stats applied via updateItemAttributes
     * to prevent doubling.
     */
    public static void updatePlayerStats(PlayerEntity player) {
        removeAllModifiers(player);

        List<ItemStack> armorItems = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                // If the item has custom attributes set, Minecraft applies them automatically.
                // We ONLY add stats here if they AREN'T already in the item's component.
                if (!stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
                    armorItems.add(stack);
                }
            }
        }

        Map<String, double[]> totalArmorStats = new HashMap<>();

        for (ItemStack stack : armorItems) {
            List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
            for (ItemStatManager.ItemStat itemStat : stats) {
                totalArmorStats.compute(itemStat.statId, (key, oldVal) -> {
                    if (oldVal == null) return itemStat.values.clone();
                    for (int i = 0; i < oldVal.length && i < itemStat.values.length; i++) {
                        oldVal[i] += itemStat.values[i];
                    }
                    return oldVal;
                });
            }
        }

        for (Map.Entry<String, double[]> entry : totalArmorStats.entrySet()) {
            String statId = entry.getKey();
            double[] totalValues = entry.getValue();

            CustomStat stat = StatRegistry.getStat(statId);
            if (stat != null) {
                applyStatModifier(player, stat, totalValues);
            }
        }
    }

    /**
     * Updates an item's attribute modifiers based on its custom stats.
     */
    public static void updateItemAttributes(ItemStack stack) {
        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        if (equipType.isEmpty()) {
            return;
        }

        EquipmentType type = equipType.get();
        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);

        ItemStack defaultStack = new ItemStack(stack.getItem());
        AttributeModifiersComponent originalDefaults = defaultStack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                AttributeModifiersComponent.DEFAULT
        );

        int quality = com.cranks.dungeons.block.entity.QualityStationBlockEntity.getQuality(stack);

        if (stats.isEmpty() && quality == 0) {
            stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            return;
        }

        AttributeModifierSlot slot = getSlotForEquipmentType(type);
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();

        // 1. Gather local percent modifiers and identify flat runes
        double localAttackDamagePercent = 0;
        double localArmorPercent = 0;
        double localEvasionPercent = 0;
        double localEnergyShieldPercent = 0;

        if (quality > 0) {
            // Quality is no longer added to local percent here.
            // It scales the base item attributes uniformly below.
        }

        Map<RegistryEntry<EntityAttribute>, Double> flatRuneValues = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, CustomStat> attributeToStatMap = new HashMap<>();

        for (ItemStatManager.ItemStat itemStat : stats) {
            CustomStat stat = StatRegistry.getStat(itemStat.statId);
            if (stat != null) {
                for (int i = 0; i < stat.getComponents().size(); i++) {
                    CustomStat.StatComponent comp = stat.getComponents().get(i);
                    double val = itemStat.getValue(i);

                    for (int attrIndex = 0; attrIndex < comp.getAttributes().size(); attrIndex++) {
                        RegistryEntry<EntityAttribute> attribute = comp.getAttributes().get(attrIndex);

                        // Skip stats that shouldn't be applied to standard vanilla attributes
                        if (attribute.equals(ModAttributes.DURABILITY_BONUS)) continue;
                        if (attribute.equals(ModAttributes.CHANCE_TO_BLOCK)) continue;
                        if (attribute.equals(ModAttributes.BLOCK_VALUE)) continue;
                        if (attribute.equals(ModAttributes.HEALTH_ON_BLOCK)) continue;
                        if (attribute.equals(ModAttributes.EVASION_RATING)) continue;
                        if (attribute.equals(ModAttributes.EVASION_RATING_PERCENT)) continue;
                        if (attribute.equals(ModAttributes.ENERGY_SHIELD)) continue;
                        if (attribute.equals(ModAttributes.ENERGY_SHIELD_PERCENT)) continue;

                        Identifier id = Identifier.of(CranksDungeons.MOD_ID, "runic_" + stat.getId() + "_" + i + "_" + attrIndex);
                        
                        EntityAttributeModifier modifier = new EntityAttributeModifier(
                                id,
                                val,
                                comp.isPercentage() ? EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE : EntityAttributeModifier.Operation.ADD_VALUE
                        );

                        builder.add(attribute, modifier, slot);
                    }

                    if (stat.getId().equals("local_attack_damage_percent")) localAttackDamagePercent += val;
                    else if (stat.getId().equals("local_armor_percent")) localArmorPercent += val;
                    else if (stat.getId().equals("local_evasion_percent")) localEvasionPercent += val;
                    else if (stat.getId().equals("local_energy_shield_percent")) localEnergyShieldPercent += val;

                    for (int attrIndex = 0; attrIndex < comp.getAttributes().size(); attrIndex++) {
                        RegistryEntry<EntityAttribute> attribute = comp.getAttributes().get(attrIndex);
                        
                        // Don't add skipped attributes to flatRuneValues to avoid double dipping, 
                        // as they are handled elsewhere or shouldn't be added to the item modifier list twice
                        if (attribute.equals(ModAttributes.DURABILITY_BONUS)) continue;
                        if (attribute.equals(ModAttributes.CHANCE_TO_BLOCK)) continue;
                        if (attribute.equals(ModAttributes.BLOCK_VALUE)) continue;
                        if (attribute.equals(ModAttributes.HEALTH_ON_BLOCK)) continue;

                        flatRuneValues.merge(attribute, val, Double::sum);
                        attributeToStatMap.put(attribute, stat);
                    }
                }
            }
        }

        // 2. Gather default values from the original item
        Map<RegistryEntry<EntityAttribute>, Double> finalFlatValues = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, AttributeModifierSlot> defaultSlots = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, Identifier> originalIds = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> unhandledDefaults = new HashMap<>();

        RegistryEntry<EntityAttribute> attackDamageAttr = null;
        RegistryEntry<EntityAttribute> armorAttr = null;
        RegistryEntry<EntityAttribute> evasionAttr = null;
        RegistryEntry<EntityAttribute> energyShieldAttr = null;

        double qBonus = quality > 0 ? quality / 100.0 : 0.0;
        Map<RegistryEntry<EntityAttribute>, Double> scaledDefaults = new HashMap<>();

        for (var entry : originalDefaults.modifiers()) {
            RegistryEntry<EntityAttribute> attr = entry.attribute();
            
            boolean isAttack = isAttribute(attr, EntityAttributes.ATTACK_DAMAGE);
            boolean isArmor = isAttribute(attr, EntityAttributes.ARMOR);
            boolean isEvasion = isAttribute(attr, ModAttributes.EVASION_RATING);
            boolean isES = isAttribute(attr, ModAttributes.ENERGY_SHIELD);

            if (isAttack) attackDamageAttr = attr;
            if (isArmor) armorAttr = attr;
            if (isEvasion) evasionAttr = attr;
            if (isES) energyShieldAttr = attr;

            if (entry.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
                double baseVal = entry.modifier().value();
                
                // Apply Quality Scaling uniformly to the base value
                if (qBonus > 0) {
                    if (attr.equals(EntityAttributes.ATTACK_SPEED)) {
                        baseVal = (4.0 + baseVal) * (1.0 + qBonus) - 4.0;
                    } else {
                        baseVal = baseVal * (1.0 + qBonus);
                    }
                }
                scaledDefaults.put(attr, baseVal);

                if (isAttack || isArmor || isEvasion || isES || flatRuneValues.containsKey(attr)) {
                    finalFlatValues.put(attr, baseVal);
                    defaultSlots.put(attr, entry.slot());
                    originalIds.put(attr, entry.modifier().id());
                } else {
                    unhandledDefaults.put(attr, new EntityAttributeModifier(
                            entry.modifier().id(),
                            baseVal,
                            entry.modifier().operation()
                    ));
                    defaultSlots.put(attr, entry.slot());
                }
            } else {
                unhandledDefaults.put(attr, entry.modifier());
                defaultSlots.put(attr, entry.slot());
            }
        }

        // Add rune flat values to the final flat values
        for (var entry : flatRuneValues.entrySet()) {
            finalFlatValues.merge(entry.getKey(), entry.getValue(), Double::sum);
        }

        // 3. Apply Local Percentages
        if (localAttackDamagePercent > 0 && attackDamageAttr != null && finalFlatValues.containsKey(attackDamageAttr)) {
            double oldVal = finalFlatValues.get(attackDamageAttr);
            double newVal = oldVal * (1.0 + localAttackDamagePercent);
            System.out.println("Updating attack damage from " + oldVal + " to " + newVal);
            finalFlatValues.put(attackDamageAttr, newVal);
        }
        if (localArmorPercent > 0 && armorAttr != null && finalFlatValues.containsKey(armorAttr)) {
            finalFlatValues.put(armorAttr, finalFlatValues.get(armorAttr) * (1.0 + localArmorPercent));
        }
        if (localEvasionPercent > 0 && evasionAttr != null && finalFlatValues.containsKey(evasionAttr)) {
            finalFlatValues.put(evasionAttr, finalFlatValues.get(evasionAttr) * (1.0 + localEvasionPercent));
        }
        if (localEnergyShieldPercent > 0 && energyShieldAttr != null && finalFlatValues.containsKey(energyShieldAttr)) {
            finalFlatValues.put(energyShieldAttr, finalFlatValues.get(energyShieldAttr) * (1.0 + localEnergyShieldPercent));
        }

        // 4. Build Modifiers
        // Re-add unhandled defaults
        for (var entry : unhandledDefaults.entrySet()) {
            builder.add(entry.getKey(), entry.getValue(), defaultSlots.getOrDefault(entry.getKey(), slot));
        }

        // Add calculated attributes
        for (var entry : finalFlatValues.entrySet()) {
            RegistryEntry<EntityAttribute> attr = entry.getKey();
            double combinedValue = entry.getValue();
            AttributeModifierSlot modifierSlot = defaultSlots.getOrDefault(attr, slot);
            CustomStat stat = attributeToStatMap.get(attr);
            String statId = stat != null ? stat.getId() : attr.value().getTranslationKey().replace("attribute.name.", "");
            
            Identifier modifierId = Identifier.of(CranksDungeons.MOD_ID, "item_" + statId.replace(".", "_"));
            Identifier baseModifierId = originalIds.get(attr);
            Identifier finalModifierId = baseModifierId != null ? baseModifierId : modifierId;

            boolean isMultiplicative = attr.equals(EntityAttributes.ATTACK_SPEED) || 
                                       attr.equals(ModAttributes.RELOAD_SPEED) ||
                                       attr.equals(ModAttributes.MINING_EFFICIENCY);

            if (isMultiplicative) {
                double defaultValue = scaledDefaults.getOrDefault(attr, 0.0);
                double runeValue = flatRuneValues.getOrDefault(attr, 0.0);
                
                if (defaultValue != 0) {
                    builder.add(attr, new EntityAttributeModifier(
                            finalModifierId,
                            defaultValue,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ), modifierSlot);
                }

                if (runeValue != 0) {
                    builder.add(attr, new EntityAttributeModifier(
                            modifierId.withPath(modifierId.getPath() + "_rune"),
                            runeValue,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ), modifierSlot);
                }
            } else {
                if (combinedValue != 0) {
                    EntityAttributeModifier modifier = new EntityAttributeModifier(
                            finalModifierId,
                            combinedValue,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    );
                    builder.add(attr, modifier, modifierSlot);
                }
            }
        }

        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static AttributeModifierSlot getSlotForEquipmentType(EquipmentType type) {
        return switch (type) {
            case HELMET -> AttributeModifierSlot.HEAD;
            case CHESTPLATE, ELYTRA -> AttributeModifierSlot.CHEST;
            case LEGGINGS -> AttributeModifierSlot.LEGS;
            case BOOTS -> AttributeModifierSlot.FEET;
            case SHIELD -> AttributeModifierSlot.OFFHAND;
            default -> AttributeModifierSlot.MAINHAND;
        };
    }

    public static boolean isArmorType(EquipmentType type) {
        return type == EquipmentType.HELMET ||
                type == EquipmentType.CHESTPLATE ||
                type == EquipmentType.ELYTRA ||
                type == EquipmentType.LEGGINGS ||
                type == EquipmentType.BOOTS;
    }

    private static boolean isAttribute(RegistryEntry<EntityAttribute> attr1, RegistryEntry<EntityAttribute> attr2) {
        if (attr1.equals(attr2)) return true;
        return attr1.value().getTranslationKey().equals(attr2.value().getTranslationKey());
    }

    public static double getItemStatValue(ItemStack stack, String baseStatId) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        for (ItemStatManager.ItemStat itemStat : stats) {
            if (itemStat.statId.equals(baseStatId) || itemStat.statId.endsWith("_" + baseStatId)) {
                return itemStat.getValue(0);
            }
        }
        return 0.0;
    }

    private static void applyStatModifier(PlayerEntity player, CustomStat stat, double[] values) {
        for (int i = 0; i < stat.getComponents().size(); i++) {
            CustomStat.StatComponent comp = stat.getComponents().get(i);
            double val = values != null && i < values.length ? values[i] : 0;
            if (val == 0) continue;

            for (int attrIndex = 0; attrIndex < comp.getAttributes().size(); attrIndex++) {
                RegistryEntry<EntityAttribute> attribute = comp.getAttributes().get(attrIndex);

                // For armor stats, we generally skip everything EXCEPT standard player attributes
                if (attribute.equals(ModAttributes.DURABILITY_BONUS)) continue;
                if (attribute.equals(ModAttributes.CHANCE_TO_BLOCK)) continue;
                if (attribute.equals(ModAttributes.BLOCK_VALUE)) continue;
                if (attribute.equals(ModAttributes.HEALTH_ON_BLOCK)) continue;

                Identifier id = Identifier.of(CranksDungeons.MOD_ID, "runic_" + stat.getId() + "_" + i + "_" + attrIndex);
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        id,
                        val,
                        comp.isPercentage() ? EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE : EntityAttributeModifier.Operation.ADD_VALUE
                );

                try {
                    player.getAttributes().getCustomInstance(attribute).addPersistentModifier(modifier);

                    Map<Identifier, RegistryEntry<EntityAttribute>> applied = appliedModifiers.computeIfAbsent(player.getUuid(), k -> new HashMap<>());
                    applied.put(id, attribute);
                } catch (Exception e) {
                    CranksDungeons.LOGGER.error("Failed to apply stat modifier {} to player {}", id, player.getName().getString(), e);
                }
            }
        }
    }

    private static void removeAllModifiers(PlayerEntity player) {
        Map<Identifier, RegistryEntry<EntityAttribute>> modifiers = appliedModifiers.get(player.getUuid());
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }

        for (Map.Entry<Identifier, RegistryEntry<EntityAttribute>> entry : modifiers.entrySet()) {
            Identifier modifierId = entry.getKey();
            RegistryEntry<EntityAttribute> attribute = entry.getValue();
            var attributeInstance = player.getAttributeInstance(attribute);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifierId);
            }
        }
        modifiers.clear();
    }

    public static void onPlayerDisconnect(PlayerEntity player) {
        appliedModifiers.remove(player.getUuid());
    }
}