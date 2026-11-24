package com.cranks.dungeons.util;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.LivingEntity;

public final class ClientAilmentHelper {
    private ClientAilmentHelper() {}
    public static boolean isStunned(LivingEntity entity){
        return entity.hasStatusEffect(ModEffects.FREEZE)||entity.hasStatusEffect(ModEffects.ELECTROCUTE);
    }
}
