package com.cranks.dungeons.equipment;

import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;


public class ItemRequirementManager {

    public static int[] getRequirements(ItemStack stack) {
        if (stack.isEmpty()) return new int[]{0, 0, 0};
        
        EquipmentType type = EquipmentType.getTypeForItem(stack).orElse(null);
        if (type == null) return new int[]{0, 0, 0};

        if (type == EquipmentType.AXE || type == EquipmentType.PICKAXE || type == EquipmentType.SHOVEL || type == EquipmentType.HOE) {
            return new int[]{0, 0, 0};
        }

        int baseScale = 0;
        String name = stack.getItem().toString().toLowerCase();

        if (name.contains("wood") || name.contains("stone")) {
            baseScale = 0;
        } else if (name.contains("leather")) {
            baseScale = 15;
        } else if (name.contains("copper")) {
            boolean isArmor = type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE || type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS;
            baseScale = isArmor ? 15 : 0;
        } else if (name.contains("chainmail") || name.contains("gold")) {
            baseScale = 22;
        } else if (name.contains("iron")) {
            baseScale = 20;
        } else if (name.contains("diamond")) {
            baseScale = 45;
        } else if (name.contains("netherite")) {
            baseScale = 65;
        } else if (type == EquipmentType.MACE || type == EquipmentType.TRIDENT) {
            baseScale = 65;
        } else if (type == EquipmentType.ELYTRA) {
            baseScale = 90;
        } else if (type != null) {
            // Default for unknown material items
            baseScale = 10;
        }

        int scale = baseScale;

        if (scale == 0) return new int[]{0, 0, 0};

        int str = 0, dex = 0, intel = 0;

        // Roughly match PoE themes
        if (type == EquipmentType.MACE || type == EquipmentType.SHIELD) {
            str = scale;
        } else if (type == EquipmentType.SWORD || type == EquipmentType.SPEAR) {
            str = scale / 2;
            dex = scale / 2;
        } else if (type == EquipmentType.TRIDENT) {
            str = 20;
            dex = scale / 2;
        } else if (type == EquipmentType.BOW || type == EquipmentType.CROSSBOW) {
            dex = scale;
        } else if (type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE || type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS) {
            // Armor logic
            if (name.contains("iron") || name.contains("diamond") || name.contains("netherite")) {
                str = scale;
            } else if (name.contains("leather")) {
                dex = scale;
            } else if (name.contains("gold") || name.contains("copper")) {
                str = scale / 2;
                intel = scale / 2;
            } else if (name.contains("chainmail")) {
                str = scale / 2;
                dex = scale / 2;
            } else {
                str = scale;
            }
        } else {
            // Elytra or others
            dex = scale / 2;
            intel = scale / 2;
        }

        double reducedReq = com.cranks.dungeons.equipment.EquipmentStatApplier.getItemStatValue(stack, "reduced_requirements");
        if (reducedReq > 0) {
            double multiplier = 1.0 - reducedReq;
            str = (int) Math.round(str * multiplier);
            dex = (int) Math.round(dex * multiplier);
            intel = (int) Math.round(intel * multiplier);
        }

        return new int[]{str, dex, intel};
    }

    public static boolean meetsRequirements(PlayerEntity player, ItemStack stack) {
        int[] reqs = getRequirements(stack);
        if (reqs[0] == 0 && reqs[1] == 0 && reqs[2] == 0) return true;

        double playerStr = player.getAttributeValue(ModAttributes.STRENGTH);
        double playerDex = player.getAttributeValue(ModAttributes.DEXTERITY);
        double playerInt = player.getAttributeValue(ModAttributes.INTELLIGENCE);

        return playerStr >= reqs[0] && playerDex >= reqs[1] && playerInt >= reqs[2];
    }
}
