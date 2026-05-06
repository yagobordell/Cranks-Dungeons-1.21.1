package com.cranks.dungeons.equipment;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import java.util.*;

public enum EquipmentType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,

    SWORD,
    SPEAR,
    MACE,
    AXE,
    PICKAXE,
    SHOVEL,
    HOE,

    BOW,
    CROSSBOW,
    TRIDENT,

    SHIELD,
    ELYTRA;

    public String getName() {
        String name = this.name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static Optional<EquipmentType> getTypeForItem(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        Item item = stack.getItem();
        String itemName = net.minecraft.registry.Registries.ITEM.getId(item).toString().toLowerCase();

        // 1. Check for specific Classes/Items FIRST (Most Accurate)
        if (stack.isOf(Items.ELYTRA)) return Optional.of(ELYTRA);
        if (item instanceof ShieldItem) return Optional.of(SHIELD);
        if (item instanceof CrossbowItem || itemName.contains("crossbow")) return Optional.of(CROSSBOW);
        if (item instanceof BowItem || itemName.contains("bow")) return Optional.of(BOW);
        if (item instanceof TridentItem || itemName.contains("trident")) return Optional.of(TRIDENT);
        if (item instanceof MaceItem || itemName.contains("mace")) return Optional.of(MACE);

        // 2. Check for generic Armor slots using Data Components
        var equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        if (equippable != null) {
            return switch (equippable.slot()) {
                case HEAD -> Optional.of(HELMET);
                case CHEST -> Optional.of(CHESTPLATE);
                case LEGS -> Optional.of(LEGGINGS);
                case FEET -> Optional.of(BOOTS);
                default -> Optional.empty();
            };
        }

        // 3. Check for weapons/tools using Tags and Strict Suffixes
        // This prevents "waxed_copper" matching "axe"

        // Swords
        if (stack.isIn(ItemTags.SWORDS) || itemName.contains("sword") || 
            itemName.contains("dagger") || itemName.contains("longsword")) {
            return Optional.of(SWORD);
        }

        // Spears (Custom items usually follow the "_spear" naming convention)
        if (itemName.contains("spear") || itemName.contains("pike") || 
            itemName.contains("halberd") || itemName.contains("glaive")) {
            return Optional.of(SPEAR);
        }

        // Axes (Strict check: must end with _axe or be in the Axe tag)
        if (stack.isIn(ItemTags.AXES) || itemName.endsWith("_axe") || itemName.equals("axe")) {
            // Extra safety to ensure pickaxes aren't caught as axes (though endsWith solves this)
            if (!itemName.endsWith("_pickaxe")) {
                return Optional.of(AXE);
            }
        }

        // Other Tools
        if (stack.isIn(ItemTags.PICKAXES) || itemName.contains("pickaxe")) {
            return Optional.of(PICKAXE);
        }
        if (stack.isIn(ItemTags.SHOVELS) || itemName.contains("shovel")) {
            return Optional.of(SHOVEL);
        }
        if (stack.isIn(ItemTags.HOES) || itemName.contains("hoe")) {
            return Optional.of(HOE);
        }

        // If it's a block or non-equipment item, it will reach here and return empty.
        return Optional.empty();
    }

    public static boolean canEnhance(ItemStack stack) {
        return getTypeForItem(stack).isPresent();
    }

    /**
     * Returns the set of EquipmentSlot values that are applicable for this item type.
     * Used to filter enchantment effects by slot so weapon enchants don't show armor stats and vice versa.
     */
    public Set<EquipmentSlot> getApplicableSlots() {
        return switch (this) {
            case HELMET    -> Set.of(EquipmentSlot.HEAD);
            case CHESTPLATE -> Set.of(EquipmentSlot.CHEST);
            case LEGGINGS  -> Set.of(EquipmentSlot.LEGS);
            case BOOTS     -> Set.of(EquipmentSlot.FEET);
            // All weapon/tool types go in the mainhand
            default        -> Set.of(EquipmentSlot.MAINHAND);
        };
    }
}