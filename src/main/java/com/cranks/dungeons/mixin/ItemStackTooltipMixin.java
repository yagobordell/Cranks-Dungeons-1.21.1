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
import com.cranks.dungeons.client.RarityTooltipContext;
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

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void customTooltipFormat(Item.TooltipContext context, net.minecraft.entity.player.PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        List<Text> tooltip = cir.getReturnValue();

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        List<ItemStatManager.ItemStat> customStats = ItemStatManager.getStats(stack);

        // Only process items that are part of our RPG system
        if (customStats.isEmpty() && equipType.isEmpty()) {
            RarityTooltipContext.clear();
            return;
        }

        if (tooltip.isEmpty()) return;

        // 1. Build the new "Item Card"
        List<Text> newTooltip = new ArrayList<>();
        
        // Item Name (Centered-ish title)
        newTooltip.add(modifyItemNameWithRarity(stack, tooltip.get(0)));
        
        // Item Type (Centered-ish subtitle)
        if (equipType.isPresent()) {
            newTooltip.add(Text.literal(equipType.get().getName()).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            addDivider(newTooltip);
        } else {
            addDivider(newTooltip);
        }
        
        // 2. Base Stats Section
        if (equipType.isPresent()) {
            addBaseStatsSection(stack, newTooltip, equipType.get());
        }

        // 3. Enchantment Section
        addEnchantmentsSection(stack, newTooltip);
        addDivider(newTooltip);

        // 4. Magical Runes Section
        boolean showDetails = false;
        try {
            var window = MinecraftClient.getInstance().getWindow();
            showDetails = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT) || 
                          InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        } catch (Exception ignored) {}
        
        addRunesSection(stack, newTooltip, showDetails);

        if (!showDetails && (!customStats.isEmpty() || ItemStatManager.hasCrypticStat(stack))) {
            addDivider(newTooltip); // Only add divider before the shift hint
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

        tooltip.clear();
        tooltip.addAll(newTooltip);
        RarityTooltipContext.set(CustomRarity.getRarity(stack));
    }

    @Unique
    private void addDivider(List<Text> tooltip) {
        // Dark gray horizontal line
        tooltip.add(Text.literal("————————————————————").formatted(Formatting.DARK_GRAY));
    }

    @Unique
    private void addBaseStatsSection(ItemStack stack, List<Text> tooltip, EquipmentType type) {
        DecimalFormat df = new DecimalFormat("#.#");
        Map<RegistryEntry<EntityAttribute>, Double> attributeTotals = calculateItemAttributeTotals(stack);

        boolean isDefensive = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE ||
                type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS || 
                type == EquipmentType.ELYTRA || type == EquipmentType.SHIELD;
        
        boolean isTool = type == EquipmentType.PICKAXE || type == EquipmentType.AXE ||
                type == EquipmentType.SHOVEL || type == EquipmentType.HOE;

        boolean addedAny = false;

        if (isTool) {
            addedAny = true; 
        } else if (isDefensive) {
            String itemName = stack.getItem().toString().toLowerCase();
            ArmorSetType armorType = getArmorSetType(itemName);

            switch (armorType) {
                case LEATHER -> {
                    if (attributeTotals.getOrDefault(ModAttributes.EVASION_RATING, 0.0) > 0) {
                        tooltip.add(Text.literal("Evasion Rating: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.EVASION_RATING))).formatted(getValueColor(stack, ModAttributes.EVASION_RATING))));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.MOVEMENT_SPEED, 0.0) != 0) {
                        double moveSpeed = attributeTotals.get(ModAttributes.MOVEMENT_SPEED) * 100;
                        tooltip.add(Text.literal("Movement Speed: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(moveSpeed) + "%").formatted(getValueColor(stack, ModAttributes.MOVEMENT_SPEED))));
                    }
                    addedAny = true;
                }
                case GOLD -> {
                    if (attributeTotals.getOrDefault(ModAttributes.ENERGY_SHIELD, 0.0) > 0) {
                        tooltip.add(Text.literal("Energy Shield: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.ENERGY_SHIELD))).formatted(getValueColor(stack, ModAttributes.ENERGY_SHIELD))));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.LUCK, 0.0) > 0) {
                        tooltip.add(Text.literal("Luck: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.LUCK))).formatted(getValueColor(stack, ModAttributes.LUCK))));
                    }
                    addedAny = true;
                }
                case CHAINMAIL -> {
                    if (attributeTotals.getOrDefault(ModAttributes.EVASION_RATING, 0.0) > 0) {
                        tooltip.add(Text.literal("Evasion Rating: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.EVASION_RATING))).formatted(getValueColor(stack, ModAttributes.EVASION_RATING))));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.ENERGY_SHIELD, 0.0) > 0) {
                        tooltip.add(Text.literal("Energy Shield: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.ENERGY_SHIELD))).formatted(getValueColor(stack, ModAttributes.ENERGY_SHIELD))));
                    }
                    if (attributeTotals.getOrDefault(EntityAttributes.ATTACK_SPEED, 0.0) != 0) {
                        double speed = attributeTotals.get(EntityAttributes.ATTACK_SPEED) * 100;
                        tooltip.add(Text.literal("Attack Speed: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(speed) + "%").formatted(getValueColor(stack, EntityAttributes.ATTACK_SPEED))));
                    }
                    addedAny = true;
                }
                case COPPER, IRON -> {
                    if (attributeTotals.getOrDefault(EntityAttributes.ARMOR, 0.0) > 0) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(getValueColor(stack, EntityAttributes.ARMOR))));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.KNOCKBACK_RESISTANCE, 0.0) > 0) {
                        double kbRes = attributeTotals.get(ModAttributes.KNOCKBACK_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Knockback Resistance: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(kbRes) + "%").formatted(getValueColor(stack, ModAttributes.KNOCKBACK_RESISTANCE))));
                    }
                    if (armorType == ArmorSetType.COPPER) {
                        if (attributeTotals.getOrDefault(ModAttributes.FIRE_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Fire Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.FIRE_RESISTANCE) * 100) + "%").formatted(getValueColor(stack, ModAttributes.FIRE_RESISTANCE))));
                        }
                        if (attributeTotals.getOrDefault(ModAttributes.COLD_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Cold Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.COLD_RESISTANCE) * 100) + "%").formatted(getValueColor(stack, ModAttributes.COLD_RESISTANCE))));
                        }
                        if (attributeTotals.getOrDefault(ModAttributes.LIGHTNING_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Lightning Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.LIGHTNING_RESISTANCE) * 100) + "%").formatted(getValueColor(stack, ModAttributes.LIGHTNING_RESISTANCE))));
                        }
                    }
                    addedAny = true;
                }
                case DIAMOND -> {
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(getValueColor(stack, EntityAttributes.ARMOR))));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.MAX_HEALTH)) {
                        tooltip.add(Text.literal("Max Health: ").formatted(Formatting.GRAY)
                                .append(Text.literal("+" + df.format(attributeTotals.get(EntityAttributes.MAX_HEALTH))).formatted(getValueColor(stack, EntityAttributes.MAX_HEALTH))));
                    }
                    addedAny = true;
                }
                case NETHERITE -> {
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(getValueColor(stack, EntityAttributes.ARMOR))));
                    }
                    if (attributeTotals.containsKey(ModAttributes.BONUS_ATTACK_DAMAGE)) {
                        tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                                .append(Text.literal("+" + df.format(attributeTotals.get(ModAttributes.BONUS_ATTACK_DAMAGE))).formatted(getValueColor(stack, ModAttributes.BONUS_ATTACK_DAMAGE))));
                    }
                    addedAny = true;
                }
                default -> {
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(getValueColor(stack, EntityAttributes.ARMOR))));
                        addedAny = true;
                    }
                }
            }
        } else {
            // WEAPON/TOOL BASE STATS
            if (attributeTotals.containsKey(EntityAttributes.ATTACK_DAMAGE) && type != EquipmentType.BOW && type != EquipmentType.CROSSBOW && !isTool(stack)) {
                double finalDamage = PLAYER_BASE_ATTACK_DAMAGE + attributeTotals.get(EntityAttributes.ATTACK_DAMAGE);
                tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalDamage)).formatted(getValueColor(stack, EntityAttributes.ATTACK_DAMAGE))));
                addedAny = true;
            }
            if (attributeTotals.containsKey(EntityAttributes.ATTACK_SPEED) && !isTool(stack)) {
                double finalSpeed = PLAYER_BASE_ATTACK_SPEED + attributeTotals.get(EntityAttributes.ATTACK_SPEED);
                tooltip.add(Text.literal("Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalSpeed)).formatted(getValueColor(stack, EntityAttributes.ATTACK_SPEED))));
                addedAny = true;
            }
            if (type != EquipmentType.BOW && type != EquipmentType.CROSSBOW && !isTool(stack)) {
                double baseRange = 3.0 + attributeTotals.getOrDefault(ModAttributes.ATTACK_RANGE, 0.0);
                tooltip.add(Text.literal("Attack Range: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(baseRange)).formatted(getValueColor(stack, ModAttributes.ATTACK_RANGE))));
                addedAny = true;
            }
            if (type == EquipmentType.BOW || type == EquipmentType.CROSSBOW) {
                double finalDamage = attributeTotals.getOrDefault(EntityAttributes.ATTACK_DAMAGE, 0.0);
                tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalDamage)).formatted(getValueColor(stack, EntityAttributes.ATTACK_DAMAGE))));
                double finalReloadSpeed = 1.0 + attributeTotals.getOrDefault(ModAttributes.RELOAD_SPEED, 0.0);
                tooltip.add(Text.literal("Reload Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalReloadSpeed)).formatted(getValueColor(stack, ModAttributes.RELOAD_SPEED))));
                addedAny = true;
            }
            if (isTool(stack)) {
                double finalMiningSpeed = 1.0 + attributeTotals.getOrDefault(ModAttributes.MINING_EFFICIENCY, 0.0);
                tooltip.add(Text.literal("Harvest Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalMiningSpeed)).formatted(getValueColor(stack, ModAttributes.MINING_EFFICIENCY))));
                addedAny = true;
            }
        }

        if (addedAny) {
            addDivider(tooltip);
        }
    }

    @Unique
    private Map<RegistryEntry<EntityAttribute>, Double> calculateItemAttributeTotals(ItemStack stack) {
        Map<RegistryEntry<EntityAttribute>, Double> totals = new HashMap<>();
        Map<RegistryEntry<EntityAttribute>, Double> localPercentages = new HashMap<>();
        
        AttributeModifiersComponent attributes = stack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                AttributeModifiersComponent.DEFAULT
        );

        // 1. Gather all ADD_VALUE modifiers (flat base stats)
        for (var entry : attributes.modifiers()) {
            if (entry.modifier().operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE) {
                totals.merge(entry.attribute(), entry.modifier().value(), Double::sum);
            }
        }

        // 2. Gather specific percentage attributes that map to base attributes
        // These are our custom LOCAL percentage attributes
        double armorPct = totals.getOrDefault(ModAttributes.ARMOR_PERCENT, 0.0);
        if (armorPct != 0) localPercentages.merge(EntityAttributes.ARMOR, armorPct, Double::sum);

        double esPct = totals.getOrDefault(ModAttributes.ENERGY_SHIELD_PERCENT, 0.0);
        if (esPct != 0) localPercentages.merge(ModAttributes.ENERGY_SHIELD, esPct, Double::sum);

        double evasionPct = totals.getOrDefault(ModAttributes.EVASION_RATING_PERCENT, 0.0);
        if (evasionPct != 0) localPercentages.merge(ModAttributes.EVASION_RATING, evasionPct, Double::sum);
        
        double healthPct = totals.getOrDefault(ModAttributes.MAX_HEALTH_PERCENT, 0.0);
        if (healthPct != 0) localPercentages.merge(EntityAttributes.MAX_HEALTH, healthPct, Double::sum);

        double damagePct = totals.getOrDefault(ModAttributes.ATTACK_DAMAGE_PERCENT, 0.0);
        if (damagePct != 0) localPercentages.merge(EntityAttributes.ATTACK_DAMAGE, damagePct, Double::sum);

        // Attack speed rune uses the base attribute but with Operation.ADD_MULTIPLIED_TOTAL
        // However, in EquipmentStatApplier, we might have added it as a different modifier.
        // Let's also check for ADD_MULTIPLIED_TOTAL modifiers on the item.
        for (var entry : attributes.modifiers()) {
            if (entry.modifier().operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                localPercentages.merge(entry.attribute(), entry.modifier().value(), Double::sum);
            }
        }

        // 3. Apply percentages to flat totals IF the item already has that base stat
        for (var entry : localPercentages.entrySet()) {
            RegistryEntry<EntityAttribute> attr = entry.getKey();
            double pct = entry.getValue();
            if (totals.containsKey(attr)) {
                double flat = totals.get(attr);
                totals.put(attr, flat * (1.0 + pct));
            }
        }

        // 4. Add enchantment bonuses (after percentages, as enchantments are usually additive to the final total in PoE)
        var enchantments = stack.getEnchantments();
        for (var enchEntry : enchantments.getEnchantmentEntries()) {
            String id = enchEntry.getKey().getKey().get().getValue().toString();
            if (id.equals("minecraft:bonus_armor")) totals.merge(EntityAttributes.ARMOR, 5.0, Double::sum);
        }

        return totals;
    }
    @Unique
    private void addRunesSection(ItemStack stack, List<Text> tooltip, boolean showDetails) {
        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        boolean hasCryptic = ItemStatManager.hasCrypticStat(stack);

        if (stats.isEmpty() && !hasCryptic) return;

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

        if (!prefixes.isEmpty()) {
            if (showDetails) {
                tooltip.add(Text.literal("Prefixes:").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
            for (ItemStatManager.ItemStat p : prefixes) {
                CustomStat stat = StatRegistry.getStat(p.statId);
                tooltip.add(createRunicStatLine(stat, p.value, p.tier, p.isArtificial, showDetails));
            }
        }

        if (!suffixes.isEmpty()) {
            if (showDetails) {
                tooltip.add(Text.literal("Suffixes:").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
            for (ItemStatManager.ItemStat s : suffixes) {
                CustomStat stat = StatRegistry.getStat(s.statId);
                tooltip.add(createRunicStatLine(stat, s.value, s.tier, s.isArtificial, showDetails));
            }
        }

        if (hasCryptic) {
            tooltip.add(Text.literal("?? ").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)
                    .append(Text.literal("Cryptic Stat").formatted(Formatting.DARK_GREEN))
                    .append(Text.literal(" ??").formatted(Formatting.DARK_GREEN, Formatting.OBFUSCATED)));
        }
    }

    @Unique
    private void addEnchantmentsSection(ItemStack stack, List<Text> tooltip) {
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
        List<Text> enchantLines = new ArrayList<>();

        for (var entry : enchantmentComponent.getEnchantmentEntries()) {
            Text enchantName = entry.getKey().value().description();
            enchantLines.add(Text.literal("✦ ").formatted(Formatting.DARK_AQUA)
                    .append(Text.literal(enchantName.getString()).formatted(Formatting.AQUA)));
        }

        for (int i = 0; i < maxSlots; i++) {
            if (i < enchantLines.size()) {
                tooltip.add(enchantLines.get(i));
            } else {
                tooltip.add(Text.literal("✧ ").formatted(Formatting.DARK_GRAY)
                        .append(Text.literal("Empty Enchantment Slot").formatted(Formatting.DARK_GRAY)));
            }
        }
    }

    @Unique
    private Text createRunicStatLine(CustomStat stat, double value, int tier, boolean isArtificial, boolean showDetails) {
        String displayText = stat.formatValue(value);
        MutableText line = Text.literal(displayText).formatted(isArtificial ? Formatting.DARK_GREEN : Formatting.BLUE);
        if (showDetails) {
            line.append(Text.literal(" [T" + tier + "]").formatted(getTierColor(tier)));
        }
        return line;
    }

    @Unique
    private Formatting getValueColor(ItemStack stack, RegistryEntry<EntityAttribute> attr) {
        // 1. Check if the attribute is explicitly modified by Runes/Tomes
        List<ItemStatManager.ItemStat> itemStats = ItemStatManager.getStats(stack);
        for (ItemStatManager.ItemStat is : itemStats) {
            CustomStat stat = StatRegistry.getStat(is.statId);
            if (stat != null && stat.getAttribute() != null && 
                stat.getAttribute().getKey().isPresent() && attr.getKey().isPresent() &&
                stat.getAttribute().getKey().get().equals(attr.getKey().get())) {
                // If the rune value is non-zero, it's a modification
                if (is.value != 0) return Formatting.BLUE;
            }
        }

        // 2. Check Enchantments
        var enchants = stack.getEnchantments();
        for (var entry : enchants.getEnchantmentEntries()) {
            // Some enchantments are known to modify damage/speed directly
            String name = entry.getKey().value().description().getString().toLowerCase();
            if (attr.equals(EntityAttributes.ATTACK_DAMAGE) && (name.contains("sharpness") || name.contains("power") || name.contains("damage"))) return Formatting.BLUE;
            if (attr.equals(EntityAttributes.ATTACK_SPEED) && name.contains("haste")) return Formatting.BLUE;
        }
        
        // 3. Compare against Default Item Attributes
        // This handles "hardcoded" stats like Spear range or custom weapon speeds
        AttributeModifiersComponent currentAttrs = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (currentAttrs != null) {
            // Get a fresh stack of the same item to see its "true" defaults
            ItemStack defaultStack = new ItemStack(stack.getItem());
            AttributeModifiersComponent defaultAttrs = defaultStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
            
            double currentVal = 0;
            boolean hasAttr = false;
            for (var entry : currentAttrs.modifiers()) {
                if (entry.attribute().equals(attr)) {
                    currentVal += entry.modifier().value();
                    hasAttr = true;
                }
            }
            
            if (hasAttr) {
                double defaultVal = 0;
                for (var entry : defaultAttrs.modifiers()) {
                    if (entry.attribute().equals(attr)) {
                        defaultVal += entry.modifier().value();
                    }
                }
                // If current total value differs from default total value, it's modified
                if (Math.abs(currentVal - defaultVal) > 0.0001) {
                    return Formatting.BLUE;
                }
            }
        }

        return Formatting.WHITE;
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
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) return baseValue;
        }
        return 0.0;
    }


    @Unique
    private ArmorSetType getArmorSetType(String itemName) {
        if (itemName.contains("leather")) return ArmorSetType.LEATHER;
        if (itemName.contains("golden")) return ArmorSetType.GOLD;
        if (itemName.contains("chainmail")) return ArmorSetType.CHAINMAIL;
        if (itemName.contains("copper")) return ArmorSetType.COPPER;
        if (itemName.contains("iron")) return ArmorSetType.IRON;
        if (itemName.contains("diamond")) return ArmorSetType.DIAMOND;
        if (itemName.contains("netherite")) return ArmorSetType.NETHERITE;
        return ArmorSetType.NONE;
    }
}