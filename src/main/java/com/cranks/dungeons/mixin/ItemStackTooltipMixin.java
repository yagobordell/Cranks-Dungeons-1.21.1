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

        if (customStats.isEmpty() && equipType.isEmpty()) {
            return;
        }

        if (tooltip.isEmpty()) {
            return;
        }

        boolean isShield = equipType.isPresent() && equipType.get() == EquipmentType.SHIELD;

        if (isShield) {
            handleShieldTooltip(tooltip, stack);
        } else {
            handleNormalTooltip(tooltip, stack, equipType.orElse(null));
        }

        cir.getReturnValue().removeIf(text -> {
            String raw = text.getString().toLowerCase().trim();

            return raw.contains("when worn") ||
                    raw.contains("on body") ||
                    raw.contains("when on") ||
                    raw.startsWith("attribute.modifier") ||
                    raw.isEmpty();
        });
    }

    private void handleShieldTooltip(List<Text> tooltip, ItemStack stack) {
        // Apply rarity to the name line
        Text itemName = modifyItemNameWithRarity(stack, tooltip.get(0));

        List<Text> newTooltip = new ArrayList<>();
        newTooltip.add(itemName);

        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        if (!stats.isEmpty()) {
            newTooltip.add(Text.literal("RUNIC STATS:").formatted(Formatting.AQUA, Formatting.BOLD));

            for (ItemStatManager.ItemStat itemStat : stats) {
                CustomStat stat = StatRegistry.getStat(itemStat.statId);
                if (stat != null) {
                    newTooltip.add(createRunicStatLine(stat, itemStat.value, itemStat.tier));
                }
            }
        }

        for (int i = 1; i < tooltip.size(); i++) {
            String line = tooltip.get(i).getString();
            if (line.contains("Durability:") || line.contains("minecraft:") || line.contains("Enchantments:") || line.contains("Unbreakable")) {
                newTooltip.add(tooltip.get(i));
            }
        }

        tooltip.clear();
        tooltip.addAll(newTooltip);
    }

    private void handleNormalTooltip(List<Text> tooltip, ItemStack stack, EquipmentType equipType) {
        // Apply rarity to the name line
        Text itemName = modifyItemNameWithRarity(stack, tooltip.get(0));

        int attributeSectionStart = -1;
        int attributeSectionEnd = -1;

        for (int i = 1; i < tooltip.size(); i++) {
            String line = tooltip.get(i).getString();
            if ((line.contains("When in Main Hand:") || line.contains("When in Offhand:") ||
                    line.contains("When on Body:") || line.contains("When on Head:") ||
                    line.contains("When on Legs:") ||  line.contains("When worn:") ||
                    line.contains("When on Chest:") || line.contains("When on Feet:"))) {
                attributeSectionStart = i;
            }

            if (attributeSectionStart != -1 && attributeSectionEnd == -1) {
                if (line.trim().isEmpty() || line.contains("Unbreakable") || line.contains("Durability:") || line.contains("Enchantments:")) {
                    attributeSectionEnd = i;
                    break;
                }
            }
        }

        if (attributeSectionStart != -1 && attributeSectionEnd == -1) {
            for (int i = tooltip.size() - 1; i > attributeSectionStart; i--) {
                String line = tooltip.get(i).getString();
                if (line.contains("minecraft:") || line.contains("Durability:")) {
                    attributeSectionEnd = i;
                    break;
                }
            }
            if (attributeSectionEnd == -1) attributeSectionEnd = tooltip.size();
        }

        List<Text> newTooltip = new ArrayList<>();
        newTooltip.add(itemName);

        if (equipType != null) {
            addCustomStatsSection(stack, newTooltip, equipType);
        }

        if (attributeSectionEnd != -1 && attributeSectionEnd < tooltip.size()) {
            for (int i = attributeSectionEnd; i < tooltip.size(); i++) {
                String line = tooltip.get(i).getString();

                if (line.contains("attribute.name.cranks-dungeons") ||
                        line.contains("Attack Damage") ||
                        line.contains("Attack Speed") ||
                        line.contains("Armor")) {
                    continue;
                }

                newTooltip.add(tooltip.get(i));
            }
        }

        tooltip.clear();
        tooltip.addAll(newTooltip);
    }

    private void addCustomStatsSection(ItemStack stack, List<Text> tooltip, EquipmentType type) {
        AttributeModifiersComponent attributes = stack.getOrDefault(
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                AttributeModifiersComponent.DEFAULT
        );

        Map<RegistryEntry<EntityAttribute>, Double> attributeTotals = new HashMap<>();

        for (var entry : attributes.modifiers()) {
            attributeTotals.merge(entry.attribute(), entry.modifier().value(), Double::sum);
        }

        var enchantments = stack.getEnchantments();
        for (var enchEntry : enchantments.getEnchantmentEntries()) {
            String enchantId = enchEntry.getKey().getKey().get().getValue().toString();

            if (enchantId.equals("minecraft:bonus_armor")) {
                attributeTotals.merge(EntityAttributes.ARMOR, 5.0, Double::sum);
            }
            if (enchantId.equals("minecraft:bonus_armor_toughness")) {
                attributeTotals.merge(EntityAttributes.ARMOR_TOUGHNESS, 2.0, Double::sum);
            }
        }

        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat percentFormat = new DecimalFormat("#.#");
        boolean isArmor = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE ||
                type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS || type == EquipmentType.ELYTRA;

        if (isArmor) {
            String itemName = stack.getItem().toString().toLowerCase();
            ArmorSetType armorType = getArmorSetType(itemName);

            switch (armorType) {
                case LEATHER:
                    // Evasion Rating + Movement Speed
                    if (attributeTotals.containsKey(ModAttributes.EVASION_RATING)) {
                        tooltip.add(Text.literal("Evasion Rating: " + df.format(attributeTotals.get(ModAttributes.EVASION_RATING)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.MOVEMENT_SPEED)) {
                        double moveSpeed = attributeTotals.get(ModAttributes.MOVEMENT_SPEED) * 100;
                        tooltip.add(Text.literal("Movement Speed: +" + df.format(moveSpeed) + "%")
                                .formatted(Formatting.WHITE));
                    }
                    break;

                case GOLD:
                    // Energy Shield + Luck
                    if (attributeTotals.containsKey(ModAttributes.ENERGY_SHIELD)) {
                        tooltip.add(Text.literal("Energy Shield: " + df.format(attributeTotals.get(ModAttributes.ENERGY_SHIELD)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.LUCK)) {
                        tooltip.add(Text.literal("Luck: " + df.format(attributeTotals.get(ModAttributes.LUCK)))
                                .formatted(Formatting.WHITE));
                    }
                    break;

                case CHAINMAIL:
                    // Evasion Rating + Energy Shield + Attack Speed
                    if (attributeTotals.containsKey(ModAttributes.EVASION_RATING)) {
                        tooltip.add(Text.literal("Evasion Rating: " + df.format(attributeTotals.get(ModAttributes.EVASION_RATING)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.ENERGY_SHIELD)) {
                        tooltip.add(Text.literal("Energy Shield: " + df.format(attributeTotals.get(ModAttributes.ENERGY_SHIELD)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ATTACK_SPEED)) {
                        tooltip.add(Text.literal("Attack Speed: +" + df.format(attributeTotals.get(EntityAttributes.ATTACK_SPEED) * 100) + "%")
                                .formatted(Formatting.WHITE));
                    }
                    break;

                case COPPER:
                    // Armor + Elemental Resistances
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: " + df.format(attributeTotals.get(EntityAttributes.ARMOR)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.FIRE_RESISTANCE)) {
                        double resistance = attributeTotals.get(ModAttributes.FIRE_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Fire Resistance: " + df.format(resistance) + "%")
                                .formatted(Formatting.WHITE));
                    }

                    if (attributeTotals.containsKey(ModAttributes.COLD_RESISTANCE)) {
                        double resistance = attributeTotals.get(ModAttributes.COLD_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Cold Resistance: " + df.format(resistance) + "%")
                                .formatted(Formatting.WHITE));
                    }

                    if (attributeTotals.containsKey(ModAttributes.LIGHTNING_RESISTANCE)) {
                        double resistance = attributeTotals.get(ModAttributes.LIGHTNING_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Lightning Resistance: " + df.format(resistance) + "%")
                                .formatted(Formatting.WHITE));
                    }

                    if (attributeTotals.containsKey(ModAttributes.VOID_RESISTANCE)) {
                        double resistance = attributeTotals.get(ModAttributes.VOID_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Void Resistance: " + df.format(resistance) + "%")
                                .formatted(Formatting.WHITE));
                    }

                    break;

                case IRON:
                    // Armor + Armor Toughness + Knockback Resistance
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: " + df.format(attributeTotals.get(EntityAttributes.ARMOR)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR_TOUGHNESS)) {
                        tooltip.add(Text.literal("Armor Toughness: " + df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.KNOCKBACK_RESISTANCE)) {
                        double kbRes = attributeTotals.get(ModAttributes.KNOCKBACK_RESISTANCE) * 100;
                        tooltip.add(Text.literal("Knockback Resistance: " + df.format(kbRes) + "%")
                                .formatted(Formatting.WHITE));
                    }
                    break;

                case DIAMOND:
                    // Armor + Armor Toughness + HP + HP Regen
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: " + df.format(attributeTotals.get(EntityAttributes.ARMOR)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR_TOUGHNESS)) {
                        tooltip.add(Text.literal("Armor Toughness: " + df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.MAX_HEALTH)) {
                        tooltip.add(Text.literal("Max Health: +" + df.format(attributeTotals.get(EntityAttributes.MAX_HEALTH)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.LIFE_REGENERATION)) {
                        tooltip.add(Text.literal("HP Regen: +" + df.format(attributeTotals.get(ModAttributes.LIFE_REGENERATION)))
                                .formatted(Formatting.WHITE));
                    }
                    break;

                case NETHERITE:
                    // Armor + Armor Toughness + Attack Damage
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: " + df.format(attributeTotals.get(EntityAttributes.ARMOR)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR_TOUGHNESS)) {
                        tooltip.add(Text.literal("Armor Toughness: " + df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(ModAttributes.BONUS_ATTACK_DAMAGE)) {
                        double attack = attributeTotals.get(ModAttributes.BONUS_ATTACK_DAMAGE);
                        if (attributeTotals.containsKey(EntityAttributes.ATTACK_DAMAGE)) {
                            attack += attributeTotals.get(EntityAttributes.ATTACK_DAMAGE);
                        }
                        tooltip.add(Text.literal("Attack Damage: +" + df.format(attack))
                                .formatted(Formatting.WHITE));
                    }
                    break;

                case NONE:
                    // Default fallback
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR)) {
                        tooltip.add(Text.literal("Armor: " + df.format(attributeTotals.get(EntityAttributes.ARMOR)))
                                .formatted(Formatting.WHITE));
                    }
                    if (attributeTotals.containsKey(EntityAttributes.ARMOR_TOUGHNESS)) {
                        tooltip.add(Text.literal("Armor Toughness: " + df.format(attributeTotals.get(EntityAttributes.ARMOR_TOUGHNESS)))
                                .formatted(Formatting.WHITE));
                    }
                    break;
            }
        } else {
            // Weapon stats
            if (attributeTotals.containsKey(EntityAttributes.ATTACK_DAMAGE)) {
                double baseAttackDamage = PLAYER_BASE_ATTACK_DAMAGE + attributeTotals.get(EntityAttributes.ATTACK_DAMAGE);

                // Add attack damage from enchantments
                baseAttackDamage += getEnchantmentValue(stack, "minecraft:attack_damage", 4.0);

                // Get attack damage percentage from this weapon's stats
                double attackDamagePercent = EquipmentStatApplier.getItemStatValue(stack, "attack_damage_percent");

                // Apply percentage multiplier
                double finalAttackDamage = baseAttackDamage * (1.0 + attackDamagePercent);

                tooltip.add(Text.literal("Attack Damage: " + df.format(finalAttackDamage)).formatted(Formatting.WHITE));
            }
            if (attributeTotals.containsKey(EntityAttributes.ATTACK_SPEED)) {
                double attackSpeed = PLAYER_BASE_ATTACK_SPEED + attributeTotals.get(EntityAttributes.ATTACK_SPEED);
                attackSpeed += getEnchantmentValue(stack, "minecraft:attack_speed", 0.2);

                // Get attack speed percentage from runes
                double attackSpeedPercent = EquipmentStatApplier.getItemStatValue(stack, "attack_speed_percent");

                // Apply percentage multiplier
                double finalAttackSpeed = attackSpeed * (1.0 + attackSpeedPercent);

                tooltip.add(Text.literal("Attack Speed: " + df.format(finalAttackSpeed)).formatted(Formatting.WHITE));
            }
        }

        CustomRarity rarity = CustomRarity.getRarity(stack);
        int maxSlots = switch (rarity) {
            case COMMON -> 1;
            case RARE -> 2;
            case UNIQUE -> 3;
        };

        var enchantmentComponent = stack.getEnchantments();
        List<Text> activeEnchants = new ArrayList<>();

        for (var entry : enchantmentComponent.getEnchantmentEntries()) {
            Text enchantName = entry.getKey().value().description();
            activeEnchants.add(Text.literal("[ " + enchantName.getString() + " ]").formatted(Formatting.DARK_PURPLE));
        }

        for (int i = 0; i < maxSlots; i++) {
            if (i < activeEnchants.size()) {
                tooltip.add(activeEnchants.get(i));
            } else {
                tooltip.add(Text.literal("[ Enchantment Slot ]").formatted(Formatting.DARK_GRAY));
            }
        }

        List<ItemStatManager.ItemStat> stats = ItemStatManager.getStats(stack);
        if (!stats.isEmpty()) {
            tooltip.add(Text.literal("Runes:").formatted(Formatting.AQUA, Formatting.BOLD));

            for (ItemStatManager.ItemStat itemStat : stats) {
                CustomStat stat = StatRegistry.getStat(itemStat.statId);
                if (stat != null) {
                    tooltip.add(createRunicStatLine(stat, itemStat.value, itemStat.tier));
                }
            }
        }
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

    private Text createRunicStatLine(CustomStat stat, double value, int tier) {
        DecimalFormat df = new DecimalFormat("#.##");
        String valueStr = stat.formatValue(value).contains("%") ? df.format(value * 100) + "%" : df.format(value);
        return Text.literal(stat.getDisplayName() + ": " + valueStr + " ").formatted(Formatting.WHITE)
                .append(Text.literal("[T" + tier + "]").formatted(getTierColor(tier)));
    }

    private Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1, 2 -> Formatting.GRAY;
            case 3, 4 -> Formatting.GREEN;
            case 5 -> Formatting.GOLD;
            default -> Formatting.WHITE;
        };
    }

    private Text modifyItemNameWithRarity(ItemStack stack, Text originalName) {
        CustomRarity rarity = CustomRarity.getRarity(stack);

        return Text.literal(originalName.getString())
                .formatted(Formatting.WHITE)
                .append(Text.literal(" "))
                .append(Text.literal("[ " + rarity.getName() + " ]")
                        .formatted(rarity.getColor(), Formatting.BOLD));
    }

    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) {
                return baseValue;
            }
        }
        return 0.0;
    }
}