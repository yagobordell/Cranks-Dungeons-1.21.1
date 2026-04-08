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
            return;
        }

        if (tooltip.isEmpty()) return;

        // 1. Build the new "Item Card" from scratch
        List<Text> newTooltip = new ArrayList<>();
        
        // Header: [Name] [Rarity]
        newTooltip.add(modifyItemNameWithRarity(stack, tooltip.get(0)));
        
        // 2. Base/Implicit Section (Top-level stats)
        if (equipType.isPresent()) {
            addBaseStatsSection(stack, newTooltip, equipType.get());
        }

        // 3. Magical Runes Section
        addRunesSection(stack, newTooltip);

        // 4. Enchantment Section
        addEnchantmentsSection(stack, newTooltip);

        // 5. Append standard Minecraft data (Durability, etc.) but suppress attributes
        for (int i = 1; i < tooltip.size(); i++) {
            Text line = tooltip.get(i);
            String raw = line.getString().toLowerCase();
            
            // Suppress native attributes and our own old headers
            if (raw.contains("when in") || raw.contains("when on") || raw.contains("when worn") || 
                raw.contains("on body") || raw.contains("attribute.modifier") || 
                raw.startsWith("runes:") || raw.isEmpty()) {
                continue;
            }
            
            // Keep specific technical info
            if (raw.contains("durability:") || raw.contains("minecraft:") || raw.contains("unbreakable")) {
                newTooltip.add(line);
            }
        }

        // Update the actual tooltip list
        tooltip.clear();
        tooltip.addAll(newTooltip);
    }

    @Unique
    private void addDivider(List<Text> tooltip) {
        // Dark gray horizontal line
        tooltip.add(Text.literal("————————————————————").formatted(Formatting.DARK_GRAY));
    }

    @Unique
    private void addBaseStatsSection(ItemStack stack, List<Text> tooltip, EquipmentType type) {
        DecimalFormat df = new DecimalFormat("#.##");
        Map<RegistryEntry<EntityAttribute>, Double> attributeTotals = calculateItemAttributeTotals(stack);

        boolean isDefensive = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE ||
                type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS || 
                type == EquipmentType.ELYTRA || type == EquipmentType.SHIELD;

        boolean addedAny = false;

        if (isDefensive) {
            String itemName = stack.getItem().toString().toLowerCase();
            ArmorSetType armorType = getArmorSetType(itemName);

            switch (armorType) {
                case LEATHER:
                    if (attributeTotals.getOrDefault(ModAttributes.EVASION_RATING, 0.0) > 0) {
                        tooltip.add(Text.literal("Evasion Rating: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.EVASION_RATING))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.MOVEMENT_SPEED, 0.0) != 0) {
                        double moveSpeed = attributeTotals.get(ModAttributes.MOVEMENT_SPEED) * 100;
                        tooltip.add(Text.literal("Movement Speed: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(moveSpeed) + "%").formatted(Formatting.WHITE)));
                    }
                    addedAny = true;
                    break;

                case GOLD:
                    if (attributeTotals.getOrDefault(ModAttributes.ENERGY_SHIELD, 0.0) > 0) {
                        tooltip.add(Text.literal("Energy Shield: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.ENERGY_SHIELD))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.LUCK, 0.0) > 0) {
                        tooltip.add(Text.literal("Luck: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.LUCK))).formatted(Formatting.WHITE)));
                    }
                    addedAny = true;
                    break;

                case CHAINMAIL:
                    if (attributeTotals.getOrDefault(ModAttributes.EVASION_RATING, 0.0) > 0) {
                        tooltip.add(Text.literal("Evasion Rating: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.EVASION_RATING))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.ENERGY_SHIELD, 0.0) > 0) {
                        tooltip.add(Text.literal("Energy Shield: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(ModAttributes.ENERGY_SHIELD))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.getOrDefault(EntityAttributes.ATTACK_SPEED, 0.0) != 0) {
                        double speed = attributeTotals.get(EntityAttributes.ATTACK_SPEED) * 100;
                        tooltip.add(Text.literal("Attack Speed: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(speed) + "%").formatted(Formatting.WHITE)));
                    }
                    addedAny = true;
                    break;

                case COPPER:
                case IRON:
                    if (attributeTotals.getOrDefault(EntityAttributes.ARMOR, 0.0) > 0) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.getOrDefault(EntityAttributes.ARMOR_TOUGHNESS, 0.0) > 0) {
                        tooltip.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.getOrDefault(ModAttributes.KNOCKBACK_RESISTANCE, 0.0) > 0) {
                        double kbRes = attributeTotals.get(ModAttributes.KNOCKBACK_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Knockback Resistance: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(kbRes) + "%").formatted(Formatting.WHITE)));
                    }
                    // Handle copper specific resistances
                    if (armorType == ArmorSetType.COPPER) {
                        if (attributeTotals.getOrDefault(ModAttributes.FIRE_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Fire Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.FIRE_RESISTANCE) * 100) + "%").formatted(Formatting.WHITE)));
                        }
                        if (attributeTotals.getOrDefault(ModAttributes.COLD_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Cold Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.COLD_RESISTANCE) * 100) + "%").formatted(Formatting.WHITE)));
                        }
                        if (attributeTotals.getOrDefault(ModAttributes.LIGHTNING_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Lightning Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.LIGHTNING_RESISTANCE) * 100) + "%").formatted(Formatting.WHITE)));
                        }
                        if (attributeTotals.getOrDefault(ModAttributes.VOID_RESISTANCE, 0.0) > 0) {
                            tooltip.add(Text.literal("Void Resistance: ").formatted(Formatting.GRAY)
                                    .append(Text.literal(df.format(attributeTotals.get(ModAttributes.VOID_RESISTANCE) * 100) + "%").formatted(Formatting.WHITE)));
                        }
                    }
                    addedAny = true;
                    break;

                case DIAMOND:
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR_TOUGHNESS)) {
                        tooltip.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.MAX_HEALTH)) {
                        tooltip.add(Text.literal("Max Health: ").formatted(Formatting.GRAY)
                                .append(Text.literal("+" + df.format(attributeTotals.get(EntityAttributes.MAX_HEALTH))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.containsKey(ModAttributes.LIFE_REGENERATION)) {
                        tooltip.add(Text.literal("HP Regen: ").formatted(Formatting.GRAY)
                                .append(Text.literal("+" + df.format(attributeTotals.get(ModAttributes.LIFE_REGENERATION))).formatted(Formatting.WHITE)));
                    }
                    addedAny = true;
                    break;

                case NETHERITE:
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR_TOUGHNESS)) {
                        tooltip.add(Text.literal("Toughness: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS))).formatted(Formatting.WHITE)));
                    }
                    if (attributeTotals.containsKey(ModAttributes.BONUS_ATTACK_DAMAGE)) {
                        tooltip.add(Text.literal("Attack Damage: ").formatted(Formatting.GRAY)
                                .append(Text.literal("+" + df.format(attributeTotals.get(ModAttributes.BONUS_ATTACK_DAMAGE))).formatted(Formatting.WHITE)));
                    }
                    addedAny = true;
                    break;
                
                default:
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: ").formatted(Formatting.GRAY)
                                .append(Text.literal(df.format(attributeTotals.get(EntityAttributes.ARMOR))).formatted(Formatting.WHITE)));
                        addedAny = true;
                    }
                    break;
            }
        } else {
            // WEAPON/TOOL BASE STATS
            if (attributeTotals.containsKey(EntityAttributes.ATTACK_DAMAGE)) {
                double baseDamage = PLAYER_BASE_ATTACK_DAMAGE + attributeTotals.get(EntityAttributes.ATTACK_DAMAGE);
                baseDamage += getEnchantmentValue(stack, "minecraft:attack_damage", 4.0);
                double damagePercent = EquipmentStatApplier.getItemStatValue(stack, "attack_damage_percent");
                double finalDamage = baseDamage * (1.0 + damagePercent);
                
                tooltip.add(Text.literal("Physical Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalDamage)).formatted(Formatting.WHITE)));
                addedAny = true;
            }
            if (attributeTotals.containsKey(EntityAttributes.ATTACK_SPEED)) {
                double baseSpeed = PLAYER_BASE_ATTACK_SPEED + attributeTotals.get(EntityAttributes.ATTACK_SPEED);
                baseSpeed += getEnchantmentValue(stack, "minecraft:attack_speed", 0.2);
                double speedPercent = EquipmentStatApplier.getItemStatValue(stack, "attack_speed");
                double finalSpeed = baseSpeed * (1.0 + speedPercent);

                tooltip.add(Text.literal("Attack Speed: ").formatted(Formatting.GRAY)
                        .append(Text.literal(df.format(finalSpeed)).formatted(Formatting.WHITE)));
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
        
        AttributeModifiersComponent attributes = stack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                AttributeModifiersComponent.DEFAULT
        );

        for (var entry : attributes.modifiers()) {
            totals.merge(entry.attribute(), entry.modifier().value(), Double::sum);
        }

        var enchantments = stack.getEnchantments();
        for (var enchEntry : enchantments.getEnchantmentEntries()) {
            String id = enchEntry.getKey().getKey().get().getValue().toString();
            if (id.equals("minecraft:bonus_armor")) totals.merge(EntityAttributes.ARMOR, 5.0, Double::sum);
            if (id.equals("minecraft:bonus_armor_toughness")) totals.merge(EntityAttributes.ARMOR_TOUGHNESS, 2.0, Double::sum);
        }

        return totals;
    }

    @Unique
    private void addRunesSection(ItemStack stack, List<Text> tooltip) {
        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        boolean hasCryptic = ItemStatManager.hasCrypticStat(stack);

        if (stats.isEmpty() && !hasCryptic) return;

        for (ItemStatManager.ItemStat itemStat : stats) {
            CustomStat stat = StatRegistry.getStat(itemStat.statId);
            if (stat != null) {
                tooltip.add(createRunicStatLine(stat, itemStat.value, itemStat.tier, itemStat.isArtificial));
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
    private void addEnchantmentsSection(ItemStack stack, List<Text> tooltip) {
        Optional<EquipmentType> type = EquipmentType.getTypeForItem(stack);
        if (type.isPresent() && type.get() == EquipmentType.SHIELD) {
            return;
        }

        CustomRarity rarity = CustomRarity.getRarity(stack);
        int maxSlots = switch (rarity) {
            case COMMON -> 1;
            case RARE -> 2;
            case UNIQUE -> 3;
        };

        var enchantmentComponent = stack.getEnchantments();
        List<Text> enchantLines = new ArrayList<>();

        for (var entry : enchantmentComponent.getEnchantmentEntries()) {
            Text enchantName = entry.getKey().value().description();
            enchantLines.add(Text.literal("✦ ").formatted(Formatting.DARK_PURPLE)
                    .append(Text.literal(enchantName.getString()).formatted(Formatting.LIGHT_PURPLE)));
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
    private Text createRunicStatLine(CustomStat stat, double value, int tier, boolean isArtificial) {
        String displayText = stat.formatValue(value);
        MutableText line = Text.literal(displayText).formatted(isArtificial ? Formatting.DARK_GREEN : Formatting.WHITE);
        return line.append(Text.literal(" [T" + tier + "]").formatted(getTierColor(tier)));
    }

    @Unique
    private Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1, 2 -> Formatting.GRAY;
            case 3, 4 -> Formatting.GREEN;
            case 5 -> Formatting.GOLD;
            default -> Formatting.WHITE;
        };
    }

    @Unique
    private Text modifyItemNameWithRarity(ItemStack stack, Text originalName) {
        CustomRarity rarity = CustomRarity.getRarity(stack);
        return Text.literal(originalName.getString())
                .formatted(Formatting.WHITE)
                .append(Text.literal(" "))
                .append(Text.literal("[ " + rarity.getName() + " ]")
                        .formatted(rarity.getColor(), Formatting.ITALIC));
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