package com.cranks.dungeons.inventory;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class GearSalvage {
    private static final Identifier IRON_RECYCLING = Identifier.ofVanilla("iron_nugget_from_blasting");
    private static final Identifier GOLD_RECYCLING = Identifier.ofVanilla("gold_nugget_from_blasting");

    private enum Material {
        IRON(Items.IRON_INGOT, Items.IRON_NUGGET), GOLD(Items.GOLD_INGOT, Items.GOLD_NUGGET),
        DIAMOND(Items.DIAMOND, null), LEATHER(Items.LEATHER, null);

        final Item unit;
        final Item nugget;

        Material(Item unit, Item nugget) {
            this.unit = unit;
            this.nugget = nugget;
        }
    }

    private record Gear(Material material, int cost) {}
    private static final Map<Item, Gear> GEAR = buildGear();

    private GearSalvage() {}

    private static Map<Item, Gear> buildGear() {
        Map<Item, Gear> gear = new HashMap<>();
        addArmor(gear, Material.IRON, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
        addArmor(gear, Material.GOLD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
        addArmor(gear, Material.DIAMOND, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
        addArmor(gear, Material.LEATHER, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        addTools(gear, Material.IRON, Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE);
        addTools(gear, Material.GOLD, Items.GOLDEN_SWORD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE);
        addTools(gear, Material.DIAMOND, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE);
        gear.put(Items.CHAINMAIL_HELMET, new Gear(Material.IRON, 2));
        gear.put(Items.CHAINMAIL_CHESTPLATE, new Gear(Material.IRON, 4));
        gear.put(Items.CHAINMAIL_LEGGINGS, new Gear(Material.IRON, 3));
        gear.put(Items.CHAINMAIL_BOOTS, new Gear(Material.IRON, 2));
        gear.put(Items.IRON_HORSE_ARMOR, new Gear(Material.IRON, 5));
        gear.put(Items.GOLDEN_HORSE_ARMOR, new Gear(Material.GOLD, 5));
        gear.put(Items.DIAMOND_HORSE_ARMOR, new Gear(Material.DIAMOND, 5));
        gear.put(Items.LEATHER_HORSE_ARMOR, new Gear(Material.LEATHER, 7));
        return Map.copyOf(gear);
    }

    private static void addArmor(Map<Item, Gear> gear, Material material, Item helmet, Item chestplate, Item leggings, Item boots) {
        gear.put(helmet, new Gear(material, 5));
        gear.put(chestplate, new Gear(material, 8));
        gear.put(leggings, new Gear(material, 7));
        gear.put(boots, new Gear(material, 4));
    }

    private static void addTools(Map<Item, Gear> gear, Material material, Item sword, Item pickaxe, Item axe, Item shovel, Item hoe) {
        gear.put(sword, new Gear(material, 2));
        gear.put(pickaxe, new Gear(material, 3));
        gear.put(axe, new Gear(material, 3));
        gear.put(shovel, new Gear(material, 1));
        gear.put(hoe, new Gear(material, 2));
    }

    public static boolean supports(ItemStack stack, boolean blast) {
        Gear gear = GEAR.get(stack.getItem());
        return gear != null && (gear.material.nugget != null) == blast;
    }

    public static ItemStack output(ItemStack stack, boolean blast) {
        if (!supports(stack, blast)) return ItemStack.EMPTY;
        Gear gear = GEAR.get(stack.getItem());
        int max = Math.max(1, stack.getMaxDamage());
        int remaining = max - Math.clamp(stack.getDamage(), 0, max);
        int scale = blast ? 9 : 1;
        int units = (int) ((long) gear.cost * scale * remaining / (3L * max));
        if (units == 0) return ItemStack.EMPTY;
        // A furnace has one result slot. Return whole ingots when possible, or nuggets for smaller yields.
        return blast && units < 9 ? new ItemStack(gear.material.nugget, units)
                : new ItemStack(gear.material.unit, units / scale);
    }

    public static boolean needsConfirmation(ItemStack stack) {
        return stack.hasEnchantments() || stack.contains(DataComponentTypes.TRIM)
                || stack.contains(DataComponentTypes.CUSTOM_NAME) || stack.contains(DataComponentTypes.DYED_COLOR)
                || stack.contains(DataComponentTypes.CUSTOM_DATA);
    }

    public static boolean isBlastRecipe(RecipeEntry<?> entry) {
        return entry != null && entry.value().getType() == RecipeType.BLASTING
                && (entry.id().equals(IRON_RECYCLING) || entry.id().equals(GOLD_RECYCLING));
    }

    public static ItemStack furnaceOutput(RecipeEntry<?> entry, ItemStack input, RegistryWrapper.WrapperLookup registries) {
        return isBlastRecipe(entry) ? output(input, true) : entry.value().getResult(registries);
    }

    public static boolean fits(ItemStack result, ItemStack existing, int limit) {
        return !result.isEmpty() && (existing.isEmpty() || ItemStack.areItemsAndComponentsEqual(result, existing))
                && result.getCount() + existing.getCount() <= Math.min(limit, result.getMaxCount());
    }
}
