package com.cranks.dungeons.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

public final class DamageContributionHelper {
    private DamageContributionHelper() {}

    public static void record(LivingEntity target, DamageSource source, float actualDamage) {
        if (actualDamage <= 0.0f || target.getWorld().isClient()
                || !(target instanceof MobDamageTracker tracker)) {
            return;
        }
        if (source.getAttacker() instanceof PlayerEntity player) {
            tracker.cranks$addDamageContribution(player.getUuid(), actualDamage);
        }
    }
}
