package com.cranks.dungeons.ranged;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.CombatUtility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

public final class RangedTiming {
    private RangedTiming() {}
    public static double speed(LivingEntity user, ItemStack weapon) {
        return RangedRules.speed(CombatUtility.getAttributeValue(user, EntityAttributes.GENERIC_ATTACK_SPEED, weapon, "", null),
                CombatUtility.getAttributeValue(user, ModAttributes.ATTACK_SPEED, weapon, "attack_speed", null));
    }
    public static int crossbowReloadTicks(LivingEntity user, ItemStack weapon) {
        return RangedRules.duration(net.minecraft.item.CrossbowItem.getPullTime(weapon,user),speed(user,weapon));
    }
    public static int bowDrawTicks(LivingEntity user, ItemStack weapon) { return RangedRules.duration(20, speed(user, weapon)); }
}
