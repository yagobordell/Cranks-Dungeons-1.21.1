package com.cranks.dungeons.util;

public interface DamageBreakdownProvider {
    DamageConversionHelper.DamageBreakdown cranks$getLastDamageBreakdown();
    void cranks$setLastDamageBreakdown(DamageConversionHelper.DamageBreakdown breakdown);
}
