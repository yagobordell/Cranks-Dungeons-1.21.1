package com.cranks.dungeons.inventory;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;

public final class EquipmentRepair {
    private EquipmentRepair() {}

    public record Materials(Ingredient ingredient, int fullCost) {
        public int durabilityPerMaterial(ItemStack target) {
            return Math.ceilDiv(target.getMaxDamage(), fullCost);
        }

        public int needed(ItemStack target) {
            return Math.ceilDiv(target.getDamage(), durabilityPerMaterial(target));
        }
    }

    public static boolean canRepair(ItemStack target) {
        return target.getCount() == 1 && target.isDamageable() && target.getDamage() > 0;
    }

    public static int remaining(ItemStack item) {
        return Math.max(0, item.getMaxDamage() - item.getDamage());
    }

    public static boolean isDonor(ItemStack target, ItemStack donor) {
        return donor.getCount() == 1 && donor.isOf(target.getItem()) && donor.isDamageable() && remaining(donor) > 0;
    }

    public static Materials materials(ItemStack target) {
        var item = target.getItem();
        if (item instanceof ToolItem tool) {
            int cap = switch (tool) {
                case ShovelItem ignored -> 1;
                case SwordItem ignored -> 2;
                case HoeItem ignored -> 2;
                case AxeItem ignored -> 3;
                case PickaxeItem ignored -> 3;
                default -> 4;
            };
            return new Materials(tool.getMaterial().getRepairIngredient(), cap);
        }
        if (item instanceof ArmorItem armor) return new Materials(armor.getMaterial().value().repairIngredient().get(), 4);
        if (item == Items.SHIELD) return new Materials(Ingredient.fromTag(ItemTags.PLANKS), 4);
        if (item == Items.BOW) return new Materials(Ingredient.ofItems(Items.STRING), 3);
        if (item == Items.CROSSBOW || item == Items.FISHING_ROD) return new Materials(Ingredient.ofItems(Items.STRING), 2);
        if (item == Items.SHEARS) return new Materials(Ingredient.ofItems(Items.IRON_INGOT), 2);
        if (item == Items.FLINT_AND_STEEL) return new Materials(Ingredient.ofItems(Items.IRON_INGOT), 1);
        if (item == Items.BRUSH) return new Materials(Ingredient.ofItems(Items.COPPER_INGOT), 1);
        if (item == Items.ELYTRA) return new Materials(Ingredient.ofItems(Items.PHANTOM_MEMBRANE), 4);
        if (item == Items.TRIDENT) return new Materials(Ingredient.ofItems(Items.PRISMARINE_SHARD), 4);
        if (item == Items.MACE) return new Materials(Ingredient.ofItems(Items.BREEZE_ROD), 4);

        var candidates = Registries.ITEM.stream().filter(material -> item.canRepair(target, material.getDefaultStack()))
                .toArray(Item[]::new);
        return new Materials(Ingredient.ofItems(candidates), 4);
    }

    public static int available(MaterialCrafting.Storage storage, Ingredient ingredient, int target) {
        int count = 0;
        for (int i = 0; i < storage.size(); i++) {
            if (storage.slot(i) == target) continue;
            ItemStack stack = storage.stack(i);
            if (!stack.isEmpty() && ingredient.test(stack)) count += stack.getCount();
        }
        return count;
    }
}
