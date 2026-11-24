package com.cranks.dungeons.ranged;

import com.cranks.dungeons.util.CombatUtility;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;

/** Per-launch draw strength; nested projectile launches cannot leak into one another. */
public final class RangedLaunch {
    private static final ThreadLocal<Float> POWER = ThreadLocal.withInitial(() -> 1f);
    private RangedLaunch() {}
    public static float power() { return POWER.get(); }
    public static void withPower(float power, Runnable action) {
        float previous = POWER.get(); POWER.set(power);
        try { action.run(); } finally { POWER.set(previous); }
    }
    public static double damage(PlayerEntity user, ItemStack weapon) {
        double value = 0;
        for (var entry : weapon.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()) {
            if (CombatUtility.attributesMatch(entry.attribute(), EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    && entry.slot() == AttributeModifierSlot.MAINHAND
                    && entry.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE) value += entry.modifier().value();
        }
        if (!Double.isFinite(value) || value <= 0) value = weapon.getItem() instanceof CrossbowItem ? 7 : 5;
        if (!com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(user, weapon)) value *= 0.5;
        return Math.clamp(value, 0.01, 1_000_000);
    }
}
