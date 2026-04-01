package com.cranks.dungeons.equipment;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
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
        Set<RegistryEntry<EntityAttribute>> customStatAttributes = new HashSet<>();

        for (ItemStatManager.ItemStat itemStat : stats) {
            CustomStat stat = StatRegistry.getStat(itemStat.statId);
            if (stat != null && !stat.getAttribute().equals(EntityAttributes.ATTACK_SPEED)) {
                customStatAttributes.add(stat.getAttribute());
            }
        }

        Map<RegistryEntry<EntityAttribute>, Double> defaultValues = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, AttributeModifierSlot> defaultSlots = new HashMap<>();

        for (var entry : originalDefaults.modifiers()) {
            if (customStatAttributes.contains(entry.attribute())) {
                defaultValues.put(entry.attribute(), entry.modifier().value());
                defaultSlots.put(entry.attribute(), entry.slot());
            } else {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        for (ItemStatManager.ItemStat itemStat : stats) {
            CustomStat stat = StatRegistry.getStat(itemStat.statId);
            if (stat != null) {
                if (stat.getAttribute().equals(EntityAttributes.ATTACK_SPEED)) {
                    continue;
                }

                Identifier modifierId = Identifier.of(
                        CranksDungeons.MOD_ID,
                        "item_" + stat.getId()
                );

                double defaultValue = defaultValues.getOrDefault(stat.getAttribute(), 0.0);
                AttributeModifierSlot modifierSlot = defaultSlots.getOrDefault(stat.getAttribute(), slot);
                double combinedValue = defaultValue + itemStat.value;

                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        modifierId,
                        combinedValue,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );

                builder.add(stat.getAttribute(), modifier, modifierSlot);
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
        if (stat.getAttribute().equals(EntityAttributes.ATTACK_SPEED)) {
            return;
        }
        Identifier modifierId = Identifier.of(CranksDungeons.MOD_ID, "equipment_" + stat.getId());
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                modifierId,
                value,
                EntityAttributeModifier.Operation.ADD_VALUE
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