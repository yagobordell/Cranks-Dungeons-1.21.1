package com.cranks.dungeons.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class FreezeStatusEffect extends StatusEffect {
    public FreezeStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x00FFFF);
    }
}