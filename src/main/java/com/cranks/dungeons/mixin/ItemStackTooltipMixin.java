package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.equipment.AnimalArmorHelper;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.util.ArmorSetType;
import com.cranks.dungeons.client.tooltip.TooltipLines;
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
        boolean isAnimalArmor = AnimalArmorHelper.isSupported(stack);

        // Only process items that are part of our RPG system
        if (customStats.isEmpty() && equipType.isEmpty() && !isAnimalArmor) {

            return;
        }

        if (tooltip.isEmpty())
            return;

        // 1. Build the new "Item Card"
        List<Text> newTooltip = new ArrayList<>();

        boolean showDetails = com.cranks.dungeons.client.tooltip.TooltipDetails.isHeld();

        // Item Name (Centered-ish title)
        newTooltip.add(modifyItemNameWithRarity(stack, tooltip.get(0)));

        // Item Type (Centered-ish subtitle)
        if (isAnimalArmor) {
            newTooltip.add(Text.literal("Animal Armor").formatted(Formatting.DARK_GRAY));
            addDivider(newTooltip);
        } else if (equipType.isPresent()) {
            EquipmentType typeEnum = equipType.get();
            
            boolean isTool = typeEnum == EquipmentType.PICKAXE || typeEnum == EquipmentType.SHOVEL || typeEnum == EquipmentType.HOE || typeEnum == EquipmentType.AXE;

            if (!isTool) {
                String handedness = "";
                if (typeEnum != EquipmentType.SHIELD && typeEnum != EquipmentType.HELMET && typeEnum != EquipmentType.CHESTPLATE && typeEnum != EquipmentType.LEGGINGS && typeEnum != EquipmentType.BOOTS && typeEnum != EquipmentType.ELYTRA) {
                    handedness = com.cranks.dungeons.util.CombatUtility.isTwoHandedWeapon(stack) ? " - Two Handed" : " - One Handed";
                }

                String typeName = typeEnum.getName();
                if (typeEnum == EquipmentType.ELYTRA) {
                    typeName = "Chestplate";
                } else if (typeEnum == EquipmentType.TRIDENT) {
                    typeName = "Spear";
                }

                String tierPrefix = "";
                if (com.cranks.dungeons.equipment.ItemStatManager.isUnique(stack)) {
                    tierPrefix = "Unique ";
                } else {
                    int maxTier = com.cranks.dungeons.equipment.ItemStatManager.getBestAllowedTier(stack);
                    tierPrefix = "Tier " + maxTier + " ";
                }
                newTooltip.add(Text.literal(tierPrefix + typeName + handedness).formatted(Formatting.DARK_GRAY));
            }
            
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
        if (isAnimalArmor) {
            // Animal armor deliberately stays outside EquipmentType so it cannot receive runes.
            // CHESTPLATE is used only to reuse the defensive-stat display formatting.
            addBaseStatsSection(stack, newTooltip, EquipmentType.CHESTPLATE);
        } else if (equipType.isPresent()) {
            addBaseStatsSection(stack, newTooltip, equipType.get());
        }

        // 3. Prefixes and suffixes, followed by enchantments.
        addRunesSection(stack, newTooltip, showDetails);
        addEnchantmentsSection(stack, newTooltip, showDetails);

        var enchantmentComponent = stack.getEnchantments();
        boolean hasEnchants = !enchantmentComponent.isEmpty();

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

        if(stack.isDamageable() && newTooltip.stream().noneMatch(line->line.getString().toLowerCase(java.util.Locale.ROOT).startsWith("durability:"))) {
            newTooltip.add(Text.literal("Durability: ").formatted(Formatting.GRAY)
                    .append(Text.literal(Math.max(0,stack.getMaxDamage()-stack.getDamage())+" / "+stack.getMaxDamage()).formatted(Formatting.GRAY)));
        }

        if (!showDetails && (hasEnchants || !customStats.isEmpty() || ItemStatManager.hasCrypticStat(stack))) {
            newTooltip.add(Text.literal("Hold ").formatted(Formatting.GRAY)
                    .append(com.cranks.dungeons.client.tooltip.TooltipDetails.keyName().copy().formatted(Formatting.YELLOW))
                    .append(Text.literal(" for details").formatted(Formatting.GRAY)));
        }
        TooltipLines.decorate(stack,newTooltip);
        cir.setReturnValue(newTooltip);

    }

    @Unique
    private void addDivider(List<Text> tooltip) {
        // Dark gray horizontal line
        tooltip.add(TooltipLines.mark(Text.literal("\u2014".repeat(20)).formatted(Formatting.DARK_GRAY),"separator"));
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

            if (AnimalArmorHelper.isSupported(stack)) {
                double animalMovementSpeed = getModifierTotal(stack,
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                if (animalMovementSpeed != 0.0) {
                    tooltip.add(Text.literal("Movement Speed: ").formatted(Formatting.GRAY)
                            .append(Text.literal(pctFmt.format(animalMovementSpeed * 100) + "%")
                                    .formatted(Formatting.WHITE)));
                    addedAny = true;
                }
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
                        .append(Text.literal("+" + pctFmt.format(attributeTotals.get(regenKey)))
                                .formatted(getValueColor(stack, ModAttributes.LIFE_REGENERATION))));
                addedAny = true;
            }
            String maxRegenPctKey = ModAttributes.MAX_HEALTH_REGENERATION_PERCENT.value().getTranslationKey();
            if (attributeTotals.getOrDefault(maxRegenPctKey, 0.0) > 0 && defaultTotals.getOrDefault(maxRegenPctKey, 0.0) > 0) {
                tooltip.add(Text.literal("Health Regen: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + pctFmt.format(attributeTotals.get(maxRegenPctKey) * 100.0) + "% Max Health")
                                .formatted(getValueColor(stack, ModAttributes.MAX_HEALTH_REGENERATION_PERCENT))));
                addedAny = true;
            }


            String blockValueKey = ModAttributes.BLOCK_VALUE.value().getTranslationKey();
            if (attributeTotals.getOrDefault(blockValueKey, 0.0) > 0 && defaultTotals.getOrDefault(blockValueKey, 0.0) > 0) {
                tooltip.add(Text.literal("Block Value: ").formatted(Formatting.GRAY)
                        .append(Text.literal(flatFmt.format(attributeTotals.get(blockValueKey)))
                                .formatted(getValueColor(stack, ModAttributes.BLOCK_VALUE))));
                addedAny = true;
            }
            String blockValuePctKey = ModAttributes.BLOCK_VALUE_PERCENT.value().getTranslationKey();
            if (attributeTotals.getOrDefault(blockValuePctKey, 0.0) > 0 && defaultTotals.getOrDefault(blockValuePctKey, 0.0) > 0) {
                tooltip.add(Text.literal("Block Value: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + pctFmt.format(attributeTotals.get(blockValuePctKey) * 100.0) + "%")
                                .formatted(getValueColor(stack, ModAttributes.BLOCK_VALUE_PERCENT))));
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

                addedAny = true;
            }

            String damageKey = EntityAttributes.GENERIC_ATTACK_DAMAGE.value().getTranslationKey();
            if (attributeTotals.containsKey(damageKey) && !isTool) {
                double finalDamage = attributeTotals.get(damageKey);
                // Melee weapons add player base attack damage (1.0)
                if (type != EquipmentType.BOW && type != EquipmentType.CROSSBOW) {
                    finalDamage += PLAYER_BASE_ATTACK_DAMAGE;
                }
                
                tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(finalDamage))
                                .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE))));
                
                addElementalDamageLines(tooltip, attributeTotals, pctFmt);
                addedAny = true;
            }

            String speedKey = EntityAttributes.GENERIC_ATTACK_SPEED.value().getTranslationKey();
            if (attributeTotals.containsKey(speedKey) && !isTool) {
                double finalSpeed = PLAYER_BASE_ATTACK_SPEED + attributeTotals.get(speedKey);
                
                if (type == EquipmentType.BOW || type == EquipmentType.CROSSBOW) {
                    double speedMultiplier = finalSpeed / 4.0;
                    double originalTimeTicks = (type == EquipmentType.CROSSBOW) ? 25.0 : 20.0;
                    double actualTicks = originalTimeTicks / speedMultiplier;
                    var rangedPlayer = net.minecraft.client.MinecraftClient.getInstance().player;
                    if (rangedPlayer != null) actualTicks = type == EquipmentType.CROSSBOW
                            ? com.cranks.dungeons.ranged.RangedTiming.crossbowReloadTicks(rangedPlayer, stack)
                            : com.cranks.dungeons.ranged.RangedTiming.bowDrawTicks(rangedPlayer, stack);
                    tooltip.add(Text.literal(type == EquipmentType.CROSSBOW ? "Reload Time: " : "Draw Time: ")
                            .formatted(Formatting.GRAY).append(Text.literal(pctFmt.format(actualTicks / 20.0) + "s").formatted(Formatting.WHITE)));
                    double attacksPerSecond = 20.0 / actualTicks; // 20 ticks in a second

                    tooltip.add(Text.literal("Attacks Per Second: ").formatted(Formatting.GRAY)
                            .append(Text.literal(pctFmt.format(attacksPerSecond))
                                    .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_SPEED))));
                } else {
                    tooltip.add(Text.literal("Attacks Per Second: ").formatted(Formatting.GRAY)
                            .append(Text.literal(pctFmt.format(finalSpeed))
                                    .formatted(getValueColor(stack, EntityAttributes.GENERIC_ATTACK_SPEED))));
                }
                addedAny = true;
            }

            if (type != EquipmentType.PICKAXE && type != EquipmentType.AXE && 
                type != EquipmentType.SHOVEL && type != EquipmentType.HOE) {
                String critKey = ModAttributes.CRIT_CHANCE.value().getTranslationKey();
                double bonusCrit = attributeTotals.getOrDefault(critKey, 0.0);
                double localCrit = com.cranks.dungeons.equipment.EquipmentStatApplier.getItemStatValue(stack, "local_crit_chance");
                double totalCrit = (0.05 + bonusCrit) * (1.0 + localCrit) * 100.0;
                
                Formatting critColor = (Math.abs(totalCrit - 5.0) > 0.01) ? Formatting.BLUE : Formatting.WHITE;
                
                tooltip.add(Text.literal("Critical Hit Chance: ").formatted(Formatting.GRAY)
                        .append(Text.literal(pctFmt.format(totalCrit) + "%")
                                .formatted(critColor)));
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
            tooltip.add(Text.literal("Fire Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal(df.format(attributeTotals.get(fireKey))).formatted(Formatting.RED)));
        }

        String coldKey = ModAttributes.COLD_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(coldKey) && attributeTotals.get(coldKey) > 0) {
            tooltip.add(Text.literal("Cold Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal(df.format(attributeTotals.get(coldKey))).formatted(Formatting.AQUA)));
        }

        String lightningKey = ModAttributes.LIGHTNING_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(lightningKey) && attributeTotals.get(lightningKey) > 0) {
            tooltip.add(Text.literal("Lightning Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal(df.format(attributeTotals.get(lightningKey))).formatted(Formatting.DARK_BLUE)));
        }

        String voidKey = ModAttributes.VOID_DAMAGE.value().getTranslationKey();
        if (attributeTotals.containsKey(voidKey) && attributeTotals.get(voidKey) > 0) {
            tooltip.add(Text.literal("Void Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal(df.format(attributeTotals.get(voidKey))).formatted(Formatting.DARK_PURPLE)));
        }
    }

    @Unique
    private Map<String, Double> calculateItemAttributeTotals(ItemStack stack,
            EquipmentType equipType, boolean includeModifications) {
        Map<String, Double> totals = new HashMap<>();
        Map<String, Double> baseMultipliers = new HashMap<>();
        Map<String, Double> totalMultipliers = new HashMap<>();
        ItemStack base = new ItemStack(stack.getItem());
        var attributes = includeModifications
                ? com.cranks.dungeons.equipment.EquipmentStatApplier.calculateItemAttributes(stack)
                : base.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (includeModifications && com.cranks.dungeons.equipment.ItemStatManager.getStats(stack).isEmpty()
                && com.cranks.dungeons.block.entity.QualityStationBlockEntity.getQuality(stack) == 0) {
            attributes = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);
        }

        for (var entry : attributes.modifiers()) {
            String key = entry.attribute().value().getTranslationKey();
            double value = entry.modifier().value();
            switch (entry.modifier().operation()) {
                case ADD_VALUE -> totals.merge(key, value, Double::sum);
                case ADD_MULTIPLIED_BASE -> baseMultipliers.merge(key, value, Double::sum);
                case ADD_MULTIPLIED_TOTAL -> totalMultipliers.merge(key, 1.0 + value, (first, second) -> first * second);
            }
        }
        baseMultipliers.forEach((key, value) -> totals.computeIfPresent(key, (ignored, baseValue) -> baseValue * (1.0 + value)));
        totalMultipliers.forEach((key, value) -> totals.computeIfPresent(key, (ignored, baseValue) -> baseValue * value));

        if (includeModifications) {
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
                        boolean isWeapon = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeapon(stack);
                        boolean isArmor = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmor(stack);
                        boolean isWeaponAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeaponAttribute(attr, isArmor);
                        boolean isArmorAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmorAttribute(attr, isWeapon);

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
                        if (attributesMatch(attr, com.cranks.dungeons.registry.ModAttributes.MINING_EFFICIENCY)) {
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
        CustomStat.AffixType crypticType = hasCryptic ? ItemStatManager.getCrypticAffixType(stack) : null;

        if (!prefixes.isEmpty() || (hasCryptic && crypticType == CustomStat.AffixType.PREFIX)) {
            int used=prefixes.size()+(hasCryptic&&crypticType==CustomStat.AffixType.PREFIX?1:0);
            tooltip.add(TooltipLines.section("Prefixes",used,CustomRarity.getRarity(stack).getMaxPrefixes()));
            for (ItemStatManager.ItemStat p : prefixes) {
                CustomStat stat = StatRegistry.getStat(p.statId);
                if (stat != null) {
                    tooltip.addAll(createRunicStatLines(stat, p.values, p.tier, p.isArtificial, showDetails, isUnique));
                }
            }
            if (hasCryptic && crypticType == CustomStat.AffixType.PREFIX) {
                tooltip.add(Text.literal("?? ").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)
                        .append(Text.literal("Cryptic Stat").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(" ??").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)));
            }
        }

        if (!suffixes.isEmpty() || (hasCryptic && crypticType == CustomStat.AffixType.SUFFIX)) {
            int used=suffixes.size()+(hasCryptic&&crypticType==CustomStat.AffixType.SUFFIX?1:0);
            tooltip.add(TooltipLines.section("Suffixes",used,CustomRarity.getRarity(stack).getMaxSuffixes()));
            for (ItemStatManager.ItemStat s : suffixes) {
                CustomStat stat = StatRegistry.getStat(s.statId);
                if (stat != null) {
                    tooltip.addAll(createRunicStatLines(stat, s.values, s.tier, s.isArtificial, showDetails, isUnique));
                }
            }
            if (hasCryptic && crypticType == CustomStat.AffixType.SUFFIX) {
                tooltip.add(Text.literal("?? ").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)
                        .append(Text.literal("Cryptic Stat").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(" ??").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)));
            }
        }

        addDivider(tooltip);
    }

    @Unique
    private void addEnchantmentsSection(ItemStack stack, List<Text> tooltip, boolean showDetails) {
        if (stack.isOf(net.minecraft.item.Items.ENCHANTED_BOOK)) {
            return;
        }

        int slots = EnchantmentSlotHelper.getEnchantmentSlots(stack);
        var enchantmentComponent = stack.getEnchantments();
        int used = EnchantmentSlotHelper.usedSlots(stack);

        if(slots>0||used>0)tooltip.add(TooltipLines.section("Enchantments",used,slots));

        for (var entry : enchantmentComponent.getEnchantmentEntries()) {
            int copies = EnchantmentSlotHelper.copyCount(entry.getKey(), entry.getIntValue());
            tooltip.add(Text.literal("\u2726 ").formatted(Formatting.DARK_AQUA)
                    .append(com.cranks.dungeons.client.guide.GuideText.link(EnchantmentSlotHelper.displayName(entry.getKey(), copies),
                            new com.cranks.dungeons.client.guide.GuideTarget(com.cranks.dungeons.client.guide.GuideTarget.Kind.ENCHANTMENT,
                                    entry.getKey().getKey().orElseThrow().getValue().toString())).formatted(Formatting.AQUA)));

            if (showDetails) {
                String id = com.cranks.dungeons.CranksDungeons.getEnchantmentId(entry.getKey());
                String name = id.contains(":") ? id.split(":")[1] : id;

                // Try to add descriptions
                String descKey1 = "enchantment.cranks-dungeons." + name + ".desc";
                String descKey2 = "enchantment.cranks-dungeons." + name + ".desc2";

                MutableText desc1 = Text.translatable(descKey1);
                if (!desc1.getString().equals(descKey1)) {
                    if (copies > 1) tooltip.add(Text.translatable("enchantment.cranks-dungeons.per_copy").formatted(Formatting.GRAY));
                    tooltip.add(desc1.formatted(Formatting.GRAY, Formatting.ITALIC));
                }

                MutableText desc2 = Text.translatable(descKey2);
                if (!desc2.getString().equals(descKey2)) {
                    tooltip.add(desc2.formatted(Formatting.GRAY, Formatting.ITALIC));
                }
            }
        }

        for (int i = used; i < slots; i++) {
            tooltip.add(Text.literal("\u2727 ").formatted(Formatting.DARK_GRAY)
                    .append(Text.literal("Empty Enchantment Slot").formatted(Formatting.DARK_GRAY)));
        }

        if (slots > 0 || used > 0) {
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
            Text detail=Text.empty();
            if(showDetails){
                int validTier=Math.clamp(tier,1,10);
                String tierLabel=i==0?" ["+(isUnique?"TX":"T"+validTier)+"]":"";
                String range=comp.isBooleanStat()?"":" "+comp.getFormattedMinMax(validTier);
                detail=Text.literal(tierLabel+range).formatted(getTierColor(validTier));
            }
            lines.add(TooltipLines.rune(com.cranks.dungeons.client.guide.GuideText.link(line,
                    com.cranks.dungeons.client.guide.GuideTarget.rune(stat.getId())),detail,i,stat.getComponents().size()));
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
    private double getModifierTotal(ItemStack stack, RegistryEntry<EntityAttribute> attribute,
            net.minecraft.entity.attribute.EntityAttributeModifier.Operation operation) {
        double total = 0.0;
        AttributeModifiersComponent modifiers = stack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                AttributeModifiersComponent.DEFAULT);
        for (var entry : modifiers.modifiers()) {
            if (attributesMatch(entry.attribute(), attribute) && entry.modifier().operation() == operation) {
                total += entry.modifier().value();
            }
        }
        return total;
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
        return com.cranks.dungeons.util.CombatUtility.attributesMatch(e1, e2);
    }

    @Unique
    private boolean isPercentageModifierFor(RegistryEntry<EntityAttribute> modifierAttr, RegistryEntry<EntityAttribute> baseAttr) {
        if (attributesMatch(baseAttr, net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            return attributesMatch(modifierAttr, ModAttributes.PHYSICAL_DAMAGE_PERCENT);
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
