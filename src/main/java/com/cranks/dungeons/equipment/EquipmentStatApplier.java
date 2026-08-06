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

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Central hub for applying rune/stat modifiers to equipment items.
 *
 * <h2>Stat Application Pipeline (updateItemAttributes)</h2>
 * <p>This is the core method that bakes rune stats into an item's AttributeModifiersComponent.
 * It runs whenever stats are added/removed/rerolled on an item.</p>
 *
 * <h3>Pipeline Steps:</h3>
 * <ol>
 *   <li><b>Classify rune stats</b> — Each rune stat is classified by its attribute type:
 *     <ul>
 *       <li><b>Local Percent</b> (ARMOR_PERCENT, EVASION_RATING_PERCENT, ENERGY_SHIELD_PERCENT,
 *           ATTACK_DAMAGE_PERCENT): Accumulated into local percent buffers. These scale the item's
 *           base stat value. They are NOT added as direct modifiers.
 *           <br><em>IMPORTANT: Detection is done by attribute type, NOT stat ID strings.</em></li>
 *       <li><b>Flat rune stats</b> (e.g., +Armor, +Evasion): Merged into flatRuneValues map.</li>
 *       <li><b>Other percentage stats</b> (e.g., resistances, crit): Added directly to the builder
 *           as ADD_VALUE modifiers (or ADD_MULTIPLIED_BASE for attack speed/reload speed).</li>
 *     </ul>
 *   </li>
 *   <li><b>Gather base item defaults</b> — Reads the vanilla item's original AttributeModifiersComponent,
 *       applies quality scaling, and separates core stats (armor/evasion/ES/damage) from others.</li>
 *   <li><b>Merge flat runes</b> — Flat rune values are added to the base values.</li>
 *   <li><b>Apply local percentages</b> — The accumulated percent buffers scale the combined
 *       (base + flat rune) values: finalValue = (base + flatRune) * (1 + localPercent).</li>
 *   <li><b>Build final component</b> — All values are written into a new AttributeModifiersComponent.</li>
 * </ol>
 *
 * <h3>Attribute Comparison</h3>
 * <p>Always use {@link #isAttribute} to compare RegistryEntry references. Fabric's registry
 * can return different object references for the same attribute, causing .equals() to fail.
 * The isAttribute method falls back to comparing translation keys.</p>
 *
 * <h3>Tooltip Interaction</h3>
 * <p>Because this class bakes flat rune values and local percentages into the item's
 * AttributeModifiersComponent, the tooltip (ItemStackTooltipMixin.calculateItemAttributeTotals)
 * must NOT re-add those same values from ItemStatManager. Doing so would double-count them.</p>
 */
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
     * Rebuilds an item's AttributeModifiersComponent to include rune stats and quality bonuses.
     * <p>This is the core stat pipeline — see class javadoc for the full flow.</p>
     * <p>Called by ItemStatManager whenever stats are added, removed, or rerolled on an item.</p>
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

                        // Detect local percentage stats by their attribute type.
                        // These scale the item's base stat and should NOT be added as direct modifiers.
                        boolean isLocalPercent = false;
                        if (isAttribute(attribute, ModAttributes.ATTACK_DAMAGE_PERCENT)) {
                            localAttackDamagePercent += val;
                            isLocalPercent = true;
                        } else if (isAttribute(attribute, ModAttributes.ARMOR_PERCENT)) {
                            localArmorPercent += val;
                            isLocalPercent = true;
                        } else if (isAttribute(attribute, ModAttributes.EVASION_RATING_PERCENT)) {
                            localEvasionPercent += val;
                            isLocalPercent = true;
                        } else if (isAttribute(attribute, ModAttributes.ENERGY_SHIELD_PERCENT)) {
                            localEnergyShieldPercent += val;
                            isLocalPercent = true;
                        }

                        // Skip local percent stats - they're applied later to scale base values
                        if (isLocalPercent) {
                            continue;
                        }

                        if (isAttribute(attribute, ModAttributes.DURABILITY_BONUS) ||
                            isAttribute(attribute, ModAttributes.CHANCE_TO_BLOCK) ||
                            isAttribute(attribute, ModAttributes.BLOCK_VALUE) ||
                            isAttribute(attribute, ModAttributes.HEALTH_ON_BLOCK)) {
                            continue;
                        }

                        if (comp.isPercentage()) {
                            Identifier id = Identifier.of(CranksDungeons.MOD_ID, "runic_" + stat.getId() + "_" + i + "_" + attrIndex);
                            boolean isMultiplicative = isAttribute(attribute, EntityAttributes.GENERIC_ATTACK_SPEED) || 
                                                       isAttribute(attribute, ModAttributes.MINING_EFFICIENCY);
                            EntityAttributeModifier.Operation op = isMultiplicative ? 
                                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE : 
                                    EntityAttributeModifier.Operation.ADD_VALUE;
                            EntityAttributeModifier modifier = new EntityAttributeModifier(
                                    id, val, op
                            );
                            builder.add(attribute, modifier, slot);
                        } else {
                            flatRuneValues.merge(attribute, val, Double::sum);
                            attributeToStatMap.put(attribute, stat);
                        }
                    }
                }
            }
        }

        // 1.5 Parse Enchantments and convert to local stats
        var enchantments = stack.getEnchantments();
        for (var enchEntry : enchantments.getEnchantmentEntries()) {
            var enchantment = enchEntry.getKey().value();
            int level = enchEntry.getIntValue();

            var attributeEffects = enchantment.effects().get(net.minecraft.component.EnchantmentEffectComponentTypes.ATTRIBUTES);
            if (attributeEffects != null) {
                for (net.minecraft.enchantment.effect.AttributeEnchantmentEffect effect : attributeEffects) {
                    RegistryEntry<EntityAttribute> attr = effect.attribute();
                    
                    boolean isWeaponAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeaponAttribute(attr);
                    boolean isArmorAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmorAttribute(attr);
                    boolean isWeapon = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeapon(stack);
                    boolean isArmor = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmor(stack);

                    if (isWeaponAttr && !isWeapon) continue;
                    if (isArmorAttr && !isArmor) continue;

                    double value = (double) effect.amount().getValue(level);
                    double effectiveness = ItemStatManager.getAttributeValue(stack, ModAttributes.ENCHANTMENT_EFFECTIVENESS);
                    if (effectiveness > 0) {
                        value *= (1.0 + effectiveness);
                    }

                    if (isAttribute(attr, ModAttributes.ATTACK_DAMAGE_PERCENT)) {
                        localAttackDamagePercent += value;
                    } else if (isAttribute(attr, ModAttributes.ARMOR_PERCENT)) {
                        localArmorPercent += value;
                    } else if (isAttribute(attr, ModAttributes.EVASION_RATING_PERCENT)) {
                        localEvasionPercent += value;
                    } else if (isAttribute(attr, ModAttributes.ENERGY_SHIELD_PERCENT)) {
                        localEnergyShieldPercent += value;
                    } else if (effect.operation() == EntityAttributeModifier.Operation.ADD_VALUE &&
                               !isAttribute(attr, EntityAttributes.GENERIC_ATTACK_SPEED) &&
                               !isAttribute(attr, ModAttributes.MINING_EFFICIENCY)) {
                        flatRuneValues.merge(attr, value, Double::sum);
                    } else {
                        EntityAttributeModifier.Operation op = effect.operation();
                        if (isAttribute(attr, ModAttributes.MINING_EFFICIENCY) && op == EntityAttributeModifier.Operation.ADD_VALUE) {
                            op = EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                        }
                        builder.add(attr, new EntityAttributeModifier(effect.id(), value, op), slot);
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
            
            boolean isAttack = isAttribute(attr, EntityAttributes.GENERIC_ATTACK_DAMAGE);
            boolean isArmor = isAttribute(attr, EntityAttributes.GENERIC_ARMOR);
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
                    if (isAttribute(attr, EntityAttributes.GENERIC_ATTACK_SPEED)) {
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

        RegistryEntry<EntityAttribute> actualAttackAttr = attackDamageAttr != null ? attackDamageAttr : EntityAttributes.GENERIC_ATTACK_DAMAGE;
        RegistryEntry<EntityAttribute> actualArmorAttr = armorAttr != null ? armorAttr : EntityAttributes.GENERIC_ARMOR;
        RegistryEntry<EntityAttribute> actualEvasionAttr = evasionAttr != null ? evasionAttr : ModAttributes.EVASION_RATING;
        RegistryEntry<EntityAttribute> actualESAttr = energyShieldAttr != null ? energyShieldAttr : ModAttributes.ENERGY_SHIELD;

        // 3. Apply Local Percentages
        if (localAttackDamagePercent > 0 && finalFlatValues.containsKey(actualAttackAttr)) {
            double oldVal = finalFlatValues.get(actualAttackAttr);
            double newVal = oldVal * (1.0 + localAttackDamagePercent);
            finalFlatValues.put(actualAttackAttr, newVal);
        }
        if (localArmorPercent > 0 && finalFlatValues.containsKey(actualArmorAttr)) {
            finalFlatValues.put(actualArmorAttr, finalFlatValues.get(actualArmorAttr) * (1.0 + localArmorPercent));
        }
        if (localEvasionPercent > 0 && finalFlatValues.containsKey(actualEvasionAttr)) {
            finalFlatValues.put(actualEvasionAttr, finalFlatValues.get(actualEvasionAttr) * (1.0 + localEvasionPercent));
        }
        if (localEnergyShieldPercent > 0 && finalFlatValues.containsKey(actualESAttr)) {
            finalFlatValues.put(actualESAttr, finalFlatValues.get(actualESAttr) * (1.0 + localEnergyShieldPercent));
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

            boolean isMultiplicative = isAttribute(attr, EntityAttributes.GENERIC_ATTACK_SPEED) || 
                                       isAttribute(attr, ModAttributes.MINING_EFFICIENCY);

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

    /**
     * Safely compares two attribute registry entries.
     * <p>Fabric's registry can produce different RegistryEntry objects for the same attribute
     * (depending on when/how they were loaded), causing standard .equals() to return false.
     * This method falls back to comparing translation keys as a reliable secondary check.</p>
     * <p><b>ALWAYS use this instead of .equals() when comparing attributes in this class.</b></p>
     */
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
                if (isAttribute(attribute, ModAttributes.DURABILITY_BONUS)) continue;
                if (isAttribute(attribute, ModAttributes.CHANCE_TO_BLOCK)) continue;
                if (isAttribute(attribute, ModAttributes.BLOCK_VALUE)) continue;
                if (isAttribute(attribute, ModAttributes.HEALTH_ON_BLOCK)) continue;

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