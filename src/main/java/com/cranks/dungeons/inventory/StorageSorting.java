package com.cranks.dungeons.inventory;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import java.util.*;

/** Plans without changing source stacks; component equality defines stack compatibility. */
public final class StorageSorting {
    private StorageSorting(){}
    public static List<ItemStack> plan(List<ItemStack> source){
        var merged=new ArrayList<ItemStack>();
        for(var original:source){
            if(original.isEmpty())continue;
            if(original.getCount()>original.getMaxCount())return null;
            var remaining=original.copy();
            for(var existing:merged)if(ItemStack.areItemsAndComponentsEqual(existing,remaining)){
                int amount=Math.min(remaining.getCount(),existing.getMaxCount()-existing.getCount());
                if(amount>0){existing.increment(amount);remaining.decrement(amount);}
                if(remaining.isEmpty())break;
            }
            if(!remaining.isEmpty())merged.add(remaining);
        }
        merged.sort(Comparator.comparingInt(StorageSorting::category)
                .thenComparing(stack->stack.getName().getString(),String.CASE_INSENSITIVE_ORDER)
                .thenComparing(stack->Registries.ITEM.getId(stack.getItem()).toString()));
        if(merged.size()>source.size())return null;
        while(merged.size()<source.size())merged.add(ItemStack.EMPTY);
        return List.copyOf(merged);
    }
    private static int category(ItemStack stack){
        if(stack.getItem() instanceof BlockItem)return 0;
        var equipment=EquipmentType.getTypeForItem(stack).orElse(null);
        if(equipment!=null)return switch(equipment){case SWORD,SPEAR,MACE,TRIDENT,BOW,CROSSBOW->1;case HELMET,CHESTPLATE,LEGGINGS,BOOTS,SHIELD,ELYTRA->2;default->3;};
        if(stack.getItem() instanceof ToolItem||stack.getItem() instanceof FishingRodItem||stack.getItem() instanceof ShearsItem)return 3;
        if(stack.contains(DataComponentTypes.FOOD))return 4;
        if(stack.contains(DataComponentTypes.POTION_CONTENTS)||stack.getItem() instanceof PotionItem)return 5;
        return 6;
    }
}
