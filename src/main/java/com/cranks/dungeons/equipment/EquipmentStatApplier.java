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

        Map<String, Double> totalArmorStats = new HashMap<>();

        for (ItemStack stack : armorItems) {
            List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
            for (ItemStatManager.ItemStat itemStat : stats) {
                totalArmorStats.merge(itemStat.statId, itemStat.value, Double::sum);
            }
        }

        for (Map.Entry<String, Double> entry : totalArmorStats.entrySet()) {
            String statId = entry.getKey();
            double totalValue = entry.getValue();

            CustomStat stat = StatRegistry.getStat(statId);
            if (stat != null && totalValue != 0) {
                applyStatModifier(player, stat, totalValue);
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

        if (stats.isEmpty()) {
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

        Map<RegistryEntry<EntityAttribute>, Double> flatRuneValues = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, CustomStat> attributeToStatMap = new HashMap<>();

        for (ItemStatManager.ItemStat itemStat : stats) {
            CustomStat stat = StatRegistry.getStat(itemStat.statId);
            if (stat != null) {
                RegistryEntry<EntityAttribute> attr = stat.getAttribute();
                if (attr.equals(ModAttributes.ATTACK_DAMAGE_PERCENT)) localAttackDamagePercent += itemStat.value;
                else if (attr.equals(ModAttributes.ARMOR_PERCENT)) localArmorPercent += itemStat.value;
                else if (attr.equals(ModAttributes.EVASION_RATING_PERCENT)) localEvasionPercent += itemStat.value;
                else if (attr.equals(ModAttributes.ENERGY_SHIELD_PERCENT)) localEnergyShieldPercent += itemStat.value;
                else {
                    flatRuneValues.merge(attr, itemStat.value, Double::sum);
                    attributeToStatMap.put(attr, stat);
                }
            }
        }

        // 2. Gather default values from the original item
        Map<RegistryEntry<EntityAttribute>, Double> finalFlatValues = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, AttributeModifierSlot> defaultSlots = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> unhandledDefaults = new HashMap<>();

        for (var entry : originalDefaults.modifiers()) {
            RegistryEntry<EntityAttribute> attr = entry.attribute();
            if (attr.equals(EntityAttributes.ATTACK_DAMAGE) ||
                attr.equals(EntityAttributes.ARMOR) ||
                attr.equals(ModAttributes.EVASION_RATING) ||
                attr.equals(ModAttributes.ENERGY_SHIELD) ||
                flatRuneValues.containsKey(attr)) {
                
                finalFlatValues.put(attr, entry.modifier().value());
                defaultSlots.put(attr, entry.slot());
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
        if (localAttackDamagePercent > 0 && finalFlatValues.containsKey(EntityAttributes.ATTACK_DAMAGE)) {
            finalFlatValues.put(EntityAttributes.ATTACK_DAMAGE, finalFlatValues.get(EntityAttributes.ATTACK_DAMAGE) * (1.0 + localAttackDamagePercent));
        }
        if (localArmorPercent > 0 && finalFlatValues.containsKey(EntityAttributes.ARMOR)) {
            finalFlatValues.put(EntityAttributes.ARMOR, finalFlatValues.get(EntityAttributes.ARMOR) * (1.0 + localArmorPercent));
        }
        if (localEvasionPercent > 0 && finalFlatValues.containsKey(ModAttributes.EVASION_RATING)) {
            finalFlatValues.put(ModAttributes.EVASION_RATING, finalFlatValues.get(ModAttributes.EVASION_RATING) * (1.0 + localEvasionPercent));
        }
        if (localEnergyShieldPercent > 0 && finalFlatValues.containsKey(ModAttributes.ENERGY_SHIELD)) {
            finalFlatValues.put(ModAttributes.ENERGY_SHIELD, finalFlatValues.get(ModAttributes.ENERGY_SHIELD) * (1.0 + localEnergyShieldPercent));
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

            boolean isMultiplicative = attr.equals(EntityAttributes.ATTACK_SPEED) || 
                                       attr.equals(ModAttributes.RELOAD_SPEED) ||
                                       attr.equals(ModAttributes.MINING_EFFICIENCY);

            if (isMultiplicative) {
                double defaultValue = unhandledDefaults.containsKey(attr) ? 0 : originalDefaults.modifiers().stream().filter(m -> m.attribute().equals(attr)).mapToDouble(m -> m.modifier().value()).findFirst().orElse(0.0);
                double runeValue = flatRuneValues.getOrDefault(attr, 0.0);
                
                if (defaultValue != 0) {
                    builder.add(attr, new EntityAttributeModifier(
                            modifierId.withPath(modifierId.getPath() + "_base"),
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
                            modifierId,
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

    private static boolean isArmorType(EquipmentType type) {
        return type == EquipmentType.HELMET ||
                type == EquipmentType.CHESTPLATE ||
                type == EquipmentType.ELYTRA ||
                type == EquipmentType.LEGGINGS ||
                type == EquipmentType.BOOTS;
    }

    public static double getItemStatValue(ItemStack stack, String baseStatId) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        for (ItemStatManager.ItemStat itemStat : stats) {
            if (itemStat.statId.equals(baseStatId) || itemStat.statId.endsWith("_" + baseStatId)) {
                return itemStat.value;
            }
        }
        return 0.0;
    }

    private static void applyStatModifier(PlayerEntity player, CustomStat stat, double value) {
        Identifier modifierId = Identifier.of(CranksDungeons.MOD_ID, "equipment_" + stat.getId());
        boolean isAttackSpeed = stat.getAttribute().equals(EntityAttributes.ATTACK_SPEED);
        
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                modifierId,
                value,
                isAttackSpeed ? EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL : EntityAttributeModifier.Operation.ADD_VALUE
        );

        var attributeInstance = player.getAttributeInstance(stat.getAttribute());
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifierId);
            attributeInstance.addTemporaryModifier(modifier);
            appliedModifiers.computeIfAbsent(player.getUuid(), k -> new HashMap<>()).put(modifierId, stat.getAttribute());
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