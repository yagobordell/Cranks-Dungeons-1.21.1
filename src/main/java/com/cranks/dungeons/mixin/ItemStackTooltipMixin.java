package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.util.ArmorSetType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.enchantment.Enchantment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.*;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {

    private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0;
    private static final double PLAYER_BASE_ATTACK_SPEED = 4.0;

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void customTooltipFormat(Item.TooltipContext context, net.minecraft.entity.player.PlayerEntity player,
            TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        List<Text> tooltip = cir.getReturnValue();

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        List<ItemStatManager.ItemStat> customStats = ItemStatManager.getStats(stack);

        // Only process items that are part of our RPG system
        if (customStats.isEmpty() && equipType.isEmpty()) {

            return;
        }

        if (tooltip.isEmpty())
            return;

        // 1. Build the new "Item Card"
        List<Text> newTooltip = new ArrayList<>();

        boolean showDetails = false;
        try {
            var window = MinecraftClient.getInstance().getWindow().getHandle();
            showDetails = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        } catch (Exception ignored) {
        }

        // Item Name (Centered-ish title)
        newTooltip.add(modifyItemNameWithRarity(stack, tooltip.get(0)));

        // Item Type (Centered-ish subtitle)
        if (equipType.isPresent()) {
            String handedness = "";
            EquipmentType typeEnum = equipType.get();
            if (typeEnum != EquipmentType.SHIELD && typeEnum != EquipmentType.HELMET && typeEnum != EquipmentType.CHESTPLATE && typeEnum != EquipmentType.LEGGINGS && typeEnum != EquipmentType.BOOTS && typeEnum != EquipmentType.ELYTRA) {
                handedness = com.cranks.dungeons.util.CombatUtility.isTwoHandedWeapon(stack) ? " - Two Handed" : " - One Handed";
            }
            newTooltip.add(Text.literal(equipType.get().getName() + handedness).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            
            int quality = com.cranks.dungeons.block.entity.QualityStationBlockEntity.getQuality(stack);
            if (quality > 0) {
                newTooltip.add(Text.literal("Quality: ").formatted(Formatting.GRAY).append(Text.literal("+" + quality + "%").formatted(Formatting.BLUE)));
            }

            addRequirementsSection(player, stack, newTooltip);
            addDivider(newTooltip);
        } else if (ItemStatManager.getStats(stack).size() > 0) {
            newTooltip.add(Text.literal("Magical Item").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            addDivider(newTooltip);
        } else {
            addDivider(newTooltip);
        }

        // 2. Base Stats Section
        if (equipType.isPresent()) {
            addBaseStatsSection(stack, newTooltip, equipType.get());
        }

        // 3. Enchantment Section
        addEnchantmentsSection(stack, newTooltip, showDetails);

        // 4. Magical Runes Section
        addRunesSection(stack, newTooltip, showDetails);

        var enchantmentComponent = stack.getEnchantments();
        boolean hasEnchants = !enchantmentComponent.isEmpty();

        if (!showDetails && (hasEnchants || !customStats.isEmpty() || ItemStatManager.hasCrypticStat(stack))) {
            newTooltip.add(Text.literal("Hold ").formatted(Formatting.GRAY)
                    .append(Text.literal("SHIFT").formatted(Formatting.YELLOW))
                    .append(Text.literal(" for details").formatted(Formatting.GRAY)));
        }

        // 5. Technical Info (Durability, etc.)
        for (int i = 1; i < tooltip.size(); i++) {
            Text line = tooltip.get(i);
            String raw = line.getString().toLowerCase();
            if (raw.contains("when in") || raw.contains("when on") || raw.contains("when worn") ||
                    raw.contains("on body") || raw.contains("attribute.modifier") ||
                    raw.startsWith("runes:") || raw.isEmpty()) {
                continue;
            }
            if (raw.contains("durability:") || raw.contains("minecraft:") || raw.contains("unbreakable")) {
                newTooltip.add(line);
            }
        }

        cir.setReturnValue(newTooltip);

    }

    @Unique
    private void addDivider(List<Text> tooltip) {
        // Dark gray horizontal line
        tooltip.add(Text.literal("————————————————————").formatted(Formatting.DARK_GRAY));
    }

    @Unique
    private void addBaseStatsSection(ItemStack stack, List<Text> tooltip, EquipmentType type) {
        DecimalFormat flatFmt = new DecimalFormat("#"); // integers for flat values
        DecimalFormat pctFmt = new DecimalFormat("#.#"); // one decimal for percent values
        Map<String, Double> attributeTotals = calculateItemAttributeTotals(stack, type, true);
        ItemStack defaultStack = new ItemStack(stack.getItem());
        Map<String, Double> defaultTotals = calculateItemAttributeTotals(defaultStack, type, false);

        boolean isDefensive = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE ||
                type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS ||
                type == EquipmentType.ELYTRA || type == EquipmentType.SHIELD;

        boolean isTool = type == EquipmentType.PICKAXE || type == EquipmentType.AXE ||
                type == EquipmentType.SHOVEL || type == EquipmentType.HOE;

        boolean addedAny = false;

        if (isDefensive) {
            String itemName = stack.getItem().toString().toLowerCase();
            ArmorSetType armorType = getArmorSetType(itemName);

            String armorKey = EntityAttributes.GENERIC_ARMOR.value().getTranslationKey();
            if (attributeTotals.getOrDefault(armorKey, 0.0) > 0) {
                tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                        .append(Text.literal(flatFmt.format(attributeTotals.get(armorKey)))
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_ARMOR))));
                addedAny = true;
            }

            String evasionKey = ModAttributes.EVASION_RATING.value().getTranslationKey();
            if (attributeTotals.getOrDefault(evasionKey, 0.0) > 0) {
                tooltip.add(Text.literal("Evasion Rating: ").formatted(Formatting.GRAY)
                        .append(Text.literal(flatFmt.format(attributeTotals.get(evasionKey)))
                                .formatted(getValueColor(stack, ModAttributes.EVASION_RATING))));
                addedAny = true;
            }

            String esKey = ModAttributes.ENERGY_SHIELD.value().getTranslationKey();
            if (attributeTotals.getOrDefault(esKey, 0.0) > 0) {
                tooltip.add(Text.literal("Energy Shield: ").formatted(Formatting.GRAY)
                        .append(Text.literal(flatFmt.format(attributeTotals.get(esKey)))
                                .formatted(getValueColor(stack, ModAttributes.ENERGY_SHIELD))));
                addedAny = true;
            }

            String movementKey = ModAttributes.MOVEMENT_SPEED.value().getTranslationKey();
            if (attributeTotals.getOrDefault(movementKey, 0.0) != 0 && defaultTotals.getOrDefault(movementKey, 0.0) != 0) {
                double moveSpeed = attributeTotals.get(movementKey) * 100;
                tooltip.add(Text.literal("Movement Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(moveSpeed) + "%")
                                .formatted(getValueColor(stack, ModAttributes.MOVEMENT_SPEED))));
                addedAny = true;
            }

            String speedKey = EntityAttributes.GENERIC_ATTACK_SPEED.value().getTranslationKey();
            if (attributeTotals.getOrDefault(speedKey, 0.0) != 0 && defaultTotals.getOrDefault(speedKey, 0.0) != 0) {
                double speed = attributeTotals.get(speedKey) * 100;
                tooltip.add(Text.literal("Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(speed) + "%")
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_SPEED))));
                addedAny = true;
            }

            String kbResKey = ModAttributes.KNOCKBACK_RESISTANCE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(kbResKey, 0.0) > 0 && defaultTotals.getOrDefault(kbResKey, 0.0) > 0) {
                double kbRes = attributeTotals.get(kbResKey) * 100;
                tooltip.add(Text.literal("Knockback Resistance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(kbRes) + "%")
                                .formatted(getValueColor(stack, ModAttributes.KNOCKBACK_RESISTANCE))));
                addedAny = true;
            }

            String fireKey = ModAttributes.FIRE_RESISTANCE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(fireKey, 0.0) > 0 && defaultTotals.getOrDefault(fireKey, 0.0) > 0) {
                tooltip.add(Text.literal("Fire Resistance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(attributeTotals.get(fireKey) * 100) + "%")
                                .formatted(getValueColor(stack, ModAttributes.FIRE_RESISTANCE))));
                addedAny = true;
            }

            String coldKey = ModAttributes.COLD_RESISTANCE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(coldKey, 0.0) > 0 && defaultTotals.getOrDefault(coldKey, 0.0) > 0) {
                tooltip.add(Text.literal("Cold Resistance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(attributeTotals.get(coldKey) * 100) + "%")
                                .formatted(getValueColor(stack, ModAttributes.COLD_RESISTANCE))));
                addedAny = true;
            }

            String lightKey = ModAttributes.LIGHTNING_RESISTANCE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(lightKey, 0.0) > 0 && defaultTotals.getOrDefault(lightKey, 0.0) > 0) {
                tooltip.add(Text.literal("Lightning Resistance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(attributeTotals.get(lightKey) * 100) + "%")
                                .formatted(getValueColor(stack, ModAttributes.LIGHTNING_RESISTANCE))));
                addedAny = true;
            }

            String voidKey = ModAttributes.VOID_RESISTANCE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(voidKey, 0.0) > 0 && defaultTotals.getOrDefault(voidKey, 0.0) > 0) {
                tooltip.add(Text.literal("Void Resistance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(attributeTotals.get(voidKey) * 100) + "%")
                                .formatted(getValueColor(stack, ModAttributes.VOID_RESISTANCE))));
                addedAny = true;
            }

            String luckKey = ModAttributes.ITEM_RARITY_PERCENT.value().getTranslationKey();
            if (attributeTotals.getOrDefault(luckKey, 0.0) > 0 && defaultTotals.getOrDefault(luckKey, 0.0) > 0) {
                tooltip.add(Text.literal("Rarity of Items Found: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(attributeTotals.get(luckKey) * 100) + "%")
                                .formatted(getValueColor(stack, ModAttributes.ITEM_RARITY_PERCENT))));
                addedAny = true;
            }

            // Universal Defensive Stats
            String healthKey = EntityAttributes.GENERIC_MAX_HEALTH.value().getTranslationKey();
            if (attributeTotals.getOrDefault(healthKey, 0.0) > 0 && defaultTotals.getOrDefault(healthKey, 0.0) > 0) {
                tooltip.add(Text.literal("Max Health: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + flatFmt.format(attributeTotals.get(healthKey)))
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_MAX_HEALTH))));
                addedAny = true;
            }
            String regenKey = ModAttributes.LIFE_REGENERATION.value().getTranslationKey();
            if (attributeTotals.getOrDefault(regenKey, 0.0) > 0 && defaultTotals.getOrDefault(regenKey, 0.0) > 0) {
                tooltip.add(Text.literal("Health Regen: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + flatFmt.format(attributeTotals.get(regenKey)))
                                .formatted(getValueColor(stack, ModAttributes.LIFE_REGENERATION))));
                addedAny = true;
            }


            String blockValueKey = ModAttributes.BLOCK_VALUE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(blockValueKey, 0.0) > 0 && defaultTotals.getOrDefault(blockValueKey, 0.0) > 0) {
                tooltip.add(Text.literal("Block Value: ").formatted(Formatting.GRAY)
                        .append(Text.literal(flatFmt.format(attributeTotals.get(blockValueKey)))
                                .formatted(getValueColor(stack, ModAttributes.BLOCK_VALUE))));
                addedAny = true;
            }

            String healthBlockKey = ModAttributes.HEALTH_ON_BLOCK.value().getTranslationKey();
            if (attributeTotals.getOrDefault(healthBlockKey, 0.0) > 0) {
                tooltip.add(Text.literal("Health on Block: ").formatted(Formatting.GRAY)
                        .append(Text.literal(flatFmt.format(attributeTotals.get(healthBlockKey)))
                                .formatted(getValueColor(stack, ModAttributes.HEALTH_ON_BLOCK))));
                addedAny = true;
            }
        } else {
            // WEAPON/TOOL BASE STATS
            // TOOLS
            if (type == EquipmentType.PICKAXE || type == EquipmentType.AXE || 
                type == EquipmentType.SHOVEL || type == EquipmentType.HOE) {
                
                String miningKey = ModAttributes.MINING_EFFICIENCY.value().getTranslationKey();
                double bonusMining = attributeTotals.getOrDefault(miningKey, 0.0);
                tooltip.add(Text.literal("Gathering Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(1.0 + bonusMining))
                                .formatted(getValueColor(stack, ModAttributes.MINING_EFFICIENCY))));

                String breakRangeKey = ModAttributes.BREAKING_RANGE.value().getTranslationKey();
                double bonusBreakRange = attributeTotals.getOrDefault(breakRangeKey, 0.0);
                tooltip.add(Text.literal("Breaking Range: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(4.5 + bonusBreakRange))
                                .formatted(getValueColor(stack, ModAttributes.BREAKING_RANGE))));

                addedAny = true;
            }

            String damageKey = EntityAttributes.GENERIC_ATTACK_DAMAGE.value().getTranslationKey();
            if (attributeTotals.containsKey(damageKey) && type != EquipmentType.BOW
                    && type != EquipmentType.CROSSBOW && !isTool) {
                double finalDamage = PLAYER_BASE_ATTACK_DAMAGE + attributeTotals.get(damageKey);
                tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(finalDamage))
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE))));
                
                addElementalDamageLines(tooltip, attributeTotals, pctFmt);
                addedAny = true;
            }
            String speedKey = EntityAttributes.GENERIC_ATTACK_SPEED.value().getTranslationKey();
            if (attributeTotals.containsKey(speedKey) && !isTool) {
                double finalSpeed = PLAYER_BASE_ATTACK_SPEED + attributeTotals.get(speedKey);
                tooltip.add(Text.literal("Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(finalSpeed))
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_SPEED))));
                addedAny = true;
            }

            if (type == EquipmentType.BOW || type == EquipmentType.CROSSBOW) {
                String bowDamageKey = EntityAttributes.GENERIC_ATTACK_DAMAGE.value().getTranslationKey();
                double finalDamage = attributeTotals.getOrDefault(bowDamageKey, 0.0);
                tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(finalDamage))
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE))));
                
                addElementalDamageLines(tooltip, attributeTotals, pctFmt);
                
                String reloadKey = ModAttributes.RELOAD_SPEED.value().getTranslationKey();
                double finalReloadSpeed = 1.0 + attributeTotals.getOrDefault(reloadKey, 0.0);
                tooltip.add(Text.literal("Reload Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(finalReloadSpeed))
                                .formatted(getValueColor(stack, ModAttributes.RELOAD_SPEED))));
                addedAny = true;
            }
        }

        if (addedAny) {
            addDivider(tooltip);
        }
    }

    @Unique
    private void addElementalDamageLines(List<Text> tooltip, Map<String, Double> attributeTotals, DecimalFormat df) {
        String fireKey = ModAttributes.FIRE_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(fireKey) && attributeTotals.get(fireKey) > 0) {
            tooltip.add(Text.literal("Fire Damage: ").formatted(Formatting.RED)
                    .append(Text.literal(df.format(attributeTotals.get(fireKey))).formatted(Formatting.RED)));
        }

        String coldKey = ModAttributes.COLD_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(coldKey) && attributeTotals.get(coldKey) > 0) {
            tooltip.add(Text.literal("Cold Damage: ").formatted(Formatting.AQUA)
                    .append(Text.literal(df.format(attributeTotals.get(coldKey))).formatted(Formatting.AQUA)));
        }

        String lightningKey = ModAttributes.LIGHTNING_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(lightningKey) && attributeTotals.get(lightningKey) > 0) {
            tooltip.add(Text.literal("Lightning Damage: ").formatted(Formatting.DARK_BLUE)
                    .append(Text.literal(df.format(attributeTotals.get(lightningKey))).formatted(Formatting.DARK_BLUE)));
        }

        String voidKey = ModAttributes.VOID_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(voidKey) && attributeTotals.get(voidKey) > 0) {
            tooltip.add(Text.literal("Void Damage: ").formatted(Formatting.DARK_PURPLE)
                    .append(Text.literal(df.format(attributeTotals.get(voidKey))).formatted(Formatting.DARK_PURPLE)));
        }
    }

    /**
     * Calculates the total attribute values for an item, combining base stats, rune stats,
     * and enchantment stats into a single map keyed by translation key.
     *
     * <h3>Important: Double-Counting Prevention</h3>
     * <p>When {@code includeModifications} is true, the item's AttributeModifiersComponent
     * (read at step 1) ALREADY contains flat rune values and local percentage scaling,
     * because {@link com.cranks.dungeons.equipment.EquipmentStatApplier#updateItemAttributes}
     * bakes them in. Therefore, when reading rune stats at step 2, we MUST skip stats whose
     * attributes are already handled by the component (armor, evasion, energy shield,
     * attack damage, and their percentage variants). Failing to do so causes the tooltip
     * to show inflated values (e.g., 116 armor instead of 82).</p>
     *
     * @param stack The item to calculate stats for
     * @param equipType The equipment type of the item
     * @param includeModifications If true, includes rune and enchantment modifications.
     *                            If false, returns only the base item's default stats.
     * @return Map of translation key -> total attribute value
     */
    @Unique
    private Map<String, Double> calculateItemAttributeTotals(ItemStack stack,
            EquipmentType equipType, boolean includeModifications) {
        Map<String, Double> totals = new HashMap<>();

        AttributeModifiersComponent attributes;
        if (includeModifications) {
            attributes = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        } else {
            ItemStack defaultStack = new ItemStack(stack.getItem());
            attributes = defaultStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        }

        int quality = com.cranks.dungeons.block.entity.QualityStationBlockEntity.getQuality(stack);
        double qBonus = quality > 0 ? quality / 100.0 : 0.0;

        for (var entry : attributes.modifiers()) {
            if (entry.modifier().operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE) {
                String key = entry.attribute().value().getTranslationKey();
                double baseVal = entry.modifier().value();
                
                if (!includeModifications && qBonus > 0) {
                    if (entry.attribute().equals(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                        baseVal = (4.0 + baseVal) * (1.0 + qBonus) - 4.0;
                    } else {
                        baseVal = baseVal * (1.0 + qBonus);
                    }
                }
                
                totals.merge(key, baseVal, Double::sum);
            }
        }

        if (includeModifications) {
            // 2. Gather attribute modifiers from Runes
            // NOTE: Flat rune values for core stats (armor, evasion, ES, attack damage) are already
            // baked into the ATTRIBUTE_MODIFIERS component by EquipmentStatApplier.updateItemAttributes().
            // Local percent stats (ARMOR_PERCENT, EVASION_RATING_PERCENT, etc.) also scale the base
            // values there. We must NOT add them again here or they'll be double-counted.
            List<com.cranks.dungeons.equipment.ItemStatManager.ItemStat> runeStats = com.cranks.dungeons.equipment.ItemStatManager.getStats(stack);
            for (com.cranks.dungeons.equipment.ItemStatManager.ItemStat is : runeStats) {
                com.cranks.dungeons.stat.CustomStat stat = com.cranks.dungeons.stat.StatRegistry.getStat(is.statId);
                if (stat != null) {
                    for (int i = 0; i < stat.getComponents().size(); i++) {
                        com.cranks.dungeons.stat.CustomStat.StatComponent comp = stat.getComponents().get(i);
                        if (!comp.getAttributes().isEmpty()) {
                            RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> runeAttr = comp.getAttributes().get(0);
                            // Skip stats already baked into the item's attribute component
                            if (attributesMatch(runeAttr, EntityAttributes.GENERIC_ARMOR) ||
                                attributesMatch(runeAttr, EntityAttributes.GENERIC_ATTACK_DAMAGE) ||
                                attributesMatch(runeAttr, ModAttributes.EVASION_RATING) ||
                                attributesMatch(runeAttr, ModAttributes.ENERGY_SHIELD) ||
                                attributesMatch(runeAttr, ModAttributes.ARMOR_PERCENT) ||
                                attributesMatch(runeAttr, ModAttributes.EVASION_RATING_PERCENT) ||
                                attributesMatch(runeAttr, ModAttributes.ENERGY_SHIELD_PERCENT) ||
                                attributesMatch(runeAttr, ModAttributes.ATTACK_DAMAGE_PERCENT)) {
                                continue;
                            }
                            String key = runeAttr.value().getTranslationKey();
                            totals.merge(key, is.getValue(i), Double::sum);
                        }
                    }
                }
            }

            // 3. Gather attribute modifiers from Enchantments
            var enchantments = stack.getEnchantments();
            Map<String, Double> multTotal = new HashMap<>();

            for (var enchEntry : enchantments.getEnchantmentEntries()) {
                var enchantment = enchEntry.getKey().value();
                int level = enchEntry.getIntValue();

                var attributeEffects = enchantment.effects().get(EnchantmentEffectComponentTypes.ATTRIBUTES);
                if (attributeEffects != null) {
                    for (AttributeEnchantmentEffect effect : attributeEffects) {
                        RegistryEntry<EntityAttribute> attr = effect.attribute();
                        
                        // Apply the same filtering as EnchantmentHelperMixin
                        boolean isWeaponAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeaponAttribute(attr);
                        boolean isArmorAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmorAttribute(attr);
                        boolean isWeapon = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeapon(stack);
                        boolean isArmor = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmor(stack);

                        if (isWeaponAttr && !isWeapon) continue;
                        if (isArmorAttr && !isArmor) continue;

                        String key = attr.value().getTranslationKey();
                        double value = (double) effect.amount().getValue(level);

                        // Scale effectiveness
                        double effectiveness = com.cranks.dungeons.equipment.ItemStatManager.getAttributeValue(stack, com.cranks.dungeons.registry.ModAttributes.ENCHANTMENT_EFFECTIVENESS);
                        if (effectiveness > 0) {
                            value *= (1.0 + effectiveness);
                        }

                        // Special case: Mining Efficiency (Gathering Speed) should be multiplicative
                        if (attr.equals(com.cranks.dungeons.registry.ModAttributes.MINING_EFFICIENCY)) {
                            multTotal.merge(key, value, Double::sum);
                        } else {
                            // Default to ADD_VALUE for enchantments in vanilla
                            totals.merge(key, value, Double::sum);
                        }
                    }
                }
            }

            // Apply multipliers (though Mining Efficiency is the main one currently using this in our logic)
            for (var entry : multTotal.entrySet()) {
                String key = entry.getKey();
                double currentBonus = totals.getOrDefault(key, 0.0);
                double multValue = entry.getValue();
                // Formula: (Base(1.0) + FlatBonuses) * (1.0 + MultBonuses) - Base(1.0)
                totals.put(key, (1.0 + currentBonus) * (1.0 + multValue) - 1.0);
            }
        }

        // 3.5 Merge BONUS_ATTACK_DAMAGE into ATTACK_DAMAGE
        String bonusDmgKey = ModAttributes.BONUS_ATTACK_DAMAGE.value().getTranslationKey();
        if (totals.containsKey(bonusDmgKey)) {
            String atkDmgKey = EntityAttributes.GENERIC_ATTACK_DAMAGE.value().getTranslationKey();
            totals.merge(atkDmgKey, totals.remove(bonusDmgKey), Double::sum);
        }
        return totals;
    }

    @Unique
    private void addRunesSection(ItemStack stack, List<Text> tooltip, boolean showDetails) {
        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        boolean hasCryptic = ItemStatManager.hasCrypticStat(stack);

        if (stats.isEmpty() && !hasCryptic)
            return;

        List<ItemStatManager.ItemStat> prefixes = new ArrayList<>();
        List<ItemStatManager.ItemStat> suffixes = new ArrayList<>();

        for (ItemStatManager.ItemStat itemStat : stats) {
            CustomStat stat = StatRegistry.getStat(itemStat.statId);
            if (stat != null) {
                if (stat.getAffixType() == CustomStat.AffixType.PREFIX) {
                    prefixes.add(itemStat);
                } else {
                    suffixes.add(itemStat);
                }
            }
        }

        boolean isUnique = CustomRarity.getRarity(stack) == CustomRarity.UNIQUE;

        if (!prefixes.isEmpty()) {
            if (showDetails && !isUnique) {
                tooltip.add(Text.literal("Prefixes:").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
            for (ItemStatManager.ItemStat p : prefixes) {
                CustomStat stat = StatRegistry.getStat(p.statId);
                if (stat != null) {
                    tooltip.addAll(createRunicStatLines(stat, p.values, p.tier, p.isArtificial, showDetails, isUnique));
                }
            }
        }

        if (!suffixes.isEmpty()) {
            if (showDetails && !isUnique) {
                tooltip.add(Text.literal("Suffixes:").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
            for (ItemStatManager.ItemStat s : suffixes) {
                CustomStat stat = StatRegistry.getStat(s.statId);
                if (stat != null) {
                    tooltip.addAll(createRunicStatLines(stat, s.values, s.tier, s.isArtificial, showDetails, isUnique));
                }
            }
        }

        if (hasCryptic) {
            tooltip.add(Text.literal("?? ").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)
                    .append(Text.literal("Cryptic Stat").formatted(Formatting.DARK_GREEN))
                    .append(Text.literal(" ??").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)));
        }

        addDivider(tooltip);
    }

    @Unique
    private void addEnchantmentsSection(ItemStack stack, List<Text> tooltip, boolean showDetails) {
        Optional<EquipmentType> type = EquipmentType.getTypeForItem(stack);
        if (type.isPresent() && type.get() == EquipmentType.SHIELD) {
            return;
        }

        CustomRarity rarity = CustomRarity.getRarity(stack);
        int maxSlots = switch (rarity) {
            case UNIQUE -> 3;
            default -> 1;
        };

        var enchantmentComponent = stack.getEnchantments();
        int enchantsCount = 0;
        DecimalFormat df = new DecimalFormat("#.#");

        for (var entry : enchantmentComponent.getEnchantmentEntries()) {
            if (enchantsCount >= maxSlots)
                break;

            Text enchantName = entry.getKey().value().description();
            tooltip.add(Text.literal("✦ ").formatted(Formatting.DARK_AQUA)
                    .append(Text.literal(enchantName.getString()).formatted(Formatting.AQUA)));

            if (showDetails) {
                String id = entry.getKey().getKey().get().getValue().toString();
                String name = id.contains(":") ? id.split(":")[1] : id;

                // Try to add descriptions
                String descKey1 = "enchantment.cranks-dungeons." + name + ".desc";
                String descKey2 = "enchantment.cranks-dungeons." + name + ".desc2";

                MutableText desc1 = Text.translatable(descKey1);
                if (!desc1.getString().equals(descKey1)) {
                    tooltip.add(desc1.formatted(Formatting.GRAY, Formatting.ITALIC));
                }

                MutableText desc2 = Text.translatable(descKey2);
                if (!desc2.getString().equals(descKey2)) {
                    tooltip.add(desc2.formatted(Formatting.GRAY, Formatting.ITALIC));
                }
            }

            enchantsCount++;
        }

        for (int i = enchantsCount; i < maxSlots; i++) {
            tooltip.add(Text.literal("✧ ").formatted(Formatting.DARK_GRAY)
                    .append(Text.literal("Empty Enchantment Slot").formatted(Formatting.DARK_GRAY)));
        }

        if (maxSlots > 0) {
            addDivider(tooltip);
        }
    }

    @Unique
    private List<Text> createRunicStatLines(CustomStat stat, double[] values, int tier, boolean isArtificial, boolean showDetails, boolean isUnique) {
        List<Text> lines = new ArrayList<>();
        for (int i = 0; i < stat.getComponents().size(); i++) {
            CustomStat.StatComponent comp = stat.getComponents().get(i);
            double value = (values != null && i < values.length) ? values[i] : 0;
            String displayText = comp.formatValue(value, stat.getEquipmentType());
            MutableText line = Text.literal(displayText).formatted(isArtificial ? Formatting.DARK_GREEN : Formatting.BLUE);
            if (showDetails && i == stat.getComponents().size() - 1) {
                if (isUnique && !comp.isBooleanStat()) {
                    line.append(Text.literal(" " + comp.getFormattedMinMax(tier)).formatted(getTierColor(tier)));
                } else if (isUnique) {
                    line.append(Text.literal(" [TX]").formatted(getTierColor(tier)));
                } else {
                    line.append(Text.literal(" [T" + tier + "]").formatted(getTierColor(tier)));
                }
            }
            lines.add(line);
        }
        return lines;
    }

    @Unique
    private Formatting getValueColor(ItemStack stack, RegistryEntry<EntityAttribute> attr) {
        Optional<EquipmentType> typeOpt = EquipmentType.getTypeForItem(stack);
        if (typeOpt.isEmpty()) return Formatting.WHITE;
        
        EquipmentType type = typeOpt.get();
        String attrKey = attr.value().getTranslationKey();
        
        Map<String, Double> currentTotals = calculateItemAttributeTotals(stack, type, true);
        double currentVal = currentTotals.getOrDefault(attrKey, 0.0);

        ItemStack defaultStack = new ItemStack(stack.getItem());
        Map<String, Double> defaultTotals = calculateItemAttributeTotals(defaultStack, type, false);
        double defaultVal = defaultTotals.getOrDefault(attrKey, 0.0);

        if (Math.abs(currentVal - defaultVal) > 0.0001) {
            return Formatting.BLUE;
        }

        return Formatting.WHITE;
    }

    @Unique
    private void addRequirementsSection(net.minecraft.entity.player.PlayerEntity player, ItemStack stack, List<Text> tooltip) {
        int[] reqs = com.cranks.dungeons.equipment.ItemRequirementManager.getRequirements(stack);
        if (reqs[0] == 0 && reqs[1] == 0 && reqs[2] == 0) return;

        boolean meets = player == null || com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(player, stack);
        
        MutableText reqText = Text.literal("Requires: ").formatted(Formatting.GRAY);
        boolean first = true;
        
        if (reqs[0] > 0) {
            boolean hasStr = player == null || player.getAttributeValue(ModAttributes.STRENGTH) >= reqs[0];
            reqText.append(Text.literal(reqs[0] + " Str").formatted(hasStr ? Formatting.WHITE : Formatting.RED));
            first = false;
        }
        if (reqs[1] > 0) {
            if (!first) reqText.append(Text.literal(", ").formatted(Formatting.GRAY));
            boolean hasDex = player == null || player.getAttributeValue(ModAttributes.DEXTERITY) >= reqs[1];
            reqText.append(Text.literal(reqs[1] + " Dex").formatted(hasDex ? Formatting.WHITE : Formatting.RED));
            first = false;
        }
        if (reqs[2] > 0) {
            if (!first) reqText.append(Text.literal(", ").formatted(Formatting.GRAY));
            boolean hasInt = player == null || player.getAttributeValue(ModAttributes.INTELLIGENCE) >= reqs[2];
            reqText.append(Text.literal(reqs[2] + " Int").formatted(hasInt ? Formatting.WHITE : Formatting.RED));
        }

        tooltip.add(reqText);
        
        if (!meets) {
            tooltip.add(Text.literal("Stats are reduced because requirements are not met!").formatted(Formatting.RED, Formatting.ITALIC));
        }
    }

    @Unique
    private Formatting getTierColor(int tier) {
        return Formatting.WHITE;
    }

    @Unique
    private Text modifyItemNameWithRarity(ItemStack stack, Text originalName) {
        CustomRarity rarity = CustomRarity.getRarity(stack);
        // Clear style and apply rarity color to ensure it shows up correctly
        return Text.literal(originalName.getString()).formatted(rarity.getColor());
    }

    @Unique
    private boolean isTool(ItemStack stack) {
        String itemName = stack.getItem().toString().toLowerCase();
        return itemName.endsWith("pickaxe") || itemName.endsWith("axe") ||
                itemName.endsWith("shovel") || itemName.endsWith("hoe");
    }


    @Unique
    private ArmorSetType getArmorSetType(String itemName) {
        if (itemName.contains("leather"))
            return ArmorSetType.LEATHER;
        if (itemName.contains("golden"))
            return ArmorSetType.GOLD;
        if (itemName.contains("chainmail"))
            return ArmorSetType.CHAINMAIL;
        if (itemName.contains("copper"))
            return ArmorSetType.COPPER;
        if (itemName.contains("iron"))
            return ArmorSetType.IRON;
        if (itemName.contains("diamond"))
            return ArmorSetType.DIAMOND;
        if (itemName.contains("netherite"))
            return ArmorSetType.NETHERITE;
        return ArmorSetType.NONE;
    }

    /**
     * Safely compares two attribute registry entries using multiple fallback strategies.
     * <p>Fabric's registry can return different RegistryEntry objects for the same attribute,
     * so reference equality and .equals() may fail. This method also checks registry keys.
     * <b>ALWAYS use this instead of .equals() when comparing attributes in tooltip code.</b></p>
     */
    @Unique
    private boolean attributesMatch(RegistryEntry<EntityAttribute> e1, RegistryEntry<EntityAttribute> e2) {
        if (e1 == e2) return true;
        if (e1 == null || e2 == null) return false;
        if (e1.equals(e2)) return true;
        return e1.getKey().isPresent() && e2.getKey().isPresent() && e1.getKey().get().equals(e2.getKey().get());
    }

    @Unique
    private boolean isPercentageModifierFor(RegistryEntry<EntityAttribute> modifierAttr, RegistryEntry<EntityAttribute> baseAttr) {
        if (attributesMatch(baseAttr, net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            return attributesMatch(modifierAttr, ModAttributes.ATTACK_DAMAGE_PERCENT);
        }
        if (attributesMatch(baseAttr, net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR)) {
            return attributesMatch(modifierAttr, ModAttributes.ARMOR_PERCENT);
        }
        if (attributesMatch(baseAttr, net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH)) {
            return attributesMatch(modifierAttr, ModAttributes.MAX_HEALTH_PERCENT);
        }
        if (attributesMatch(baseAttr, ModAttributes.ENERGY_SHIELD)) {
            return attributesMatch(modifierAttr, ModAttributes.ENERGY_SHIELD_PERCENT);
        }
        if (attributesMatch(baseAttr, ModAttributes.EVASION_RATING)) {
            return attributesMatch(modifierAttr, ModAttributes.EVASION_RATING_PERCENT);
        }
        return false;
    }
}