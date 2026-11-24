package com.cranks.dungeons.mount;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

/** Live controlling-rider bonuses. Nothing is written to mount genes or attributes. */
public final class RidingStats {
    private RidingStats() {}

    private static double value(LivingEntity mount, RegistryEntry<EntityAttribute> attribute) {
        return MountLocomotion.supported(mount)
                && mount.getControllingPassenger() instanceof PlayerEntity rider && rider.isAlive()
                ? rider.getAttributeValue(attribute) : 0;
    }

    public static float speed(LivingEntity mount, float base) {
        return (float) Math.min(Float.MAX_VALUE, RidingRules.increase(base, value(mount, ModAttributes.RIDING_SPEED)));
    }

    public static double agility(LivingEntity mount, double base) {
        return RidingRules.increase(base, value(mount, ModAttributes.RIDING_AGILITY));
    }

    public static double sprintCost(LivingEntity mount, double base) {
        return RidingRules.reduce(base, value(mount, ModAttributes.MOUNT_ENDURANCE));
    }

    public static double recovery(LivingEntity mount, double base) {
        // A strider's environmental heat loss is not regeneration.
        return base > 0 ? RidingRules.increase(base, value(mount, ModAttributes.MOUNT_RECOVERY)) : base;
    }

    public static float fallDamage(LivingEntity mount, float base) {
        return (float) Math.min(Float.MAX_VALUE, RidingRules.reduce(base, value(mount, ModAttributes.SURE_FOOTING)));
    }
}
